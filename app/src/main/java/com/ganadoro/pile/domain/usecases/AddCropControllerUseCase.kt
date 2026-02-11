package com.ganadoro.pile.domain.usecases

import android.graphics.Bitmap
import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.data.util.ImageTransformationHelper
import com.ganadoro.pile.domain.models.ImageFilterType
import com.ganadoro.pile.domain.repositories.FileRepository
import com.tanishranjan.cropkit.CropController
import com.tanishranjan.cropkit.CropData
import com.tanishranjan.cropkit.CropDefaults
import com.tanishranjan.cropkit.CropShape
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Use case responsible for loading a crop controller for a specific document image.
 */
class AddCropControllerUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val fileRepository: FileRepository,
    private val imageTransformationHelper: ImageTransformationHelper
) {
    /**
     * Loads a crop controller for the given document image.
     *
     * @param documentId The ID of the document.
     * @param documentImage The [DocumentImage] to load the crop controller for.
     * @return The loaded crop controller.
     * @throws IllegalStateException If the bitmap for the image is not found.
     */
    suspend operator fun invoke(
        documentId: String,
        documentImage: DocumentImage
    ): CropController = withContext(ioDispatcher) {
        val bitmap = getUncroppedBitmap(documentId, documentImage)
            ?: throw IllegalStateException("Bitmap not found for image ${documentImage.id}")

        Napier.d { "A! Loaded crop controller for image ${documentImage.id}" }
        Napier.d { "A! documentImage.crop ${documentImage.crop}" }
        Napier.d { "A! documentImage.crop?.toCropData() ${documentImage.crop?.toCropData()}" }
        Napier.d { "A! documentImage.crop?.toCropData() ?: CropData.Zero ${documentImage.crop?.toCropData() ?: CropData.Zero}" }

        val a = CropController(
            bitmap = bitmap,
            cropColors = CropDefaults.cropColors(),
            cropOptions = CropDefaults.cropOptions(
                initialCropData = documentImage.crop?.toCropData() ?: CropData.Zero,
                cropShape = CropShape.FreeForm
            )
        )

        Napier.d { "A! cropController = $a" }
        Napier.d { "A! cropOptions = ${a.cropOptions}" }
        Napier.d { "A! cropControllergetCropData = ${a.getCropData()}" }

        return@withContext a
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
    ): Bitmap? {
        val imageFile = fileRepository.getImageFile(documentId, documentImage.id)

        return imageTransformationHelper.transform(
            file = imageFile,
            rotation = documentImage.rotation.toInt(),
            cropData = null,
            filter = ImageFilterType.fromId(documentImage.filter.toInt())
        )
    }
}