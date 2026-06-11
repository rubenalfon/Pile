package es.pile.features.home.domain.useCases

import es.pile.core.domain.models.DocumentStatusConstants
import es.pile.core.domain.repositories.DocumentImageRepository
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.FileRepository
import es.pile.features.home.domain.models.TemporaryDocumentBackup
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Use case responsible for handling the lifecycle of temporary documents.
 * Includes logic for soft deletion (backup), restoration (undo), and hard deletion (file cleanup).
 */
class ManageTemporaryDocumentUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val documentModelRepository: DocumentModelRepository,
    private val documentImageRepository: DocumentImageRepository,
    private val fileRepository: FileRepository
) {
    /**
     * Finds the existing temporary document (if any) and performs a soft delete.
     * It removes the data from the database but keeps the files in storage, returning a backup object.
     *
     * @return A [TemporaryDocumentBackup] if a temporary document existed, null otherwise.
     */
    suspend fun deleteForUndo(): TemporaryDocumentBackup? =
        withContext(ioDispatcher) {
            val documentToDelete = documentModelRepository
                .getDocumentModelsByStatus(DocumentStatusConstants.TEMPORARY)
                .first()
                .firstOrNull()

            if (documentToDelete == null) return@withContext null

            val documentImages = documentToDelete.imageIds.mapNotNull { id ->
                documentImageRepository.getDocumentImageById(id).first()
            }

            documentModelRepository.deleteDocumentModel(documentToDelete.id)
            for (image in documentImages) {
                documentImageRepository.deleteDocumentImage(image.id)
            }

            TemporaryDocumentBackup(
                document = documentToDelete,
                images = documentImages
            )
        }

    /**
     * Restores a previously deleted document from a backup.
     * Re-inserts the data into the database.
     *
     * @param backup The backup object returned by [deleteForUndo].
     */
    suspend fun restoreBackup(backup: TemporaryDocumentBackup) = withContext(ioDispatcher) {
        try {
            documentModelRepository.insertDocumentModel(backup.document)
            backup.images.forEach { image ->
                documentImageRepository.insertDocumentImage(image)
            }
        } catch (e: Exception) {
            Napier.e("Failed to restore document backup", e)
            throw e
        }
    }

    /**
     * Permanently deletes the physical files associated with a document.
     * This should be called when the user can no longer "Undo" the action.
     *
     * @param documentId The ID of the document folder to delete.
     */
    suspend fun confirmPermanentDeletion(documentId: String) = withContext(ioDispatcher) {
        try {
            fileRepository.deleteDocumentStorage(documentId = documentId)
        } catch (e: Exception) {
            Napier.e("Failed to delete document storage", e)
            throw e
        }
    }
}