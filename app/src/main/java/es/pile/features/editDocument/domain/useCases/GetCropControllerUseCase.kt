package es.pile.features.editDocument.domain.useCases

import com.tanishranjan.cropkit.CropController
import com.tanishranjan.cropkit.CropData
import com.tanishranjan.cropkit.CropDefaults
import com.tanishranjan.cropkit.CropShape
import es.pile.DocumentImage
import es.pile.core.data.util.ImageTransformationHelper
import es.pile.core.domain.models.ImageFilterType
import es.pile.core.domain.models.ResizedBitmap
import es.pile.core.domain.repositories.FileRepository
import es.pile.features.editDocument.domain.helper.CropControllerFactory
import es.pile.features.editDocument.domain.models.ExtendedCropController
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Use case responsible for loading a crop controller for a specific document image.
 */
class GetCropControllerUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val fileRepository: FileRepository,
    private val imageTransformationHelper: ImageTransformationHelper,
    private val cropControllerFactory: CropControllerFactory
) {
    /**
     * Loads a crop controller for the given document image.
     *
     * @param documentId The ID of the document.
     * @param documentImage The [es.pile.DocumentImage] to load the crop controller for.
     * @return A pair containing the [CropController] and the scale factor.
     * @throws IllegalStateException If the bitmap for the image is not found.
     */
    suspend operator fun invoke(
        documentId: String,
        documentImage: DocumentImage
    ): ExtendedCropController = withContext(ioDispatcher) {
        val resizedBitmap = getUncroppedBitmap(documentId, documentImage)
            ?: throw IllegalStateException("Bitmap not found for image ${documentImage.id}")

        val imageCropData = documentImage.crop

        val resizedImageCropData = imageCropData?.scale(resizedBitmap.scaleFactor)

        val resizedCropData = resizedImageCropData?.toCropData()

        val cropController = cropControllerFactory.create(
            bitmap = resizedBitmap.bitmap,
            cropColors = CropDefaults.cropColors(),
            cropOptions = CropDefaults.cropOptions(
                initialCropData = resizedCropData ?: CropData.Zero,
                cropShape = CropShape.FreeForm
            )
        )

        ExtendedCropController(cropController, resizedBitmap.scaleFactor)
    }

    /**
     * Retrieves the transformed bitmap for the given document image without any cropping applied.
     *
     * @param documentId The ID of the document.
     * @param documentImage The [DocumentImage] to retrieve the bitmap for.
     */
    private suspend fun getUncroppedBitmap(
        documentId: String,
        documentImage: DocumentImage
    ): ResizedBitmap? {
        val storageType = if (documentImage.isDraft) FileRepository.StorageType.CACHE
        else FileRepository.StorageType.PERSISTENT

        val imageFile = fileRepository.getImageFile(
            storageType = storageType, documentId = documentId, imageId = documentImage.id
        )

        return imageTransformationHelper.transform(
            file = imageFile,
            rotation = documentImage.rotation.toInt(),
            cropData = null,
            filter = ImageFilterType.fromId(documentImage.filter.toInt())
        )
    }
}