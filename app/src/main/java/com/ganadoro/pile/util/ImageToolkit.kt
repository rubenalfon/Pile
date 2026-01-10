package com.ganadoro.pile.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Guarda y redimensiona una lista de Uris de forma paralela evaluando ambas dimensiones.
 * @param maxSize Límite máximo para el lado más largo (ancho o alto).
 * @param quality Calidad de compresión (1-100).
 */
suspend fun saveResizedImagesToInternalStorage(
    context: Context,
    uris: List<Uri>,
    documentId: String,
    maxSize: Int = 1200,
    quality: Int = 85
): List<File> = withContext(Dispatchers.IO) {
    val storageDir = File(context.filesDir, documentId).apply {
        if (!exists()) mkdirs()
    }

    uris.map { uri ->
        async {
            try {
                val rotation = getRotationDegrees(context, uri)

                val fileName = "img_${UUID.randomUUID()}.jpg"
                val destFile = File(storageDir, fileName)

                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, options)
                }
                options.inSampleSize = calculateInSampleSize(options, maxSize)
                options.inJustDecodeBounds = false

                val bitmap = context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, options)
                }

                bitmap?.let {
                    var finalBitmap = scaleBitmap(it, maxSize)

                    if (rotation != 0) {
                        finalBitmap = rotateBitmap(finalBitmap, rotation)
                    }

                    FileOutputStream(destFile).use { out ->
                        finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    }
                    if (finalBitmap != it) finalBitmap.recycle()
                    finalBitmap.recycle()
                }

                destFile
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }.awaitAll().filterNotNull()
}

private fun getRotationDegrees(context: Context, uri: Uri): Int {
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val exif = ExifInterface(input)
            when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } ?: 0
    } catch (e: Exception) {
        0
    }
}

private fun rotateBitmap(source: Bitmap, degrees: Int): Bitmap {
    if (degrees == 0) return source
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    source.recycle()
    return rotated
}

/**
 * Calcula el inSampleSize evaluando tanto ancho como alto.
 */
private fun calculateInSampleSize(options: BitmapFactory.Options, maxSize: Int): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > maxSize || width > maxSize) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        // Calcula el mayor factor de potencia de 2 que mantenga ambas dimensiones mayores al límite
        while (halfHeight / inSampleSize >= maxSize || halfWidth / inSampleSize >= maxSize) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

/**
 * Escala el bitmap asegurando que el lado más largo no supere el maxSize.
 */
private fun scaleBitmap(source: Bitmap, maxSize: Int): Bitmap {
    val width = source.width
    val height = source.height

    if (width <= maxSize && height <= maxSize) return source

    val ratio = width.toFloat() / height.toFloat()
    val targetWidth: Int
    val targetHeight: Int

    if (width > height) {
        targetWidth = maxSize
        targetHeight = (maxSize / ratio).toInt()
    } else {
        targetHeight = maxSize
        targetWidth = (maxSize * ratio).toInt()
    }

    return source.scale(targetWidth, targetHeight)
}

