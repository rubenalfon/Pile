package es.pile.features.backup.data

import android.accounts.Account
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import es.pile.R
import es.pile.core.domain.backup.BackupAccountInfo
import es.pile.core.domain.backup.BackupProvider
import es.pile.core.domain.backup.BackupStorageInfo
import es.pile.core.domain.backup.RemoteFile
import es.pile.core.domain.backup.UserCancelledException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Google Drive implementation of [BackupProvider] using the App Data Folder.
 * Uses the modern Identity Services (AuthorizationClient).
 */
class GoogleDriveBackupProvider(
    private val context: Context
) : BackupProvider {
    override val name: String = context.getString(R.string.google_drive)
    override val icon: Int = R.drawable.google_drive
    override val iconFill: Int = R.drawable.google_drive_fill

    private var driveService: Drive? = null

    override suspend fun authenticate(
        onResolutionRequired: suspend (PendingIntent) -> Result<Intent>,
        selectedAccountEmail: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val serverClientId = context.getString(R.string.google_drive_client_id)
            if (serverClientId.isEmpty()) {
                throw Exception("Google Drive Client ID not configured in local.properties")
            }

            val authClient = Identity.getAuthorizationClient(context)

            val requestedScopes = listOf(
                Scope(DriveScopes.DRIVE_APPDATA),
                Scope("email")
            )
            val authorizationRequestBuilder = AuthorizationRequest.builder()
                .setRequestedScopes(requestedScopes)

            selectedAccountEmail?.let {
                authorizationRequestBuilder.setAccount(Account(it, "com.google"))
            }

            val authorizationRequest = authorizationRequestBuilder.build()
            val authResult = authClient.authorize(authorizationRequest).await()

            val accessToken = if (authResult.hasResolution()) {
                val intent = onResolutionRequired(authResult.pendingIntent!!).getOrElse {
                    throw UserCancelledException()
                }
                val resultFromIntent = authClient.getAuthorizationResultFromIntent(intent)
                resultFromIntent.accessToken
            } else {
                authResult.accessToken
            }

            if (accessToken == null) throw Exception("Failed to obtain access token")

            val requestInitializer = HttpRequestInitializer { request ->
                request.headers.authorization = "Bearer $accessToken"
            }

            driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer
            ).setApplicationName(context.getString(R.string.app_name)).build()
        }
    }

    override suspend fun uploadFile(
        fileName: String,
        content: InputStream,
        metadata: Map<String, String>
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val service = driveService ?: throw Exception("Not authenticated with Google Drive")

            val existingFiles = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$fileName'")
                .execute()

            val fileMetadata = File().apply {
                name = fileName
                parents = listOf("appDataFolder")
                properties = metadata
            }

            val mediaContent = InputStreamContent(null, content)

            val uploadedFile = if (existingFiles.files.isNotEmpty()) {
                // When updating, we don't include parents in the metadata as it's not writable
                val updateMetadata = File().apply {
                    name = fileName
                    properties = metadata
                }
                service.files().update(existingFiles.files[0].id, updateMetadata, mediaContent)
                    .execute()
            } else {
                service.files().create(fileMetadata, mediaContent).execute()
            }
            uploadedFile.id
        }
    }

    override suspend fun downloadFile(fileId: String): Result<InputStream> =
        withContext(Dispatchers.IO) {
            runCatching {
                val service = driveService ?: throw Exception("Not authenticated with Google Drive")
                service.files().get(fileId).executeMediaAsInputStream()
            }
        }

    override suspend fun listFiles(): Result<List<RemoteFile>> = withContext(Dispatchers.IO) {
        runCatching {
            val service = driveService ?: throw Exception("Not authenticated with Google Drive")
            val result = service.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id, name, size, properties, modifiedTime)")
                .execute()

            result.files.map {
                RemoteFile(
                    id = it.id,
                    name = it.name,
                    size = it.getSize() ?: 0L,
                    lastModified = it.modifiedTime?.value,
                    metadata = it.properties ?: emptyMap()
                )
            }
        }
    }

    override suspend fun getAccountInfo(): Result<BackupAccountInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val service = driveService ?: throw Exception("Not authenticated with Google Drive")
            val about = service.about().get().setFields("user").execute()
            BackupAccountInfo(
                email = about.user.emailAddress,
                displayName = about.user.displayName
            )
        }
    }

    override suspend fun getStorageInfo(): Result<BackupStorageInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val service = driveService ?: throw Exception("Not authenticated with Google Drive")
            val about = service.about().get().setFields("storageQuota").execute()

            // Calculate app usage by summing file sizes in appDataFolder
            val files = listFiles().getOrThrow()
            val appUsage = files.sumOf { it.size }

            BackupStorageInfo(
                totalBytes = about.storageQuota.limit ?: 0L,
                usedBytes = about.storageQuota.usage ?: 0L,
                appUsedBytes = appUsage
            )
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
            driveService = null
        }
    }

    override suspend fun deleteFile(fileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val service = driveService ?: throw Exception("Not authenticated with Google Drive")
            service.files().delete(fileId).execute()
            Unit
        }
    }
}
