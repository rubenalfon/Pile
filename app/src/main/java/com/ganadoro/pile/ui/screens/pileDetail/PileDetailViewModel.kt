package com.ganadoro.pile.ui.screens.pileDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.repositories.BitmapCacheRepository
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.PileModelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PileDetailUiState(
    var pile: PileModel? = null,
    var documentList: List<DocumentModel>? = null
)

class PileDetailViewModel(
    private val pileModelRepository: PileModelRepository,
    private val documentModelRepository: DocumentModelRepository,
    private val bitmapCacheRepository: BitmapCacheRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PileDetailUiState())
    val uiState: StateFlow<PileDetailUiState> = _uiState.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    fun loadPile(pileId: String) {
        viewModelScope.launch {
            val pile = pileModelRepository.getPileModelById(pileId) ?: return@launch

            val documentsFlow = documentModelRepository.getDocumentModelsByPileId(pileId)

            documentsFlow.collect { documents ->
                _uiState.update {
                    it.copy(
                        pile = pile,
                        documentList = documents
                    )
                }
            }
        }
    }

    fun requestBitmapLoad(documentId: String, imageId: String) {
        bitmapCacheRepository.ensureBitmapIsLoaded(documentId, imageId)
    }

    fun updatePileName(newPileName: String, newPileIconId: String, newPileColor: Long) {
        viewModelScope.launch {
            val newPile = uiState.value.pile!!.copy(name = newPileName, iconId = newPileIconId, colorNumber = newPileColor)

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