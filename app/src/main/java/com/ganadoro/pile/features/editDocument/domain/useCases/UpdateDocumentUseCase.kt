package com.ganadoro.pile.features.editDocument.domain.useCases

import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.core.domain.repositories.DocumentImageRepository
import com.ganadoro.pile.core.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.core.domain.repositories.FileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

/**
 * Use case responsible for finalizing and persisting changes to a document and its images.
 *
 * This class orchestrates the process of a document's lifecycle by:
 * 1. Moving newly added images from temporary (CACHE) to permanent (PERSISTENT) storage.
 * 2. Deleting images from storage that were removed by the user.
 * 3. Updating the database records for both the [com.ganadoro.pile.DocumentModel] and its [com.ganadoro.pile.DocumentImage].
 * 4. Ensuring all images lose their "draft" status upon successful persistence.
 */
class UpdateDocumentUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val fileRepository: FileRepository,
    private val documentModelRepository: DocumentModelRepository,
    private val documentImageRepository: DocumentImageRepository
) {
    /**
     * Updates an existing document and synchronizes its associated image files.
     *
     * @param documentModel The updated document metadata to be saved.
     * @param imageList The current list of images that should be associated with the document.
     *
     * @throws Exception If any file operation (move/delete) or database update fails.
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

        // Delete from persistent storage deleted document images
        val deletedImageIds = originalDocumentModel?.imageIds?.filter { imageId ->
            imageId !in imageList.map { it.id }
        }

        deletedImageIds?.forEach {
            fileRepository.deleteDocumentImage(
                FileRepository.StorageType.PERSISTENT,
                documentModel.id,
                it
            )
        }

        // Update documentModel
        documentModelRepository.updateDocumentModel(
            documentModel.copy(modificationDateTime = LocalDateTime.now())
        )

        // update documentImages
        imageList.forEach { image ->
            if (image.isDraft) documentImageRepository.insertDocumentImage(image.copy(isDraft = false))
            else documentImageRepository.updateDocumentImage(image)
        }
    }
}