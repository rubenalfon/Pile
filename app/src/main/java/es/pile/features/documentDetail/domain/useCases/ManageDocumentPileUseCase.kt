package es.pile.features.documentDetail.domain.useCases

import es.pile.DocumentModel
import es.pile.core.domain.repositories.DocumentModelRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Use case responsible for managing the association between a document and a pile.
 *
 * It allows toggling the presence of a document within a specific pile, updating the
 * document's metadata accordingly in the database.
 */
class ManageDocumentPileUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val documentModelRepository: DocumentModelRepository
) {
    /**
     * Toggles the association of a document with a pile.
     *
     * If the document is already part of the pile, it will be removed.
     * If it is not part of the pile, it will be added.
     *
     * @param document The document to be updated.
     * @param pileId The unique identifier of the pile to add or remove.
     */
    suspend operator fun invoke(
        document: DocumentModel,
        pileId: String
    ) = withContext(ioDispatcher) {
        val documentPiles = document.documentPileIds

        val updatedDocumentPiles = documentPiles.toMutableList().apply {
            if (contains(pileId)) remove(pileId)
            else add(pileId)
        }

        documentModelRepository.updateDocumentModel(
            document.copy(documentPileIds = updatedDocumentPiles)
        )
    }
}