package com.ganadoro.pile.features.search.ui

import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import java.time.LocalDate

data class SearchBarState(
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
    data class OnImageDisplayed(val document: DocumentModel, val pageNumber: Int) : SearchBarEvent
    data class OnSearchQueryChanged(val query: String) : SearchBarEvent
    data class OnFilterPilesChanged(val pileId: String) : SearchBarEvent
    data class OnFilterDateChanged(val date: LocalDate?) : SearchBarEvent
}