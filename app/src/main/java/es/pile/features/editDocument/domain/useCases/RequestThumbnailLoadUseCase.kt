package es.pile.features.editDocument.domain.useCases

import es.pile.DocumentImage
import es.pile.core.domain.repositories.BitmapCacheRepository
import es.pile.core.domain.repositories.FileRepository

/**
 * Use case responsible for resolving the physical file associated with a specific document image
 * and triggering the thumbnail bitmap loading process for specific filter.
 */
class RequestThumbnailLoadUseCase(
    private val bitmapCacheRepository: BitmapCacheRepository,
    private val fileRepository: FileRepository
) {
    /**
     * Resolves the correct image file path and requests the cache repository to load the thumbnail in it.
     *
     * @param documentId The unique ID of the document.
     * @param documentImage The document image metadata.
     * @param filterId The unique ID of the filter to be applied to the image.
     */
    suspend operator fun invoke(documentId: String, documentImage: DocumentImage, filterId: Int) {
        val storageType = if (documentImage.isDraft) FileRepository.StorageType.CACHE
        else FileRepository.StorageType.PERSISTENT

        val file = fileRepository.getImageFile(
            storageType = storageType,
            documentId = documentId,
            imageId = documentImage.id
        )

        bitmapCacheRepository.loadImageThumbnail(
            imageFile = file,
            documentImage = documentImage,
            filterId = filterId,
        )
    }
}