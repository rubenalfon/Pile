package es.pile.features.documentDetail.ui

import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults.vibrantFloatingToolbarColors
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonDefaults.smallContainerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pile.DocumentModel
import es.pile.PileModel
import es.pile.R
import es.pile.core.domain.models.DocumentDetail
import es.pile.core.domain.models.StringDetail
import es.pile.core.ui.composables.AlertNewPile
import es.pile.core.ui.composables.LoadingAlert
import es.pile.core.ui.composables.LoadingWrapper
import es.pile.core.ui.composables.Pile
import es.pile.core.ui.composables.SelectPilesBottomSheet
import es.pile.core.ui.composables.SwipeBox
import es.pile.core.ui.theme.PileTheme
import es.pile.features.documentDetail.ui.composables.SectionTitleBar
import es.pile.features.documentDetail.ui.composables.SimpleTextField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun DocumentDetailScreen(
    modifier: Modifier = Modifier,
    documentId: String,
    navigateToPileDetail: (pileId: String) -> Unit,
    navigateToEditDocument: (documentId: String) -> Unit,
    popBackStack: () -> Unit,
    viewModel: DocumentDetailViewModel = koinViewModel { parametersOf(documentId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bitmapCache by viewModel.bitmapCache.collectAsStateWithLifecycle()

    DocumentDetailContent(
        modifier = modifier,
        state = state,
        bitmapCache = bitmapCache,
        onEvent = { viewModel.handleEvent(it) },
        navigateToPileDetail = navigateToPileDetail,
        navigateToEditDocument = navigateToEditDocument,
        popBackStack = popBackStack
    )
}

@Preview(showBackground = true)
@Composable
fun DocumentDetailPreview() {
    val document = DocumentModel(
        id = "1",
        title = "Documento de ejemplo",
        imageIds = emptyList(),
        creationDateTime = LocalDateTime.now(),
        modificationDateTime = LocalDateTime.now(),
        documentStatus = 0,
        documentPileIds = listOf("1"),
        documentDetails = emptyList(),
        documentNote = "Esta es una nota de prueba para el documento.",
        documentOrganizationIds = emptyList(),
        isIncomingPdf = false
    )

    val state = DocumentDetailState(
        documentModel = document,
        documentPileModels = listOf(
            PileModel("1", "Pilas", "icon", 0xFF0000L)
        ),
        localDocumentDetails = emptyList(),
        pageCacheKeys = listOf("key1")
    )

    PileTheme {
        DocumentDetailContent(
            state = state,
            bitmapCache = emptyMap(),
            onEvent = {},
            navigateToPileDetail = {},
            navigateToEditDocument = {},
            popBackStack = {}
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DocumentDetailContent(
    modifier: Modifier = Modifier,
    state: DocumentDetailState,
    bitmapCache: Map<String, Bitmap>,
    onEvent: (DocumentDetailEvent) -> Unit,
    navigateToPileDetail: (pileId: String) -> Unit,
    navigateToEditDocument: (documentId: String) -> Unit,
    popBackStack: () -> Unit,
) {
    var showRenameDocumentAlert by rememberSaveable { mutableStateOf(false) }
    var showDeleteDocumentAlert by rememberSaveable { mutableStateOf(false) }
    var showDocumentPilesBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showNewPileAlert by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val hapticFeedback = LocalHapticFeedback.current

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val detailsActionEvent = DetailsActionEvent.OnIdMove(from.key as String, to.key as String)
        onEvent(DocumentDetailEvent.OnUpdateDetails(detailsActionEvent))

        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarStrings = Pair(
        stringResource(R.string.detail_deleted),
        stringResource(R.string.undo)
    )

    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onEvent(DocumentDetailEvent.OnDownload)
        }
    }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { uiText ->
            snackbarHostState.showSnackbar(
                message = uiText.asString(context),
                duration = SnackbarDuration.Short
            )
            onEvent(DocumentDetailEvent.OnMessageDismissed)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.displayCutout,
        modifier = modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onEvent(DocumentDetailEvent.OnUpdateEditingMode(false))
                focusManager.clearFocus()
            },
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            ScreenTopAppBar(
                popBackStack = popBackStack,
                title = state.documentModel?.title ?: ""
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            AnimatedVisibility(
                state.documentModel != null,
                enter = fadeIn(), exit = fadeOut()
            ) {
                ToolBar(
                    modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues()),
                    showEditDocument = !(state.documentModel?.isIncomingPdf ?: true),
                    onRenameDocument = {
                        onEvent(DocumentDetailEvent.OnUpdateEditingMode(false))
                        focusManager.clearFocus()
                        showRenameDocumentAlert = true
                    },
                    onDeleteDocument = {
                        onEvent(DocumentDetailEvent.OnUpdateEditingMode(false))
                        focusManager.clearFocus()
                        showDeleteDocumentAlert = true
                    },
                    onDownloadDocument = {
                        onEvent(DocumentDetailEvent.OnUpdateEditingMode(false))
                        focusManager.clearFocus()

                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                            permissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        } else {
                            onEvent(DocumentDetailEvent.OnDownload)
                        }
                    },
                    onShareDocument = {
                        onEvent(DocumentDetailEvent.OnUpdateEditingMode(false))
                        focusManager.clearFocus()
                        onEvent(DocumentDetailEvent.OnShare)
                    },
                    onEditDocument = {
                        onEvent(DocumentDetailEvent.OnUpdateEditingMode(false))
                        focusManager.clearFocus()
                        navigateToEditDocument(state.documentModel?.id ?: "")
                    },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            LoadingWrapper(state.documentModel == null || state.documentPileModels == null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState,
                    horizontalAlignment = Alignment.Start
                ) {
                    item {
                        ImagePager(
                            bitmapCache = bitmapCache,
                            pageCacheKeys = state.pageCacheKeys,
                            onLoadBitmap = {
                                onEvent(DocumentDetailEvent.OnImageDisplayed(it))
                            },
                            onClick = {
                                onEvent(DocumentDetailEvent.OnUpdateEditingMode(false))
                                focusManager.clearFocus()
                                onEvent(DocumentDetailEvent.OnOpenDocument)
                            }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }

                    documentDetailsSection(
                        reorderableLazyListState = reorderableLazyListState,
                        documentDetails = state.localDocumentDetails,
                        isEditingMode = state.isDetailsEditing,
                        updateEditingMode = {
                            focusManager.clearFocus()
                            onEvent(DocumentDetailEvent.OnUpdateEditingMode(it))
                        },
                        onEvent = {
                            onEvent(DocumentDetailEvent.OnUpdateDetails(it))
                            if (it !is DetailsActionEvent.OnRemove) return@documentDetailsSection

                            scope.launch {
                                val result = snackbarHostState
                                    .showSnackbar(
                                        message = snackbarStrings.first,
                                        actionLabel = snackbarStrings.second,
                                        duration = SnackbarDuration.Long
                                    )
                                when (result) {
                                    SnackbarResult.ActionPerformed -> { // Undo
                                        val action = DetailsActionEvent.OnRestore
                                        onEvent(
                                            DocumentDetailEvent.OnUpdateDetails(
                                                action
                                            )
                                        )
                                    }

                                    SnackbarResult.Dismissed -> {
                                        val action = DetailsActionEvent.OnPurge
                                        onEvent(
                                            DocumentDetailEvent.OnUpdateDetails(
                                                action
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    )

                    item { Spacer(Modifier.height(8.dp)) }

                    item {
                        DocumentNoteSection(
                            documentModel = state.documentModel,
                            onUpdateDocumentNote = {
                                onEvent(DocumentDetailEvent.OnUpdateNote(it))
                            },
                            onFocused = {
                                onEvent(DocumentDetailEvent.OnUpdateEditingMode(false))
                            }
                        )
                    }

                    item { Spacer(Modifier.height(8.dp)) }

                    documentPilesSection(
                        documentPileModels = state.documentPileModels ?: emptyList(),
                        onPileClick = {
                            onEvent(DocumentDetailEvent.OnUpdateEditingMode(false))
                            focusManager.clearFocus()
                            navigateToPileDetail(it)
                        },
                        onEditDocumentPiles = {
                            onEvent(DocumentDetailEvent.OnUpdateEditingMode(false))
                            focusManager.clearFocus()
                            showDocumentPilesBottomSheet = true
                        }
                    )

                    item { Spacer(Modifier.height(16.dp)) }

                    item {
                        if (state.documentModel != null) {
                            AddedSection(
                                creationDate = state.documentModel.creationDateTime,
                                modificationDate = state.documentModel.modificationDateTime
                            )
                        }
                    }

                    item { Spacer(Modifier.height(140.dp)) }
                }
            }
        }
    }

    if (showRenameDocumentAlert) {
        AlertRenameDocument(
            documentName = state.documentModel?.title ?: "",
            onDismiss = { showRenameDocumentAlert = false },
            onConfirm = { newName ->
                showRenameDocumentAlert = false
                onEvent(DocumentDetailEvent.OnRenameDocument(newName))
            }
        )
    }

    if (showDeleteDocumentAlert) {
        AlertDeleteDocument(
            onDismiss = { showDeleteDocumentAlert = false },
            onConfirm = {
                showDeleteDocumentAlert = false
                onEvent(DocumentDetailEvent.OnDeleteDocument)
                popBackStack()
            }
        )
    }

    if (showDocumentPilesBottomSheet) {
        SelectPilesBottomSheet(
            title = stringResource(R.string.piles),
            pileList = state.allPiles,
            selectedFilterPiles = state.documentModel?.documentPileIds ?: emptyList(),
            onDismissBottomSheet = { showDocumentPilesBottomSheet = false },
            onPileClick = { onEvent(DocumentDetailEvent.OnUpdatePileSelection(it)) },
            onNewPile = { showNewPileAlert = true }
        )
    }

    if (showNewPileAlert) {
        AlertNewPile(
            onDismiss = { showNewPileAlert = false },
            onConfirm = { pileName, pileIconId, pileColorNumber ->
                showNewPileAlert = false
                onEvent(
                    DocumentDetailEvent.OnNewPile(
                        pileName = pileName, iconId = pileIconId, color = pileColorNumber
                    )
                )
            }
        )
    }

    AlertExporting(isExporting = state.isExporting)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
private fun ScreenTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    popBackStack: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 16.dp)
            )
        },
        navigationIcon = {
            FilledIconButton(
                modifier = Modifier
                    .padding(start = 14.dp, end = 4.dp)
                    .size(smallContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide)),
                onClick = popBackStack
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_24px),
                    contentDescription = stringResource(R.string.return_)
                )
            }
        },
        colors = topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ImagePager(
    modifier: Modifier = Modifier,
    bitmapCache: Map<String, Bitmap>,
    pageCacheKeys: List<String>,
    onLoadBitmap: suspend (pageNumber: Int) -> Unit,
    onClick: () -> Unit
) {
    val pageCount = pageCacheKeys.size

    val pagerState = rememberPagerState(pageCount = { pageCount })

    var isPageNumberVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = pagerState.currentPage) {
        if (pageCount == 1) return@LaunchedEffect

        isPageNumberVisible = true
        delay(5.seconds)
        isPageNumberVisible = false
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 8.dp,
            modifier = modifier
                .height(500.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable { onClick.invoke() }
        ) { page ->
            val key = pageCacheKeys.getOrNull(page) ?: ""
            val cachedBitmap: Bitmap? = bitmapCache[key]

            if (cachedBitmap == null) {
                LaunchedEffect(key1 = key) {
                    onLoadBitmap(page)
                }
            }

            LoadingWrapper(cachedBitmap == null) {
                if (cachedBitmap == null) return@LoadingWrapper
                Image(
                    bitmap = cachedBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.image_number, page + 1),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentScale = ContentScale.Fit
                )
            }
        }

        AnimatedVisibility(
            isPageNumberVisible,
            enter = fadeIn(MaterialTheme.motionScheme.slowEffectsSpec()),
            exit = fadeOut(MaterialTheme.motionScheme.slowEffectsSpec())
        ) {
            Box(
                Modifier
                    .padding(top = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${pagerState.pageCount}",
                    maxLines = 1,
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 14.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

private fun LazyListScope.documentDetailsSection(
    reorderableLazyListState: ReorderableLazyListState,
    documentDetails: List<DocumentDetail>?,
    isEditingMode: Boolean,
    updateEditingMode: (state: Boolean) -> Unit,
    onEvent: (event: DetailsActionEvent) -> Unit
) {
    item {
        SectionTitleBar(
            title = stringResource(R.string.details),
            modifier = Modifier.padding(horizontal = 16.dp),
            onButtonCLick = {
                updateEditingMode.invoke(!isEditingMode)
            },
            isSaveMode = isEditingMode
        )
    }

    if (documentDetails.isNullOrEmpty()) {
        item {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(CardDefaults.shape)
                    .clickable {
                        updateEditingMode.invoke(true)
                        onEvent(DetailsActionEvent.OnNew)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    stringResource(R.string.no_document_details),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    } else {
        items(documentDetails, key = { it.id }) { documentDetail ->
            val index = documentDetails.indexOf(documentDetail)
            ReorderableItem(
                reorderableLazyListState,
                key = documentDetail.id
            ) { isDragging ->
                SwipeBox(
                    onDelete = { onEvent(DetailsActionEvent.OnRemove(index)) },
                    contentPaddingValues = PaddingValues(horizontal = 16.dp),
                    enabled = isEditingMode,
                    modifier = Modifier.padding(bottom = if (index != documentDetails.size - 1) 2.dp else 0.dp)
                ) {
                    if (documentDetail !is StringDetail) return@SwipeBox
                    DocumentDetailItem(
                        documentDetail = documentDetail,
                        index = index,
                        isDragging = isDragging,
                        isFirstItem = index == 0,
                        isLastItem = index == documentDetails.size - 1,
                        isEditingMode = isEditingMode,
                        onUpdateEditingMode = updateEditingMode,
                        onMove = { from, to ->
                            onEvent(DetailsActionEvent.OnIndexMove(from, to))
                        },
                        onTextChange = { newName, newValue ->
                            onEvent(
                                DetailsActionEvent.OnUpdateText(
                                    documentDetail.id,
                                    newName,
                                    newValue
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    }

    item {
        AnimatedVisibility(
            visible = isEditingMode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Button(
                onClick = {
                    onEvent(DetailsActionEvent.OnNew)
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_a_detail)
                    )
                    Text(
                        stringResource(R.string.add_a_detail),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun ReorderableCollectionItemScope.DocumentDetailItem(
    documentDetail: StringDetail,
    index: Int,
    isDragging: Boolean,
    isFirstItem: Boolean,
    isLastItem: Boolean,
    isEditingMode: Boolean,
    onUpdateEditingMode: (Boolean) -> Unit,
    onMove: (from: Int, to: Int) -> Unit,
    onTextChange: (newName: String, newValue: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val topCornersDp by animateDpAsState(
        targetValue = if (isFirstItem) 12.dp else 4.dp,
        label = "topCorners"
    )
    val bottomCornersDp by animateDpAsState(
        targetValue = if (isLastItem) 12.dp else 4.dp,
        label = "bottomCorners"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isEditingMode) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        label = "containerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isEditingMode) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
        label = "contentColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.05f else 1.0f,
        label = "scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 0.dp,
        label = "elevation"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val hapticFeedback = LocalHapticFeedback.current

    val moveUpString = stringResource(R.string.move_up_detail)
    val moveDownString = stringResource(R.string.move_down_detail)

    Card(
        modifier = modifier
            .combinedClickable(
                enabled = !isEditingMode,
                indication = null,
                interactionSource = interactionSource,
                onClick = {},
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUpdateEditingMode(true)
                }
            )
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(
                        label = moveUpString,
                        action = {
                            if (index > 0) {
                                onMove(index, index - 1)
                                true
                            } else false
                        }
                    ),
                    CustomAccessibilityAction(
                        label = moveDownString,
                        action = {
                            if (!isLastItem) {
                                onMove(index, index + 1)
                                true
                            } else false
                        }
                    )
                )
            }
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = elevation.toPx()
            },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(
            topStart = topCornersDp,
            topEnd = topCornersDp,
            bottomStart = bottomCornersDp,
            bottomEnd = bottomCornersDp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .longPressDraggableHandle(
                    interactionSource = interactionSource,
                    enabled = isEditingMode,
                    onDragStarted = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                    }
                )
                .clearAndSetSemantics { },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SimpleTextField(
                value = documentDetail.name,
                onValueChange = { newText ->
                    onTextChange(newText, documentDetail.value)
                },
                modifier = Modifier,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = contentColor),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = true,
                enabled = isEditingMode,
                hint = stringResource(R.string.detail_hint_title)
            )

            Spacer(Modifier.weight(1f))

            SimpleTextField(
                value = documentDetail.value,
                onValueChange = { newText ->
                    onTextChange(documentDetail.name, newText)
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = contentColor
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier,
                singleLine = true,
                enabled = isEditingMode,
                hint = stringResource(R.string.detail_hint_value)
            )
        }
    }
}

@Composable
private fun DocumentNoteSection(
    documentModel: DocumentModel?,
    onUpdateDocumentNote: (newText: String) -> Unit,
    onFocused: () -> Unit = {},
) {
    var isFocused by rememberSaveable { mutableStateOf(false) }

    val containerColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        label = "containerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
        label = "contentColor"
    )

    var unsavedDocumentNoteDetail by rememberSaveable {
        mutableStateOf(
            documentModel?.documentNote ?: ""
        )
    }

    LaunchedEffect(unsavedDocumentNoteDetail) {
        delay(200.milliseconds)

        if (unsavedDocumentNoteDetail == documentModel?.documentNote) return@LaunchedEffect

        onUpdateDocumentNote.invoke(unsavedDocumentNoteDetail)
    }

    SectionTitleBar(
        title = stringResource(R.string.note),
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            SimpleTextField(
                value = unsavedDocumentNoteDetail,
                onValueChange = { newText ->
                    unsavedDocumentNoteDetail = newText
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        if (isFocused) {
                            onFocused()
                        } else {
                            onUpdateDocumentNote.invoke(unsavedDocumentNoteDetail)
                        }
                    },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                hint = stringResource(R.string.add_a_note)
            )
        }
    }
}

private fun LazyListScope.documentPilesSection(
    documentPileModels: List<PileModel>,
    onPileClick: (pileId: String) -> Unit,
    onEditDocumentPiles: () -> Unit
) {
    item {
        SectionTitleBar(
            title = stringResource(R.string.piles),
            onButtonCLick = onEditDocumentPiles,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }

    items(
        count = documentPileModels.size,
        key = { index -> documentPileModels[index].id }
    ) { index ->
        val pileModel = documentPileModels[index]

        val topCornersDp by animateDpAsState(
            targetValue = if (index == 0) 14.dp else 4.dp,
            label = "topCorners"
        )
        val bottomCornersDp by animateDpAsState(
            targetValue = if (index == documentPileModels.size - 1) 12.dp else 4.dp,
            label = "bottomCorners"
        )

        Pile(
            pileModel = pileModel,
            modifier = Modifier
                .animateItem()
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            isColored = true,
            customShape = RoundedCornerShape(
                topStart = topCornersDp,
                topEnd = topCornersDp,
                bottomStart = bottomCornersDp,
                bottomEnd = bottomCornersDp
            ),
            onClick = { onPileClick.invoke(pileModel.id) }
        )

        if (index != documentPileModels.size - 1) {
            Spacer(Modifier.height(2.dp))
        }
    }

    item {
        AnimatedVisibility(documentPileModels.isEmpty()) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(CardDefaults.shape)
                    .clickable {
                        onEditDocumentPiles()
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    stringResource(R.string.no_piles_in_document),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun AddedSection(
    modifier: Modifier = Modifier,
    creationDate: LocalDateTime,
    modificationDate: LocalDateTime
) {
    val formatter = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .withLocale(Locale.getDefault())
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.padding(horizontal = 16.dp).padding(start = 8.dp)
    ) {
        Text(
            text = stringResource(
                R.string.added_date,
                creationDate.format(formatter)
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary,
        )
        if (modificationDate.toLocalDate() != creationDate.toLocalDate()) {
            Text(
                text = stringResource(
                    R.string.modified_date,
                    modificationDate.format(formatter)
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ToolBar(
    modifier: Modifier = Modifier,
    showEditDocument: Boolean,
    onRenameDocument: () -> Unit,
    onDeleteDocument: () -> Unit,
    onDownloadDocument: () -> Unit,
    onShareDocument: () -> Unit,
    onEditDocument: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalFloatingToolbar(
            colors = vibrantFloatingToolbarColors(),
            expanded = true,
            content = {
                IconButton(onClick = onRenameDocument) {
                    Icon(
                        painter = painterResource(R.drawable.edit_24px),
                        contentDescription = stringResource(R.string.change_document_title)
                    )
                }
                IconButton(onClick = onDeleteDocument) {
                    Icon(
                        painter = painterResource(R.drawable.delete_24px),
                        contentDescription = stringResource(R.string.delete_document)
                    )
                }
                IconButton(onClick = onDownloadDocument) {
                    Icon(
                        painter = painterResource(R.drawable.download_24px),
                        contentDescription = stringResource(R.string.save_document)
                    )
                }
                IconButton(onClick = onShareDocument) {
                    Icon(
                        painter = painterResource(R.drawable.share_24px),
                        contentDescription = stringResource(R.string.share_document)
                    )
                }
            }
        )

        if (showEditDocument) {
            FloatingActionButton(
                onClick = onEditDocument,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    painter = painterResource(R.drawable.instant_mix_24px),
                    contentDescription = stringResource(R.string.edit_document)
                )
            }
        }
    }
}


@Composable
private fun AlertRenameDocument(
    modifier: Modifier = Modifier,
    documentName: String,
    onDismiss: () -> Unit,
    onConfirm: (documentName: String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var hasRequestedFocus by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasRequestedFocus) {
            delay(100.milliseconds) // Prevents errors
            focusRequester.requestFocus()
            hasRequestedFocus = true
        }
    }

    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = documentName,
                selection = TextRange(documentName.length)
            )
        )
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_document)) },
        text = {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                label = { Text(stringResource(R.string.document_name)) },
                trailingIcon = {
                    if (textFieldValue.text.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.delete_text),
                            modifier = Modifier.clickable {
                                textFieldValue = textFieldValue.copy(text = "")
                            })
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onConfirm.invoke(textFieldValue.text)
                    }
                ),
                modifier = Modifier.focusRequester(focusRequester)
            )
        },
        confirmButton = {
            TextButton(
                enabled = textFieldValue.text.isNotEmpty(),
                onClick = {
                    onConfirm.invoke(textFieldValue.text)
                }
            ) {
                Text(stringResource(R.string.edit))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss.invoke()
            }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}


@Composable
private fun AlertDeleteDocument(
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
        title = { Text(stringResource(R.string.delete_document_alert_title)) },

        text = {
            Text(stringResource(R.string.delete_document_alert_body))
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

@Composable
fun AlertExporting(isExporting: Boolean, modifier: Modifier = Modifier) {
    var showExportLoading by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isExporting) {
        if (isExporting) {
            delay(200.milliseconds)
            showExportLoading = true
        } else {
            showExportLoading = false
        }
    }

    if (showExportLoading) {
        LoadingAlert(title = stringResource(R.string.exporting_document), modifier = modifier)
    }
}