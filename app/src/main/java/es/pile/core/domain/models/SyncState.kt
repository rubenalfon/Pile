package es.pile.core.domain.models

import es.pile.R
import es.pile.core.ui.util.UiText

sealed interface SyncState {
    data object Idle : SyncState
    data object NoProvider : SyncState
    data object KeyRequired : SyncState
    data object InvalidKey : SyncState
    data object Syncing : SyncState
    data object VerifyingKey : SyncState
    data object Uploading : SyncState
    data object Downloading : SyncState
    data object WaitingForWifi : SyncState
    data class Success(val lastSyncTimestamp: Long) : SyncState
    data class EncryptionMismatch(val isCloudEncrypted: Boolean) : SyncState
    data class Error(val message: UiText) : SyncState

    val isSyncing: Boolean
        get() = this is Syncing || this is Uploading || this is Downloading || this is VerifyingKey

    val errorMessage: UiText?
        get() = when (this) {
            is Error -> message
            is EncryptionMismatch -> UiText.StringResource(
                if (isCloudEncrypted) R.string.error_cloud_is_encrypted else R.string.error_cloud_not_encrypted
            )
            else -> null
        }
}
