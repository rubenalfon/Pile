package com.ganadoro.pile.models

import java.time.LocalDate
import java.util.UUID

data class DocumentModel(
    val id: UUID,
    var title: String,
    val date: LocalDate = LocalDate.now(),
    var documentRoute: String
)