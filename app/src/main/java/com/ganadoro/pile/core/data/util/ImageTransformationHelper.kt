package com.ganadoro.pile.core.data.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import androidx.exifinterface.media.ExifInterface
import com.ganadoro.pile.core.domain.models.ImageCropData
import com.ganadoro.pile.core.domain.models.ImageFilterType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Helper class responsible for applying graphical transformations to Bitmaps.
 * Handles Rotation, Cropping, and Color Filters using standard Android Graphics APIs.
 */
class ImageTransformationHelper(
    private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * Takes a raw file and applies a chain of transformations (Rotation -> Crop -> Filter).
     *
     * @param file The source image file.
     * @param rotation Degrees to rotate.
     * @param cropData The cropping parameters.
     * @param filter The [ImageFilterType] to be applied.
     * @return The transformed [android.graphics.Bitmap]. Returns null if the transformation fails.
     */
    suspend fun transform(
        file: File,
        rotation: Int,
        cropData: ImageCropData?,
        filter: ImageFilterType
    ): Bitmap? = withContext(ioDispatcher) {
        var bitmap = BitmapFactory.decodeFile(file.absolutePath)
            ?: return@withContext null

        val finalRotation = rotation + getExifRotation(file)

        if (finalRotation != 0) bitmap = rotateBitmap(bitmap, finalRotation)

        if (cropData != null) bitmap = cropBitmap(bitmap, cropData)

        if (filter != ImageFilterType.ORIGINAL) bitmap = applyFilter(bitmap, filter)

        return@withContext bitmap
    }

    /**
     * Retrieves the rotation degrees from the EXIF data of an image file.
     *
     * @param file The source image file.
     * @return The rotation degrees.
     */
    private suspend fun getExifRotation(file: File): Int = withContext(ioDispatcher) {
        try {
            val exif = ExifInterface(file.absolutePath)
            when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (_: Exception) {
            0
        }
    }

    /**
     * Rotates the bitmap by the specified degrees.
     * Recycles the input bitmap to save memory.
     *
     * @param source The source bitmap to be rotated.
     * @param degrees The number of degrees to rotate the bitmap.
     * @return The rotated [Bitmap].
     */
    private fun rotateBitmap(source: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }

        val rotated = Bitmap.createBitmap(
            source, 0, 0, source.width, source.height, matrix, true
        )

        if (source != rotated && !source.isRecycled) {
            source.recycle()
        }
        return rotated
    }

    /**
     * Crops the bitmap based on the provided coordinates.
     * Recycles the input bitmap to save memory.
     *
     * @param source The source bitmap to be cropped.
     * @param cropData The cropping parameters.
     * @return The cropped [Bitmap].
     */
    private fun cropBitmap(source: Bitmap, cropData: ImageCropData): Bitmap {
        val x = cropData.x.coerceAtLeast(0)
        val y = cropData.y.coerceAtLeast(0)
        val w = cropData.width.coerceAtMost(source.width - x)
        val h = cropData.height.coerceAtMost(source.height - y)

        val cropped = Bitmap.createBitmap(source, x, y, w, h)

        if (source != cropped && !source.isRecycled) {
            source.recycle()
        }
        return cropped
    }

    /**
     * Applies a color filter to the bitmap using a Canvas.
     * Recycles the input bitmap to save memory.
     *
     * @param source The source bitmap to be filtered.
     * @param filter The [ImageFilterType] to be applied.
     * @return The filtered [Bitmap].
     */
    private fun applyFilter(source: Bitmap, filter: ImageFilterType): Bitmap {
        val result = createBitmap(source.width, source.height)
        val canvas = Canvas(result)

        val paint = Paint()
        val colorMatrix = ColorMatrix()

        when (filter) {
            ImageFilterType.ORIGINAL -> return source
            ImageFilterType.GRAYSCALE -> colorMatrix.setSaturation(0f)
            ImageFilterType.HIGH_CONTRAST -> {
                colorMatrix.setSaturation(0f)

                val contrastMatrix = ColorMatrix()
                val contrastScale = 2.0f
                val translateColor = (-128f * contrastScale) + 128f
                contrastMatrix.set(
                    floatArrayOf(
                        contrastScale, 0f, 0f, 0f, translateColor,
                        0f, contrastScale, 0f, 0f, translateColor,
                        0f, 0f, contrastScale, 0f, translateColor,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                colorMatrix.postConcat(contrastMatrix)
            }
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)

        if (source != result && !source.isRecycled) {
            source.recycle()
        }
        return result
    }

    /**
     * Efficiently loads a downsampled version of the image (thumbnail)
     * and applies transformations.
     *
     * @param maxSize The desired width of the thumbnail (e.g., 200px).
     */
    suspend fun transformThumbnail(
        file: File,
        maxSize: Int,
        rotation: Int,
        cropData: ImageCropData?,
        filter: ImageFilterType
    ): Bitmap? = withContext(ioDispatcher) {
        if (!file.exists()) return@withContext null
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.absolutePath, options)

        options.inSampleSize = calculateInSampleSize(options, maxSize)
        options.inJustDecodeBounds = false

        var bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            ?: return@withContext null

        val finalRotation = rotation + getExifRotation(file)

        if (finalRotation != 0) bitmap = rotateBitmap(bitmap, finalRotation)

        if (cropData != null) {
            bitmap = cropThumbnail(bitmap, cropData, options.inSampleSize)
        }

        if (filter != ImageFilterType.ORIGINAL) bitmap = applyFilter(bitmap, filter)

        return@withContext bitmap
    }

    /**
     * Calculates the optimal sample size for decoding a bitmap.
     *
     * @param options The [BitmapFactory.Options] object containing bitmap metadata.
     * @param maxSize The maximum size of the bitmap in pixels.
     * @return The optimal sample size.
     */
    fun calculateInSampleSize(options: BitmapFactory.Options, maxSize: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > maxSize || width > maxSize) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= maxSize || halfWidth / inSampleSize >= maxSize) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun cropThumbnail(source: Bitmap, crop: ImageCropData, sampleSize: Int): Bitmap {
        val scaledX = crop.x / sampleSize
        val scaledY = crop.y / sampleSize
        val scaledW = crop.width / sampleSize
        val scaledH = crop.height / sampleSize

        val safeX = scaledX.coerceAtLeast(0)
        val safeY = scaledY.coerceAtLeast(0)
        val safeW = scaledW.coerceAtMost(source.width - safeX)
        val safeH = scaledH.coerceAtMost(source.height - safeY)

        val cropped = Bitmap.createBitmap(source, safeX, safeY, safeW, safeH)
        if (cropped != source) source.recycle()
        return cropped
    }
}