package com.ganadoro.pile.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.util.Size
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface

fun Bitmap.resizeKeepingRatio(maxSize: Size): Bitmap {
    val widthRatio = maxSize.width.toFloat() / this.width
    val heightRatio = maxSize.height.toFloat() / this.height

    val scaleFactor = minOf(widthRatio, heightRatio)

    if (scaleFactor >= 1.0f && (this.width <= maxSize.width && this.height <= maxSize.height)) {
        return this
    }

    val newSize = Size(
        /* width = */ (this.width * scaleFactor).toInt(),
        /* height = */ (this.height * scaleFactor).toInt()
    )

    if (newSize.width <= 0 || newSize.width <= 0) {
        return this
    }

    return this.scale(width = newSize.width, height = newSize.height)
}

fun Bitmap.applyColorFilter(colorMatrix: ColorMatrix): Bitmap {
    val resultBitmap = createBitmap(this.width, this.height)

    val canvas = Canvas(resultBitmap)

    val paint = Paint()
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)

    canvas.drawBitmap(this, 0f, 0f, paint)

    return resultBitmap
}

fun prepareBitmapFromUri(
    context: Context,
    uri: Uri,
    maxSize: Size = Size(1200, 1200)
): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val rotation = try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val exif = ExifInterface(pfd.fileDescriptor)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }

        val scaledBitmap = originalBitmap.resizeKeepingRatio(maxSize)

        when (rotation) {
            90, 180, 270 -> {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
            }
            else -> scaledBitmap
        }

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


///** TODO: DELETE This
// * Procesa una lista de archivos en paralelo y devuelve sus Bitmaps.
// */
//suspend fun prepareBitmapsFromFiles(
//    files: List<File>
//): List<Bitmap> = withContext(Dispatchers.IO) {
//    files.map { file ->
//        async {
//            prepareBitmapFromFile(file)
//        }
//    }.awaitAll().filterNotNull()
//}
