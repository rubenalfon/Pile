package es.pile.core.domain.models

import com.tanishranjan.cropkit.CropData
import kotlinx.serialization.Serializable

/**
 * Represents the cropping parameters for an image.
 * @property x The x-coordinate of the top-left corner of the crop rectangle.
 * @property y The y-coordinate of the top-left corner of the crop rectangle.
 * @property width The width of the crop rectangle.
 * @property height The height of the crop rectangle.
 */
@Serializable
data class ImageCropData(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) {
    /**
     * Converts the [ImageCropData] to a [CropData] object.
     * @return A [CropData] object representing the crop rectangle.
     */
    fun toCropData(): CropData = CropData(
        x = x,
        y = y,
        width = width,
        height = height
    )

    /**
     * Scales the crop rectangle by a given factor.
     *
     * @param scaleFactor The scaling factor.
     * @return A new [ImageCropData] object with the scaled dimensions.
     */
    fun scale(scaleFactor: Float) = ImageCropData (
        x = (x * scaleFactor).toInt(),
        y = (y * scaleFactor).toInt(),
        width = (width * scaleFactor).toInt(),
        height = (height * scaleFactor).toInt()
    )

    companion object {
        /**
         * Creates an [ImageCropData] object from a [CropData] object.
         * @param cropData The [CropData] object to convert.
         */
        fun fromCropData(cropData: CropData): ImageCropData = ImageCropData(
            x = cropData.x,
            y = cropData.y,
            width = cropData.width,
            height = cropData.height
        )
    }
}