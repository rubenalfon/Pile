package es.pile.features.home.ui

import android.net.Uri
import es.pile.DocumentModel
import es.pile.PileModel
import es.pile.core.domain.models.DocumentCoverItem
import es.pile.core.ui.util.UiText


data class HomeState(
    val pileModels: List<PileModel> = emptyList(),
    val documentCoverItems: List<DocumentCoverItem> = emptyList(),
    val temporaryDocument: DocumentModel? = null,
    val coloredPileIds: List<String> = emptyList(),
    val cameraUri: Uri? = null,
    val showDraftWarning: Boolean = false,
    val isLoadingNewDocument: Boolean = false,
    val isInitialLoading: Boolean = true,
    val errorMessage: UiText? = null
)

sealed interface HomeEvent {
    data class OnImageDisplayed(val document: DocumentModel) : HomeEvent

    data object OnRemoveDraftDocument : HomeEvent
    data object OnRestoreDraftDocument : HomeEvent
    data object OnPurgeDraftDocument : HomeEvent

    data class OnCreatePile(val pileName: String, val iconId: String, val color: Long) : HomeEvent

    data class OnPdfImported(val uri: Uri) : HomeEvent
    data class OnImagesImported(val uris: List<Uri>) : HomeEvent
    data object OnCameraClick : HomeEvent
    data object OnCameraUriConsumed : HomeEvent

    data object OnConfirmImport : HomeEvent
    data object OnDismissDraftWarning : HomeEvent

    data object OnErrorDismissed : HomeEvent
}
