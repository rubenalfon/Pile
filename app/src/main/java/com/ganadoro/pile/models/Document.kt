package com.ganadoro.pile.models

import java.util.UUID

data class Document(
    val id: UUID,
    var title: String,
    var documentRoute: String
)