package es.pile.core.domain.models

import es.pile.core.ui.util.UiText

sealed interface SyncState {
    data object Idle : SyncState
    data object NoProvider : SyncState
    data object Syncing : SyncState
    data object Uploading : SyncState
    data object Downloading : SyncState
    data class Success(val lastSyncTimestamp: Long) : SyncState
    data class Error(val message: UiText) : SyncState
}
