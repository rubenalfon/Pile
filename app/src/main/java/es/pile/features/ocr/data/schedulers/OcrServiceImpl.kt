package es.pile.features.ocr.data.schedulers

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import es.pile.features.ocr.data.workers.OcrWorker
import es.pile.features.ocr.data.workers.PendingOcrWorker
import es.pile.features.ocr.domain.schedulers.OcrService

class OcrServiceImpl(
    private val workManager: WorkManager
) : OcrService {

    override fun scheduleOcrExtraction(documentId: String) {
        val data = OcrWorker.buildInputData(documentId)

        val ocrRequest = OneTimeWorkRequestBuilder<OcrWorker>()
            .setInputData(data)
            .build()

        workManager.enqueueUniqueWork(
            OcrWorker.getWorkName(documentId),
            ExistingWorkPolicy.REPLACE,
            ocrRequest
        )
    }

    override fun processPendingScans() {
        val pendingOcrRequest = OneTimeWorkRequestBuilder<PendingOcrWorker>().build()

        workManager.enqueueUniqueWork(
            PendingOcrWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            pendingOcrRequest
        )
    }
}
