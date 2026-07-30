package es.pile.features.backup.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import es.pile.features.backup.domain.BackupAuthHandler

/**
 * Google-specific implementation of [BackupAuthHandler] using the Android Credential Manager.
 */
class GoogleBackupAuthHandler : BackupAuthHandler {
    /**
     * Launches the Google account picker using [CredentialManager].
     *
     * @param context The context used to initialize [CredentialManager].
     * @param clientId The Google Cloud console client ID.
     * @return The ID token of the selected Google account, or null if the process fails or is canceled.
     */
    override suspend fun launchAccountPicker(context: Context, clientId: String): String? {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(clientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            googleIdTokenCredential.id
        } catch (_: Exception) {
            null
        }
    }
}
