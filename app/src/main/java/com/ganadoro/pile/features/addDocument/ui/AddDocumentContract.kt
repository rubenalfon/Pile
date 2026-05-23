package com.ganadoro.pile.features.addDocument.ui

import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel

data class AddDocumentState(
    val documentModel: DocumentModel? = null,
    val coverDocumentImage: DocumentImage? = null,
    val coverImageCacheKey: String = "",
    val documentName: String = "",
    val allPileModels: List<PileModel>? = null,
    val noDocumentNameError: Boolean = false
)

sealed interface AddDocumentEvent {
    data object OnImageVisible : AddDocumentEvent
    data class OnNameChanged(val name: String) : AddDocumentEvent
    data class OnAddPile(val pileName: String) : AddDocumentEvent
    data class OnCreateNewPile(val pileName: String, val iconId: String, val color: Long) : AddDocumentEvent
    data object OnSaveDocument : AddDocumentEvent
}