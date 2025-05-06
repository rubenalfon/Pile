package com.ganadoro.pile.ui.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.ganadoro.pile.ui.compostables.Pile
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    var piles: List<Pile> = emptyList(),
//    val pileFiles: List<PileFile> = emptyList()
)

class HomeViewModel(

): ViewModel() {
    private var _uiState = MutableStateFlow(HomeUiState())
    var uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        Napier.d { "HomeViewModel init" }
        _uiState.value.piles = listOf(
            Pile(name = "Mis Pilas 1", icon = Icons.Default.Add, color = Color.Red),
            Pile(name = "Mis Pilas 2", icon = Icons.Default.AddRoad),
            Pile(name = "Mis Pilas2")
        )

    }
}