package es.pile.core.domain.useCases

import es.pile.DocumentModel
import es.pile.core.domain.repositories.BitmapCacheRepository
import es.pile.core.domain.repositories.DocumentImageRepository
import es.pile.core.domain.repositories.FileRepository
import kotlinx.coroutines.flow.firstOrNull

/**
 * Use case responsible for resolving the physical file associated with a specific document page
 * and triggering the bitmap loading process into the cache.
 */
class RequestBitmapLoadUseCase(
    private val documentImageRepository: DocumentImageRepository,
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
        if (document.isIncomingPdf) {
            val file = fileRepository.getPDFFile(documentId = document.id)

            bitmapCacheRepository.loadBitmap(
                file = file,
                document = document,
                pageNumber = pageNumber
            )
        } else {
            val imageId = document.imageIds.getOrNull(pageNumber) ?: return
            val file = fileRepository.getImageFile(documentId = document.id, imageId =  imageId)

            val documentImage = documentImageRepository.getDocumentImageById(imageId).firstOrNull()

            bitmapCacheRepository.loadBitmap(
                file = file,
                document = document,
                pageNumber = pageNumber,
                documentImage = documentImage
            )
        }
    }
}