package es.pile.features.home.data.schedulers

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import es.pile.features.home.data.workers.CleanupWorker
import es.pile.features.home.domain.schedulers.CleanupScheduler

/**
 * Android-specific implementation of [CleanupScheduler] using the WorkManager API.
 *
 * @property workManager The instance of [androidx.work.WorkManager] used for scheduling tasks.
 */
class WorkManagerCleanupScheduler(
    private val workManager: WorkManager
) : CleanupScheduler {

    override fun scheduleDocumentDeletion(documentId: String) {
        val data = CleanupWorker.buildInputData(documentId)

        val erasureRequest = OneTimeWorkRequestBuilder<CleanupWorker>()
            .setInputData(data)
            .build()

        workManager.enqueueUniqueWork(
            "cleanup_${documentId}",
            ExistingWorkPolicy.KEEP,
            erasureRequest
        )
    }
}