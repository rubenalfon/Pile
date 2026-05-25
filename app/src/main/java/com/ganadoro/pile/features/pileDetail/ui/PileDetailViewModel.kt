package com.ganadoro.pile.features.pileDetail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.core.domain.models.DocumentCoverItem
import com.ganadoro.pile.core.domain.repositories.BitmapCacheRepository
import com.ganadoro.pile.core.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.core.domain.repositories.PileModelRepository
import com.ganadoro.pile.core.domain.useCases.RequestBitmapLoadUseCase
import com.ganadoro.pile.features.pileDetail.domain.usecases.DeletePileUseCase
import com.ganadoro.pile.features.pileDetail.domain.usecases.UpdatePileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PileDetailViewModel(
    private val pileId: String,
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase,
    private val updatePileUseCase: UpdatePileUseCase,
    private val deletePileUseCase: DeletePileUseCase,
    private val pileModelRepository: PileModelRepository,
    private val documentModelRepository: DocumentModelRepository,
    private val bitmapCacheRepository: BitmapCacheRepository
) : ViewModel() {
    private val _state = MutableStateFlow(PileDetailState())
    val state: StateFlow<PileDetailState> = _state.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    init {
        viewModelScope.launch {
            val pileFlow = pileModelRepository.getPileModelById(pileId)
            val documentFlow = documentModelRepository.getDocumentModelsByPileId(pileId)

            pileFlow.combine(documentFlow) { pile, documents ->

                val documentCoverItems = documents.mapIndexed { index, documentModel ->
                    DocumentCoverItem(
                        document = documentModel,
                        coverImageCacheKey = bitmapCacheRepository.getImageKey(documentModel, index)
                    )
                }

                _state.update { it.copy(pile = pile, documentCoverItems = documentCoverItems) }
            }.collect()
        }
    }

    fun handleEvent(event: PileDetailEvent) {
        when (event) {
            is PileDetailEvent.OnImageDisplayed -> requestBitmapLoad(event.document)
            PileDetailEvent.OnDeletePile -> deletePile()
            is PileDetailEvent.OnPileChange -> updatePile(event.name, event.iconId, event.color)
        }
    }

    private fun requestBitmapLoad(document: DocumentModel) {
        viewModelScope.launch {
            requestBitmapLoadUseCase(document, 0)
        }
    }

    private fun updatePile(name: String, iconId: String, color: Long) {
        val pile = state.value.pile ?: return

        viewModelScope.launch {
            updatePileUseCase(id = pile.id, name = name, iconId = iconId, color = color)
        }
    }

    private fun deletePile() {
        val pile = state.value.pile ?: return

        viewModelScope.launch {
            deletePileUseCase(pile.id)
        }
    }
}