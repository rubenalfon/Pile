package es.pile.features.backup.ui

import android.app.PendingIntent
import android.content.Intent
import es.pile.core.domain.backup.BackupProviderInfo
import es.pile.core.domain.models.SyncState
import es.pile.core.ui.util.UiText

data class BackupState(
    val isLoading: Boolean = true,
    val availableProviders: List<BackupProviderInfo> = emptyList(),
    val selectedProvider: BackupProviderInfo? = null,
    val accountEmail: String? = null,
    val storageUsage: UiText? = null,
    val backupUsingCellular: Boolean = false,
    val isEncryptionOn: Boolean = false,
    val lastSyncTimestamp: Long? = null,
    val pendingResolution: PendingIntent? = null,
    val isAuthErrorAlertVisible: Boolean = false,
    val navigateToUrl: UiText? = null,
    val isAccountPickerVisible: Boolean = false,
    val isEnterKeyDialogVisible: Boolean = false,
    val syncState: SyncState = SyncState.Idle
)

sealed interface BackupEvent {
    data object OnBackClicked : BackupEvent
    data object OnNavigateToEncryption : BackupEvent

    data class OnProviderSelected(val provider: BackupProviderInfo?) : BackupEvent
    data object OnSyncClicked : BackupEvent
    data object OnCellularBackupToggled : BackupEvent
    data class OnResolutionResult(val result: Result<Intent>) : BackupEvent
    data object OnRetryAuthentication : BackupEvent
    data object OnCancelAuthentication : BackupEvent
    data object OnSwitchAccountClicked : BackupEvent
    data object OnManageStorageClicked : BackupEvent
    data class OnAccountSelected(val email: String?) : BackupEvent
    data object OnUrlNavigated : BackupEvent
    data class OnEnterKeySubmitted(val key: String) : BackupEvent
    data object OnDismissEnterKeyDialog : BackupEvent
}
