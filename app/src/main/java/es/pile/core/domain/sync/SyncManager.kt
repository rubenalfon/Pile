package es.pile.core.domain.sync

import es.pile.core.domain.models.SyncState
import kotlinx.coroutines.flow.StateFlow

interface SyncManager {
    /**
     * Observable state of the current synchronization process.
     */
    val syncState: StateFlow<SyncState>

    /**
     * Manually requests a synchronization.
     * @param force If true, it bypasses internal checks (like debounce).
     */
    fun requestSync(force: Boolean = false)
    
    /**
     * Starts the automatic monitoring of local changes to trigger syncs.
     */
    fun startAutoSync()

    /**
     * Validates a recovery key by attempting a test sync.
     * If valid, it saves the key and triggers a full sync.
     */
    suspend fun validateAndSetKey(key: String): Result<Unit>
}
