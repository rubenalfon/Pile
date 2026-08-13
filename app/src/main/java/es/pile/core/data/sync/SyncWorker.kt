package es.pile.core.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import es.pile.core.data.backup.EncryptionKeyRequiredException
import es.pile.core.data.backup.InvalidEncryptionKeyException
import es.pile.core.domain.models.SyncState
import es.pile.core.domain.usecases.sync.PerformSyncUseCase
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val performSyncUseCase: PerformSyncUseCase by inject()

    override suspend fun doWork(): Result {
        val result = performSyncUseCase { state ->
            val progress = when (state) {
                SyncState.Downloading -> STATE_DOWNLOADING
                SyncState.Uploading -> STATE_UPLOADING
                else -> STATE_SYNCING
            }

            runBlocking {
                setProgress(workDataOf(PROGRESS_STATE_KEY to progress))
            }
        }

        return result.fold(
            onSuccess = { Result.success() },
            onFailure = { error ->
                if (isNetworkError(error)) {
                    Result.retry()
                } else {
                    val errorType = when (error) {
                        is EncryptionKeyRequiredException -> ERROR_TYPE_KEY_REQUIRED
                        is InvalidEncryptionKeyException -> ERROR_TYPE_INVALID_KEY
                        else -> ERROR_TYPE_GENERIC
                    }
                    val outputData = workDataOf(
                        ERROR_TYPE_KEY to errorType,
                        ERROR_MESSAGE_KEY to (error.message ?: "Sync failed")
                    )
                    Result.failure(outputData)
                }
            }
        )
    }

    private fun isNetworkError(throwable: Throwable): Boolean {
        return throwable is java.net.UnknownHostException ||
                throwable is java.net.ConnectException ||
                throwable is java.net.SocketTimeoutException ||
                throwable is java.io.IOException && !isDiskError(throwable)
    }

    private fun isDiskError(throwable: Throwable): Boolean {
        val message = throwable.message?.lowercase() ?: ""
        return message.contains("disk full") || message.contains("no space")
    }

    companion object {
        const val WORK_NAME = "SyncWorker"

        const val PROGRESS_STATE_KEY = "STATE"
        const val STATE_DOWNLOADING = "DOWNLOADING"
        const val STATE_UPLOADING = "UPLOADING"
        const val STATE_SYNCING = "SYNCING"
        
        const val ERROR_TYPE_KEY = "ERROR_TYPE"
        const val ERROR_MESSAGE_KEY = "ERROR_MESSAGE"
        const val ERROR_TYPE_KEY_REQUIRED = "KEY_REQUIRED"
        const val ERROR_TYPE_INVALID_KEY = "INVALID_KEY"
        const val ERROR_TYPE_GENERIC = "GENERIC"
    }
}
