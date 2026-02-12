package com.ganadoro.pile.domain.usecases.image

import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.domain.models.ImageCropData
import com.ganadoro.pile.domain.models.ImageFilterType
import com.ganadoro.pile.domain.repositories.BitmapCacheRepository
import com.ganadoro.pile.domain.repositories.DocumentImageRepository

/**
 * Use case to crop a [DocumentImage] using the provided [ImageCropData].
 *
 * This class handles the business logic of cropping an image. It updates the image information in
 * the data layer, and invalidates the corresponding bitmap and all its filter thumbnails from the
 * cache to ensure the view is refreshed.
 */
class CropImageUseCase(
    private val documentImageRepository: DocumentImageRepository,
    private val bitmapCacheRepository: BitmapCacheRepository
) {
    /**
     * Crops the given [DocumentImage] using the provided [ImageCropData].
     *
     * The crop is updated in the [DocumentImageRepository]. To ensure UI consistency, the main
     * image and all possible filter thumbnails are removed from [BitmapCacheRepository].
     *
     * @param document The [DocumentModel] associated with the image.
     * @param documentImage The [DocumentImage] to crop.
     * @param cropData The [ImageCropData] containing the crop information.
     */
    suspend operator fun invoke(
        document: DocumentModel,
        documentImage: DocumentImage,
        cropData: ImageCropData
    ) {
        val updatedDocumentImage = documentImage.copy(crop = cropData)

        documentImageRepository.updateDocumentImage(updatedDocumentImage)

        val imageIndex = document.imageIds.indexOf(documentImage.id)
        val imageKey = bitmapCacheRepository.getImageKey(document, imageIndex)
        bitmapCacheRepository.removeFromCache(imageKey)

        for (filter in ImageFilterType.entries) {
            val thumbnailKey = bitmapCacheRepository.getImageThumbnailKey(documentImage.id, filter.id)
            bitmapCacheRepository.removeFromCache(thumbnailKey)
        }
    }
}