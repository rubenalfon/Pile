package com.ganadoro.pile.features.pileDetail.ui

import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.core.domain.models.DocumentCoverItem


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