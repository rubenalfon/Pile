package com.ganadoro.pile.domain.usecases.document

import com.ganadoro.pile.domain.repositories.DocumentImageRepository
import com.ganadoro.pile.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.domain.repositories.FileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Use case responsible for orchestrating the deletion of a specific page within a document.
 *
 * It ensures data consistency by updating the parent document's metadata, removing
 * image-specific records from the database, and purging the physical file from storage.
 */
class DeleteDocumentPageUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val documentModelRepository: DocumentModelRepository,
    private val documentImageRepository: DocumentImageRepository,
    private val fileRepository: FileRepository
) {
    /**
     * Deletes a page from a document across all storage layers.
     *
     * This operation performs the following steps:
     * 1. Removes the [imageId] reference from the [com.ganadoro.pile.DocumentModel].
     * 2. Deletes the [com.ganadoro.pile.DocumentImage] metadata from the database.
     * 3. Permanently removes the physical image file from internal storage.
     *
     * @param documentId The unique identifier of the document containing the page.
     * @param imageId The unique identifier of the image/page to be removed.
     */
    suspend operator fun invoke(
        documentId: String,
        imageId: String
    ) = withContext(ioDispatcher) {
        val document = documentModelRepository.getDocumentModelById(documentId).first()
            ?: return@withContext

        val newImageIds = document.imageIds.filter { it != imageId }
        val updatedDocument = document.copy(imageIds = newImageIds)
        documentModelRepository.updateDocumentModel(updatedDocument)

        documentImageRepository.deleteDocumentImage(imageId)

        fileRepository.deleteDocumentImage(documentId, imageId)

    }
}