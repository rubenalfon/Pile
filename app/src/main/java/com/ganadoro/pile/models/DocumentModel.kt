package com.ganadoro.pile.models

import java.util.UUID

data class DocumentModel(
    val id: UUID,
    var title: String,
    var documentRoute: String
)