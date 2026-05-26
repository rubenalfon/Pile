package com.ganadoro.pile.core.domain.models

import com.ganadoro.pile.DocumentImage

data class ImageItem (
    val image: DocumentImage,
    val cacheKey: String
)