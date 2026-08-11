package es.pile.core.domain.repositories

import es.pile.core.domain.backup.BackupProvider
import es.pile.core.domain.models.BackupStats
import es.pile.core.domain.models.SyncState

/**
 * Repository for orchestrating backup and restore operations across different providers.
 */
interface BackupRepository {
    /**
     * List of all providers available in the current build.
     */
    val availableProviders: List<BackupProvider>
    
    /**
     * Performs a two-way synchronization between local and cloud storage.
     * It uploads missing local files and metadata, and downloads missing cloud files.
     *
     * @param provider The cloud storage provider.
     * @param tempMasterKey An optional master key to use for this sync session (not yet saved to settings).
     * @param onProgress Callback to report current sync state (Downloading, Uploading, etc.).
     */
    suspend fun sync(
        provider: BackupProvider,
        tempMasterKey: String? = null,
        onProgress: (SyncState) -> Unit = {}
    ): Result<Unit>

    /**
     * Analyzes the state of local vs cloud files to determine backup statistics.
     */
    suspend fun getBackupStats(provider: BackupProvider): Result<BackupStats>
}
