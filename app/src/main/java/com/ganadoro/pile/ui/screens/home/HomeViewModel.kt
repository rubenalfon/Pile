package com.ganadoro.pile.ui.screens.home

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CircleNotifications
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Work
import androidx.lifecycle.ViewModel
import com.ganadoro.pile.models.DocumentModel
import com.ganadoro.pile.models.PileModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    var pileModels: List<PileModel> = emptyList(),
    var documentModels: List<DocumentModel> = emptyList()
)

class HomeViewModel(
    context: Context
) : ViewModel() {
    private var _uiState = MutableStateFlow(HomeUiState())
    var uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        Napier.d { "HomeViewModel init" }
        _uiState.value.pileModels = listOf(
            PileModel(name = "Home", icon = Icons.Default.Home, colorNumber = 0),
            PileModel(name = "Work", icon = Icons.Default.AddRoad, colorNumber = 1),
            PileModel(name = "Church", icon = Icons.Default.CircleNotifications, colorNumber = 23),
            PileModel(name = "Legal", icon = Icons.Default.Work, colorNumber = 7),
            PileModel(name = "Vehicles", icon = Icons.Default.DirectionsCar, colorNumber = 29),
            PileModel(name = "Nightime", icon = Icons.Default.Bedtime)

        )

        _uiState.value.documentModels = listOf(
            DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                documentRoute = ""
            ),
            DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                documentRoute = ""
            ), DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                documentRoute = ""
            ),
            DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                documentRoute = ""
            ), DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                documentRoute = ""
            ),
            DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                documentRoute = ""
            ),
            DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                documentRoute = ""
            )
        )

    }
}