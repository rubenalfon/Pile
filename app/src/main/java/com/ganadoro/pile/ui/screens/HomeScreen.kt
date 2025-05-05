package com.ganadoro.pile.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun HomeScreenPrev() {
    Surface(modifier = Modifier.fillMaxSize()) {
        HomeScreen()
    }
}


@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column {
        Text("Search bar")
        Text("Tus Pilas")
        Text("Todos los documentos")

    }
}