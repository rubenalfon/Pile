package es.pile.features.editDocument.domain.useCases

import es.pile.DocumentModel
import es.pile.core.domain.models.ImageFilterType
import es.pile.core.domain.repositories.BitmapCacheRepository

/**
 * Use case responsible for removing image bitmaps and their processed thumbnails
 * from the memory or disk cache.
 *
 * This is typically used to free up resources when a page is removed or updated,
 * ensuring that the UI does not display stale imagery and that memory is managed efficiently.
 */
class RemoveBitmapFromCacheUseCase(
    private val bitmapCacheRepository: BitmapCacheRepository
) {
    /**
     * Removes both the original image bitmap and all its associated filter
     * thumbnails from the cache.
     *
     * @param document The [DocumentModel] containing the image.
     * @param imageId The unique identifier of the image to be cleared.
     */
    fun removeImageThumbnails(document: DocumentModel, imageId: String) {
        removeImage(document, imageId)
        removeThumbnails(imageId)
    }

    /**
     * Removes the specific full-sized image bitmap from the cache.
     *
     * @param document The [DocumentModel] where the image belongs.
     * @param imageId The unique identifier of the image.
     */
    fun removeImage(document: DocumentModel, imageId: String) {
        val imageIndex = document.imageIds.indexOf(imageId)
        val imageKey = bitmapCacheRepository.getImageKey(document, imageIndex)
        bitmapCacheRepository.removeFromCache(imageKey)
    }

    /**
     * Iterates through all available [ImageFilterType] and removes the cached
     * thumbnail for each one corresponding to the provided [imageId].
     *
     * @param imageId The unique identifier of the image whose thumbnails should be removed.
     */
    fun removeThumbnails(imageId: String) {
        for (filter in ImageFilterType.entries) {
            val thumbnailKey = bitmapCacheRepository.getImageThumbnailKey(imageId, filter.id)
            bitmapCacheRepository.removeFromCache(thumbnailKey)
        }
    }
}