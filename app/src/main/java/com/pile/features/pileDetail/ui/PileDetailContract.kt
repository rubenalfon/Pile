package com.pile.features.pileDetail.ui

import com.pile.DocumentModel
import com.pile.PileModel
import com.pile.core.domain.models.DocumentCoverItem


data class PileDetailState(
    val pile: PileModel? = null,
    val documentCoverItems: List<DocumentCoverItem> = emptyList()
)

sealed interface PileDetailEvent {
    data class OnImageDisplayed(val document: DocumentModel) : PileDetailEvent
    data class OnPileChange(val name: String, val iconId: String, val color: Long) :
        PileDetailEvent

    data object OnDeletePile : PileDetailEvent
}