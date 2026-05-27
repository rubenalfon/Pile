package com.pile.features.search.ui

import com.pile.DocumentModel
import com.pile.PileModel
import java.time.LocalDate

data class SearchBarState(
    val isLoading: Boolean = true,
    val pileList: List<PileModel> = emptyList(),
    val documentList: List<DocumentModel> = emptyList(),
    val filteredDocumentList: List<DocumentSearchItem> = emptyList(),
    val searchQuery: String = "",
    val selectedFilterPiles: List<String> = emptyList(),
    val selectedFilterDate: LocalDate? = null
)

data class DocumentSearchItem(
    val document: DocumentModel,
    val coverImageCacheKey: String
)

sealed interface SearchBarEvent {
    data object OnSearch : SearchBarEvent
    data object OnCloseSearch : SearchBarEvent
    data class OnImageDisplayed(val document: DocumentModel) : SearchBarEvent
    data class OnUpdateSearchQuery(val query: String) : SearchBarEvent
    data class OnUpdateFilterPiles(val pileId: String) : SearchBarEvent
    data class OnUpdateFilterDate(val date: LocalDate?) : SearchBarEvent
}