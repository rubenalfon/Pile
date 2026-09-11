package es.pile.features.backup.ui.wipe

import es.pile.core.ui.util.UiText

data class WipeCloudState(
    val isWiping: Boolean = false,
    val isSuccess: Boolean = false,
    val showConfirmationDialog: Boolean = false,
    val error: UiText? = null
)

sealed interface WipeCloudEvent {
    data object OnConfirmWipe : WipeCloudEvent
    data object OnBackClicked : WipeCloudEvent
    data object ShowConfirmationDialog : WipeCloudEvent
    data object HideConfirmationDialog : WipeCloudEvent
}
