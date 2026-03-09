package com.ganadoro.pile.features.home.domain.useCases

import android.net.Uri
import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.core.domain.models.DocumentStatusConstants
import com.ganadoro.pile.core.domain.models.ImageResolution
import com.ganadoro.pile.core.domain.repositories.DocumentImageRepository
import com.ganadoro.pile.core.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.core.domain.repositories.FileRepository
import com.ganadoro.pile.core.domain.repositories.SettingsRepository
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
    private val fileRepository: FileRepository,
    private val settingsRepository: SettingsRepository,
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
     * @return The newly created [com.ganadoro.pile.DocumentModel] in a [DocumentStatusConstants.TEMPORARY] state.
     * @throws Exception if file copying or database insertion fails.
     */
    suspend fun createFromPdf(uri: Uri): DocumentModel = withContext(ioDispatcher) {
        cleanupExistingTemporaryDocument()

        val newDocument = createBaseDocument(isPdf = true)

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
     * @return The newly created [DocumentModel] containing the image IDs.
     * @throws Exception if image processing or database operations fail.
     */
    suspend fun createFromImages(uris: List<Uri>): DocumentModel = withContext(ioDispatcher) {
        cleanupExistingTemporaryDocument()

        val newDocument = createBaseDocument(isPdf = false)

        val imageResolution = settingsRepository.userSettings.first().imageResolution

        try {
            val savedFiles = fileRepository.saveImagesToStorage(
                storageType = FileRepository.StorageType.PERSISTENT,
                uris = uris,
                documentId = newDocument.id,
                maxSize = if (imageResolution == ImageResolution.ORIGINAL) 0 else 1200,
                quality = if (imageResolution == ImageResolution.ORIGINAL) 100 else 85
            )

            val imageModels = savedFiles.map { file ->
                DocumentImage(
                    id = file.name,
                    isDraft = false,
                    crop = null,
                    filter = 0,
                    rotation = 0
                )
            }

            imageModels.forEach { documentImageRepository.insertDocumentImage(it) }

            val finalDocument = newDocument.copy(imageIds = imageModels.map { it.id })
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
     * @return The persisted base [DocumentModel].
     */
    private suspend fun createBaseDocument(isPdf: Boolean): DocumentModel {
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
            documentPileIds = emptyList(),
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
        }
    }
}