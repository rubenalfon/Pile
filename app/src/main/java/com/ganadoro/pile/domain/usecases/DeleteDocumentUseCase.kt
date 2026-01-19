package com.ganadoro.pile.domain.usecases

import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.domain.repositories.DocumentImageRepository
import com.ganadoro.pile.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.domain.repositories.FileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Use case responsible for deleting a document from the repository and associated files.
 */
class DeleteDocumentUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val documentModelRepository: DocumentModelRepository,
    private val documentImageRepository: DocumentImageRepository,
    private val fileRepository: FileRepository
) {
    /**
     * Deletes a document from the repository and associated files.
     *
     * @param document The document to be deleted.
     * @return A [Result] indicating the success or failure of the operation.
     */
    suspend operator fun invoke(document: DocumentModel) = withContext(ioDispatcher) {
        documentModelRepository.deleteDocumentModel(document.id)

        val imageIds = document.imageIds
        imageIds.forEach { imageId ->
            documentImageRepository.deleteDocumentImage(imageId)
        }

        fileRepository.deleteDocumentStorage(document.id)
    }
}