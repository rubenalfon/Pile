package es.pile.features.documentDetail.domain.useCases.export

import es.pile.DocumentModel
import es.pile.core.domain.repositories.FileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Use case responsible for providing a valid and up-to-date PDF file for a document.
 *
 * It determines whether to return an existing PDF (if it's an incoming PDF or still valid)
 * or trigger a new generation process if the document's content has changed.
 */
class GetUpToDatePdfUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val fileRepository: FileRepository,
    private val generatePdfUseCase: GeneratePdfUseCase
) {
    /**
     * Retrieves an up-to-date PDF file for the given [document].
     *
     * @param document The document model to get the PDF for.
     * @return A [File] pointing to the up-to-date PDF.
     */
    suspend operator fun invoke(
        document: DocumentModel
    ): File = withContext(ioDispatcher) {
        if (document.isIncomingPdf)
            return@withContext fileRepository.getPDFFile(documentId = document.id)

        if (fileRepository.isPdfOutdated(document))
            return@withContext generatePdfUseCase(document)
        else
            return@withContext fileRepository.getPDFFile(documentId = document.id)

    }
}
