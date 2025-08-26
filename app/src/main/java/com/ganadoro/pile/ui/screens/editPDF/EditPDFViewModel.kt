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

data class EditPDFUiState(
    var documentModel: DocumentModel? = null,
    var bitmaps: List<Bitmap> = emptyList(),
    val editedBitmaps: Map<Int, Bitmap> = emptyMap(),
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
        if (uiState.value.editedBitmaps.isNotEmpty()) {
            updateDocumentPDF()
        }

        onNext?.invoke()
    }

    fun updateUIMode(mode: EditPDFUIMode) {
        _uiState.update { it.copy(uiMode = mode) }

        viewModelScope.launch {
            if (mode != EditPDFUIMode.COLOR) {
                _uiState.update { it.copy(colorModifiedBitmaps = null) }
            }

            if (mode == EditPDFUIMode.COLOR) {
                _uiState.update {
                    val selectedOriginalBitmap =
                        _uiState.value.bitmaps[_uiState.value.selectedImageIndex]

                    it.copy(
                        colorModifiedBitmaps = colorMatrixList.map { colorMatrix ->
                            selectedOriginalBitmap.applyColorFilter(colorMatrix)
                        }
                    )
                }
            }
        }
    }

    fun setSelectedImageIndex(index: Int) {
        if (uiState.value.uiMode != EditPDFUIMode.SCROLL) return

        viewModelScope.launch {
            Napier.d { "EditPDFViewModel.setSelectedImageIndex: $index" }
            _uiState.update { it.copy(selectedImageIndex = index) }
        }
    }

    fun setSelectedColorIndex(index: Int) {
        if (uiState.value.uiMode != EditPDFUIMode.COLOR) return

        viewModelScope.launch {
            Napier.d { "EditPDFViewModel.setSelectedColorIndex: $index" }
            val updatedSelectedColorIndex = uiState.value.selectedColorIndex.toMutableMap()

            updatedSelectedColorIndex[uiState.value.selectedImageIndex] = index

            _uiState.update { it.copy(selectedColorIndex = updatedSelectedColorIndex) }

            _uiState.update {
                it.copy(
                    editedBitmaps = it.editedBitmaps + (it.selectedImageIndex to it.bitmaps[it.selectedImageIndex].applyColorFilter(
                        colorMatrixList[it.selectedColorIndex[it.selectedImageIndex] ?: 0]
                    ))
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

            val finalBitmapList = uiState.value.bitmaps.mapIndexed { index, it ->
                if (uiState.value.editedBitmaps.containsKey(index)) {
                    uiState.value.editedBitmaps[index]!!
                } else {
                    it
                }
            }

            try {
                createPdfWithImages(
                    bitmaps = finalBitmapList,
                    outputFile = documentFile
                )
            } catch (ex: Exception) {
                Napier.e { "EditPDFViewModel.updateDocumentPDF: ${ex.message}" }
            }
        }
    }
}