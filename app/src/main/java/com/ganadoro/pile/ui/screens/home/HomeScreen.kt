package com.ganadoro.pile.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.R
import com.ganadoro.pile.ui.screens.home.compostables.DocumentsDivider
import com.ganadoro.pile.ui.screens.home.compostables.HomeScreenSectionTitle
import com.ganadoro.pile.ui.screens.home.compostables.PileGrid
import com.ganadoro.pile.ui.screens.home.compostables.SearchBar
import io.github.aakira.napier.Napier
import org.koin.androidx.compose.getViewModel
import java.time.LocalDate

@Preview
@Composable
private fun HomeScreenPrev() {
    Surface(modifier = Modifier.fillMaxSize()) {
        HomeScreen()
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val viewModel = getViewModel<HomeViewModel>()

    val uiState by viewModel.uiState.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .semantics { isTraversalGroup = true },
    ) {
        SearchBar(
            Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .semantics { traversalIndex = 0f }
                .padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        HomeScreenSectionTitle(
            title = stringResource(R.string.your_piles),
            trailingButtonText = stringResource(R.string.edit),
            trailingButtonOnClick = { Napier.d { "Clicked on edit your piles" } },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        PileGrid(
            modifier = Modifier.padding(horizontal = 8.dp),
            piles = uiState.piles
        )

        Spacer(Modifier.height(30.dp))

        HomeScreenSectionTitle(
            title = stringResource(R.string.all_documents),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(8.dp))

        DocumentsDivider(
            date = LocalDate.now(),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}