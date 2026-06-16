package es.pile.features.editDocument.ui

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonDefaults.smallContainerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tanishranjan.cropkit.ImageCropper
import es.pile.DocumentImage
import es.pile.DocumentModel
import es.pile.R
import es.pile.core.domain.models.ImageCropData
import es.pile.core.domain.models.ImageFilterType
import es.pile.core.domain.models.ImageItem
import es.pile.core.ui.composables.LoadingAlert
import es.pile.core.ui.composables.LoadingWrapper
import es.pile.core.ui.controllers.rememberDocumentImportController
import es.pile.core.ui.theme.PileTheme
import es.pile.features.editDocument.domain.models.ExtendedCropController
import es.pile.features.editDocument.ui.composables.ActiveIndicator
import es.pile.features.editDocument.ui.composables.AddItemCarousel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.time.LocalDateTime


@Composable
fun EditDocumentScreen(
    modifier: Modifier = Modifier,
    documentId: String,
    popBackStack: () -> Unit,
    onNext: () -> Unit,
    viewModel: EditDocumentViewModel = koinViewModel { parametersOf(documentId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bitmapCache by viewModel.bitmapCache.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { navigationType ->
            when (navigationType) {
                EditDocumentViewModel.NavigationType.BACK -> popBackStack()
                EditDocumentViewModel.NavigationType.NEXT -> onNext()
            }
        }
    }

    EditDocumentContent(
        modifier = modifier,
        state = state,
        bitmapCache = bitmapCache,
        onEvent = { viewModel.handleEvent(it) }
    )
}

@Preview(showBackground = true)
@Composable
fun EditDocumentPreview() {
    val mockImage = DocumentImage(
        id = "img1",
        isDraft = true,
        crop = ImageCropData(0, 0, 100, 100),
        filter = 0L,
        rotation = 0L
    )

    val mockDocument = DocumentModel(
        id = "1",
        title = "Mock Document",
        imageIds = listOf("img1"),
        creationDateTime = LocalDateTime.now(),
        modificationDateTime = LocalDateTime.now(),
        documentStatus = 0,
        documentPileIds = emptyList(),
        documentDetails = emptyList(),
        documentNote = "",
        documentOrganizationIds = emptyList(),
        isIncomingPdf = false
    )

    val mockState = EditDocumentState(
        draftDocument = mockDocument,
        imageItems = listOf(
            ImageItem(mockImage, "cache_key_1")
        ),
        thumbnailKeys = emptyList(),
        imageFilters = emptyList(),
        selectedImageIndex = 0,
        uiMode = EditDocumentMode.SCROLL
    )

    PileTheme {
        EditDocumentContent(
            state = mockState,
            bitmapCache = emptyMap(),
            onEvent = {}
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditDocumentContent(
    modifier: Modifier = Modifier,
    state: EditDocumentState,
    bitmapCache: Map<String, Bitmap>,
    onEvent: (EditDocumentEvent) -> Unit
) {
    // Only intended for selecting images on the gallery.
    val importActions = rememberDocumentImportController(
        onImagesSelected = { onEvent(EditDocumentEvent.OnImportImages(it)) }
    )

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarStrings = Pair(
        stringResource(R.string.image_deleted),
        stringResource(R.string.undo)
    )

    BackHandler(state.isDocumentModified) {
        onEvent(EditDocumentEvent.OnBackClicked())
    }

    val context = LocalContext.current

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { uiText ->
            snackbarHostState.showSnackbar(
                message = uiText.asString(context),
                duration = SnackbarDuration.Short
            )
            onEvent(EditDocumentEvent.OnErrorDismissed)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            ScreenTopAppBar(popBackStack = {
                onEvent(EditDocumentEvent.OnBackClicked())
            })
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            ToolBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = ScreenOffset),
                uiMode = state.uiMode,
                isSinglePage = state.imageItems.count() == 1,
                onUpdateUiMode = { onEvent(EditDocumentEvent.OnModeChange(it)) },
                onDeleteImage = {
                    onEvent(EditDocumentEvent.OnRemoveSelectedImage)

                    scope.launch {
                        val result = snackbarHostState
                            .showSnackbar(
                                message = snackbarStrings.first,
                                actionLabel = snackbarStrings.second,
                                duration = SnackbarDuration.Short
                            )
                        when (result) {
                            SnackbarResult.ActionPerformed -> { // restore
                                onEvent(EditDocumentEvent.OnRestoreRemovedImage)
                            }

                            SnackbarResult.Dismissed -> {
                                onEvent(EditDocumentEvent.OnPurgeRemovedImage)
                            }
                        }
                    }
                },
                onSave = { onEvent(EditDocumentEvent.OnSave) }
            )
        }
    ) { innerPadding ->
        LoadingWrapper(
            state.draftDocument == null || state.imageItems.isEmpty()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val pagerState = rememberPagerState(pageCount = { state.imageItems.size })
                var isCarouselDragging by remember { mutableStateOf(false) }

                PagerSyncEffect(
                    pagerState = pagerState,
                    selectedImageIndex = state.selectedImageIndex,
                    isPaused = isCarouselDragging,
                    onSelectImageIndex = { onEvent(EditDocumentEvent.OnSelectImage(it)) }
                )

                ImagePager(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .weight(1f),
                    uiMode = state.uiMode,
                    pagerState = pagerState,
                    imageItems = state.imageItems,
                    userScrollEnabled = state.uiMode == EditDocumentMode.SCROLL && !isCarouselDragging,
                    bitmapCache = bitmapCache,
                    cropControllers = state.cropControllers,
                    onLoadBitmap = { onEvent(EditDocumentEvent.OnImageDisplayed(it)) },
                    onLoadCropController = {
                        onEvent(EditDocumentEvent.OnCropDisplayed(it))
                    }
                )

                val lazyListState = rememberLazyListState()
                val selectedImageIndex = state.selectedImageIndex

                // Animate scroll
                LaunchedEffect(selectedImageIndex) {
                    if (isCarouselDragging) return@LaunchedEffect

                    val totalItems = lazyListState.layoutInfo.totalItemsCount
                    if (selectedImageIndex !in 0..<totalItems) return@LaunchedEffect
                    if (!lazyListState.canScrollBackward && !lazyListState.canScrollForward) return@LaunchedEffect

                    lazyListState.animateScrollToItem(selectedImageIndex, -150)
                }

                AnimatedVisibility(visible = state.uiMode == EditDocumentMode.SCROLL) {
                    ThumbnailRow(
                        modifier = Modifier.padding(bottom = 16.dp),
                        lazyListState = lazyListState,
                        imageItems = state.imageItems,
                        bitmapCache = bitmapCache,
                        onLoadBitmap = { onEvent(EditDocumentEvent.OnImageDisplayed(it)) },
                        selectedImageIndex = selectedImageIndex,
                        onDragStateChange = { isCarouselDragging = it },
                        onSelectImage = {
                            onEvent(EditDocumentEvent.OnSelectImage(it))
                        },
                        onMoveImage = { from, to ->
                            onEvent(EditDocumentEvent.OnMoveImage(from, to))
                        },
                        onNewImage = importActions.launchGallery
                    )
                }

                AnimatedVisibility(visible = state.uiMode == EditDocumentMode.COLOR) {
                    EditColorRow(
                        modifier = Modifier.padding(bottom = 16.dp),
                        imageFilters = state.imageFilters,
                        activeFilterIndex = state.imageItems.getOrNull(state.selectedImageIndex)?.image?.filter?.toInt()
                            ?: 0,
                        onSelectColorIndex = {
                            onEvent(EditDocumentEvent.OnUpdateFilter(it))
                        },
                        thumbnailKeys = state.thumbnailKeys,
                        onLoadThumbnail = {
                            onEvent(EditDocumentEvent.OnThumbnailDisplayed(it))
                        },
                        bitmapCache = bitmapCache
                    )
                }

                AnimatedVisibility(visible = state.uiMode == EditDocumentMode.CROP_ROTATE) {
                    CropRotateButtons(
                        onRotate = { onEvent(EditDocumentEvent.OnRotate) }
                    )
                }
            }
        }
    }

    if (state.isLoadingNewImage) {
        LoadingAlert(stringResource(R.string.adding_images))
    }

    if (state.showUnsavedChangesAlert) {
        AlertUnsavedChanges(
            onKeepEditing = {
                onEvent(EditDocumentEvent.OnExitCanceled)
            },
            onDiscard = {
                onEvent(EditDocumentEvent.OnBackClicked(force = true))
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
private fun ScreenTopAppBar(
    modifier: Modifier = Modifier,
    popBackStack: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(stringResource(R.string.preview))
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

@Composable
private fun ImagePager(
    modifier: Modifier = Modifier,
    uiMode: EditDocumentMode,
    pagerState: PagerState,
    imageItems: List<ImageItem>,
    userScrollEnabled: Boolean,
    bitmapCache: Map<String, Bitmap>,
    cropControllers: Map<String, ExtendedCropController>,
    onLoadBitmap: (pageNumber: Int) -> Unit,
    onLoadCropController: (key: String) -> Unit
) {
    HorizontalPager(
        state = pagerState,
        key = { page -> imageItems.getOrNull(page)?.image?.id ?: page },
        contentPadding = PaddingValues(horizontal = 16.dp),
        pageSpacing = 16.dp,
        userScrollEnabled = userScrollEnabled,
        modifier = modifier
    ) { page ->
        val key = imageItems.getOrNull(page)?.cacheKey ?: ""
        val cachedBitmap: Bitmap? = bitmapCache[key]

        if (cachedBitmap == null) {
            LaunchedEffect(key1 = key) {
                onLoadBitmap(page)
            }
        }

        Crossfade(
            targetState = uiMode,
            label = "Image pager crossfade"
        ) { uiMode ->
            if (uiMode != EditDocumentMode.CROP_ROTATE) {
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
            } else {
                val extendedCropController = cropControllers[key]

                if (extendedCropController == null) {
                    LaunchedEffect(key1 = cachedBitmap) {
                        if (cachedBitmap == null) return@LaunchedEffect
                        onLoadCropController(key)
                    }
                }
                LoadingWrapper(extendedCropController == null) {
                    if (extendedCropController == null) return@LoadingWrapper
                    ImageCropper(
                        modifier = Modifier
                            .fillMaxSize(),
                        cropController = extendedCropController.cropController
                    )
                }
            }
        }
    }
}

@Composable
private fun PagerSyncEffect(
    pagerState: PagerState,
    selectedImageIndex: Int,
    isPaused: Boolean,
    onSelectImageIndex: (Int) -> Unit
) {
    var isSelectedImageIndexRecent by rememberSaveable { mutableStateOf(false) }
    var isChangedFromCarouselRecent by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(selectedImageIndex, isPaused) {
        if (isPaused) return@LaunchedEffect
        if (isChangedFromCarouselRecent || pagerState.isScrollInProgress) return@LaunchedEffect
        if (pagerState.currentPage == selectedImageIndex) return@LaunchedEffect

        isSelectedImageIndexRecent = true
        pagerState.animateScrollToPage(selectedImageIndex)
        isSelectedImageIndexRecent = false
    }

    LaunchedEffect(pagerState, isPaused) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (isPaused || isSelectedImageIndexRecent) return@collect

                isChangedFromCarouselRecent = true
                onSelectImageIndex(page)
                isChangedFromCarouselRecent = false
            }
    }
}

@Composable
private fun ThumbnailRow(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    imageItems: List<ImageItem>,
    bitmapCache: Map<String, Bitmap>,
    onLoadBitmap: (pageNumber: Int) -> Unit,
    selectedImageIndex: Int,
    onSelectImage: (Int) -> Unit,
    onDragStateChange: (Boolean) -> Unit,
    onMoveImage: (Int, Int) -> Unit,
    onNewImage: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onMoveImage(from.index, to.index)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    val isAnyItemDragging = reorderableLazyListState.isAnyItemDragging
    LaunchedEffect(isAnyItemDragging) {
        onDragStateChange(isAnyItemDragging)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier
                .weight(1f)
                .height(84.dp)
                .padding(end = 8.dp),
            state = lazyListState,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(start = 16.dp, end = 0.dp)
        ) {
            items(imageItems, key = { it.image.id }) { imageItem ->
                val index = imageItems.indexOf(imageItem)
                ReorderableItem(
                    reorderableLazyListState,
                    key = imageItem.image.id
                ) { isDragging ->
                    ThumbnailItem(
                        modifier = Modifier.animateItem(),
                        index = index,
                        isSelected = index == selectedImageIndex,
                        isLastItem = index == imageItems.lastIndex,
                        isDragging = isDragging,
                        isAnyItemDragging = isAnyItemDragging,
                        cacheKey = imageItem.cacheKey,
                        bitmapCache = bitmapCache,
                        onLoadBitmap = onLoadBitmap,
                        onMove = onMoveImage,
                        onSelect = onSelectImage
                    )
                }
            }
        }

        AddItemCarousel(
            modifier = Modifier
                .padding(end = 16.dp)
                .width(56.dp)
                .height(84.dp)
                .clip(RoundedCornerShape(20.dp)),
            onItemClick = onNewImage
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReorderableCollectionItemScope.ThumbnailItem(
    modifier: Modifier,
    index: Int,
    isSelected: Boolean,
    isLastItem: Boolean,
    isDragging: Boolean,
    isAnyItemDragging: Boolean,
    cacheKey: String,
    bitmapCache: Map<String, Bitmap>,
    onLoadBitmap: (index: Int) -> Unit,
    onMove: (from: Int, to: Int) -> Unit,
    onSelect: (Int) -> Unit
) {
    val cachedBitmap = bitmapCache[cacheKey]

    LaunchedEffect(cacheKey) {
        if (cachedBitmap == null) onLoadBitmap(index)
    }

    val animatedCornerRadius by animateDpAsState(
        targetValue = if (isSelected) 42.dp else 20.dp,
        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
        label = "corner"
    )

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.1f else 1.0f,
        label = "scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 0.dp,
        label = "elevation"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val hapticFeedback = LocalHapticFeedback.current

    val movePrevious = stringResource(R.string.move_previous)
    val moveNext = stringResource(R.string.move_next)

    Box(
        modifier = modifier
            .longPressDraggableHandle(
                onDragStarted = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                },
                onDragStopped = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                }
            )
            .clickable(
                enabled = !isAnyItemDragging,
                interactionSource = interactionSource,
                indication = null,
                onClick = { onSelect(index) }
            )
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(
                        label = movePrevious,
                        action = {
                            if (index > 0) {
                                onMove(index, index - 1)
                                true
                            } else false
                        }
                    ),
                    CustomAccessibilityAction(
                        label = moveNext,
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
        contentAlignment = Alignment.Center
    ) {
        if (cachedBitmap != null) {
            Image(
                bitmap = cachedBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(84.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(animatedCornerRadius))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            )
        } else {
            Box(
                modifier = Modifier
                    .width(84.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        ActiveIndicator(isSelected = isSelected)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EditColorRow(
    modifier: Modifier = Modifier,
    imageFilters: List<ImageFilterType>,
    activeFilterIndex: Int,
    onSelectColorIndex: (Int) -> Unit,
    thumbnailKeys: List<String>,
    onLoadThumbnail: (Int) -> Unit,
    bitmapCache: Map<String, Bitmap>,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(imageFilters.size) { i ->
            Box(
                contentAlignment = Alignment.Center
            ) {
                val key = thumbnailKeys.getOrNull(i) ?: ""
                val cachedBitmap: Bitmap? = bitmapCache[key]

                val isSelected = i == activeFilterIndex

                val animatedCornerRadius by animateDpAsState(
                    if (isSelected) 42.dp else 20.dp,
                    animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                )

                val modifier = Modifier
                    .size(84.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(animatedCornerRadius))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable {
                        onSelectColorIndex.invoke(i)
                    }


                if (cachedBitmap == null) {
                    LaunchedEffect(key1 = key) {
                        onLoadThumbnail(i)
                    }
                    Box(modifier)
                } else {
                    Image(
                        modifier = modifier,
                        bitmap = cachedBitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.image_number, i + 1),
                        contentScale = ContentScale.Crop
                    )
                }

                ActiveIndicator(isSelected = isSelected)
            }
        }
    }
}

@Composable
private fun CropRotateButtons(
    modifier: Modifier = Modifier,
    onRotate: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.padding(bottom = 16.dp)
    ) {
        OutlinedButton(
            onClick = onRotate
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.rotate_24px),
                    contentDescription = stringResource(R.string.rotate_image_counterclockwise)
                )
                Text(stringResource(R.string.rotate))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ToolBar(
    modifier: Modifier = Modifier,
    uiMode: EditDocumentMode,
    isSinglePage: Boolean,
    onUpdateUiMode: (uiMode: EditDocumentMode) -> Unit,
    onDeleteImage: () -> Unit,
    onSave: () -> Unit,
) {
    var isDeleteImageAlertExpanded by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalFloatingToolbar(
            expanded = true,
            content = {
                IconButton(
                    onClick = { onUpdateUiMode(EditDocumentMode.COLOR) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (uiMode == EditDocumentMode.COLOR) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                        contentColor = if (uiMode == EditDocumentMode.COLOR) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.monochrome_photos_24px),
                        contentDescription = stringResource(R.string.edit_image_colors)
                    )
                }
                IconButton(
                    onClick = { onUpdateUiMode(EditDocumentMode.CROP_ROTATE) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (uiMode == EditDocumentMode.CROP_ROTATE) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                        contentColor = if (uiMode == EditDocumentMode.CROP_ROTATE) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.transform_24px),
                        contentDescription = stringResource(R.string.resize_image)
                    )
                }
                IconButton(
                    enabled = !isSinglePage,
                    onClick = { isDeleteImageAlertExpanded = true }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete_24px),
                        contentDescription = stringResource(R.string.delete_image)
                    )
                }
            },
            modifier = Modifier.padding(end = 8.dp)
        )
        FloatingActionButton(
            onClick = {
                if (uiMode == EditDocumentMode.SCROLL) onSave() else onUpdateUiMode(
                    EditDocumentMode.SCROLL
                )
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Crossfade(
                targetState = uiMode,
                label = "Change ui mode toolbar icon"
            ) { targetState ->
                when (targetState) {
                    EditDocumentMode.SCROLL -> {
                        Icon(
                            painter = painterResource(R.drawable.save_24px),
                            contentDescription = stringResource(R.string.save_changes)
                        )
                    }

                    else -> {
                        Icon(
                            painter = painterResource(R.drawable.check_24px),
                            contentDescription = stringResource(R.string.save_document)
                        )
                    }
                }
            }
        }
    }

    if (isDeleteImageAlertExpanded) {
        AlertDeleteImage(
            onDismiss = { isDeleteImageAlertExpanded = false },
            onConfirm = {
                isDeleteImageAlertExpanded = false
                onDeleteImage.invoke()
            }
        )
    }
}

@Composable
private fun AlertDeleteImage(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(
                painter = painterResource(R.drawable.delete_24px),
                contentDescription = null
            )
        },
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_image)) },
        text = { Text(stringResource(R.string.delete_image_body)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm.invoke()
                }
            ) {
                Text(stringResource(R.string.delete))
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
private fun AlertUnsavedChanges(
    modifier: Modifier = Modifier,
    onKeepEditing: () -> Unit,
    onDiscard: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onKeepEditing,
        title = { Text(stringResource(R.string.discard_changes)) },
        text = { Text(stringResource(R.string.discard_changes_body)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDiscard.invoke()
                }
            ) {
                Text(stringResource(R.string.discard))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onKeepEditing.invoke()
            }) {
                Text(stringResource(R.string.keep_editing))
            }
        }
    )
}