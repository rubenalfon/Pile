package es.pile.features.home.domain.useCases

import es.pile.DocumentModel
import es.pile.PileModel
import es.pile.core.domain.models.DocumentStatusConstants.TEMPORARY
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.PileModelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Use case responsible for orchestrating and aggregating the essential data required for the Home screen.
 *
 * This use case combines streams of [PileModel] and [DocumentModel] to provide a unified [HomeData] object.
 * It performs the following logical operations:
 * 1. Identifies the current "temporary" document (if any) that hasn't been finalized.
 * 2. Filters out temporary documents from the main document list to show only persistent ones.
 * 3. Extracts a distinct list of Pile IDs that are currently associated with documents to assist in UI coloring or badges.
 *
 * @property pileModelRepository Repository to fetch all available piles.
 * @property documentModelRepository Repository to fetch all documents.
 */
class GetHomeDataUseCase(
    private val pileModelRepository: PileModelRepository,
    private val documentModelRepository: DocumentModelRepository
) {
    /**
     * Represents the aggregated state for the Home screen.
     *
     * @property piles The list of all created piles.
     * @property documents The list of persistent documents (excluding temporary ones).
     * @property temporaryDocument The single document currently in [es.pile.core.domain.models.DocumentStatusConstants.TEMPORARY] state, if it exists.
     * @property coloredPileIds A list of IDs for piles that are currently linked to at least one document.
     */
    data class HomeData(
        val piles: List<PileModel>,
        val documents: List<DocumentModel>,
        val temporaryDocument: DocumentModel?,
        val coloredPileIds: List<String>
    )

    /**
     * Returns a [Flow] that emits updated [HomeData] whenever the underlying piles or documents change.
     *
     * @return A cold flow of [HomeData].
     */
    operator fun invoke(): Flow<HomeData> {
        return combine(
            pileModelRepository.pileModels,
            documentModelRepository.documentModels
        ) { piles, documents ->
            val temporaryDocument = documents.find { it.documentStatus == TEMPORARY }
            val coloredPileIds = documents.flatMap { it.documentPileIds }.distinct()
            val persistentDocuments = documents.filter { it.documentStatus != TEMPORARY }

            HomeData(
                piles = piles,
                documents = persistentDocuments,
                temporaryDocument = temporaryDocument,
                coloredPileIds = coloredPileIds
            )
        }
    }
}
