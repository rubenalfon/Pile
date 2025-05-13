package com.ganadoro.pile.ui.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CircleNotifications
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.ganadoro.pile.models.Document
import com.ganadoro.pile.ui.compostables.Pile
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    var piles: List<Pile> = emptyList(),
    var documents: List<Document> = emptyList()
)

class HomeViewModel(

) : ViewModel() {
    private var _uiState = MutableStateFlow(HomeUiState())
    var uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        Napier.d { "HomeViewModel init" }
        _uiState.value.piles = listOf(
            Pile(name = "Home", icon = Icons.Default.Home, color = Color.Blue),
            Pile(name = "Work", icon = Icons.Default.AddRoad),
            Pile(name = "Church", icon = Icons.Default.CircleNotifications, color = Color.Red),
            Pile(name = "Nightime", icon = Icons.Default.Bedtime),
        )

        _uiState.value.documents = listOf(
            Document(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                documentRoute = ""
            ),
            Document(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                documentRoute = ""
            ),
            Document(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                documentRoute = ""
            )
        )

    }
}