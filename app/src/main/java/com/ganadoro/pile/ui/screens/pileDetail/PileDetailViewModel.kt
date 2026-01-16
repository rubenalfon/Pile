package com.ganadoro.pile.ui.screens.pileDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.domain.usecase.RequestBitmapLoadUseCase
import com.ganadoro.pile.repositories.BitmapCacheRepository
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.PileModelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PileDetailUiState(
    val pile: PileModel? = null,
    val documentList: List<DocumentModel>? = null
)

class PileDetailViewModel(
    private val pileId: String,
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase,
    private val pileModelRepository: PileModelRepository,
    private val documentModelRepository: DocumentModelRepository,
    private val bitmapCacheRepository: BitmapCacheRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PileDetailUiState())
    val uiState: StateFlow<PileDetailUiState> = _uiState.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    init {
        viewModelScope.launch {
            val pileFlow = pileModelRepository.getPileModelById(pileId)
            val documentFlow = documentModelRepository.getDocumentModelsByPileId(pileId)

            pileFlow.combine(documentFlow) { pile, documents ->
                pile to documents
            }.collect { (pile, documents) ->
                _uiState.update {
                    it.copy(
                        pile = pile,
                        documentList = documents
                    )
                }
            }
        }
    }

    fun requestBitmapLoad(document: DocumentModel, pageNumber: Int) {
        viewModelScope.launch {
            requestBitmapLoadUseCase(document, pageNumber)
        }
    }

    fun requestImageKey(document: DocumentModel, pageNumber: Int): String =
        bitmapCacheRepository.getImageKey(document, pageNumber)

    fun updatePile(newPileName: String, newPileIconId: String, newPileColor: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val pile = uiState.value.pile ?: return@launch

            val newPile = pile.copy(
                name = newPileName,
                iconId = newPileIconId,
                colorNumber = newPileColor
            )

            pileModelRepository.updatePileModel(newPile)
        }
    }

    fun deletePile() {
        if (uiState.value.pile == null) return

        viewModelScope.launch(Dispatchers.IO) {
            pileModelRepository.deletePileModel(uiState.value.pile!!.id)
        }
    }
}