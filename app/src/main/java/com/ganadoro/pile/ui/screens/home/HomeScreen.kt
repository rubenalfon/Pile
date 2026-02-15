package com.ganadoro.pile.ui.screens.home

import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ganadoro.pile.R
import com.ganadoro.pile.ui.composables.AlertNewPile
import com.ganadoro.pile.ui.composables.LoadingWrapper
import com.ganadoro.pile.ui.composables.SwipeBox
import com.ganadoro.pile.ui.composables.itemDocumentsCompleteList
import com.ganadoro.pile.ui.composables.itemPileGrid
import com.ganadoro.pile.ui.controllers.ImportActions
import com.ganadoro.pile.ui.controllers.rememberDocumentImportController
import com.ganadoro.pile.ui.screens.home.compostables.HomeScreenSectionTitle
import com.ganadoro.pile.ui.screens.search.SearchBarScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateToPileDetail: (pileId: String) -> Unit,
    navigateToDocumentDetail: (documentId: String) -> Unit,
    navigateToEditPDF: (documentId: String) -> Unit,
    navigateToAddDocument: (documentId: String) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bitmapCache by viewModel.bitmapCache.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { document ->
            if (document.isIncomingPdf) navigateToAddDocument(document.id)
            else navigateToEditPDF(document.id)
        }
    }

    val listState = rememberLazyListState()

    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isNewPileAlertExpanded by rememberSaveable { mutableStateOf(false) }

    val importActions = rememberDocumentImportController(
        onPdfSelected = viewModel::importPDFIntent,
        onImagesSelected = viewModel::importImagesIntent,
        createTempImageUri = viewModel::handleCameraCapture
    )

    var isSearchBarExpanded by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarStrings = Pair(
        stringResource(R.string.document_unsaved_changes_deleted),
        stringResource(R.string.undo)
    )

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.displayCutout,
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            AnimatedVisibility(
                !isSearchBarExpanded,
                enter = fadeIn(), exit = fadeOut()
            ) {
                FabMenuWithController(
                    modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues()),
                    fabMenuExpanded = fabMenuExpanded,
                    updateFabMenuExpanded = { fabMenuExpanded = it },
                    importActions = importActions
                )
            }
        },
        topBar = {
            val horizontalPaddingAnimated by animateDpAsState(
                targetValue = if (isSearchBarExpanded) 0.dp else 16.dp,
            )
            val bottomPaddingAnimated by animateDpAsState(
                targetValue = if (isSearchBarExpanded) 0.dp else 8.dp,
            )
            val displayCutoutStartPaddingAnimated by animateDpAsState(
                targetValue = if (isSearchBarExpanded) 0.dp else WindowInsets.displayCutout.asPaddingValues()
                    .calculateStartPadding(LocalLayoutDirection.current)
            )
            val displayCutoutEndPaddingAnimated by animateDpAsState(
                targetValue = if (isSearchBarExpanded) 0.dp else WindowInsets.displayCutout.asPaddingValues()
                    .calculateEndPadding(LocalLayoutDirection.current)
            )

            SearchBarScreen(
                modifier = Modifier
                    .padding(horizontal = horizontalPaddingAnimated)
                    .padding(bottom = bottomPaddingAnimated)
                    .padding(start = displayCutoutStartPaddingAnimated)
                    .padding(end = displayCutoutEndPaddingAnimated),
                expanded = isSearchBarExpanded,
                onExpandedChange = { isSearchBarExpanded = it },
                onSettingsClick = { /*TODO: Add settings */ },
                navigateToDocumentDetail = navigateToDocumentDetail
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        val documentsColorSection = MaterialTheme.colorScheme.surface

        var availableWidth by remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current

        val layoutDirection = LocalLayoutDirection.current


        LoadingWrapper(
            isLoading = uiState.pileModels == null || uiState.documentList == null || uiState.coloredPileIds == null
        ) {
            Box(
                Modifier
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        end = innerPadding.calculateEndPadding(layoutDirection)
                    )
                    .fillMaxSize()
                    .background(documentsColorSection)
            ) {
                LazyColumn(
                    Modifier
                        .padding(bottom = innerPadding.calculateBottomPadding())
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .onGloballyPositioned { coordinates ->
                            val widthPx = coordinates.size.width
                            availableWidth = with(density) { widthPx.toDp() }.value.dp
                        }
                        .pointerInteropFilter {
                            when (it.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    fabMenuExpanded = false
                                }
                            }
                            false
                        },
                    state = listState
                ) {
                    item { Spacer(Modifier.height(8.dp)) }

                    item {
                        val tempDocument = uiState.temporaryDocument
                        AnimatedVisibility(
                            visible = tempDocument != null,
                            enter = fadeIn(tween(100)) + expandVertically(),
                            exit = fadeOut(tween(100)) + shrinkVertically()
                        ) {
                            UnsavedDocumentCard(
                                onNavigateUnsavedDocument = {
                                    if (tempDocument == null) return@UnsavedDocumentCard

                                    if (tempDocument.isIncomingPdf)
                                        navigateToAddDocument(tempDocument.id)
                                    else
                                        navigateToEditPDF(tempDocument.id)
                                },
                                onDismiss = {
                                    viewModel.partialDeleteUnsavedDocument()

                                    scope.launch {
                                        val result = snackbarHostState
                                            .showSnackbar(
                                                message = snackbarStrings.first,
                                                actionLabel = snackbarStrings.second,
                                                duration = SnackbarDuration.Long
                                            )
                                        when (result) {
                                            SnackbarResult.ActionPerformed -> { // restore
                                                viewModel.restoreUnsavedDeletedDocument()
                                            }

                                            SnackbarResult.Dismissed -> {
                                                viewModel.confirmErasureUnsavedDeletedDocument()
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }

                    item {
                        HomeScreenSectionTitle(
                            title = stringResource(R.string.your_piles),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }

                    itemPileGrid(
                        availableWidth = availableWidth,
                        piles = uiState.pileModels!!,
                        onPileClick = navigateToPileDetail,
                        onNewPileClick = { isNewPileAlertExpanded = true },
                        coloredPileIds = uiState.coloredPileIds!!
                    )

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
                                .background(documentsColorSection)
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

                    itemDocumentsCompleteList(
                        availableWidth = availableWidth,
                        backgroundColor = documentsColorSection,
                        documents = uiState.documentList!!,
                        onDocumentClick = navigateToDocumentDetail,
                        bitmapCache = bitmapCache,
                        onLoadBitmap = viewModel::requestBitmapLoad,
                        onRequestImageKey = viewModel::requestImageKey
                    )

                    item {
                        Box(
                            Modifier
                                .height(100.dp)
                                .fillMaxWidth()
                                .background(documentsColorSection)
                        )
                    }
                }
            }
        }
    }
    if (isNewPileAlertExpanded) {
        AlertNewPile(
            onDismiss = { isNewPileAlertExpanded = false },
            onConfirm = { pileName, pileIconId, pileColorNumber ->
                isNewPileAlertExpanded = false
                viewModel.addPile(pileName = pileName, iconId = pileIconId, color = pileColorNumber)
            }
        )
    }

    if (uiState.isLoadingNewDocument) {
        LoadingNewDocumentAlert()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FabMenuWithController(
    modifier: Modifier = Modifier,
    fabMenuExpanded: Boolean,
    updateFabMenuExpanded: (Boolean) -> Unit = {},
    importActions: ImportActions
) {
    val items = listOf(
        Triple(
            painterResource(R.drawable.ic_clip),
            stringResource(R.string.import_pdf_file),
            importActions.launchPdfPicker
        ),
        Triple(
            rememberVectorPainter(Icons.Filled.Photo),
            stringResource(R.string.import_from_gallery),
            importActions.launchGallery
        ),
        Triple(
            rememberVectorPainter(Icons.Filled.CameraAlt),
            stringResource(R.string.take_a_photo),
            importActions.launchCamera
        )
    )

    BackHandler(fabMenuExpanded) { updateFabMenuExpanded(false) }

    val expandedString = stringResource(R.string.expanded)
    val collapsedString = stringResource(R.string.collapsed)
    val toggleMenuString = stringResource(R.string.toggle_menu)
    val closeMenuString = stringResource(R.string.close_menu)

    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = fabMenuExpanded,
        button = {
            ToggleFloatingActionButton(
                modifier = Modifier
                    .semantics {
                        traversalIndex = -1f
                        stateDescription =
                            if (fabMenuExpanded) expandedString else collapsedString
                        contentDescription = toggleMenuString
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
        items.forEachIndexed { i, (icon, label, action) ->
            FloatingActionButtonMenuItem(
                modifier =
                    Modifier.semantics {
                        isTraversalGroup = true
                        if (i == items.size - 1) {
                            customActions = listOf(
                                CustomAccessibilityAction(
                                    label = closeMenuString,
                                    action = {
                                        action()
                                        updateFabMenuExpanded(false)
                                        true
                                    }
                                )
                            )
                        }
                    },
                onClick = {
                    action()
                    updateFabMenuExpanded(false)
                },
                icon = { Icon(icon, contentDescription = null) },
                text = { Text(text = label) }
            )
        }
    }

}

@Composable
private fun UnsavedDocumentCard(
    modifier: Modifier = Modifier,
    onNavigateUnsavedDocument: () -> Unit,
    onDismiss: () -> Unit = {},
) {
    SwipeBox(
        onDelete = onDismiss,
        modifier = Modifier,
        contentPaddingValues = PaddingValues(horizontal = 16.dp)
    ) {
        Card(
            modifier = modifier,
            onClick = { onNavigateUnsavedDocument() },
            shape = RoundedCornerShape(16.dp),
            colors = CardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    stringResource(R.string.user_document_unsaved_changes),
                    Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingNewDocumentAlert(modifier: Modifier = Modifier) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = {},
        title = { Text(stringResource(R.string.loading_new_document)) },
        text = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(
                    Modifier.size(108.dp)
                )
            }
        },
        confirmButton = { }
    )
}