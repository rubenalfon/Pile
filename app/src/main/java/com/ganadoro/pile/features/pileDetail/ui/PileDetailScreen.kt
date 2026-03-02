package com.ganadoro.pile.features.pileDetail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ganadoro.pile.R
import com.ganadoro.pile.core.ui.composables.AlertEditPile
import com.ganadoro.pile.core.ui.composables.LoadingWrapper
import com.ganadoro.pile.core.ui.composables.itemDocumentsCompleteList
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PileDetailScreen(
    modifier: Modifier = Modifier,
    pileId: String,
    navigateToDocumentDetail: (documentId: String) -> Unit,
    navigateToSearchScreen: () -> Unit,
    popBackStack: () -> Unit,
    viewModel: PileDetailViewModel = koinViewModel { parametersOf(pileId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bitmapCache by viewModel.bitmapCache.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var isUpdatePileExpanded by rememberSaveable { mutableStateOf(false) }
    var isDeletePileExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets.displayCutout,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                pileName = uiState.pile?.name ?: "",
                popBackStack = popBackStack,
                onSearchClick = navigateToSearchScreen,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            ToolBar(
                modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues()),
                onPileUpdateClicked = { isUpdatePileExpanded = true },
                onPileDeleteClicked = { isDeletePileExpanded = true }
            )
        }) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            var availableWidth by remember { mutableStateOf(0.dp) }
            val density = LocalDensity.current

            LoadingWrapper(uiState.pile == null || uiState.documentList == null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .onGloballyPositioned { coordinates ->
                            val widthPx = coordinates.size.width
                            availableWidth = with(density) { widthPx.toDp() }.value.dp
                        }
                ) {
                    itemDocumentsCompleteList(
                        availableWidth = availableWidth,
                        documents = uiState.documentList!!,
                        onDocumentClick = { documentId ->
                            navigateToDocumentDetail(documentId)
                        },
                        bitmapCache = bitmapCache,
                        onLoadBitmap = viewModel::requestBitmapLoad,
                        onRequestImageKey = viewModel::requestImageKey
                    )
                    item {
                        Spacer(Modifier.height(100.dp))
                    }
                }
            }
        }

        if (isUpdatePileExpanded) {
            AlertEditPile(
                pileModel = uiState.pile!!,
                onDismiss = { isUpdatePileExpanded = false },
                onConfirm = { pileName, pileIconId, colorNumber ->
                    isUpdatePileExpanded = false
                    viewModel.updatePile(pileName, pileIconId, colorNumber)
                }
            )
        }

        if (isDeletePileExpanded) {
            AlertDeletePile(
                onDismiss = { isDeletePileExpanded = false },
                onConfirm = {
                    isDeletePileExpanded = false
                    viewModel.deletePile()
                    popBackStack()
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ToolBar(
    modifier: Modifier = Modifier,
    onPileUpdateClicked: () -> Unit,
    onPileDeleteClicked: () -> Unit
) {
    HorizontalFloatingToolbar(
        modifier = modifier,
        expanded = true,
        content = {
            IconButton(onClick = onPileUpdateClicked) {
                Icon(
                    painter = painterResource(R.drawable.edit_24px),
                    contentDescription = stringResource(R.string.edit_pile)
                )
            }
            IconButton(onClick = onPileDeleteClicked) {
                Icon(
                    painter = painterResource(R.drawable.delete_24px),
                    contentDescription = stringResource(R.string.delete_pile)
                )
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
private fun TopAppBar(
    modifier: Modifier = Modifier,
    pileName: String,
    popBackStack: () -> Unit,
    onSearchClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    LargeFlexibleTopAppBar(
        modifier = modifier,
        title = {
            Text(
                pileName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        titleHorizontalAlignment = Alignment.Start,
        navigationIcon = {
            IconButton(onClick = popBackStack) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_24px),
                    contentDescription = stringResource(R.string.return_)
                )
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.search)
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun AlertDeletePile(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(
                painter = painterResource(R.drawable.warning_24px),
                contentDescription = null
            )
        },
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_pile_alert_title)) },

        text = {
            Text(stringResource(R.string.delete_pile_alert_body))
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}