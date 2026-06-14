package es.pile.features.home.domain.useCases

import android.net.Uri
import es.pile.DocumentImage
import es.pile.DocumentModel
import es.pile.core.domain.models.DocumentStatusConstants
import es.pile.core.domain.repositories.DocumentImageRepository
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.useCases.SaveImagesUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID

/**
 * Use case responsible for orchestrating the creation of new documents in the application.
 *
 * It manages the lifecycle of document creation from various sources (PDFs or Images),
 * handles the cleanup of previous temporary sessions, and ensures data integrity
 * by providing a rollback mechanism if storage or database operations fail.
 */
class CreateDocumentUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val saveImagesUseCase: SaveImagesUseCase,
    private val fileRepository: FileRepository,
    private val documentModelRepository: DocumentModelRepository,
    private val documentImageRepository: DocumentImageRepository
) {

    /**
     * Creates a new document from a single PDF file.
     *
     * This process involves cleaning up existing temporary documents, copying the PDF to
     * internal storage, and persisting the document metadata in the database.
     *
     * @param uri The source [android.net.Uri] of the PDF file.
     * @param initialPileIds Optional list of Pile IDs to associate with the new document.
     * @return The newly created [es.pile.DocumentModel] in a [DocumentStatusConstants.TEMPORARY] state.
     * @throws Exception if file copying or database insertion fails.
     */
    suspend fun createFromPdf(uri: Uri, initialPileIds: List<String> = emptyList()): DocumentModel = withContext(ioDispatcher) {
        cleanupExistingTemporaryDocument()

        val newDocument = createBaseDocument(isPdf = true, initialPileIds = initialPileIds)

        try {
            val fileName = fileRepository.getFileNameFromUri(uri) ?: ""
            fileRepository.copyPdfToInternalStorage(uri, newDocument.id)

            val finalDocument = newDocument.copy(title = fileName)
            documentModelRepository.updateDocumentModel(finalDocument)

            return@withContext finalDocument

        } catch (e: Exception) {
            rollback(newDocument.id)
            throw e
        }
    }

    /**
     * Creates a new document from a list of image files.
     *
     * This process involves cleaning up existing temporary documents, saving all images
     * to internal storage, creating records for each individual image, and persisting
     * the main document metadata.
     *
     * @param uris A list of source [Uri]s for the images to be included in the document.
     * @param initialPileIds Optional list of Pile IDs to associate with the new document.
     * @return The newly created [DocumentModel] containing the image IDs.
     * @throws Exception if image processing or database operations fail.
     */
    suspend fun createFromImages(uris: List<Uri>, initialPileIds: List<String> = emptyList()): DocumentModel = withContext(ioDispatcher) {
        cleanupExistingTemporaryDocument()

        val newDocument = createBaseDocument(isPdf = false, initialPileIds = initialPileIds)

        try {
            val imageFiles =
                saveImagesUseCase(FileRepository.StorageType.PERSISTENT, uris, newDocument.id)

            val documentImages = imageFiles.map { file ->
                DocumentImage(
                    id = file.name,
                    isDraft = false,
                    crop = null,
                    filter = 0,
                    rotation = 0
                )
            }

            documentImages.forEach { documentImageRepository.insertDocumentImage(it) }

            val finalDocument = newDocument.copy(imageIds = documentImages.map { it.id })
            documentModelRepository.updateDocumentModel(finalDocument)

            return@withContext finalDocument

        } catch (e: Exception) {
            rollback(newDocument.id)
            throw e
        }
    }

    /**
     * Creates and inserts the initial [DocumentModel] record with default values.
     *
     * @param isPdf Boolean flag indicating if the document source is a PDF.
     * @param initialPileIds List of Pile IDs to associate with the new document.
     * @return The persisted base [DocumentModel].
     */
    private suspend fun createBaseDocument(isPdf: Boolean, initialPileIds: List<String>): DocumentModel {
        val document = DocumentModel(
            id = UUID.randomUUID().toString(),
            title = "",
            imageIds = emptyList(),
            creationDateTime = LocalDateTime.now(),
            modificationDateTime = LocalDateTime.now(),
            documentStatus = DocumentStatusConstants.TEMPORARY,
            documentDetails = emptyList(),
            documentOrganizationIds = emptyList(),
            documentNote = "",
            documentPileIds = initialPileIds,
            isIncomingPdf = isPdf
        )
        documentModelRepository.insertDocumentModel(document)
        return document
    }

    /**
     * Searches for and removes any existing document with [DocumentStatusConstants.TEMPORARY] status.
     * This ensures the app doesn't accumulate orphaned files from cancelled creation sessions.
     */
    private suspend fun cleanupExistingTemporaryDocument() {
        val tempDocument = documentModelRepository
            .getDocumentModelsByStatus(DocumentStatusConstants.TEMPORARY)
            .first()
            .firstOrNull()

        if (tempDocument != null) {
            documentModelRepository.deleteDocumentModel(tempDocument.id)
            fileRepository.deleteDocumentStorage(documentId = tempDocument.id)
        }
    }

    /**
     * Removes all traces of a document from both database and storage.
     * Typically called when an error occurs during the document creation process.
     *
     * @param documentId The ID of the document to be rolled back.
     */
    private suspend fun rollback(documentId: String) {
        try {
            fileRepository.deleteDocumentStorage(documentId = documentId)
            documentModelRepository.deleteDocumentModel(documentId)
        } catch (e: Exception) {
            Napier.e("Error during rollback", e)
            throw e
        }
    }
}