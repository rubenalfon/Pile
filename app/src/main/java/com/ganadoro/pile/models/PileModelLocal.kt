package com.ganadoro.pile.models

import androidx.compose.ui.graphics.vector.ImageVector
import java.util.UUID

data class PileModelLocal(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val icon: ImageVector,
    val colorNumber: Int? = null // max = 30
)