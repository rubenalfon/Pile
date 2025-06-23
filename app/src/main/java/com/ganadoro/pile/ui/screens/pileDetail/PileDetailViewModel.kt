package com.ganadoro.pile.ui.screens.pileDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.repositories.PileModelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

data class PileDetailUiState(
    var pile: PileModel? = null,
    var documentList: List<DocumentModel> = emptyList()
)

class PileDetailViewModel(
    private val pileModelRepository: PileModelRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PileDetailUiState())
    val uiState: StateFlow<PileDetailUiState> = _uiState.asStateFlow()

    fun loadPile(pileId: String) {
        viewModelScope.launch {
            val pile = pileModelRepository.getPileModelById(pileId)
            _uiState.value = _uiState.value.copy(pile = pile)
        }

        _uiState.value.documentList = listOf( // TODO: Remove and search in db
            DocumentModel(
                id = UUID.randomUUID().toString(),
                title = "Mi documento",
                creationDate = LocalDate.of(2025, 4, 1),
                modificationDate = LocalDate.of(2025, 4, 1),
                documentDetails = emptyList(),
                documentOrganizationIds = emptyList(),
                documentNote = "",
                documentPileIds = emptyList()
            ),
            DocumentModel(
                id = UUID.randomUUID().toString(),
                title = "Mi documento",
                creationDate = LocalDate.of(2025, 4, 1),
                modificationDate = LocalDate.of(2025, 4, 1),
                documentDetails = emptyList(),
                documentOrganizationIds = emptyList(),
                documentNote = "",
                documentPileIds = emptyList()
            ),
            DocumentModel(
                id = UUID.randomUUID().toString(),
                title = "Mi documento",
                creationDate = LocalDate.of(2025, 4, 1),
                modificationDate = LocalDate.of(2025, 4, 1),
                documentDetails = emptyList(),
                documentOrganizationIds = emptyList(),
                documentNote = "",
                documentPileIds = emptyList()
            ),
            DocumentModel(
                id = UUID.randomUUID().toString(),
                title = "Mi documento",
                creationDate = LocalDate.of(2025, 3, 1),
                modificationDate = LocalDate.of(2025, 3, 1),
                documentDetails = emptyList(),
                documentOrganizationIds = emptyList(),
                documentNote = "",
                documentPileIds = emptyList()
            ),
            DocumentModel(
                id = UUID.randomUUID().toString(),
                title = "Mi documento",
                creationDate = LocalDate.of(2025, 3, 1),
                modificationDate = LocalDate.of(2025, 3, 1),
                documentDetails = emptyList(),
                documentOrganizationIds = emptyList(),
                documentNote = "",
                documentPileIds = emptyList()
            ),
            DocumentModel(
                id = UUID.randomUUID().toString(),
                title = "Mi documento",
                creationDate = LocalDate.of(2025, 1, 1),
                modificationDate = LocalDate.of(2025, 1, 1),
                documentDetails = emptyList(),
                documentOrganizationIds = emptyList(),
                documentNote = "",
                documentPileIds = emptyList()
            ),
        )
    }

    fun updatePileName(newPileName: String) {
        viewModelScope.launch {
            val newPile = uiState.value.pile!!.copy(name = newPileName)

            pileModelRepository.updatePileModel(newPile)

            _uiState.value = _uiState.value.copy(pile = newPile)
        }
    }

    fun deletePile() {
        if (uiState.value.pile == null) return

        viewModelScope.launch {
            pileModelRepository.deletePileModel(uiState.value.pile!!.id)
        }
    }
}