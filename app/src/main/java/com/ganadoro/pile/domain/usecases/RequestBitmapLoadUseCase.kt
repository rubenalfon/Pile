package com.ganadoro.pile.domain.usecases

import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.domain.repositories.BitmapCacheRepository
import com.ganadoro.pile.domain.repositories.FileRepository

/**
 * Use case responsible for resolving the physical file associated with a specific document page
 * and triggering the bitmap loading process into the cache.
 */
class RequestBitmapLoadUseCase(
    private val bitmapCacheRepository: BitmapCacheRepository,
    private val fileRepository: FileRepository
) {
    /**
     * Resolves the correct file path (PDF or Image) and requests the cache repository to load it.
     *
     * @param document The document metadata.
     * @param pageNumber The 0-based index of the page to load.
     */
    suspend operator fun invoke(document: DocumentModel, pageNumber: Int) {
        val file = if (document.isIncomingPdf) {
            fileRepository.getPDFFile(document.id)
        } else {
            val imageId = document.imageIds.getOrNull(pageNumber) ?: return
            fileRepository.getImageFile(document.id, imageId)
        }

        bitmapCacheRepository.loadBitmap(
            file = file,
            document = document,
            pageNumber = pageNumber
        )
    }
}