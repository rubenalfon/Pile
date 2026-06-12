package es.pile.features.search.domain.useCases

import es.pile.DocumentModel
import es.pile.core.domain.models.StringDetail
import java.time.LocalDate

/**
 * Use case that encapsulates the complex search and filtering logic for documents.
 *
 * It allows filtering by a text query, association with specific piles, and a specific date.
 * The filtering is inclusive of the text query across the title, notes, and specific document details.
 */
class SearchDocumentsUseCase {

    /**
     * Executes the search logic over the provided [documentList].
     *
     * The priority of filtering is:
     * 1. **Piles**: Only documents belonging to any of the [selectedFilterPiles] are kept (if not empty).
     * 2. **Date**: Only documents created or modified on [selectedFilterDate] are kept (if not null).
     * 3. **Search Query**: Documents are filtered by [searchQuery] (case-insensitive) looking into:
     *    - Document Title
     *    - Document Notes
     *    - String values within Document Details ([es.pile.core.domain.models.StringDetail])
     *
     * If all filters are empty/null, it returns an empty list to avoid displaying everything by default.
     *
     * @param documentList The full list of documents to filter from.
     * @param searchQuery The text query to match against document fields.
     * @param selectedFilterPiles List of Pile IDs to restrict the search to.
     * @param selectedFilterDate The specific date to filter by (creation or modification).
     * @return A filtered list of [DocumentModel].
     */
    fun execute(
        documentList: List<DocumentModel>,
        searchQuery: String,
        selectedFilterPiles: List<String>,
        selectedFilterDate: LocalDate?
    ): List<DocumentModel> {
        if (searchQuery.isBlank() && selectedFilterPiles.isEmpty() && selectedFilterDate == null) {
            return emptyList()
        }

        val pileFiltered = if (selectedFilterPiles.isEmpty()) {
            documentList
        } else {
            documentList.filter { document ->
                document.documentPileIds.any { it in selectedFilterPiles }
            }
        }

        val dateFiltered = if (selectedFilterDate == null) {
            pileFiltered
        } else {
            pileFiltered.filter { document ->
                document.creationDateTime.toLocalDate() == selectedFilterDate ||
                        document.modificationDateTime.toLocalDate() == selectedFilterDate
            }
        }

        if (searchQuery.isBlank()) return dateFiltered

        return dateFiltered.filter { document ->
            document.title
                .plus(" ").plus(document.documentNote)
                .plus(" ").plus(document.documentDetails.map {
                    (it as? StringDetail)?.value ?: ""
                }.joinToString(" "))
                .contains(
                    other = searchQuery,
                    ignoreCase = true
                )
        }
    }
}
