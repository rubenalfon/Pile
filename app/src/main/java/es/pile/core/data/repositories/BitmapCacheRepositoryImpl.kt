package es.pile.core.data.repositories

import android.graphics.Bitmap
import es.pile.DocumentImage
import es.pile.DocumentModel
import es.pile.core.data.util.ImageTransformationHelper
import es.pile.core.data.util.PdfRenderHelper
import es.pile.core.domain.models.ImageFilterType
import es.pile.core.domain.repositories.BitmapCacheRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File


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

    override suspend fun loadBitmap(
        file: File,
        document: DocumentModel,
        pageNumber: Int,
        documentImage: DocumentImage?
    ) = withContext(ioDispatcher) {
        val imageId = getImageKey(document, pageNumber)

        if (_bitmapCache.value.containsKey(imageId)) return@withContext

        val bitmap = if (document.isIncomingPdf) {
            pdfRenderHelper.renderPageToBitmap(file, pageNumber)
        } else {
            imageTransformationHelper.transform(
                file = file,
                rotation = documentImage?.rotation?.toInt() ?: 0,
                cropData = documentImage?.crop,
                filter = ImageFilterType.fromId(documentImage?.filter?.toInt() ?: 0)
            )?.bitmap
        }

        if (bitmap != null) {
            _bitmapCache.update { currentCache ->
                currentCache + (imageId to bitmap)
            }
        }
    }

    override fun getImageThumbnailKey(imageId: String, filterId: Int): String =
        "${imageId}_filter_${filterId}"

    override suspend fun loadImageThumbnail(
        imageFile: File,
        documentImage: DocumentImage,
        filterId: Int
    ) = withContext(ioDispatcher) {
        val thumbnailId = getImageThumbnailKey(documentImage.id, filterId)

        if (_bitmapCache.value.containsKey(thumbnailId)) return@withContext

        val bitmap = imageTransformationHelper.transform(
            file = imageFile,
            rotation = documentImage.rotation.toInt(),
            cropData = documentImage.crop,
            filter = ImageFilterType.fromId(filterId),
            reqSize = 700
        )?.bitmap

        if (bitmap != null) {
            _bitmapCache.update { currentCache ->
                currentCache + (thumbnailId to bitmap)
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