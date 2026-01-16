package com.ganadoro.pile.ui.screens.editDocument

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.repositories.DocumentImageRepository
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.util.createContrastBrightnessMatrix
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class EditDocumentUIMode {
    SCROLL, COLOR, CROP_ROTATE
}

data class EditDocumentUiState(
    val documentModel: DocumentModel? = null,
    val documentImages: List<DocumentImage> = emptyList(),
    val originalBitmaps: List<Bitmap> = emptyList(),
    val modifiedBitmaps: Map<Int, Bitmap> = emptyMap(),
    val selectedImageIndex: Int = 0,
    val uiMode: EditDocumentUIMode = EditDocumentUIMode.SCROLL
)

@SuppressLint("StaticFieldLeak")
class EditPDFViewModel(
    private val context: Context, // Safe
    private val documentModelRepository: DocumentModelRepository,
    private val documentImageRepository: DocumentImageRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(EditDocumentUiState())
    var uiState: StateFlow<EditDocumentUiState> = _uiState.asStateFlow()

    var onNext: (() -> Unit)? = null

    private var colorMatrixList = listOf(
        ColorMatrix(),
        ColorMatrix().apply { setSaturation(0f) },
        ColorMatrix().apply { setSaturation(0f) }.createContrastBrightnessMatrix(2f, -100f)
    )

    fun loadDocument(documentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val documentModel = documentModelRepository.getDocumentModelById(documentId).first()
                    ?: return@launch

                val documentImages = documentModel.imageIds.mapNotNull { id ->
                    documentImageRepository.getDocumentImageById(id).first()
                }

                _uiState.update {
                    it.copy(
                        documentModel = documentModel,
                        documentImages = documentImages
                    )
                }

                val documentFolder = File(context.filesDir, documentId)

                val imageFiles = documentImages.map { File(documentFolder, it.id) }
//         ToDO: Cargar bitmaps
//                val bitmaps = prepareBitmapsFromFiles(imageFiles)
//
//                _uiState.update {
//                    it.copy(originalBitmaps = bitmaps)
//                }
            } catch (e: Exception) {
                Napier.e(e) { "Error cargando el documento $documentId" }
                // TODO: Gestionar error
            }
        }
    }


    fun onNext() {
//        if (uiState.value.cropEditedBitmaps.isNotEmpty() && uiState.value.cropEditedBitmaps.isNotEmpty()) {
//            updateDocumentPDF()
//        }

        onNext?.invoke()
    }

    fun updateUIMode(mode: EditDocumentUIMode) {
        _uiState.update { it.copy(uiMode = mode) }
    }

    fun setSelectedImageIndex(index: Int) {
        if (uiState.value.uiMode != EditDocumentUIMode.SCROLL) return

        viewModelScope.launch {
            _uiState.update { it.copy(selectedImageIndex = index) }
        }
    }

    fun setSelectedColorIndex(index: Int) {
        if (uiState.value.uiMode != EditDocumentUIMode.COLOR) return

//        viewModelScope.launch {
//            val updatedSelectedColorIndex = uiState.value.selectedColorIndex.toMutableMap()
//
//            updatedSelectedColorIndex[uiState.value.selectedImageIndex] = index
//
//            _uiState.update { it.copy(selectedColorIndex = updatedSelectedColorIndex) }
//            _uiState.update { state ->
//                val index = state.selectedImageIndex
//                val colorIndex = state.selectedColorIndex[index] ?: 0
//
//                val baseBitmap = state.cropEditedBitmaps[_uiState.value.selectedImageIndex]
//                    ?: state.originalBitmaps[_uiState.value.selectedImageIndex]
//
//                val filteredBitmap = baseBitmap.applyColorFilter(colorMatrixList[colorIndex])
//
//                state.copy(
//                    modifiedBitmaps = state.modifiedBitmaps + (index to filteredBitmap),
//                    lastEditType = state.lastEditType + (index to EditType.COLOR)
//                )
//            }
//        }
    }

    fun cropImage(croppedBitmap: Bitmap) {
//        viewModelScope.launch {
//            _uiState.update {
//                it.copy(
//                    cropEditedBitmaps = it.cropEditedBitmaps + (it.selectedImageIndex to croppedBitmap),
//                    lastEditType = it.lastEditType + (it.selectedImageIndex to EditType.CROP)
//                )
//            }
//        }
    }

    fun addNewImage() {

    }

    fun deleteSelectedImage() {
        val filteredBitmaps = _uiState.value.originalBitmaps.filterIndexed { index, _ ->
            index != uiState.value.selectedImageIndex
        }

        _uiState.value = _uiState.value.copy(originalBitmaps = filteredBitmaps)
    }

    private fun updateDocumentPDF() {
//        viewModelScope.launch {
//            val documentFile = File(context.filesDir, uiState.value.documentModel!!.id)
//
//            val finalBitmapList = uiState.value.originalBitmaps.mapIndexed { index, original ->
//                uiState.value.cropEditedBitmaps[index]
//                    ?: uiState.value.modifiedBitmaps[index]
//                    ?: original
//            }
//
//            try {
//                createPdfWithImages(
//                    bitmaps = finalBitmapList, outputFile = documentFile
//                )
//            } catch (ex: Exception) {
//                Napier.e { "EditPDFViewModel.updateDocumentPDF: ${ex.message}" }
//            }
//        }
    }
}