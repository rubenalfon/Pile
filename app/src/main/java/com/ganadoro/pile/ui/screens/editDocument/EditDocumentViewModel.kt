package com.ganadoro.pile.ui.screens.editDocument

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.domain.models.ImageCropData
import com.ganadoro.pile.domain.models.ImageFilterType
import com.ganadoro.pile.domain.repositories.BitmapCacheRepository
import com.ganadoro.pile.domain.repositories.DocumentImageRepository
import com.ganadoro.pile.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.domain.usecases.AddCropControllerUseCase
import com.ganadoro.pile.domain.usecases.AddPageToDocumentUseCase
import com.ganadoro.pile.domain.usecases.ApplyImageFilterUseCase
import com.ganadoro.pile.domain.usecases.CropImageUseCase
import com.ganadoro.pile.domain.usecases.DeleteDocumentPageUseCase
import com.ganadoro.pile.domain.usecases.GetAvailableFiltersUseCase
import com.ganadoro.pile.domain.usecases.RequestBitmapLoadUseCase
import com.ganadoro.pile.domain.usecases.RequestThumbnailLoadUseCase
import com.ganadoro.pile.domain.usecases.RotateImageUseCase
import com.tanishranjan.cropkit.CropController
import io.github.aakira.napier.Napier
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
    val uiMode: EditDocumentUIMode = EditDocumentUIMode.SCROLL,
    val cropControllers: Map<String, CropController> = emptyMap()
)

@OptIn(ExperimentalCoroutinesApi::class)
class EditPDFViewModel(
    private val documentId: String,
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase,
    private val requestThumbnailLoadUseCase: RequestThumbnailLoadUseCase,
    private val addPageToDocumentUseCase: AddPageToDocumentUseCase,
    private val applyImageFilterUseCase: ApplyImageFilterUseCase,
    private val getAvailableFiltersUseCase: GetAvailableFiltersUseCase,
    private val deleteDocumentPageUseCase: DeleteDocumentPageUseCase,
    private val addCropControllerUseCase: AddCropControllerUseCase,
    private val rotateImageUseCase: RotateImageUseCase,
    private val cropImageUseCase: CropImageUseCase,
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

    fun requestThumbnailLoad(filterNumber: Int) {
        viewModelScope.launch {
            val documentImage =
                uiState.value.documentImages.getOrNull(uiState.value.selectedImageIndex)
                    ?: return@launch
            requestThumbnailLoadUseCase(documentId, documentImage, filterNumber)
        }
    }

    fun requestThumbnailKey(filterNumber: Int): String {
        val documentImage =
            uiState.value.documentImages.getOrNull(uiState.value.selectedImageIndex) ?: return ""

        return bitmapCacheRepository.getImageThumbnailKey(
            imageId = documentImage.id,
            filterId = filterNumber
        )
    }

    fun setSelectedImageIndex(index: Int) {
        if (uiState.value.uiMode != EditDocumentUIMode.SCROLL) return

        viewModelScope.launch {
            _uiState.update { it.copy(selectedImageIndex = index) }
        }
    }

    fun updateUIMode(newUiMode: EditDocumentUIMode) {
        val currentUiMode = uiState.value.uiMode

        when (currentUiMode) {
            EditDocumentUIMode.COLOR -> cleanSelectedImageCropController()
            EditDocumentUIMode.CROP_ROTATE -> cropImage()
            else -> {}
        }

        if (currentUiMode == newUiMode) _uiState.update { it.copy(uiMode = EditDocumentUIMode.SCROLL) }
        else _uiState.update { it.copy(uiMode = newUiMode) }
    }

    fun setSelectedColorIndex(index: Int) {
        val state = uiState.value
        if (state.uiMode != EditDocumentUIMode.COLOR) return

        val document = state.documentModel ?: return
        val selectedImage = state.documentImages.getOrNull(state.selectedImageIndex) ?: return

        viewModelScope.launch {
            applyImageFilterUseCase(
                document = document,
                documentImage = selectedImage,
                index
            )
        }
    }

    fun loadCropController(key: String) {
        viewModelScope.launch {
            val state = uiState.value
            val selectedImage = state.documentImages.getOrNull(state.selectedImageIndex)
                ?: return@launch

            val cropControllers = state.cropControllers
            if (cropControllers.containsKey(key)) return@launch

            try {
                val cropController = addCropControllerUseCase(documentId, selectedImage)

                _uiState.update {
                    it.copy(cropControllers = it.cropControllers + (key to cropController))
                }
            } catch (ex: Exception) {
                Napier.e("Error loading crop controller", ex)
                return@launch
                // TODO: Gestionar errores
            }
        }
    }

    private fun cropImage() {
        val state = uiState.value
        if (state.uiMode != EditDocumentUIMode.CROP_ROTATE) return

        val document = state.documentModel ?: return
        val selectedImage = state.documentImages.getOrNull(state.selectedImageIndex) ?: return
        val imageKey = requestImageKey(state.selectedImageIndex)

        val selectedCropController = state.cropControllers[imageKey] ?: return

        val cropData = selectedCropController.getCropData()

        viewModelScope.launch {
            cropImageUseCase(document, selectedImage, ImageCropData.fromCropData(cropData))
        }
    }

    fun rotateImage() {
        val state = uiState.value
        if (state.uiMode != EditDocumentUIMode.CROP_ROTATE) return

        val document = state.documentModel ?: return
        val selectedImage = state.documentImages.getOrNull(state.selectedImageIndex) ?: return
        val imageKey = requestImageKey(state.selectedImageIndex)

        viewModelScope.launch {
            state.cropControllers[imageKey]?.rotateClockwise()
            rotateImageUseCase(document, selectedImage)
        }
    }

    fun resetImage() {
        val state = uiState.value
        if (state.uiMode != EditDocumentUIMode.CROP_ROTATE) return
    }

    private fun cleanSelectedImageCropController() {
        _uiState.update { state ->
            val cropControllers = state.cropControllers
            val selectedImageKey = requestImageKey(state.selectedImageIndex)
            state.copy(cropControllers = cropControllers.filter { it.key != selectedImageKey })
        }
    }

    fun addNewImage(uriList: List<Uri>) {
        val document = uiState.value.documentModel ?: return

        viewModelScope.launch {
            try {
                addPageToDocumentUseCase.invoke(
                    document,
                    uriList
                )
            } catch (e: Exception) {
                Napier.e("Error importing images", e)
                // TODO: show in ui, toast
            }
        }
    }

    fun deleteSelectedImage() {
        val selectedImageIndex = uiState.value.selectedImageIndex
        val selectedDocumentImage = uiState.value.documentImages[selectedImageIndex]

        viewModelScope.launch {
            deleteDocumentPageUseCase(documentId = documentId, imageId = selectedDocumentImage.id)
        }
    }
}