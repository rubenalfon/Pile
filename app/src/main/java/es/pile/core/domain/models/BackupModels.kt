package es.pile.core.domain.models

/**
 * Represents statistics about the backup health and counts.
 */
data class BackupStats(
    val lastBackupTimestamp: Long?,
    val missingLocalFilesCount: Int,
    val totalRemoteFilesCount: Int
)
