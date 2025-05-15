package com.ganadoro.pile.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
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

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateToEditPiles: () -> Unit
) {
    val viewModel = getViewModel<HomeViewModel>()

    val uiState by viewModel.uiState.collectAsState()

    val listState = rememberLazyListState()

    val isListOnTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        floatingActionButtonPosition = FabPosition.EndOverlay,
        floatingActionButton = { Fab(isListOnTop = isListOnTop) },
        topBar = {
            SearchBar(
                Modifier
                    .padding(horizontal = 16.dp)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            Modifier.fillMaxSize(),
            state = listState
        ) {
            item { Spacer(Modifier.height(innerPadding.calculateTopPadding())) }
            item { Spacer(Modifier.height(24.dp)) }
            item {
                HomeScreenSectionTitle(
                    title = stringResource(R.string.your_piles),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
            item {
                PileGrid(
                    piles = uiState.piles,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item { Spacer(Modifier.height(30.dp)) }
            item {
                Column(
                    Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 24.dp,
                                topEnd = 24.dp
                            )
                        )
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(top = 16.dp)
                ) {
                    HomeScreenSectionTitle(
                        title = stringResource(R.string.all_documents),
                        modifier = Modifier

                            .padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
            item {
                DocumentsCompleteList(
                    documents = uiState.documents,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                )
            }
            item { Spacer(Modifier.height(52.dp)) }
        }
    }
}

@Composable
private fun Fab(
    modifier: Modifier = Modifier,
    isListOnTop: Boolean
) {
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
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
            )
            AnimatedVisibility(
                visible = isListOnTop,
                enter = fadeIn(tween(durationMillis = 350)) + expandHorizontally(
                    tween(
                        durationMillis = 350
                    )
                ) + slideInHorizontally(tween(durationMillis = 350)) { it },
                exit = fadeOut(tween(durationMillis = 150)) + shrinkHorizontally(
                    tween(
                        durationMillis = 350
                    )
                ) + slideOutHorizontally(tween(durationMillis = 350)) { it },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text(
                        stringResource(R.string.add_document),
                        modifier = Modifier.padding(end = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
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