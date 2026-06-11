package es.pile.core.domain.models

import es.pile.DocumentModel


data class DocumentCoverItem(
    val document: DocumentModel,
    val coverImageCacheKey: String
)