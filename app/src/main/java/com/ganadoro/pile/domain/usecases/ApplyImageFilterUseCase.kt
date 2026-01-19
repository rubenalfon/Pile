package com.ganadoro.pile.domain.usecases

import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.domain.repositories.BitmapCacheRepository
import com.ganadoro.pile.domain.repositories.DocumentImageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ApplyImageFilterUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val documentImageRepository: DocumentImageRepository,
    private val bitmapCacheRepository: BitmapCacheRepository
) {
    suspend operator fun invoke(
        document: DocumentModel,
        documentImage: DocumentImage,
        filterIndex: Int
    ) = withContext(ioDispatcher) {
        val updatedDocumentImage = documentImage.copy(filter = filterIndex.toLong())

        documentImageRepository.updateDocumentImage(updatedDocumentImage)
        val imageIndex = document.imageIds.indexOf(documentImage.id)

        val imageId = bitmapCacheRepository.getImageKey(document, imageIndex)
        bitmapCacheRepository.removeFromCache(imageId)
    }
}