package es.pile.features.ocr.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.features.ocr.domain.schedulers.OcrService
import io.github.aakira.napier.Napier

class PendingOcrWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val documentModelRepository: DocumentModelRepository,
    private val ocrService: OcrService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val documents = documentModelRepository.getAllDocumentModels()

            documents.forEach { doc ->
                val isOcrEligible = doc.imageIds.isNotEmpty() || doc.isIncomingPdf
                if (isOcrEligible && doc.ocrText.isEmpty()) {
                    ocrService.scheduleOcrExtraction(doc.id)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Napier.e("Error syncing pending OCR tasks", e)
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "sync_pending_ocr"
    }
}
