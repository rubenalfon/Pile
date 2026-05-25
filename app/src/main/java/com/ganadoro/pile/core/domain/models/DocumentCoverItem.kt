package com.ganadoro.pile.core.domain.models

import com.ganadoro.pile.DocumentModel


data class DocumentCoverItem(
    val document: DocumentModel,
    val coverImageCacheKey: String
)