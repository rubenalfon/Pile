package es.pile.core.domain.models

/**
 * Represents the synchronization state between local and cloud storage.
 */
data class BackupSyncStatus(
    val lastBackupDateTime: String?,
    val missingLocalFilesCount: Int,
    val totalRemoteFilesCount: Int
)
