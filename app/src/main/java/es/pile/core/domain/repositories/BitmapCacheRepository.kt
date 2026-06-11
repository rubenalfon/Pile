package es.pile.core.domain.repositories

import android.graphics.Bitmap
import es.pile.DocumentImage
import es.pile.DocumentModel
import kotlinx.coroutines.flow.StateFlow
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
     * @param documentImage Additional metadata associated with the image (optional).
     */
    suspend fun loadBitmap(
        file: File,
        document: DocumentModel,
        pageNumber: Int,
        documentImage: DocumentImage? = null
    )

    /**
     * Returns the unique key used in the map for an image thumbnail.
     *
     * @param imageId The unique ID of the image.
     * @param filterId The unique ID of the filter applied to the image.
     * @return The unique key for the image thumbnail.
     */
    fun getImageThumbnailKey(imageId: String, filterId: Int): String

    /**
     * Loads a thumbnail image for a specific document image into the cache.
     * If the image is already cached, this operation does nothing.
     *
     * @param imageFile The file containing the image data.
     * @param documentImage Additional metadata associated with the image.
     * @param filterId The unique ID of the filter applied to the image.
     * @return The unique key for the image thumbnail.
     */
    suspend fun loadImageThumbnail(
        imageFile: File,
        documentImage: DocumentImage,
        filterId: Int
    )

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