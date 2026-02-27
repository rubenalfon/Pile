package com.ganadoro.pile.data

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ganadoro.pile.data.workers.CleanupWorker
import com.ganadoro.pile.domain.CleanupScheduler

/**
 * Android-specific implementation of [CleanupScheduler] using the WorkManager API.
 *
 * @property workManager The instance of [WorkManager] used for scheduling tasks.
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