package es.pile.features.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pile.DocumentModel
import es.pile.core.domain.models.StringDetail
import es.pile.core.domain.repositories.BitmapCacheRepository
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.PileModelRepository
import es.pile.core.domain.useCases.RequestBitmapLoadUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class SearchViewModel(
    private val pileId: String?,
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase,
    private val pileRepository: PileModelRepository,
    private val documentRepository: DocumentModelRepository,
    private val bitmapCacheRepository: BitmapCacheRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    init {
        viewModelScope.launch {
            val pilesFlow = pileRepository.pileModels

            val documentsFlow = documentRepository.documentModels

            pilesFlow.combine(documentsFlow) { piles, documents ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        pileList = piles,
                        documentList = documents,
                        selectedFilterPiles = if (pileId != null) listOf(pileId) else emptyList()
                    )
                }
            }.collect()
        }
    }

    fun handleEvent(event: SearchEvent) {
        when (event) {
            SearchEvent.OnSearch -> filterResults()
            SearchEvent.OnCloseSearch -> resetSearch()
            is SearchEvent.OnImageDisplayed -> requestBitmapLoad(event.document)

            is SearchEvent.OnUpdateSearchQuery -> updateSearchQuery(event.query)
            is SearchEvent.OnUpdateFilterPiles -> addRemoveFilterPiles(event.pileId)
            is SearchEvent.OnUpdateFilterDate -> updateFilterDate(event.date)
        }
    }

    private fun filterResults() {
        val currentState = state.value

        if (currentState.pileList.isEmpty() || currentState.documentList.isEmpty()) return

        if (currentState.searchQuery == "") {
            _state.update { it.copy(filteredDocumentList = emptyList()) }
            return
        }

        _state.update { it.copy(isLoading = true) }

        val pileFilteredDocumentList =
            if (currentState.selectedFilterPiles.isEmpty()) currentState.documentList
            else currentState.documentList.filter { document ->
                document.documentPileIds.any { it in currentState.selectedFilterPiles }
            }

        val pileDateFilteredDocumentList =
            if (currentState.selectedFilterDate == null) pileFilteredDocumentList
            else pileFilteredDocumentList.filter { document ->
                document.creationDateTime.toLocalDate() == currentState.selectedFilterDate ||
                        document.modificationDateTime.toLocalDate() == currentState.selectedFilterDate
            }

        val filteredDocumentList = pileDateFilteredDocumentList.filter { document ->
            document.title
                .plus(" ").plus(document.documentNote)
                .plus(" ").plus(document.documentDetails.map {
                    Pair(it.name, (it as? StringDetail)?.value ?: "")
                })
                .contains(
                    other = currentState.searchQuery,
                    ignoreCase = true
                )
        }.map { document ->
            SearchItem(
                document = document,
                coverImageCacheKey = bitmapCacheRepository.getImageKey(document, 0)
            )
        }

        _state.update {
            it.copy(
                filteredDocumentList = filteredDocumentList,
                isLoading = false
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

    private fun requestBitmapLoad(document: DocumentModel) {
        viewModelScope.launch {
            requestBitmapLoadUseCase(document, 0)
        }
    }

    private fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        filterResults()
    }

    private fun addRemoveFilterPiles(pileId: String) {
        val piles = state.value.selectedFilterPiles.toMutableList()

        if (piles.contains(pileId)) {
            piles.remove(pileId)
        } else {
            piles.add(pileId)
        }

        _state.update { it.copy(selectedFilterPiles = piles) }
        filterResults()
    }

    private fun updateFilterDate(date: LocalDate?) {
        _state.update { it.copy(selectedFilterDate = date) }
        filterResults()
    }
}