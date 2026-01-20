package com.ganadoro.pile.domain.usecases

import android.net.Uri
import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.domain.repositories.DocumentImageRepository
import com.ganadoro.pile.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.domain.repositories.FileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Use case responsible for orchestrating the addition of a new page to an existing document.
 *
 * It manages the persistence of the image file, the creation of its associated metadata,
 * and the atomic update of the parent document. It also provides a rollback mechanism
 * to maintain data integrity in case of failures.
 */
class AddPageToDocumentUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val fileRepository: FileRepository,
    private val documentImageRepository: DocumentImageRepository,
    private val documentModelRepository: DocumentModelRepository
) {
    /**
     * Adds a new page (image) to an existing document.
     *
     * If the document update fails, it performs a rollback by deleting both the
     * database record and the physical file of the newly added image.
     *
     * @param document The existing [DocumentModel] to which the page will be added.
     * @param uris A list of [Uri] objects representing the image files to be added.
     * @return A [Result] indicating the success of the operation.
     */
    suspend operator fun invoke(
        document: DocumentModel,
        uris: List<Uri>
    ): Result<Boolean> = withContext(ioDispatcher) {
        runCatching {
            val savedFiles =
                fileRepository.saveImagesToInternalStorage(uris, document.id)

            val imageModels = savedFiles.map { file ->
                DocumentImage(
                    id = file.name,
                    crop = null,
                    filter = 0,
                    rotation = 0
                )
            }

            imageModels.forEach { documentImageRepository.insertDocumentImage(it) }

            val updatedDocument =
                document.copy(imageIds = document.imageIds + imageModels.map { it.id })

            try {
                documentModelRepository.updateDocumentModel(updatedDocument)
                true
            } catch (_: Exception) {
                rollback(document.id, imageModels.map { it.id })
                false
            }
        }
    }

    private suspend fun rollback(
        documentId: String,
        imageIds: List<String>
    ) {
        imageIds.forEach {
            documentImageRepository.deleteDocumentImage(it)
            fileRepository.deleteDocumentImage(documentId, it)
        }
    }
}
