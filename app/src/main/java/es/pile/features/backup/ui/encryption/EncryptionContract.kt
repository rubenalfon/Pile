package es.pile.features.backup.ui.encryption

import es.pile.core.ui.util.UiText

data class EncryptionState(
    val isEncryptionOn: Boolean = false,
    val recoveryKey: String? = null,
    val isRecoveryKeyVisible: Boolean = false,
    val isDisableAlertVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: UiText? = null
)

sealed interface EncryptionEvent {
    data object OnToggleEncryption : EncryptionEvent
    data object OnShowRecoveryKeyClicked : EncryptionEvent
    data object OnHideRecoveryKey : EncryptionEvent
    data object OnRecoveryKeyConfirmed : EncryptionEvent
    data object OnShowDisableAlert : EncryptionEvent
    data object OnHideDisableAlert : EncryptionEvent
    data object OnConfirmDisable : EncryptionEvent
    data object OnBackClicked : EncryptionEvent
}
