package com.ganadoro.pile.repositories

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import androidx.exifinterface.media.ExifInterface
import com.ganadoro.pile.DocumentModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import kotlin.use

/**
 * Repository responsible for loading and holding Bitmap images in memory (RAM).
 * Acts as a temporary cache to display images quickly in the UI.
 */
interface BitmapCacheRepository {

    /**
     * Observable state containing the currently loaded bitmaps mapped by their unique ID.
     */
    val bitmapCache: StateFlow<Map<String, Bitmap>>

    /**
     * Returns the unique key used in the map for an image.
     *
     * @param document The document model containing metadata.
     * @param pageNumber The index of the page (0-based).
     * @return The unique key for the image.
     */
    fun getImageKey(document: DocumentModel, pageNumber: Int): String

    /**
     * Loads a specific page or image associated with a document into the cache.
     * If the image is already cached, this operation does nothing.
     *
     * @param file The file containing the image data.
     * @param document The document model containing metadata.
     * @param pageNumber The index of the page to load (0-based).
     */
    suspend fun loadBitmap(file: File, document: DocumentModel, pageNumber: Int)

    /**
     * Removes a specific bitmap from memory to free up resources.
     * @param cacheKey The unique key of the image (imageId or generated PDF page key).
     */
    fun removeFromCache(cacheKey: String)

    /**
     * Clears all bitmaps from the cache and recycles them to prevent memory leaks.
     */
    fun clearCache()
}

class BitmapCacheRepositoryImpl(
    private val ioDispatcher: CoroutineDispatcher
) : BitmapCacheRepository {
    private val _bitmapCache = MutableStateFlow<Map<String, Bitmap>>(emptyMap())
    override val bitmapCache: StateFlow<Map<String, Bitmap>> = _bitmapCache.asStateFlow()

    override fun getImageKey(document: DocumentModel, pageNumber: Int): String {
        return if (document.isIncomingPdf) {
            "${document.id}_page_${pageNumber}"
        } else {
            document.imageIds.getOrNull(pageNumber).toString()
        }
    }

    override suspend fun loadBitmap(file: File, document: DocumentModel, pageNumber: Int) =
        withContext(ioDispatcher) {
            val imageId = getImageKey(document, pageNumber)

            if (_bitmapCache.value.containsKey(imageId)) return@withContext

            val bitmap = if (document.isIncomingPdf) {
                renderPdfPage(file, pageNumber)
            } else {
                prepareBitmapFromFile(file)
            }

            if (bitmap != null) {
                _bitmapCache.update { currentCache ->
                    currentCache + (imageId to bitmap)
                }
            }
        }

    override fun removeFromCache(cacheKey: String) {
        _bitmapCache.update { current ->
            val bitmap = current[cacheKey]
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
            }
            current - cacheKey
        }
    }

    override fun clearCache() {
        val bitmapsToRecycle = _bitmapCache.value.values
        _bitmapCache.update { emptyMap() }

        bitmapsToRecycle.forEach { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    /**
     * Renders a specific page of a PDF file into a [Bitmap].
     *
     * @param pdfFile The PDF file to be rendered.
     * @param pageNumber The zero-based index of the page to render.
     * @param scale Scaling factor to increase the resolution (DPI) of the output Bitmap.
     * @return A [Bitmap] of the rendered page, or null if an error occurs.
     */
    suspend fun renderPdfPage(
        pdfFile: File,
        pageNumber: Int,
        scale: Float = 5.0f
    ): Bitmap? = withContext(ioDispatcher) {
        try {
            if (!pdfFile.exists()) return@withContext null

            ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
                PdfRenderer(fd).use { renderer ->
                    if (pageNumber in 0 until renderer.pageCount) {
                        renderPage(renderer, pageNumber, scale)
                    } else null
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Internal helper that performs the actual rendering of a PDF page using [PdfRenderer].
     *
     * @param renderer The active [PdfRenderer] instance.
     * @param pageIndex The zero-based index of the page to open and render.
     * @param scale The scaling factor to apply to the page dimensions.
     * @return A [Bitmap] containing the rendered page content.
     */
    private fun renderPage(renderer: PdfRenderer, pageIndex: Int, scale: Float): Bitmap {
        return renderer.openPage(pageIndex).use { page ->
            val width = (page.width * scale).toInt()
            val height = (page.height * scale).toInt()

            val bitmap = createBitmap(width, height)

            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            bitmap
        }
    }


    /**
     * Decodes a [Bitmap] from a given file path and applies necessary rotation based on EXIF metadata.
     *
     * This function reads the image orientation from the file's EXIF data and rotates the
     * resulting Bitmap accordingly to ensure it is displayed correctly.
     *
     * @param file The image file to be decoded.
     * @return A [Bitmap] object with correct orientation, or null if decoding fails.
     */
    suspend fun prepareBitmapFromFile(
        file: File
    ): Bitmap? = withContext(ioDispatcher) {
        try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@withContext null

            val rotation = try {
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

            if (rotation != 0) {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                val rotated = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )

                if (rotated != bitmap) bitmap.recycle()
                rotated
            } else {
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
