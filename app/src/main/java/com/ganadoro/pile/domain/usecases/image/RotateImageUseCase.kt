package com.ganadoro.pile.domain.usecases.image

import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.domain.models.ImageFilterType
import com.ganadoro.pile.domain.repositories.BitmapCacheRepository
import com.ganadoro.pile.domain.repositories.DocumentImageRepository

/**
 * Use case to rotate a [DocumentImage] by 90 degrees anticlockwise.
 *
 * This class handles the business logic of rotating an image. It calculates the new rotation value,
 * updates the image information in the data layer, and invalidates the corresponding bitmap
 * and all its filter thumbnails from the cache to ensure the view is refreshed.
 */
class RotateImageUseCase(
    private val documentImageRepository: DocumentImageRepository,
    private val bitmapCacheRepository: BitmapCacheRepository
) {
    /**
     * Rotates the given [DocumentImage] anticlockwise by 90 degrees.
     *
     * The rotation is updated in the [DocumentImageRepository]. To ensure UI consistency,
     * the main image and all possible filter thumbnails are removed from [BitmapCacheRepository].
     *
     * @param document The [DocumentModel] associated with the image.
     * @param documentImage The [DocumentImage] to rotate.
     */
    suspend operator fun invoke(
        document: DocumentModel,
        documentImage: DocumentImage
    ) {
        val newRotation = (documentImage.rotation - 90) % 360
        val updatedImage = documentImage.copy(rotation = newRotation)

        documentImageRepository.updateDocumentImage(updatedImage)

        val imageIndex = document.imageIds.indexOf(documentImage.id)
        val imageKey = bitmapCacheRepository.getImageKey(document, imageIndex)
        bitmapCacheRepository.removeFromCache(imageKey)

        for (filter in ImageFilterType.entries) {
            val thumbnailKey = bitmapCacheRepository.getImageThumbnailKey(documentImage.id, filter.id)
            bitmapCacheRepository.removeFromCache(thumbnailKey)
        }
    }
}
