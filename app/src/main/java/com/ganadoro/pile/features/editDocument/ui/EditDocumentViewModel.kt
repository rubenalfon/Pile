package com.ganadoro.pile.features.editDocument.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.R
import com.ganadoro.pile.core.domain.models.ImageCropData
import com.ganadoro.pile.core.domain.models.ImageFilterType
import com.ganadoro.pile.core.domain.repositories.BitmapCacheRepository
import com.ganadoro.pile.core.domain.repositories.DocumentImageRepository
import com.ganadoro.pile.core.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.core.domain.repositories.FileRepository
import com.ganadoro.pile.core.domain.repositories.FileRepository.StorageType
import com.ganadoro.pile.core.ui.util.UiText
import com.ganadoro.pile.features.editDocument.domain.useCases.AddPageToDocumentUseCase
import com.ganadoro.pile.features.editDocument.domain.useCases.GetCropControllerUseCase
import com.ganadoro.pile.features.editDocument.domain.useCases.RemoveBitmapFromCacheUseCase
import com.ganadoro.pile.features.editDocument.domain.useCases.RequestDraftBitmapLoadUseCase
import com.ganadoro.pile.features.editDocument.domain.useCases.RequestThumbnailLoadUseCase
import com.ganadoro.pile.features.editDocument.domain.useCases.UpdateDocumentUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
class EditDocumentViewModel(
    private val documentId: String,
    private val requestDraftBitmapLoadUseCase: RequestDraftBitmapLoadUseCase,
    private val requestThumbnailLoadUseCase: RequestThumbnailLoadUseCase,
    private val addPageToDocumentUseCase: AddPageToDocumentUseCase,
    private val removeBitmapFromCacheUseCase: RemoveBitmapFromCacheUseCase,
    private val updateDocumentUseCase: UpdateDocumentUseCase,
    private val getCropControllerUseCase: GetCropControllerUseCase,
    private val documentModelRepository: DocumentModelRepository,
    private val bitmapCacheRepository: BitmapCacheRepository,
    private val documentImageRepository: DocumentImageRepository,
    private val fileRepository: FileRepository
) : ViewModel() {
    private var _state = MutableStateFlow(EditDocumentState())
    var state: StateFlow<EditDocumentState> = _state.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    private var deletedDocumentImages = mutableListOf<DocumentImage>()

    enum class NavigationType { NEXT, BACK }

    private val _navigationEvent = Channel<NavigationType>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private var originalDocument: DocumentModel? = null
    private var originalDocumentImages: List<DocumentImage> = emptyList()

    init {
        viewModelScope.launch {
            val document = documentModelRepository.getDocumentModelById(documentId)
                .filterNotNull()
                .first()

            val imageIds = document.imageIds

            val documentImages =
                imageIds.mapNotNull { documentImageRepository.getDocumentImageById(it).first() }

            originalDocument = document
            originalDocumentImages = documentImages

            _state.update { it.copy(imageFilters = ImageFilterType.entries) }
            updateImagesAndStatus(draft = document, images = documentImages)
        }
    }

    private fun updateImagesAndStatus(
        draft: DocumentModel? = state.value.draftDocument,
        images: List<DocumentImage> = state.value.documentItems.map { it.image },
        selectedIndex: Int = state.value.selectedImageIndex
    ) {
        val documentItems = draft?.let { doc ->
            images.mapIndexed { index, image ->
                DocumentEditItem(
                    image = image,
                    cacheKey = bitmapCacheRepository.getImageKey(doc, index)
                )
            }
        } ?: emptyList()

        val selectedImage = images.getOrNull(selectedIndex)
        val thumbnailKeys = selectedImage?.let { img ->
            state.value.imageFilters.indices.map { filterId ->
                bitmapCacheRepository.getImageThumbnailKey(img.id, filterId)
            }
        } ?: emptyList()

        val isModified = originalDocument != draft || originalDocumentImages != images

        _state.update {
            it.copy(
                draftDocument = draft,
                documentItems = documentItems,
                thumbnailKeys = thumbnailKeys,
                selectedImageIndex = selectedIndex,
                isDocumentModified = isModified
            )
        }
    }

    fun handleEvent(event: EditDocumentEvent) {
        when (event) {
            is EditDocumentEvent.OnBackClicked -> navigateBack(event.force)
            EditDocumentEvent.OnExitCanceled -> _state.update { it.copy(showUnsavedChangesAlert = false) }
            EditDocumentEvent.OnExitConfirmed -> navigateBack(true)
            EditDocumentEvent.OnSave -> navigateNext()

            is EditDocumentEvent.OnImageDisplayed -> requestBitmapLoad(event.pageNumber)
            is EditDocumentEvent.OnThumbnailDisplayed -> requestThumbnailLoad(event.filterIndex)
            is EditDocumentEvent.OnCropDisplayed -> loadCropController(event.imageKey)
            is EditDocumentEvent.OnSelectImage -> selectImage(event.index)

            is EditDocumentEvent.OnImportImages -> addImages(event.uris)
            is EditDocumentEvent.OnModeChange -> updateUIMode(event.mode)

            is EditDocumentEvent.OnUpdateFilter -> updateFilter(event.index)
            EditDocumentEvent.OnRotate -> rotateImage()

            EditDocumentEvent.OnRemoveSelectedImage -> removeSelectedImage()
            EditDocumentEvent.OnRestoreRemovedImage -> restoreRemovedImage()
            EditDocumentEvent.OnPurgeRemovedImage -> purgeRemovedImage()

            EditDocumentEvent.OnErrorDismissed -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun navigateBack(force: Boolean = false) {
        if (state.value.isDocumentModified && !force) {
            _state.update { it.copy(showUnsavedChangesAlert = true) }
            return
        }
        _state.update { it.copy(showUnsavedChangesAlert = false) }

        viewModelScope.launch {
            fileRepository.deleteDocumentStorage(StorageType.CACHE, documentId)

            _navigationEvent.send(NavigationType.BACK)
        }
    }

    private fun navigateNext() {
        val state = state.value
        val document = state.draftDocument ?: return

        if (state.isLoadingNewImage) return

        viewModelScope.launch {
            updateDocumentUseCase(document, state.documentItems.map { it.image })

            _navigationEvent.send(NavigationType.NEXT)
        }
    }

    private fun requestBitmapLoad(pageNumber: Int) {
        viewModelScope.launch {
            val currentState = state.value
            val document = currentState.draftDocument ?: return@launch
            val documentImage = currentState.documentItems.getOrNull(pageNumber)?.image
                ?: return@launch
            requestDraftBitmapLoadUseCase(document, documentImage)
        }
    }

    private fun requestThumbnailLoad(filterIndex: Int) {
        viewModelScope.launch {
            val currentState = state.value
            val documentImage =
                currentState.documentItems.getOrNull(currentState.selectedImageIndex)?.image
                    ?: return@launch
            requestThumbnailLoadUseCase(documentId, documentImage, filterIndex)
        }
    }

    private fun selectImage(index: Int) {
        if (state.value.uiMode != EditDocumentMode.SCROLL) return

        updateImagesAndStatus(selectedIndex = index)
    }

    private fun updateUIMode(newUiMode: EditDocumentMode) {
        val currentUiMode = state.value.uiMode

        when (currentUiMode) {
            EditDocumentMode.COLOR -> cleanSelectedCropController()
            EditDocumentMode.CROP_ROTATE -> cropImage()
            else -> {}
        }

        if (currentUiMode == newUiMode) _state.update { it.copy(uiMode = EditDocumentMode.SCROLL) }
        else _state.update { it.copy(uiMode = newUiMode) }
    }

    private fun cleanSelectedCropController() {
        _state.update { state ->
            val cropControllers = state.cropControllers
            val selectedImageKey = state.documentItems.getOrNull(state.selectedImageIndex)?.cacheKey ?: ""
            state.copy(cropControllers = cropControllers.filter { it.key != selectedImageKey })
        }
    }

    private fun updateFilter(index: Int) {
        val currentState = state.value
        if (currentState.uiMode != EditDocumentMode.COLOR) return

        val document = currentState.draftDocument ?: return
        val documentItem = currentState.documentItems.getOrNull(currentState.selectedImageIndex) ?: return
        val documentImage = documentItem.image

        val updatedDocumentImage = documentImage.copy(filter = index.toLong())

        val updatedImages = currentState.documentItems.map {
            if (it.image.id == updatedDocumentImage.id) updatedDocumentImage else it.image
        }

        updateImagesAndStatus(images = updatedImages)
        removeBitmapFromCacheUseCase.removeImage(document, documentImage.id)
    }

    private fun loadCropController(key: String) {
        viewModelScope.launch {
            val currentState = state.value
            val selectedImage = currentState.documentItems.getOrNull(currentState.selectedImageIndex)?.image
                ?: return@launch

            val cropControllers = currentState.cropControllers
            if (cropControllers.containsKey(key)) return@launch

            try {
                val extendedCropController = getCropControllerUseCase(documentId, selectedImage)

                _state.update {
                    it.copy(cropControllers = it.cropControllers + (key to extendedCropController))
                }
            } catch (ex: Exception) {
                Napier.e("Error loading crop controller", ex)
                _state.update {
                    it.copy(
                        uiMode = EditDocumentMode.SCROLL,
                        errorMessage = UiText.StringResource(R.string.error_loading_crop_controller)
                    )
                }
            }
        }
    }

    private fun cropImage() {
        val currentState = state.value
        if (currentState.uiMode != EditDocumentMode.CROP_ROTATE) return

        val document = currentState.draftDocument ?: return
        val documentItem = currentState.documentItems.getOrNull(currentState.selectedImageIndex) ?: return
        val documentImage = documentItem.image
        val imageKey = documentItem.cacheKey

        val selectedExtendedCropController = currentState.cropControllers[imageKey] ?: return


        val cropData = ImageCropData.fromCropData(
            selectedExtendedCropController.cropController.getCropData()
        )
        val scaleFactor = selectedExtendedCropController.scaleFactor

        val scaledCropData = cropData.scale(1 / scaleFactor)

        val updatedDocumentImage = documentImage.copy(crop = scaledCropData)

        val updatedImages = currentState.documentItems.map {
            if (it.image.id == updatedDocumentImage.id) updatedDocumentImage else it.image
        }

        updateImagesAndStatus(images = updatedImages)
        removeBitmapFromCacheUseCase.removeImageThumbnails(document, documentImage.id)
    }

    private fun rotateImage() {
        val currentState = state.value
        if (currentState.uiMode != EditDocumentMode.CROP_ROTATE) return

        val document = currentState.draftDocument ?: return
        val documentItem = currentState.documentItems.getOrNull(currentState.selectedImageIndex) ?: return
        val documentImage = documentItem.image
        val imageKey = documentItem.cacheKey

        val newRotation = (documentImage.rotation - 90) % 360
        val updatedDocumentImage = documentImage.copy(rotation = newRotation)

        viewModelScope.launch {
            currentState.cropControllers[imageKey]?.cropController?.rotateAntiClockwise()
        }

        val updatedImages = currentState.documentItems.map {
            if (it.image.id == updatedDocumentImage.id) updatedDocumentImage else it.image
        }

        updateImagesAndStatus(images = updatedImages)
        removeBitmapFromCacheUseCase.removeImageThumbnails(document, documentImage.id)
    }

    private fun addImages(uriList: List<Uri>) {
        val currentState = state.value
        val document = currentState.draftDocument ?: return

        if (currentState.isLoadingNewImage) return

        _state.update { it.copy(isLoadingNewImage = true) }

        viewModelScope.launch {
            val (updatedDocument, imageModels) = addPageToDocumentUseCase(document, uriList)

            val updatedImages = currentState.documentItems.map { it.image } + imageModels
            
            _state.update { it.copy(isLoadingNewImage = false) }
            updateImagesAndStatus(draft = updatedDocument, images = updatedImages)
        }
    }

    private fun removeSelectedImage() {
        val currentState = state.value
        val document = currentState.draftDocument ?: return
        val currentItems = currentState.documentItems
        val index = currentState.selectedImageIndex

        if (index !in currentItems.indices) return

        val itemToDelete = currentItems[index]
        val imageToDelete = itemToDelete.image

        val newImageList = currentItems.filter { it.image.id != imageToDelete.id }.map { it.image }

        val updatedDocument = document.copy(
            imageIds = newImageList.map { it.id }
        )

        deletedDocumentImages.add(imageToDelete)

        updateImagesAndStatus(
            draft = updatedDocument,
            images = newImageList,
            selectedIndex = index.coerceAtMost(newImageList.lastIndex)
        )
    }

    private fun restoreRemovedImage() {
        if (deletedDocumentImages.isEmpty()) return

        val currentState = state.value

        val restoredDocumentImage = deletedDocumentImages.first()
        deletedDocumentImages -= restoredDocumentImage

        val updatedImages = currentState.documentItems.map { it.image } + restoredDocumentImage
        val updatedDocument = currentState.draftDocument?.copy(
            imageIds = updatedImages.map { it.id }
        ) ?: return

        updateImagesAndStatus(draft = updatedDocument, images = updatedImages)
    }

    private fun purgeRemovedImage() {
        val document = state.value.draftDocument ?: return
        
        val imageToPurge = deletedDocumentImages.firstOrNull() ?: return
        deletedDocumentImages.removeAt(0)

        removeBitmapFromCacheUseCase.removeImageThumbnails(document, imageToPurge.id)

        if (imageToPurge.isDraft) {
            viewModelScope.launch {
                fileRepository.deleteDocumentImage(
                    storageType = StorageType.CACHE,
                    documentId = documentId,
                    imageId = imageToPurge.id
                )
            }
        }
    }
}