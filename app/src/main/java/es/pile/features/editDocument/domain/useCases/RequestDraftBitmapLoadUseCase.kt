package es.pile.features.editDocument.domain.useCases

import es.pile.DocumentImage
import es.pile.DocumentModel
import es.pile.core.domain.repositories.BitmapCacheRepository
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.repositories.FileRepository.StorageType

/**
 * Use case responsible for orchestrating the loading of draft images into bitmap cache.
 *
 * It acts as a bridge between storage and the UI, determining whether an image should be
 * fetched from [StorageType.CACHE] (for drafts) or [StorageType.PERSISTENT] (for saved documents),
 * and then requesting the [BitmapCacheRepository] to process and cache the resulting bitmap.
 */
class RequestDraftBitmapLoadUseCase(
    private val bitmapCacheRepository: BitmapCacheRepository,
    private val fileRepository: FileRepository
) {
    /**
     * Resolves the file location and triggers the bitmap loading process.
     *
     * The method performs the following steps:
     * 1. Determines the storage location based on the [DocumentImage.isDraft] status.
     * 2. Retrieves the physical [java.io.File] reference from the [fileRepository].
     * 3. Calculates the page index within the document.
     * 4. Commands the [bitmapCacheRepository] to load and cache the bitmap for UI consumption.
     *
     * @param documentModel The document context to which the image belongs.
     * @param documentImage The specific image metadata to be loaded.
     */
    suspend operator fun invoke(documentModel: DocumentModel, documentImage: DocumentImage) {
        val storageType = if (documentImage.isDraft) StorageType.CACHE
        else StorageType.PERSISTENT

        val file = fileRepository.getImageFile(
            storageType = storageType,
            documentId = documentModel.id,
            imageId = documentImage.id
        )
        val pageNumber = documentModel.imageIds.indexOf(documentImage.id)

        bitmapCacheRepository.loadBitmap(
            file = file,
            document = documentModel,
            pageNumber = pageNumber,
            documentImage = documentImage
        )
    }
}