package es.pile.features.pileDetail.ui

import es.pile.DocumentModel
import es.pile.PileModel
import es.pile.core.domain.models.DocumentCoverItem


data class PileDetailState(
    val pile: PileModel? = null,
    val documentCoverItems: List<DocumentCoverItem> = emptyList(),
    val isLoading: Boolean = true
)

sealed interface PileDetailEvent {
    data class OnImageDisplayed(val document: DocumentModel) : PileDetailEvent
    data class OnPileChange(val name: String, val iconId: String, val color: Long) :
        PileDetailEvent

    data object OnDeletePile : PileDetailEvent
}