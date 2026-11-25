package es.pile.features.home.ui

import android.graphics.Bitmap
import android.view.MotionEvent
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pile.R
import es.pile.core.domain.models.SyncState
import es.pile.core.ui.components.ImportFabMenu
import es.pile.core.ui.composables.AlertDraftDocumentWarning
import es.pile.core.ui.composables.AlertNewPile
import es.pile.core.ui.composables.LoadingAlert
import es.pile.core.ui.composables.LoadingWrapper
import es.pile.core.ui.composables.SwipeBox
import es.pile.core.ui.composables.itemDocumentsCompleteList
import es.pile.core.ui.composables.itemPileGrid
import es.pile.core.ui.controllers.rememberDocumentImportController
import es.pile.core.ui.theme.PileTheme
import es.pile.features.home.ui.compostables.HomeScreenSectionTitle
import es.pile.features.search.ui.SearchContent
import es.pile.features.search.ui.SearchEvent
import es.pile.features.search.ui.SearchState
import es.pile.features.search.ui.SearchViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateToPileDetail: (pileId: String) -> Unit,
    navigateToDocumentDetail: (documentId: String) -> Unit,
    navigateToEditDocument: (documentId: String) -> Unit,
    navigateToAddDocument: (documentId: String) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToBackup: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
    searchViewModel: SearchViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bitmapCache by viewModel.bitmapCache.collectAsStateWithLifecycle()

    val searchState by searchViewModel.state.collectAsStateWithLifecycle()
    val searchBitmapCache by searchViewModel.bitmapCache.collectAsStateWithLifecycle()

    var isNavigating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { document ->
            isNavigating = true
            if (document.isIncomingPdf) navigateToAddDocument(document.id)
            else navigateToEditDocument(document.id)
        }
    }

    HomeScreenContent(
        modifier = modifier,
        state = state,
        bitmapCache = bitmapCache,
        searchState = searchState,
        searchBitmapCache = searchBitmapCache,
        isNavigating = isNavigating,
        onEvent = { event ->
            when (event) {
                is HomeEvent.OnNavigateToPileDetail -> navigateToPileDetail(event.pileId)
                is HomeEvent.OnNavigateToDocumentDetail -> navigateToDocumentDetail(event.documentId)
                is HomeEvent.OnNavigateToEditDocument -> navigateToEditDocument(event.documentId)
                is HomeEvent.OnNavigateToAddDocument -> navigateToAddDocument(event.documentId)
                is HomeEvent.OnNavigateToSettings -> navigateToSettings()
                is HomeEvent.OnNavigateToBackup -> navigateToBackup()
                else -> viewModel.handleEvent(event)
            }
        },
        onSearchEvent = { searchViewModel.handleEvent(it) }
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PileTheme {
        HomeScreenContent(
            state = HomeState(isInitialLoading = false),
            bitmapCache = emptyMap(),
            searchState = SearchState(),
            searchBitmapCache = emptyMap(),
            isNavigating = false,
            onEvent = {},
            onSearchEvent = {}
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    state: HomeState,
    bitmapCache: Map<String, Bitmap>,
    searchState: SearchState,
    searchBitmapCache: Map<String, Bitmap>,
    isNavigating: Boolean,
    onEvent: (HomeEvent) -> Unit,
    onSearchEvent: (SearchEvent) -> Unit
) {
    val listState = rememberLazyListState()

    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isNewPileAlertExpanded by rememberSaveable { mutableStateOf(false) }

    val importActions = rememberDocumentImportController(
        cameraUri = state.cameraUri,
        onUriConsumed = { onEvent(HomeEvent.OnCameraUriConsumed) },
        onPdfSelected = { onEvent(HomeEvent.OnPdfImported(it)) },
        onImagesSelected = { onEvent(HomeEvent.OnImagesImported(it)) },
        onCameraClick = { onEvent(HomeEvent.OnCameraClick) }
    )

    var isSearchBarExpanded by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarStrings = Pair(
        stringResource(R.string.document_unsaved_changes_deleted),
        stringResource(R.string.undo)
    )

    val context = LocalContext.current

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { uiText ->
            snackbarHostState.showSnackbar(
                message = uiText.asString(context),
                duration = SnackbarDuration.Short
            )
            onEvent(HomeEvent.OnErrorDismissed)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Horizontal),
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isSearchBarExpanded,
                enter = fadeIn(), exit = fadeOut()
            ) {
                ImportFabMenu(
                    modifier = Modifier.padding(
                        WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
                    ),
                    fabMenuExpanded = fabMenuExpanded,
                    updateFabMenuExpanded = { fabMenuExpanded = it },
                    importActions = importActions
                )
            }
        },
        topBar = {
            val bottomPaddingAnimated by animateDpAsState(
                targetValue = if (isSearchBarExpanded) 0.dp else 8.dp,
                label = "bottomPadding"
            )

            val layoutDirection = LocalLayoutDirection.current

            val horizontalInsets = WindowInsets.safeDrawing
                .only(WindowInsetsSides.Horizontal)
                .asPaddingValues()

            val animatedStartPadding by animateDpAsState(
                targetValue = if (isSearchBarExpanded) 0.dp
                else 16.dp + horizontalInsets.calculateStartPadding(layoutDirection),
                label = "startPadding"
            )

            val animatedEndPadding by animateDpAsState(
                targetValue = if (isSearchBarExpanded) 0.dp
                else 16.dp + horizontalInsets.calculateEndPadding(layoutDirection),
                label = "endPadding"
            )

            SearchContent(
                modifier = Modifier
                    .padding(
                        start = animatedStartPadding,
                        end = animatedEndPadding,
                        bottom = bottomPaddingAnimated
                    ),
                state = searchState,
                bitmapCache = searchBitmapCache,
                expanded = isSearchBarExpanded,
                onExpandedChange = { isSearchBarExpanded = it },
                onSettingsClick = { onEvent(HomeEvent.OnNavigateToSettings) },
                onSyncClick = { onEvent(HomeEvent.OnNavigateToBackup) },
                syncState = state.syncState,
                onEvent = onSearchEvent,
                navigateToDocumentDetail = { onEvent(HomeEvent.OnNavigateToDocumentDetail(it)) }
            )

        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        val backgroundDocuments = MaterialTheme.colorScheme.surface

        val layoutDirection = LocalLayoutDirection.current


        LoadingWrapper(state.isInitialLoading) {
            BoxWithConstraints(
                Modifier
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        end = innerPadding.calculateEndPadding(layoutDirection)
                    )
                    .fillMaxSize()
                    .background(backgroundDocuments)
                    .pointerInteropFilter {
                        when (it.action) {
                            MotionEvent.ACTION_DOWN -> {
                                fabMenuExpanded = false
                            }
                        }
                        false
                    }
            ) {
                val availableWidth = maxWidth
                val isRefreshing = state.isManualRefreshing

                val pullToRefreshState = rememberPullToRefreshState()
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { onEvent(HomeEvent.OnRefreshSync) },
                    modifier = Modifier.fillMaxSize(),
                    state = pullToRefreshState,
                    enabled = state.syncState != SyncState.NoProvider,
                    indicator = {
                        PullToRefreshDefaults.LoadingIndicator(
                            state = pullToRefreshState,
                            isRefreshing = isRefreshing,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                ) {
                    LazyColumn(
                        Modifier
                            .padding(bottom = innerPadding.calculateBottomPadding())
                            .background(MaterialTheme.colorScheme.surfaceContainer)
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
                            val tempDocument = state.temporaryDocument
                            AnimatedVisibility(
                                visible = tempDocument != null,
                                enter = fadeIn(tween(100)) + expandVertically(),
                                exit = fadeOut(tween(100)) + shrinkVertically()
                            ) {
                                UnsavedDocumentCard(
                                    onNavigateUnsavedDocument = {
                                        if (tempDocument == null) return@UnsavedDocumentCard

                                        if (tempDocument.isIncomingPdf)
                                            onEvent(HomeEvent.OnNavigateToAddDocument(tempDocument.id))
                                        else
                                            onEvent(HomeEvent.OnNavigateToEditDocument(tempDocument.id))
                                    },
                                    onDismiss = {
                                        onEvent(HomeEvent.OnRemoveDraftDocument)

                                        scope.launch {
                                            val result = snackbarHostState
                                                .showSnackbar(
                                                    message = snackbarStrings.first,
                                                    actionLabel = snackbarStrings.second,
                                                    duration = SnackbarDuration.Long
                                                )
                                            when (result) {
                                                SnackbarResult.ActionPerformed -> { // restore
                                                    onEvent(HomeEvent.OnRestoreDraftDocument)
                                                }

                                                SnackbarResult.Dismissed -> {
                                                    onEvent(HomeEvent.OnPurgeDraftDocument)
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
                            piles = state.pileModels,
                            onPileClick = { onEvent(HomeEvent.OnNavigateToPileDetail(it)) },
                            onNewPileClick = { isNewPileAlertExpanded = true },
                            coloredPileIds = state.coloredPileIds
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
                                    .background(backgroundDocuments)
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
                        val showEmptyDocuments = state.documentCoverItems.isEmpty()

                        if (showEmptyDocuments) {
                            item {
                                HomeEmptyState(
                                    icon = painterResource(R.drawable.ic_clip),
                                    text = stringResource(R.string.no_documents_home),
                                    modifier = Modifier.background(backgroundDocuments)
                                )
                            }
                        } else {
                            itemDocumentsCompleteList(
                                availableWidth = availableWidth,
                                backgroundColor = backgroundDocuments,
                                documents = state.documentCoverItems,
                                onDocumentClick = { onEvent(HomeEvent.OnNavigateToDocumentDetail(it)) },
                                bitmapCache = bitmapCache,
                                onLoadBitmap = { onEvent(HomeEvent.OnImageDisplayed(it)) }
                            )
                        }

                        item {
                            Box(
                                Modifier
                                    .height(100.dp)
                                    .fillMaxWidth()
                                    .background(backgroundDocuments)
                            )
                        }
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
                onEvent(HomeEvent.OnCreatePile(pileName, pileIconId, pileColorNumber))
            }
        )
    }

    if (state.showDraftWarning) {
        val tempDocument = state.temporaryDocument
        AlertDraftDocumentWarning(
            onDismiss = { onEvent(HomeEvent.OnDismissDraftWarning) },
            onDiscardAndContinue = {
                onEvent(HomeEvent.OnConfirmImport)
            },
            onNavigateToDraft = {
                onEvent(HomeEvent.OnDismissDraftWarning)
                if (tempDocument != null) {
                    if (tempDocument.isIncomingPdf)
                        onEvent(HomeEvent.OnNavigateToAddDocument(tempDocument.id))
                    else
                        onEvent(HomeEvent.OnNavigateToEditDocument(tempDocument.id))
                }
            }
        )
    }

    if (state.isLoadingNewDocument || isNavigating) {
        LoadingAlert(stringResource(R.string.loading_new_document))
    }
}

@Composable
private fun HomeEmptyState(
    icon: Painter,
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
