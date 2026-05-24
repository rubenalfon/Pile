package com.ganadoro.pile.features.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.core.domain.models.StringDetail
import com.ganadoro.pile.core.domain.repositories.BitmapCacheRepository
import com.ganadoro.pile.core.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.core.domain.repositories.PileModelRepository
import com.ganadoro.pile.core.domain.useCases.RequestBitmapLoadUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class SearchBarViewModel(
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase,
    private val pileRepository: PileModelRepository,
    private val documentRepository: DocumentModelRepository,
    private val bitmapCacheRepository: BitmapCacheRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SearchBarState())
    val state: StateFlow<SearchBarState> = _state.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    init {
        viewModelScope.launch {
            val pilesFlow = pileRepository.pileModels

            val documentsFlow = documentRepository.documentModels

            pilesFlow.combine(documentsFlow) { piles, documents ->
                _state.update {
                    it.copy(
                        pileList = piles,
                        documentList = documents
                    )
                }
            }.collect()
        }
    }

    fun handleEvent(event: SearchBarEvent) {
        when (event) {
            SearchBarEvent.OnSearch -> filterResults()
            SearchBarEvent.OnCloseSearch -> resetSearch()
            is SearchBarEvent.OnImageDisplayed -> requestBitmapLoad(event.document, event.pageNumber)
            is SearchBarEvent.OnSearchQueryChanged -> updateSearchQuery(event.query)
            is SearchBarEvent.OnFilterPilesChanged -> addRemoveFilterPiles(event.pileId)
            is SearchBarEvent.OnFilterDateChanged -> updateFilterDate(event.date)
        }
    }

    private fun filterResults() {
        val state = state.value

        if (state.pileList.isEmpty() || state.documentList.isEmpty()) return

        if (state.searchQuery == "") {
            _state.update { it.copy(filteredDocumentList = emptyList()) }
            return
        }

        val pileFilteredDocumentList =
            if (state.selectedFilterPiles.isEmpty()) state.documentList
            else state.documentList.filter { document ->
                document.documentPileIds.any { it in state.selectedFilterPiles }
            }

        val pileDateFilteredDocumentList =
            if (state.selectedFilterDate == null) pileFilteredDocumentList
            else pileFilteredDocumentList.filter { document ->
                document.creationDateTime == state.selectedFilterDate || document.modificationDateTime == state.selectedFilterDate
            }

        val filteredDocumentList = pileDateFilteredDocumentList.filter { document ->
            document.title
                .plus(" ").plus(document.documentNote)
                .plus(" ").plus(document.documentDetails.map {
                    Pair(it.name, (it as? StringDetail)?.value ?: "")
                })
                .contains(
                    other = state.searchQuery,
                    ignoreCase = true
                )
        }.map { document ->
            DocumentSearchItem(
                document = document,
                coverImageCacheKey = bitmapCacheRepository.getImageKey(document, 0)
            )
        }

        _state.update {
            it.copy(
                filteredDocumentList = filteredDocumentList
            )
        }
    }

    private fun resetSearch() {
        _state.update {
            it.copy(
                searchQuery = "",
                filteredDocumentList = emptyList(),
                selectedFilterPiles = emptyList(),
                selectedFilterDate = null
            )
        }
    }

    private fun requestBitmapLoad(document: DocumentModel, pageNumber: Int) {
        viewModelScope.launch {
            requestBitmapLoadUseCase(document, pageNumber)
        }
    }

    private fun updateSearchQuery(query: String) {
        _state.update {
            it.copy(
                searchQuery = query
            )
        }
        filterResults()
    }

    private fun addRemoveFilterPiles(pileId: String) {
        val piles = state.value.selectedFilterPiles.toMutableList()

        if (piles.contains(pileId)) {
            piles.remove(pileId)
        } else {
            piles.add(pileId)
        }

        _state.update {
            it.copy(
                selectedFilterPiles = piles
            )
        }
        filterResults()
    }

    private fun updateFilterDate(date: LocalDate?) {
        _state.update {
            it.copy(
                selectedFilterDate = date
            )
        }
        filterResults()
    }
}