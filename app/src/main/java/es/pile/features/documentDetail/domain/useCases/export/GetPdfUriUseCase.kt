package es.pile.features.documentDetail.domain.useCases.export

import android.net.Uri
import es.pile.DocumentModel
import es.pile.core.domain.repositories.FileRepository

/**
 * Use case responsible for providing a secure [Uri] of a document's PDF file for external use.
 *
 * It ensures the PDF is up-to-date, creates a temporary copy with a user-friendly
 * display name, and generates a content URI that
 * can be safely shared with or opened by other applications.
 */
class GetPdfUriUseCase(
    private val getUpToDatePdfUseCase: GetUpToDatePdfUseCase,
    private val fileRepository: FileRepository
) {
    /**
     * Retrieves a shareable [Uri] for the given [document].
     *
     * @param document The document model to get the PDF URI for.
     * @return A secure content [Uri] pointing to a temporary copy of the PDF with its proper title.
     */
    suspend operator fun invoke(document: DocumentModel): Uri {
        val originalFile = getUpToDatePdfUseCase(document)

        val displayFile = fileRepository.createTempPdfCopyWithName(
            sourceFile = originalFile,
            displayName = document.title
        )
        return fileRepository.getUriForFile(displayFile)
    }
}