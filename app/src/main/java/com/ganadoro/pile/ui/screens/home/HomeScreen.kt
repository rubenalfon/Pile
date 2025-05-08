package com.ganadoro.pile.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.ganadoro.pile.models.Document
import com.ganadoro.pile.ui.compostables.DocumentGrid
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

    Box(
        modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.BottomEnd
    ) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .semantics { isTraversalGroup = true }
        ) {
            item {
                SearchBar(
                    Modifier
                        .semantics { traversalIndex = 0f }
                        .padding(horizontal = 16.dp)
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
            item {
                HomeScreenSectionTitle(
                    title = stringResource(R.string.your_piles),
                    trailingButtonText = stringResource(R.string.edit),
                    trailingButtonOnClick = { Napier.d { "Clicked on edit your piles" } },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item {
//                PileGrid(
//                    piles = uiState.piles,
//                    modifier = Modifier.padding(horizontal = 8.dp)
//                )
            }
            item { Spacer(Modifier.height(30.dp)) }
            item {
                HomeScreenSectionTitle(
                    title = stringResource(R.string.all_documents),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
            item {
//                DocumentsCompleteList(documents = uiState.documents)
            }
            item { Spacer(Modifier.height(52.dp)) }
        }

        Fab()
    }

}

@Composable
private fun Fab(modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = { Napier.d { "Clicked on add document" } },
        modifier = modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(Modifier.padding(horizontal = 4.dp))
            Text(
                stringResource(R.string.add_document),
                modifier = Modifier.padding(end = 4.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun DocumentsCompleteList(
    modifier: Modifier = Modifier,
    documents: List<Document>
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DocumentsDivider(
            date = LocalDate.now(),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        DocumentGrid(
            modifier = Modifier.padding(horizontal = 16.dp),
            documents = documents
        )
    }
}