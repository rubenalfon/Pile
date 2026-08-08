package es.pile.features.pileDetail.ui

import android.graphics.Bitmap
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pile.DocumentModel
import es.pile.PileModel
import es.pile.R
import es.pile.core.domain.models.DocumentCoverItem
import es.pile.core.ui.composables.AlertDraftDocumentWarning
import es.pile.core.ui.composables.AlertEditPile
import es.pile.core.ui.composables.LoadingAlert
import es.pile.core.ui.composables.LoadingWrapper
import es.pile.core.ui.composables.Pile
import es.pile.core.ui.composables.itemDocumentsCompleteList
import es.pile.core.ui.controllers.rememberDocumentImportController
import es.pile.core.ui.theme.PileTheme
import es.pile.features.home.ui.FabMenuWithController
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PileDetailScreen(
    modifier: Modifier = Modifier,
    pileId: String,
    navigateToDocumentDetail: (documentId: String) -> Unit,
    navigateToSearchScreen: (pileId: String) -> Unit,
    navigateToEditDocument: (documentId: String) -> Unit,
    navigateToAddDocument: (documentId: String) -> Unit,
    popBackStack: () -> Unit,
    popToHome: () -> Unit,
    viewModel: PileDetailViewModel = koinViewModel { parametersOf(pileId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bitmapCache by viewModel.bitmapCache.collectAsStateWithLifecycle()

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && state.pile == null) {
            popBackStack()
        }
    }

    var isNavigating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { document ->
            isNavigating = true
            if (document.isIncomingPdf) navigateToAddDocument(document.id)
            else navigateToEditDocument(document.id)
        }
    }

    PileDetailContent(
        modifier = modifier,
        state = state,
        bitmapCache = bitmapCache,
        isNavigating = isNavigating,
        onEvent = { viewModel.handleEvent(it) },
        popBackStack = popBackStack,
        popToHome = popToHome,
        navigateToDocumentDetail = navigateToDocumentDetail,
        navigateToSearchScreen = { navigateToSearchScreen(pileId) },
        navigateToAddDocument = navigateToAddDocument,
        navigateToEditDocument = navigateToEditDocument
    )
}

@Preview
@Composable
private fun PileDetailPrev() {
    PileTheme {
        PileDetailContent(
            state = PileDetailState(
                pile = PileModel(
                    id = "1",
                    name = "Sample Pile",
                    iconId = "Bank",
                    colorNumber = 1L
                ),
                isLoading = false,
                documentCoverItems = listOf(
                    DocumentCoverItem(
                        document = DocumentModel(
                            id = "doc1",
                            title = "Document 1",
                            imageIds = emptyList(),
                            creationDateTime = LocalDateTime.now(),
                            modificationDateTime = LocalDateTime.now(),
                            documentStatus = 1,
                            documentPileIds = listOf("1"),
                            documentDetails = emptyList(),
                            documentNote = "",
                            documentOrganizationIds = emptyList(),
                            isIncomingPdf = false
                        ),
                        coverImageCacheKey = "key1"
                    )
                )
            ),
            bitmapCache = emptyMap(),
            onEvent = {},
            popBackStack = {},
            popToHome = {},
            navigateToDocumentDetail = {},
            navigateToSearchScreen = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PileDetailContent(
    modifier: Modifier = Modifier,
    state: PileDetailState,
    bitmapCache: Map<String, Bitmap>,
    isNavigating: Boolean = false,
    onEvent: (PileDetailEvent) -> Unit,
    popBackStack: () -> Unit,
    popToHome: () -> Unit,
    navigateToDocumentDetail: (documentId: String) -> Unit,
    navigateToSearchScreen: () -> Unit,
    navigateToAddDocument: (documentId: String) -> Unit = {},
    navigateToEditDocument: (documentId: String) -> Unit = {},
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it.asString(context),
                duration = SnackbarDuration.Short
            )
            onEvent(PileDetailEvent.OnErrorDismissed)
        }
    }

    var isUpdatePileExpanded by rememberSaveable { mutableStateOf(false) }
    var isDeletePileExpanded by rememberSaveable { mutableStateOf(false) }
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    val importActions = rememberDocumentImportController(
        cameraUri = state.cameraUri,
        onUriConsumed = { onEvent(PileDetailEvent.OnCameraUriConsumed) },
        onPdfSelected = { onEvent(PileDetailEvent.OnPdfImported(it)) },
        onImagesSelected = { onEvent(PileDetailEvent.OnImagesImported(it)) },
        onCameraClick = { onEvent(PileDetailEvent.OnCameraClick) }
    )

    Scaffold(
        contentWindowInsets = WindowInsets.displayCutout,
        modifier = modifier,
        topBar = {
            state.pile?.let { pileModel ->
                TopAppBar(
                    pileModel = pileModel,
                    popBackStack = popBackStack,
                    onSearchClick = navigateToSearchScreen,
                    onEditClick = { isUpdatePileExpanded = true },
                    onDeleteClick = { isDeletePileExpanded = true }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            AnimatedVisibility(
                visible = !state.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FabMenuWithController(
                    modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues()),
                    fabMenuExpanded = fabMenuExpanded,
                    updateFabMenuExpanded = { fabMenuExpanded = it },
                    importActions = importActions
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            var availableWidth by remember { mutableStateOf(0.dp) }
            val density = LocalDensity.current

            LoadingWrapper(
                isLoading = state.isLoading,
                modifier = Modifier.pointerInteropFilter {
                    when (it.action) {
                        MotionEvent.ACTION_DOWN -> {
                            fabMenuExpanded = false
                        }
                    }
                    false
                }
            ) {
                if (state.documentCoverItems.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(32.dp)
                                .padding(top = 100.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.user_ic_category_24px),
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.no_documents_in_pile),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
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
                            documents = state.documentCoverItems,
                            onDocumentClick = { documentId ->
                                navigateToDocumentDetail(documentId)
                            },
                            bitmapCache = bitmapCache,
                            onLoadBitmap = { document ->
                                onEvent(PileDetailEvent.OnImageDisplayed(document))
                            },
                        )
                        item {
                            Spacer(Modifier.height(100.dp))
                        }
                    }
                }
            }

            if (state.isLoadingNewDocument || isNavigating) {
                LoadingAlert(title = stringResource(R.string.loading_new_document))
            }
        }

        if (isUpdatePileExpanded) {
            state.pile?.let { pile ->
                AlertEditPile(
                    pileModel = pile,
                    onDismiss = { isUpdatePileExpanded = false },
                    onConfirm = { pileName, pileIconId, colorNumber ->
                        isUpdatePileExpanded = false
                        onEvent(
                            PileDetailEvent.OnPileChange(
                                pileName,
                                pileIconId,
                                colorNumber
                            )
                        )
                    }
                )
            }
        }

        if (isDeletePileExpanded) {
            AlertDeletePile(
                onDismiss = { isDeletePileExpanded = false },
                onConfirm = {
                    isDeletePileExpanded = false
                    onEvent(PileDetailEvent.OnDeletePile)
                    popToHome()
                }
            )
        }

        if (state.showDraftWarning) {
            val tempDocument = state.temporaryDocument
            AlertDraftDocumentWarning(
                onDismiss = { onEvent(PileDetailEvent.OnDismissDraftWarning) },
                onDiscardAndContinue = {
                    onEvent(PileDetailEvent.OnConfirmImport)
                },
                onNavigateToDraft = {
                    onEvent(PileDetailEvent.OnDismissDraftWarning)
                    if (tempDocument != null) {
                        if (tempDocument.isIncomingPdf) navigateToAddDocument(tempDocument.id)
                        else navigateToEditDocument(tempDocument.id)
                    }
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
private fun TopAppBar(
    modifier: Modifier = Modifier,
    pileModel: PileModel,
    popBackStack: () -> Unit,
    onSearchClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = modifier
            .statusBarsPadding()
            .displayCutoutPadding()
            .padding(horizontal = 4.dp)
            .padding(bottom = 12.dp),
    ) {
        Row(Modifier.padding(vertical = 8.dp)) {
            IconButton(onClick = popBackStack) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_24px),
                    contentDescription = stringResource(R.string.return_)
                )
            }

            Spacer(Modifier.weight(1f))

            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.search)
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(
                    painter = painterResource(R.drawable.edit_24px),
                    contentDescription = stringResource(R.string.edit_pile)
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(R.drawable.delete_24px),
                    contentDescription = stringResource(R.string.delete_pile)
                )
            }
        }

        Pile(
            pileModel = pileModel,
            isColored = true,
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )
    }
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