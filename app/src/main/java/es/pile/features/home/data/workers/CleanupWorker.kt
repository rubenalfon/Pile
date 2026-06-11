package es.pile.features.home.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import es.pile.features.home.data.workers.CleanupWorker.Companion.DOCUMENT_ID
import es.pile.features.home.domain.useCases.ManageTemporaryDocumentUseCase

/**
 * Background worker responsible for executing the permanent deletion of a document.
 *
 * This worker retrieves the [DOCUMENT_ID] from the input data,
 * and delegates the business logic to [ManageTemporaryDocumentUseCase].
 *
 * If the operation fails, it is configured to [Result.retry] based on the
 * WorkManager's backoff policy.
 */
class CleanupWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val manageTemporaryDocumentUseCase: ManageTemporaryDocumentUseCase
) : CoroutineWorker(context, workerParams) {
    /**
     * Constants and helpers for [CleanupWorker] input data.
     */
    companion object {
        /** Key for the document identifier in the [inputData] map. */
        const val DOCUMENT_ID = "DOC_ID_KEY"

        /**
         * Helper to create the [androidx.work.Data] required to start this worker.
         * @param documentId The ID to be passed to the worker.
         */
        fun buildInputData(documentId: String) = workDataOf(DOCUMENT_ID to documentId)
    }

    /**
     * Execution point for the background task.
     *
     * @return [Result.success] if processed, [Result.failure] if ID is missing,
     * or [Result.retry] if an exception occurs.
     */
    override suspend fun doWork(): Result {
        return try {
            val documentId = inputData.getString(DOCUMENT_ID) ?: return Result.failure()

            manageTemporaryDocumentUseCase.confirmPermanentDeletion(documentId)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}