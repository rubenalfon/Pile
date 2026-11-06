package es.pile.features.ocr.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import es.pile.features.ocr.domain.usecases.UpdateDocumentOcrTextUseCase
import io.github.aakira.napier.Napier

class OcrWorker(
    context: Context,
    params: WorkerParameters,
    private val updateDocumentOcrTextUseCase: UpdateDocumentOcrTextUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val documentId = inputData.getString(DOCUMENT_ID) ?: return Result.failure()

            val ocrResult = updateDocumentOcrTextUseCase(documentId)

            if (ocrResult.isSuccess) {
                Result.success()
            } else {
                Napier.e("OCR worker failed for document $documentId", ocrResult.exceptionOrNull())
                Result.retry()
            }
        } catch (e: Exception) {
            Napier.e("Error executing OCR worker", e)
            Result.retry()
        }
    }

    companion object {
        fun getWorkName(documentId: String) = "ocr_$documentId"

        const val DOCUMENT_ID = "DOC_ID_KEY"
        fun buildInputData(documentId: String) = workDataOf(DOCUMENT_ID to documentId)
    }
}
