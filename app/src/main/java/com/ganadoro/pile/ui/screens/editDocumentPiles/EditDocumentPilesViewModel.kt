package com.ganadoro.pile.ui.screens.editDocumentPiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.PileModelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class EditDocumentPilesUiState(
    var documentModel: DocumentModel? = null,
    var allPileModels: List<PileModel>? = null,
    var selectedPileModelIds: List<String>? = null
)

class EditDocumentPilesViewModel(
    private val documentModelRepository: DocumentModelRepository,
    private val pileModelRepository: PileModelRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(EditDocumentPilesUiState())
    var uiState: StateFlow<EditDocumentPilesUiState> = _uiState.asStateFlow()

    fun customInnit(documentId: String) {
        viewModelScope.launch {
            val documentModel =
                documentModelRepository.getDocumentModelById(documentId).first() ?: return@launch

            val allPilesDeferred = async {
                pileModelRepository.pileModels.first()
            }

            _uiState.update {
                it.copy(
                    documentModel = documentModel,
                    allPileModels = allPilesDeferred.await(),
                    selectedPileModelIds = documentModel.documentPileIds
                )
            }
        }
    }

    fun updatePileSelectState(pileId: String) {
        val piles = _uiState.value.selectedPileModelIds?.toMutableList() ?: return

        if (piles.contains(pileId)) {
            piles.remove(pileId)
        } else {
            piles.add(pileId)
        }

        _uiState.update { it.copy(selectedPileModelIds = piles) }

        viewModelScope.launch(Dispatchers.IO) {
            val updatedDocumentModel = _uiState.value.documentModel?.copy(
                documentPileIds = _uiState.value.selectedPileModelIds ?: return@launch
            ) ?: return@launch

            documentModelRepository.updateDocumentModel(updatedDocumentModel)
        }
    }

    fun addPile(pileName: String, iconId: String, color: Long) {
        viewModelScope.launch {
            val newPile = PileModel(
                id = UUID.randomUUID().toString(),
                name = pileName,
                iconId = iconId,
                colorNumber = color
            )

            pileModelRepository.insertPileModel(newPile)
        }
    }
}