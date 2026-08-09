package es.pile.core.domain.usecases.sync

import es.pile.core.domain.models.SyncState
import es.pile.core.domain.repositories.BackupRepository
import es.pile.core.domain.repositories.SettingsRepository
import kotlinx.coroutines.flow.first

class PerformSyncUseCase(
    private val backupRepository: BackupRepository,
    private val settingsRepository: SettingsRepository
) {
    /**
     * Performs a synchronization with the selected cloud provider.
     * @param tempMasterKey An optional master key if it's not yet saved (e.g. initial setup).
     * @param onProgress Callback to report current sync state.
     */
    suspend operator fun invoke(
        tempMasterKey: String? = null,
        onProgress: (SyncState) -> Unit = {}
    ): Result<Unit> {
        val settings = settingsRepository.userSettings.first()
        val providerName = settings.selectedBackupProviderName ?: return Result.success(Unit)
        
        val provider = backupRepository.availableProviders.find { it.name == providerName }
            ?: return Result.failure(Exception("Provider not found: $providerName"))

        // Authenticate first if needed (Providers should handle internal session)
        return provider.authenticate(
            onResolutionRequired = { Result.failure(Exception("Manual resolution required")) }
        ).fold(
            onSuccess = {
                backupRepository.sync(provider, tempMasterKey, onProgress)
            },
            onFailure = { Result.failure(it) }
        )
    }
}
