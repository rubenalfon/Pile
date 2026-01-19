package com.ganadoro.pile.data.repositories

import android.graphics.Bitmap
import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.data.util.ImageTransformationHelper
import com.ganadoro.pile.data.util.PdfRenderHelper
import com.ganadoro.pile.domain.models.ImageFilterType
import com.ganadoro.pile.domain.repositories.BitmapCacheRepository
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