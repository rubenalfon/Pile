package com.ganadoro.pile.ui.screens.home

import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.R
import com.ganadoro.pile.models.DocumentModel
import com.ganadoro.pile.ui.compostables.DocumentGrid
import com.ganadoro.pile.ui.screens.home.compostables.DocumentsDivider
import com.ganadoro.pile.ui.screens.home.compostables.HomeScreenSectionTitle
import com.ganadoro.pile.ui.screens.home.compostables.PileGrid
import com.ganadoro.pile.ui.screens.home.compostables.SearchBar
import io.github.aakira.napier.Napier
import org.koin.androidx.compose.getViewModel
import java.time.LocalDate
import java.util.UUID

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateToEditPiles: (id: UUID) -> Unit
) {
    val viewModel = getViewModel<HomeViewModel>()

    val uiState by viewModel.uiState.collectAsState()

    val listState = rememberLazyListState() // TODO: Send to viewmodel

    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        floatingActionButtonPosition = FabPosition.EndOverlay,
        floatingActionButton = {
            FabMenu(
                fabMenuExpanded = fabMenuExpanded,
                updateFabMenuExpanded = { fabMenuExpanded = it }
            )
        },
        topBar = {
            SearchBar(
                Modifier
                    .padding(horizontal = 16.dp)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .pointerInteropFilter {
                    when (it.action) {
                        MotionEvent.ACTION_DOWN -> {
                            fabMenuExpanded = false

                        }
                    }
                    false // IMPORTANTE: devuelve false para no consumirlo
                },
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
                    pileModels = uiState.pileModels,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onPileClick = navigateToEditPiles,
                    onNewPileClick = { Napier.d("New pile clicked") }
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
                    documentModels = uiState.documentModels,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                )
            }
            item {
                Box(
                    Modifier
                        .height(52.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FabMenu(
    modifier: Modifier = Modifier,
    fabMenuExpanded: Boolean,
    updateFabMenuExpanded: (Boolean) -> Unit = {}
) {
    val items: List<Pair<Painter, String>> =
        listOf(
            painterResource(R.drawable.ic_clip) to stringResource(R.string.import_pdf_file),
            rememberVectorPainter(Icons.Filled.Photo) to stringResource(R.string.import_from_gallery),
            rememberVectorPainter(Icons.Filled.CameraAlt) to stringResource(R.string.take_a_photo)
        )

    BackHandler(fabMenuExpanded) { updateFabMenuExpanded(false) }

    val expanded = stringResource(R.string.expanded)
    val collapsed = stringResource(R.string.collapsed)
    val toggleMenu = stringResource(R.string.toggle_menu)
    val closeMenu = stringResource(R.string.close_menu)

    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = fabMenuExpanded,
        button = {
            ToggleFloatingActionButton(
                modifier =
                    Modifier
                        .semantics {
                            traversalIndex = -1f
                            stateDescription = if (fabMenuExpanded) expanded else collapsed
                            contentDescription = toggleMenu
                        },
                checked = fabMenuExpanded,
                onCheckedChange = { updateFabMenuExpanded(it) }
            ) {
                val imageVector by remember {
                    derivedStateOf {
                        if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                    }
                }
                Icon(
                    painter = rememberVectorPainter(imageVector),
                    contentDescription = null,
                    modifier = Modifier.animateIcon({ checkedProgress })
                )
            }
        }
    ) {
        items.forEachIndexed { i, item ->
            FloatingActionButtonMenuItem(
                modifier =
                    Modifier.semantics {
                        isTraversalGroup = true
                        if (i == items.size - 1) {
                            customActions =
                                listOf(
                                    CustomAccessibilityAction(
                                        label = closeMenu,
                                        action = {
                                            updateFabMenuExpanded(false)
                                            true
                                        }
                                    )
                                )
                        }
                    },
                onClick = { updateFabMenuExpanded(false) },
                icon = { Icon(item.first, contentDescription = null) },
                text = { Text(text = item.second) },
            )
        }
    }

}

@Composable
fun DocumentsCompleteList(
    modifier: Modifier = Modifier,
    documentModels: List<DocumentModel>
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
            documentModels = documentModels
        )
    }
}