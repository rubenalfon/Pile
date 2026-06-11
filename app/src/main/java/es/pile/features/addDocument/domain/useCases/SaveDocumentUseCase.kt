package es.pile.features.addDocument.domain.useCases

import es.pile.DocumentModel
import es.pile.core.domain.models.DocumentStatusConstants
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.features.addDocument.domain.models.DocumentSaveException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

/**
 * Use case responsible for finalizing the saving process of a document.
 *
 * It transitions a document from its initial state to the [DocumentStatusConstants.SAVED] status,
 * ensuring it has a valid name and updating the creation and modification timestamps.
 */
class SaveDocumentUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val documentModelRepository: DocumentModelRepository
) {
    /**
     * Executes the saving logic for the provided [documentModel].
     *
     * @param documentModel The document metadata to be persisted.
     * @param documentName The title to be assigned to the document.
     * @return A [Result] indicating success or failure (e.g., [DocumentSaveException.EmptyName]).
     */
    suspend operator fun invoke(
        documentModel: DocumentModel,
        documentName: String
    ): Result<Unit> = runCatching {
        if (documentModel.documentStatus == DocumentStatusConstants.SAVED) {
            throw DocumentSaveException.AlreadySaved
        }

        if (documentName.isBlank()) {
            throw DocumentSaveException.EmptyName
        }

        val now = LocalDateTime.now()

        withContext(ioDispatcher) {
            val updatedDocument = documentModel.copy(
                title = documentName,
                creationDateTime = now,
                modificationDateTime = now,
                documentStatus = DocumentStatusConstants.SAVED,
                documentDetails = emptyList(),
                documentOrganizationIds = emptyList()
            )

            documentModelRepository.updateDocumentModel(updatedDocument)
        }
    }
}