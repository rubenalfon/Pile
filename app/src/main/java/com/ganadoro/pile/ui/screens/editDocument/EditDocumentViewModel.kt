package com.ganadoro.pile.ui.screens.editDocument

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.domain.usecase.RequestBitmapLoadUseCase
import com.ganadoro.pile.repositories.BitmapCacheRepository
import com.ganadoro.pile.repositories.DocumentImageRepository
import com.ganadoro.pile.repositories.DocumentModelRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EditDocumentUIMode {
    SCROLL, COLOR, CROP_ROTATE
}

data class EditDocumentUiState(
    val documentModel: DocumentModel? = null,
    val documentImages: List<DocumentImage> = emptyList(),
    val selectedImageIndex: Int = 0,
    val uiMode: EditDocumentUIMode = EditDocumentUIMode.SCROLL
)

@OptIn(ExperimentalCoroutinesApi::class)
class EditPDFViewModel(
    private val documentId: String,
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase,
    private val documentModelRepository: DocumentModelRepository,
    private val bitmapCacheRepository: BitmapCacheRepository,
    private val documentImageRepository: DocumentImageRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(EditDocumentUiState())
    var uiState: StateFlow<EditDocumentUiState> = _uiState.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    private val _navigationEvent = Channel<Unit>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            val documentFlow = documentModelRepository.getDocumentModelById(documentId)
                .distinctUntilChanged()

            val documentImagesFlow = documentFlow
                .map { it?.imageIds ?: emptyList() }
                .distinctUntilChanged()
                .flatMapLatest { ids ->
                    if (ids.isEmpty()) flowOf(emptyList())
                    else {
                        combine(ids.map { documentImageRepository.getDocumentImageById(it) }) { images ->
                            images.filterNotNull()
                        }
                    }
                }

            combine(documentFlow, documentImagesFlow) { document, images ->
                if (document == null) return@combine

                _uiState.update { currentState ->
                    currentState.copy(
                        documentModel = document,
                        documentImages = images
                    )
                }
            }.collect()
        }
    }

    fun requestBitmapLoad(pageNumber: Int) {
        viewModelScope.launch {
            val document = uiState.value.documentModel ?: return@launch
            requestBitmapLoadUseCase(document, pageNumber)
        }
    }

    fun requestImageKey(pageNumber: Int): String {
        val document = uiState.value.documentModel ?: return ""
        return bitmapCacheRepository.getImageKey(document, pageNumber)
    }

    fun updateUIMode(newUiMode: EditDocumentUIMode) {
        val currentUiMode = uiState.value.uiMode
        if (currentUiMode == newUiMode) _uiState.update { it.copy(uiMode = EditDocumentUIMode.SCROLL) }
        else _uiState.update { it.copy(uiMode = newUiMode) }
    }

    fun setSelectedImageIndex(index: Int) { // TODO: Update and usecase
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
//        val filteredBitmaps = _uiState.value.originalBitmaps.filterIndexed { index, _ ->
//            index != uiState.value.selectedImageIndex
//        }
//
//        _uiState.value = _uiState.value.copy(originalBitmaps = filteredBitmaps)
    }
}