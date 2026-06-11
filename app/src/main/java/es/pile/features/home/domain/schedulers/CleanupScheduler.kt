package es.pile.features.home.domain.schedulers

/**
 * Interface defining the contract for scheduling deferred document cleanup operations.
 */
interface CleanupScheduler {
    /**
     * Schedules a background task to permanently delete a document and its
     * associated resources after a predefined period.
     *
     * @param documentId The unique identifier of the document to be cleaned up.
     */
    fun scheduleDocumentDeletion(documentId: String)
}