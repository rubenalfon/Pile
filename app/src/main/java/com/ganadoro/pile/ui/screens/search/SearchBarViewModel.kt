package com.ganadoro.pile.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.models.StringDetail
import com.ganadoro.pile.repositories.BitmapCacheRepository
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.PileModelRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.Thread.sleep
import java.time.LocalDate

data class SearchBarUiState(
    var pileList: List<PileModel>? = null,
    var documentList: List<DocumentModel>? = null,
    var filteredDocumentList: List<DocumentModel> = emptyList(),
    var searchQuery: String = "",
    val selectedFilterPiles: List<String> = emptyList(),
    val selectedFilterDate: LocalDate? = null
)


class SearchBarViewModel(
    private val pileRepository: PileModelRepository,
    private val documentRepository: DocumentModelRepository,
    private val bitmapCacheRepository: BitmapCacheRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchBarUiState())
    val uiState: StateFlow<SearchBarUiState> = _uiState.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    fun init() {
        viewModelScope.launch {
            val pilesFlow = pileRepository.pileModels

            val documentsFlow = documentRepository.documentModels

            pilesFlow.combine(documentsFlow) { piles, documents ->
                Pair(piles, documents)
            }.collect { (piles, documents) ->
                _uiState.update {
                    it.copy(
                        pileList = piles,
                        documentList = documents
                    )
                }
            }
        }
    }

    fun deinit() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    searchQuery = ""
                )
            }

            sleep(150) // Fixes visual errors in the animation

            _uiState.update {
                it.copy(
                    pileList = null,
                    documentList = null,
                    filteredDocumentList = emptyList(),
                    selectedFilterPiles = emptyList(),
                    selectedFilterDate = null
                )
            }
        }
    }

    fun requestBitmapLoad(documentId: String) {
        bitmapCacheRepository.ensureBitmapIsLoaded(documentId)
    }

    fun updateSearchQuery(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query
            )
        }
        filterResults()
    }

    fun addRemoveFilterPiles(pileId: String) {
        val piles = uiState.value.selectedFilterPiles.toMutableList()

        if (piles.contains(pileId)) {
            piles.remove(pileId)
        } else {
            piles.add(pileId)
        }

        _uiState.update {
            it.copy(
                selectedFilterPiles = piles
            )
        }
        filterResults()
    }

    fun updateFilterDate(date: LocalDate?) {
        Napier.d { "Date: $date" }
        _uiState.update {
            it.copy(
                selectedFilterDate = date
            )
        }
        filterResults()
    }


    fun filterResults() {
        if (uiState.value.pileList == null || uiState.value.documentList == null) return

        if (uiState.value.searchQuery == "") {
            _uiState.update {
                it.copy(
                    filteredDocumentList = emptyList()
                )
            }
            return
        }


        val pileFilteredDocumentList =
            if (uiState.value.selectedFilterPiles.isEmpty()) uiState.value.documentList!!
            else uiState.value.documentList!!.filter { document ->
                document.documentPileIds.any { it in uiState.value.selectedFilterPiles }
            }

        val pileDateFilteredDocumentList =
            if (uiState.value.selectedFilterDate == null) pileFilteredDocumentList
            else pileFilteredDocumentList.filter { document ->
                document.creationDate == uiState.value.selectedFilterDate || document.modificationDate == uiState.value.selectedFilterDate
            }

        val filteredDocumentList = pileDateFilteredDocumentList.filter { document ->
            Napier.d { "SS".plus(
                document.title
                .plus(" ").plus(document.documentNote)
                .plus(" ").plus(document.documentDetails.map {
                    Pair(it.name, (it as? StringDetail)?.value ?: "" )
                })
            )
            }

            document.title
                .plus(" ").plus(document.documentNote)
                .plus(" ").plus(document.documentDetails.map {
                    Pair(it.name, (it as? StringDetail)?.value ?: "" )
                })
                .contains(
                    other = uiState.value.searchQuery,
                    ignoreCase = true
                )
        }

        _uiState.update {
            it.copy(
                filteredDocumentList = filteredDocumentList
            )
        }
    }
}