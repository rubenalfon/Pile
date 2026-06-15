package es.pile.features.pileDetail.ui

import android.net.Uri
import es.pile.DocumentModel
import es.pile.PileModel
import es.pile.core.domain.models.DocumentCoverItem
import es.pile.core.ui.util.UiText


data class PileDetailState(
    val pile: PileModel? = null,
    val documentCoverItems: List<DocumentCoverItem> = emptyList(),
    val temporaryDocument: DocumentModel? = null,
    val cameraUri: Uri? = null,
    val isLoading: Boolean = true,
    val showDraftWarning: Boolean = false,
    val isLoadingNewDocument: Boolean = false,
    val errorMessage: UiText? = null
)

sealed interface PileDetailEvent {
    data class OnImageDisplayed(val document: DocumentModel) : PileDetailEvent
    data class OnPileChange(val name: String, val iconId: String, val color: Long) :
        PileDetailEvent

    data object OnDeletePile : PileDetailEvent

    data class OnPdfImported(val uri: Uri) : PileDetailEvent
    data class OnImagesImported(val uris: List<Uri>) : PileDetailEvent
    data object OnCameraClick : PileDetailEvent
    data object OnCameraUriConsumed : PileDetailEvent

    data object OnConfirmImport : PileDetailEvent
    data object OnDismissDraftWarning : PileDetailEvent

    data object OnErrorDismissed : PileDetailEvent
}