package com.pile.features.home.ui

import android.net.Uri
import com.pile.DocumentModel
import com.pile.PileModel
import com.pile.core.domain.models.DocumentCoverItem
import com.pile.core.ui.util.UiText


data class HomeState(
    val pileModels: List<PileModel> = emptyList(),
    val documentCoverItems: List<DocumentCoverItem> = emptyList(),
    val temporaryDocument: DocumentModel? = null,
    val coloredPileIds: List<String> = emptyList(),
    val cameraUri: Uri? = null,
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

    data object OnErrorDismissed : HomeEvent
}
