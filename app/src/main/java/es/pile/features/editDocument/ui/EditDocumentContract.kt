package es.pile.features.editDocument.ui

import android.net.Uri
import es.pile.DocumentModel
import es.pile.core.domain.models.ImageFilterType
import es.pile.core.domain.models.ImageItem
import es.pile.core.ui.util.UiText
import es.pile.features.editDocument.domain.models.ExtendedCropController


data class EditDocumentState(
    val draftDocument: DocumentModel? = null,
    val imageItems: List<ImageItem> = emptyList(),
    val thumbnailKeys: List<String> = emptyList(),
    val imageFilters: List<ImageFilterType> = emptyList(),
    val selectedImageIndex: Int = 0,
    val uiMode: EditDocumentMode = EditDocumentMode.SCROLL,
    val cropControllers: Map<String, ExtendedCropController> = emptyMap(),
    val isLoadingNewImage: Boolean = false,
    val showUnsavedChangesAlert: Boolean = false,
    val isDocumentModified: Boolean = false,
    val errorMessage: UiText? = null
)

enum class EditDocumentMode {
    SCROLL, COLOR, CROP_ROTATE
}

sealed interface EditDocumentEvent {
    data class OnBackClicked(val force: Boolean = false) : EditDocumentEvent
    data object OnExitCanceled : EditDocumentEvent
    data object OnExitConfirmed : EditDocumentEvent
    data object OnSave : EditDocumentEvent
    
    data class OnImageDisplayed(val pageNumber: Int) : EditDocumentEvent
    data class OnThumbnailDisplayed(val filterIndex: Int) : EditDocumentEvent
    data class OnCropDisplayed(val imageKey: String) : EditDocumentEvent
    data class OnSelectImage(val index: Int) : EditDocumentEvent
    data class OnMoveImage(val fromIndex: Int, val toIndex: Int) : EditDocumentEvent

    data class OnImportImages(val uris: List<Uri>) : EditDocumentEvent
    data class OnModeChange(val mode: EditDocumentMode) : EditDocumentEvent
    
    data class OnUpdateFilter(val index: Int) : EditDocumentEvent
    data object OnRotate : EditDocumentEvent
    
    data object OnRemoveSelectedImage : EditDocumentEvent
    data object OnRestoreRemovedImage : EditDocumentEvent
    data object OnPurgeRemovedImage : EditDocumentEvent

    data object OnErrorDismissed : EditDocumentEvent
}
