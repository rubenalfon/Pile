package com.ganadoro.pile.repositories

import android.graphics.Bitmap
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.data.util.ImageTransformationHelper
import com.ganadoro.pile.data.util.PdfRenderHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File

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
    private val ioDispatcher: CoroutineDispatcher,
    private val imageTransformationHelper: ImageTransformationHelper,
    private val pdfRenderHelper: PdfRenderHelper
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
                pdfRenderHelper.renderPageToBitmap(file, pageNumber)
            } else {
                imageTransformationHelper.transform( // TODO: Hacer bien
                    file, 0, null, 0
                )
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

}
