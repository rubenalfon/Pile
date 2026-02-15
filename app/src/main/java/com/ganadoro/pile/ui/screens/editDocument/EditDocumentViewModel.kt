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
import com.ganadoro.pile.domain.repositories.FileRepository
import com.ganadoro.pile.domain.repositories.FileRepository.StorageType
import com.ganadoro.pile.domain.usecases.document.AddPageToDocumentUseCase
import com.ganadoro.pile.domain.usecases.document.UpdateDocumentUseCase
import com.ganadoro.pile.domain.usecases.image.GetAvailableFiltersUseCase
import com.ganadoro.pile.domain.usecases.image.GetCropControllerUseCase
import com.ganadoro.pile.domain.usecases.image.RemoveBitmapFromCacheUseCase
import com.ganadoro.pile.domain.usecases.image.RequestDraftBitmapLoadUseCase
import com.ganadoro.pile.domain.usecases.image.RequestThumbnailLoadUseCase
import com.tanishranjan.cropkit.CropController
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EditDocumentUIMode {
    SCROLL, COLOR, CROP_ROTATE
}

data class EditDocumentUiState(
    val draftDocument: DocumentModel? = null,
    val documentImages: List<DocumentImage> = emptyList(),
    val imageFilters: List<ImageFilterType>? = null,
    val selectedImageIndex: Int = 0,
    val uiMode: EditDocumentUIMode = EditDocumentUIMode.SCROLL,
    val cropControllers: Map<String, CropController> = emptyMap()
)

@OptIn(ExperimentalCoroutinesApi::class)
class EditPDFViewModel(
    private val documentId: String,
    private val requestDraftBitmapLoadUseCase: RequestDraftBitmapLoadUseCase,
    private val requestThumbnailLoadUseCase: RequestThumbnailLoadUseCase,
    private val addPageToDocumentUseCase: AddPageToDocumentUseCase,
    private val removeBitmapFromCacheUseCase: RemoveBitmapFromCacheUseCase,
    private val getAvailableFiltersUseCase: GetAvailableFiltersUseCase,
    private val updateDocumentUseCase: UpdateDocumentUseCase,
    private val getCropControllerUseCase: GetCropControllerUseCase,
    private val documentModelRepository: DocumentModelRepository,
    private val bitmapCacheRepository: BitmapCacheRepository,
    private val documentImageRepository: DocumentImageRepository,
    private val fileRepository: FileRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(EditDocumentUiState())
    var uiState: StateFlow<EditDocumentUiState> = _uiState.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    private var deletedDocumentImages = mutableListOf<DocumentImage>()

    enum class NavigationType { NEXT, BACK }

    private val _navigationEvent = Channel<NavigationType>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            val document = documentModelRepository.getDocumentModelById(documentId)
                .filterNotNull()
                .first()

            val imageIds = document.imageIds

            val documentImages =
                imageIds.mapNotNull { documentImageRepository.getDocumentImageById(it).first() }


            _uiState.update { currentState ->
                currentState.copy(
                    draftDocument = document,
                    documentImages = documentImages,
                    imageFilters = getAvailableFiltersUseCase()
                )
            }
        }
    }

    fun onNavigateBack() {
        viewModelScope.launch {
            fileRepository.deleteDocumentStorage(StorageType.CACHE, documentId)

            _navigationEvent.send(NavigationType.BACK)
        }
    }

    fun onNavigateNext() {
        val state = uiState.value
        val document = state.draftDocument ?: return

        viewModelScope.launch {
            updateDocumentUseCase(document, state.documentImages)

            _navigationEvent.send(NavigationType.NEXT)
        }
    }

    fun requestBitmapLoad(pageNumber: Int) {
        viewModelScope.launch {
            val document = uiState.value.draftDocument ?: return@launch
            val documentImage = uiState.value.documentImages.getOrNull(pageNumber)
                ?: return@launch
            requestDraftBitmapLoadUseCase(document, documentImage)
        }
    }

    fun requestImageKey(pageNumber: Int): String {
        val document = uiState.value.draftDocument ?: return ""
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

        val document = state.draftDocument ?: return
        val documentImage = state.documentImages.getOrNull(state.selectedImageIndex) ?: return

        val updatedDocumentImage = documentImage.copy(filter = index.toLong())

        _uiState.update { state ->
            state.copy(documentImages = state.documentImages.map {
                if (it.id == updatedDocumentImage.id) updatedDocumentImage else it
            })
        }
        removeBitmapFromCacheUseCase.removeImage(document, documentImage.id)
    }

    fun loadCropController(key: String) {
        viewModelScope.launch {
            val state = uiState.value
            val selectedImage = state.documentImages.getOrNull(state.selectedImageIndex)
                ?: return@launch

            val cropControllers = state.cropControllers
            if (cropControllers.containsKey(key)) return@launch

            try {
                val cropController = getCropControllerUseCase(documentId, selectedImage)

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

        val document = state.draftDocument ?: return
        val documentImage = state.documentImages.getOrNull(state.selectedImageIndex) ?: return
        val imageKey = requestImageKey(state.selectedImageIndex)

        val selectedCropController = state.cropControllers[imageKey] ?: return

        val cropData = ImageCropData.fromCropData(selectedCropController.getCropData())

        val updatedDocumentImage = documentImage.copy(crop = cropData)

        _uiState.update { state ->
            state.copy(documentImages = state.documentImages.map {
                if (it.id == updatedDocumentImage.id) updatedDocumentImage else it
            })
        }
        removeBitmapFromCacheUseCase.removeImageThumbnails(document, documentImage.id)
    }

    fun rotateImage() {
        val state = uiState.value
        if (state.uiMode != EditDocumentUIMode.CROP_ROTATE) return

        val document = state.draftDocument ?: return
        val documentImage = state.documentImages.getOrNull(state.selectedImageIndex) ?: return
        val imageKey = requestImageKey(state.selectedImageIndex)

        val newRotation = (documentImage.rotation - 90) % 360
        val updatedDocumentImage = documentImage.copy(rotation = newRotation)

        viewModelScope.launch {
            state.cropControllers[imageKey]?.rotateAntiClockwise()
        }

        _uiState.update { state ->
            state.copy(documentImages = state.documentImages.map {
                if (it.id == updatedDocumentImage.id) updatedDocumentImage else it
            })
        }
        removeBitmapFromCacheUseCase.removeImageThumbnails(document, documentImage.id)
    }

    private fun cleanSelectedImageCropController() {
        _uiState.update { state ->
            val cropControllers = state.cropControllers
            val selectedImageKey = requestImageKey(state.selectedImageIndex)
            state.copy(cropControllers = cropControllers.filter { it.key != selectedImageKey })
        }
    }

    fun addNewImage(uriList: List<Uri>) {
        val document = uiState.value.draftDocument ?: return

        viewModelScope.launch {
            val (updatedDocument, imageModels) =
                addPageToDocumentUseCase(document, uriList)

            _uiState.update {
                it.copy(
                    draftDocument = updatedDocument,
                    documentImages = it.documentImages + imageModels
                )
            }
        }
    }

    fun deleteSelectedImage() {
        _uiState.update { state ->
            val document = state.draftDocument ?: return@update state
            val currentImages = state.documentImages
            val index = state.selectedImageIndex

            if (index !in currentImages.indices) return@update state

            val imageToDelete = currentImages[index]

            val newImagesList = currentImages.filter { it.id != imageToDelete.id }

            val newImageIds = document.imageIds.filter { it != imageToDelete.id }
            val updatedDocument = document.copy(imageIds = newImageIds)

            deletedDocumentImages.add(imageToDelete)

            state.copy(
                draftDocument = updatedDocument,
                documentImages = newImagesList,
            )
        }
    }
}