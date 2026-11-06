package es.pile.features.ocr.domain.schedulers

/**
 * Service responsible for scheduling and managing OCR (Optical Character Recognition) tasks.
 * This interface abstracts the underlying execution mechanism (e.g., WorkManager) to process documents.
 */
interface OcrService {
    /**
     * Schedules a background task to perform OCR extraction on a specific document.
     * This is typically called after a document is created or modified.
     *
     * @param documentId The unique identifier of the document to be processed.
     */
    fun scheduleOcrExtraction(documentId: String)

    /**
     * Checks for any documents that require OCR processing but haven't been processed yet,
     * and schedules them for extraction.
     */
    fun processPendingScans()
}
