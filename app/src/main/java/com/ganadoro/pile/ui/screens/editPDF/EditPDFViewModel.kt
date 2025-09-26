package com.ganadoro.pile.ui.screens.editPDF

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.util.applyColorFilter
import com.ganadoro.pile.util.createContrastBrightnessMatrix
import com.ganadoro.pile.util.createPdfWithImages
import com.ganadoro.pile.util.renderAllPdfPages
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class EditPDFUIMode {
    SCROLL, COLOR, CROP_ROTATE
}

enum class EditType {
    COLOR, CROP
}

data class EditPDFUiState(
    var documentModel: DocumentModel? = null,
    var bitmaps: List<Bitmap> = emptyList(),
    val colorEditedBitmaps: Map<Int, Bitmap> = emptyMap(),
    val cropEditedBitmaps: Map<Int, Bitmap> = emptyMap(),
    val lastEditType: Map<Int, EditType> = emptyMap(),
    var selectedImageIndex: Int = 0,
    var uiMode: EditPDFUIMode = EditPDFUIMode.SCROLL,
    var selectedColorIndex: Map<Int, Int> = emptyMap(),
    var colorModifiedBitmaps: List<Bitmap>? = null
)

@SuppressLint("StaticFieldLeak")
class EditPDFViewModel(
    private val context: Context, // Safe
    private val documentModelRepository: DocumentModelRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(EditPDFUiState())
    var uiState: StateFlow<EditPDFUiState> = _uiState.asStateFlow()

    var onNext: (() -> Unit)? = null

    private var colorMatrixList = listOf(
        ColorMatrix(),
        ColorMatrix().apply { setSaturation(0f) },
        ColorMatrix().apply { setSaturation(0f) }.createContrastBrightnessMatrix(2f, -100f)
    )

    fun loadDocument(documentId: String) {
        Napier.d { "EditPDFViewModel.loadDocument: $documentId" }
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                _uiState.update {
                    it.copy(
                        documentModel = documentModelRepository.getDocumentModelById(documentId)
                            .first()
                    )
                }
            }
            launch {
                val file = File(context.filesDir, documentId)
                _uiState.update { it.copy(bitmaps = renderAllPdfPages(file)) }
            }
        }
    }

    fun onNext() {
        if (uiState.value.cropEditedBitmaps.isNotEmpty() && uiState.value.cropEditedBitmaps.isNotEmpty()) {
            updateDocumentPDF()
        }

        onNext?.invoke()
    }

    fun updateUIMode(mode: EditPDFUIMode) {
        _uiState.update { it.copy(uiMode = mode) }

        viewModelScope.launch {
            if (mode != EditPDFUIMode.COLOR) {
                _uiState.update { it.copy(colorModifiedBitmaps = null) }
                return@launch
            }

            _uiState.update {
                val selectedOriginalColorBitmap =
                    _uiState.value.cropEditedBitmaps[_uiState.value.selectedImageIndex]
                        ?: _uiState.value.bitmaps[_uiState.value.selectedImageIndex]

                it.copy(
                    colorModifiedBitmaps = colorMatrixList.map { colorMatrix ->
                        selectedOriginalColorBitmap.applyColorFilter(colorMatrix)
                    })
            }
        }
    }

    fun setSelectedImageIndex(index: Int) {
        if (uiState.value.uiMode != EditPDFUIMode.SCROLL) return

        viewModelScope.launch {
            _uiState.update { it.copy(selectedImageIndex = index) }
        }
    }

    fun setSelectedColorIndex(index: Int) {
        if (uiState.value.uiMode != EditPDFUIMode.COLOR) return

        viewModelScope.launch {
            val updatedSelectedColorIndex = uiState.value.selectedColorIndex.toMutableMap()

            updatedSelectedColorIndex[uiState.value.selectedImageIndex] = index

            _uiState.update { it.copy(selectedColorIndex = updatedSelectedColorIndex) }
            _uiState.update { state ->
                val index = state.selectedImageIndex
                val colorIndex = state.selectedColorIndex[index] ?: 0

                val baseBitmap = state.cropEditedBitmaps[_uiState.value.selectedImageIndex]
                    ?: state.bitmaps[_uiState.value.selectedImageIndex]

                val filteredBitmap = baseBitmap.applyColorFilter(colorMatrixList[colorIndex])

                state.copy(
                    colorEditedBitmaps = state.colorEditedBitmaps + (index to filteredBitmap),
                    lastEditType = state.lastEditType + (index to EditType.COLOR)
                )
            }
        }
    }

    fun cropImage(croppedBitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    cropEditedBitmaps = it.cropEditedBitmaps + (it.selectedImageIndex to croppedBitmap),
                    lastEditType = it.lastEditType + (it.selectedImageIndex to EditType.CROP)
                )
            }
        }
    }

    fun addNewImage() {

    }

    fun deleteSelectedImage() {
        val filteredBitmaps = _uiState.value.bitmaps.filterIndexed { index, _ ->
            index != uiState.value.selectedImageIndex
        }

        _uiState.value = _uiState.value.copy(bitmaps = filteredBitmaps)
    }

    private fun updateDocumentPDF() {
        viewModelScope.launch {
            val documentFile = File(context.filesDir, uiState.value.documentModel!!.id)

            val finalBitmapList = uiState.value.bitmaps.mapIndexed { index, original ->
                uiState.value.cropEditedBitmaps[index]
                    ?: uiState.value.colorEditedBitmaps[index]
                    ?: original
            }

            try {
                createPdfWithImages(
                    bitmaps = finalBitmapList, outputFile = documentFile
                )
            } catch (ex: Exception) {
                Napier.e { "EditPDFViewModel.updateDocumentPDF: ${ex.message}" }
            }
        }
    }
}