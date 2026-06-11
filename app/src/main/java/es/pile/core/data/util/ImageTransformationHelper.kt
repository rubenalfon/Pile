package es.pile.core.data.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.core.graphics.createBitmap
import androidx.exifinterface.media.ExifInterface
import es.pile.core.domain.models.ImageCropData
import es.pile.core.domain.models.ImageFilterType
import es.pile.core.domain.models.ResizedBitmap
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Helper class responsible for applying graphical transformations to Bitmaps.
 * Handles Rotation, Cropping, and Color Filters using standard Android Graphics APIs.
 */
class ImageTransformationHelper(
    private val ioDispatcher: CoroutineDispatcher,
    private val appContext: Context,
    private val contentResolver: ContentResolver = appContext.contentResolver,
) {
    /**
     * Takes a raw file and applies a chain of transformations safely managing memory
     * (Rotation -> Crop -> Filter).
     *
     * @param file The source image file.
     * @param rotation Degrees to rotate.
     * @param cropData The cropping parameters.
     * @param filter The [ImageFilterType] to be applied.
     * @param reqSize The maximum size of the image in pixels (default: 1080). 0 for no limit.
     * @return An object [ResizedBitmap] that contains the transformed [Bitmap] and its scale
     * factor as a [Float]. Returns null if the transformation fails.
     */
    suspend fun transform(
        file: File,
        rotation: Int,
        cropData: ImageCropData?,
        filter: ImageFilterType,
        reqSize: Int = 1080
    ): ResizedBitmap? = withContext(ioDispatcher) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
            val originalWidth = options.outWidth

            // Subsampling
            options.inSampleSize = if (reqSize > 0) {
                calculateInSampleSize(options, reqSize)
            } else 1

            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888


            var bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                ?: return@withContext null


            val finalRotation = rotation + getExifRotation(file)
            if (finalRotation != 0) bitmap = rotateBitmap(bitmap, finalRotation)


            val scaleFactor: Float = bitmap.width.toFloat() / originalWidth.toFloat()

            Napier.d { "\uD83D\uDE08 ${file.name}'s scale: $scaleFactor" }

            if (cropData != null) {
                val scaledCrop = cropData.scale(scaleFactor)

                bitmap = cropBitmap(bitmap, scaledCrop)
            }

            if (filter != ImageFilterType.ORIGINAL) bitmap = applyFilter(bitmap, filter)

            return@withContext ResizedBitmap(bitmap, scaleFactor)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            if (reqSize < 265) return@withContext null

            transform(
                file = file,
                rotation = rotation,
                cropData = cropData,
                filter = filter,
                reqSize = reqSize / 2
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Retrieves the rotation degrees of an image from its physical [File].
     *
     * @param file The image file.
     * @return The rotation degrees (0, 90, 180, or 270).
     */
    suspend fun getExifRotation(file: File): Int = withContext(ioDispatcher) {
        try {
            if (!file.exists()) return@withContext 0
            val exif = ExifInterface(file.absolutePath)

            Napier.d { "correct" }
            getExifRotation(exif)
        } catch (_: IOException) {
            Napier.e { "Error getting rotation degrees" }
            0
        }
    }

    /**
     * Retrieves the rotation degrees of an image from its [Uri].
     *
     * @param uri The image URI.
     * @return The rotation degrees (0, 90, 180, or 270).
     */
    suspend fun getExifRotation(uri: Uri): Int = withContext(ioDispatcher) {
        try {
            contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                getExifRotation(exif)
            } ?: 0
        } catch (_: IOException) {
            0
        }
    }

    /**
     * Helper function to map ExifInterface orientation tags to degrees.
     *
     * @param exif The ExifInterface to be mapped.
     * @return The rotation degrees (0, 90, 180, or 270).
     */
    private fun getExifRotation(exif: ExifInterface): Int {
        return when (exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
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
}