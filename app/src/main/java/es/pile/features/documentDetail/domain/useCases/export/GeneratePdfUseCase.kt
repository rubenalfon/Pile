package es.pile.features.documentDetail.domain.useCases.export

import es.pile.DocumentModel
import es.pile.core.domain.repositories.DocumentImageRepository
import es.pile.core.domain.repositories.FileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Use case responsible for generating a PDF file from a list of document images.
 *
 * It retrieves all associated image metadata, ensures there is content to process,
 * and delegates the physical PDF creation to the file repository.
 */
class GeneratePdfUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val fileRepository: FileRepository,
    private val documentImageRepository: DocumentImageRepository
) {
    /**
     * Generates a PDF for the given [document].
     *
     * @param document The document model containing the IDs of the images to be included.
     * @return The generated PDF [File].
     * @throws IllegalStateException If the document has no images associated with it.
     */
    suspend operator fun invoke(
        document: DocumentModel
    ): File = withContext(ioDispatcher) {
        val imageIds = document.imageIds
        val documentImages = imageIds.mapNotNull {
            documentImageRepository.getDocumentImageById(it).first()
        }

        if (documentImages.isEmpty()) {
            throw IllegalStateException("Cannot generate PDF from empty document")
        }

        return@withContext fileRepository.createPdfFromImages(document.id, documentImages)
    }
}