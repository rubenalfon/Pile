package es.pile.core.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
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
                SyncState.Downloading -> "DOWNLOADING"
                SyncState.Uploading -> "UPLOADING"
                else -> "SYNCING"
            }

            runBlocking {
                setProgress(workDataOf("STATE" to progress))
            }
        }
        return if (result.isSuccess) {
            Result.success()
        } else {
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "SyncWorker"
    }
}
