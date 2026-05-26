package com.pile.core.domain.models

import com.pile.DocumentImage

data class ImageItem (
    val image: DocumentImage,
    val cacheKey: String
)