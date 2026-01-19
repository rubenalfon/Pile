package com.ganadoro.pile.ui.screens.editDocument

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.domain.models.ImageFilterType
import com.ganadoro.pile.domain.repositories.BitmapCacheRepository
import com.ganadoro.pile.domain.repositories.DocumentImageRepository
import com.ganadoro.pile.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.domain.usecases.ApplyImageFilterUseCase
import com.ganadoro.pile.domain.usecases.DeleteDocumentPageUseCase
import com.ganadoro.pile.domain.usecases.GetAvailableFiltersUseCase
import com.ganadoro.pile.domain.usecases.RequestBitmapLoadUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EditDocumentUIMode {
    SCROLL, COLOR, CROP_ROTATE
}

data class EditDocumentUiState(
    val documentModel: DocumentModel? = null,
    val documentImages: List<DocumentImage> = emptyList(),
    val imageFilters: List<ImageFilterType>? = null,
    val selectedImageIndex: Int = 0,
    val uiMode: EditDocumentUIMode = EditDocumentUIMode.SCROLL
)

@OptIn(ExperimentalCoroutinesApi::class)
class EditPDFViewModel(
    private val documentId: String,
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase,
    private val applyImageFilterUseCase: ApplyImageFilterUseCase,
    private val getAvailableFiltersUseCase: GetAvailableFiltersUseCase,
    private val deleteDocumentPageUseCase: DeleteDocumentPageUseCase,
    private val documentModelRepository: DocumentModelRepository,
    private val bitmapCacheRepository: BitmapCacheRepository,
    private val documentImageRepository: DocumentImageRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(EditDocumentUiState())
    var uiState: StateFlow<EditDocumentUiState> = _uiState.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

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
                        documentImages = images,
                        imageFilters = currentState.imageFilters ?: getAvailableFiltersUseCase()
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

    fun setSelectedImageIndex(index: Int) {
        if (uiState.value.uiMode != EditDocumentUIMode.SCROLL) return

        viewModelScope.launch {
            _uiState.update { it.copy(selectedImageIndex = index) }
        }
    }

    fun setSelectedColorIndex(index: Int) {
        if (uiState.value.uiMode != EditDocumentUIMode.COLOR) return

        val document = uiState.value.documentModel ?: return
        val selectedImageIndex = uiState.value.selectedImageIndex
        val selectedDocumentImage = uiState.value.documentImages[selectedImageIndex]

        viewModelScope.launch {
            applyImageFilterUseCase(
                document = document,
                documentImage = selectedDocumentImage,
                index
            )
        }
    }

    fun cropImage(croppedBitmap: Bitmap) {// TODO USECASE

    }

    fun addNewImage() { // TODO USECASE

    }

    fun deleteSelectedImage() {
        val selectedImageIndex = uiState.value.selectedImageIndex
        val selectedDocumentImage = uiState.value.documentImages[selectedImageIndex]

        viewModelScope.launch {
            deleteDocumentPageUseCase(documentId = documentId, imageId = selectedDocumentImage.id)
        }
    }
}