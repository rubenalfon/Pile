package es.pile.features.documentDetail.domain.useCases.export

import es.pile.DocumentModel
import es.pile.core.domain.repositories.FileRepository

/**
 * Use case responsible for exporting a document's PDF file to the public storage.
 *
 * It retrieves the most recent version of the document's PDF and delegates the 
 * transfer to the system's Downloads folder through the repository.
 */
class ExportDocumentUseCase(
    private val getUpToDatePdfUseCase: GetUpToDatePdfUseCase,
    private val fileRepository: FileRepository
) {
    /**
     * Exports the provided [document] to the device's public Downloads directory.
     *
     * @param document The document model to be exported.
     * @return A [Result] containing the name or path of the exported file on success, 
     * or an exception on failure.
     */
    suspend operator fun invoke(document: DocumentModel): Result<String> {
        val sourceFile = getUpToDatePdfUseCase(document)
        return fileRepository.exportFileToDownloads(sourceFile, document.title)
    }
}
