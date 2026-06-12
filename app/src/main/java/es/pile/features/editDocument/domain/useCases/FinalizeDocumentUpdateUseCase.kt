package es.pile.features.editDocument.domain.useCases

import es.pile.DocumentImage
import es.pile.DocumentModel
import es.pile.core.domain.repositories.DocumentImageRepository
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.FileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

/**
 * Use case responsible for finalizing and persisting changes made to a document during an edit session.
 *
 * This use case handles the transition of images from a temporary state to a permanent one.
 * It performs the following sequential operations:
 * 1. **File Persistence**: Moves newly added images (marked as [DocumentImage.isDraft]) from CACHE
 *    to PERSISTENT storage within the [FileRepository].
 * 2. **File Cleanup**: Deletes images from PERSISTENT storage that were removed by the user
 *    during the current edit session.
 * 3. **Metadata Update**: Updates the [DocumentModel] in the database, refreshing the [DocumentModel.modificationDateTime].
 * 4. **Image Record Sync**: Inserts new image records (removing the draft flag) or updates existing ones.
 */
class FinalizeDocumentUpdateUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val fileRepository: FileRepository,
    private val documentModelRepository: DocumentModelRepository,
    private val documentImageRepository: DocumentImageRepository
) {
    /**
     * Executes the finalization process.
     *
     * @param documentModel The updated document metadata to persist.
     * @param imageList The final list of [DocumentImage] associated with the document.
     * @throws Exception If any file operation or database update fails.
     */
    suspend operator fun invoke(
        documentModel: DocumentModel,
        imageList: List<DocumentImage>
    ) = withContext(ioDispatcher) {
        val originalDocumentModel =
            documentModelRepository.getDocumentModelById(documentModel.id).first()

        imageList.filter { it.isDraft }.forEach { image ->
            // Move draft images to persistent storage
            fileRepository.copyImageToInternalStorage(documentModel.id, image)

            // Delete draft images from cache storage
            fileRepository.deleteDocumentImage(
                FileRepository.StorageType.CACHE,
                documentModel.id,
                image.id
            )
        }

        // Cleanup deleted images from persistent storage
        val currentImageIds = imageList.map { it.id }
        val deletedImageIds = originalDocumentModel?.imageIds?.filter { it !in currentImageIds }

        deletedImageIds?.forEach { imageId ->
            fileRepository.deleteDocumentImage(
                FileRepository.StorageType.PERSISTENT,
                documentModel.id,
                imageId
            )
        }

        // Update document metadata
        documentModelRepository.updateDocumentModel(
            documentModel.copy(modificationDateTime = LocalDateTime.now())
        )

        // Update image records
        imageList.forEach { image ->
            if (image.isDraft) {
                documentImageRepository.insertDocumentImage(image.copy(isDraft = false))
            } else {
                documentImageRepository.updateDocumentImage(image)
            }
        }
    }
}
