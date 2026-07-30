package es.pile.core.domain.backup

import java.io.InputStream

/**
 * Exception thrown when the user explicitly cancels an authentication flow.
 */
class UserCancelledException : Exception("User cancelled the authentication flow")

/**
 * Interface defining the contract for cloud storage providers.
 */
interface BackupProvider {
    /**
     * Unique name of the provider (e.g., "Google Drive").
     */
    val name: String

    /**
     * Icon representing the provider.
     */
    val icon: Int

    /**
     * Icon representing the provider in a filled state.
     */
     val iconFill: Int

    /**
     * Triggers the authentication flow for the provider.
     * 
     * @param onResolutionRequired Callback to handle cases where user interaction is needed (e.g., showing a login dialog).
     * @param selectedAccountEmail Optional email to target a specific account during authorization.
     */
    suspend fun authenticate(
        onResolutionRequired: suspend (intent: android.app.PendingIntent) -> Result<android.content.Intent>,
        selectedAccountEmail: String? = null
    ): Result<Unit>

    /**
     * Uploads a file to the cloud storage.
     */
    suspend fun uploadFile(
        fileName: String,
        content: InputStream,
        metadata: Map<String, String> = emptyMap()
    ): Result<String>

    /**
     * Downloads a file from the cloud storage as an [InputStream].
     */
    suspend fun downloadFile(fileId: String): Result<InputStream>

    /**
     * Lists all files relevant to the app in the cloud storage.
     */
    suspend fun listFiles(): Result<List<RemoteFile>>

    /**
     * Retrieves information about the account authenticated with the provider.
     */
    suspend fun getAccountInfo(): Result<BackupAccountInfo>

    /**
     * Retrieves storage information from the provider.
     */
    suspend fun getStorageInfo(): Result<BackupStorageInfo>

    /**
     * Logs out the user from the provider.
     */
    suspend fun logout(): Result<Unit>

    /**
     * Deletes a file from the cloud storage.
     */
    suspend fun deleteFile(fileId: String): Result<Unit>
}

/**
 * Lightweight model representing a backup provider for the UI.
 */
data class BackupProviderInfo(
    val name: String,
    val icon: Int,
    val iconFill: Int
)

fun BackupProvider.toInfo() = BackupProviderInfo(
    name = name,
    icon = icon,
    iconFill = iconFill
)

/**
 * Data class representing a file stored in a cloud provider.
 */
data class RemoteFile(
    val id: String,
    val name: String,
    val size: Long = 0L,
    val lastModified: Long? = null,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Information about the account used for backup.
 */
data class BackupAccountInfo(
    val email: String,
    val displayName: String? = null
)

/**
 * Information about the storage status in the cloud provider.
 */
data class BackupStorageInfo(
    val totalBytes: Long,
    val usedBytes: Long,
    val appUsedBytes: Long = 0L
)
