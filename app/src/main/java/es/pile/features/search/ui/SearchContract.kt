package es.pile.features.search.ui

import es.pile.DocumentModel
import es.pile.PileModel
import java.time.LocalDate

data class SearchState(
    val isLoading: Boolean = true,
    val pileList: List<PileModel> = emptyList(),
    val documentList: List<DocumentModel> = emptyList(),
    val filteredDocumentList: List<SearchItem> = emptyList(),
    val searchQuery: String = "",
    val selectedFilterPiles: List<String> = emptyList(),
    val selectedFilterDate: LocalDate? = null
)

data class SearchItem(
    val document: DocumentModel,
    val coverImageCacheKey: String
)

sealed interface SearchEvent {
    data object OnSearch : SearchEvent
    data object OnCloseSearch : SearchEvent
    data class OnImageDisplayed(val document: DocumentModel) : SearchEvent
    data class OnUpdateSearchQuery(val query: String) : SearchEvent
    data class OnUpdateFilterPiles(val pileId: String) : SearchEvent
    data class OnUpdateFilterDate(val date: LocalDate?) : SearchEvent
}