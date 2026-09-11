package es.pile.features.backup.domain

import android.content.Context

/**
 * Interface defining the contract for handling backup-related authentication.
 */
interface BackupAuthHandler {
    /**
     * Launches a UI component to allow the user to select an account for backup.
     *
     * @param context The context used to launch the account picker.
     * @param clientId The client ID required for authentication (e.g., Google Drive client ID).
     * @return The identifier of the selected account (e.g., email or ID token), or null if the operation was cancelled or failed.
     */
    suspend fun launchAccountPicker(context: Context, clientId: String): String?
}
