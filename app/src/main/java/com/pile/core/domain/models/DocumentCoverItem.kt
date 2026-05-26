package com.pile.core.domain.models

import com.pile.DocumentModel


data class DocumentCoverItem(
    val document: DocumentModel,
    val coverImageCacheKey: String
)