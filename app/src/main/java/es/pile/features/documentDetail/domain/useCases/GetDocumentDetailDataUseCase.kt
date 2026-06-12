package es.pile.features.documentDetail.domain.useCases

import es.pile.DocumentModel
import es.pile.PileModel
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.repositories.PileModelRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest

/**
 * Use case that aggregates all necessary domain data for the Document Detail screen.
 *
 * It provides a reactive stream that updates whenever the document metadata, its pile associations,
 * or the global list of available piles change.
 *
 * It also handles PDF-specific logic, such as resolving the total page count for documents
 * marked as [DocumentModel.isIncomingPdf].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GetDocumentDetailDataUseCase(
    private val documentModelRepository: DocumentModelRepository,
    private val pileModelRepository: PileModelRepository,
    private val fileRepository: FileRepository
) {
    /**
     * Data wrapper for the Document Detail state.
     *
     * @property document The [DocumentModel] containing core metadata.
     * @property documentPiles The list of [PileModel] currently associated with this document.
     * @property pdfPageCount The number of pages if the document is a PDF, null otherwise.
     * @property allPiles The list of all available piles in the system (for management/selection).
     */
    data class DocumentDetailData(
        val document: DocumentModel,
        val documentPiles: List<PileModel>,
        val pdfPageCount: Int?,
        val allPiles: List<PileModel>
    )

    /**
     * Starts observing data for a specific document.
     *
     * @param documentId The unique identifier of the document.
     * @return A [Flow] emitting [DocumentDetailData] whenever relevant data changes.
     *         Emits null if the document does not exist.
     */
    operator fun invoke(documentId: String): Flow<DocumentDetailData?> {
        val documentFlow = documentModelRepository.getDocumentModelById(documentId).distinctUntilChanged()

        val documentPilesFlow = documentFlow
            .map { it?.documentPileIds ?: emptyList() }
            .distinctUntilChanged()
            .flatMapLatest { ids ->
                if (ids.isEmpty()) flowOf(emptyList())
                else pileModelRepository.getPileModelsByIds(ids)
            }

        val pdfPagesFlow = documentFlow
            .distinctUntilChanged()
            .mapLatest { document ->
                if (document != null && document.isIncomingPdf) {
                    fileRepository.getPageCount(document.id).getOrElse { error ->
                        Napier.e("Error getting PDF page count", error)
                        0
                    }
                } else {
                    null
                }
            }

        val allPilesFlow = pileModelRepository.pileModels

        return combine(
            documentFlow,
            documentPilesFlow,
            pdfPagesFlow,
            allPilesFlow
        ) { document, piles, pdfPages, allPiles ->
            if (document == null) return@combine null

            DocumentDetailData(
                document = document,
                documentPiles = piles,
                pdfPageCount = pdfPages,
                allPiles = allPiles
            )
        }
    }
}
