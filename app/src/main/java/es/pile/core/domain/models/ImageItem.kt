package es.pile.core.domain.models

import es.pile.DocumentImage

data class ImageItem (
    val image: DocumentImage,
    val cacheKey: String
)