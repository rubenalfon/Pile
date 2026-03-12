package com.ganadoro.pile.features.editDocument.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ganadoro.pile.R
import com.ganadoro.pile.core.domain.models.ImageFilterType
import com.ganadoro.pile.core.ui.composables.LoadingAlert
import com.ganadoro.pile.core.ui.composables.LoadingWrapper
import com.ganadoro.pile.core.ui.controllers.rememberDocumentImportController
import com.ganadoro.pile.features.editDocument.ui.composables.ActiveIndicator
import com.ganadoro.pile.features.editDocument.ui.composables.AddItemCarousel
import com.tanishranjan.cropkit.CropController
import com.tanishranjan.cropkit.ImageCropper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditDocumentScreen(
    modifier: Modifier = Modifier,
    documentId: String,
    popBackStack: () -> Unit,
    onNext: () -> Unit,
    viewModel: EditDocumentViewModel = koinViewModel { parametersOf(documentId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bitmapCache by viewModel.bitmapCache.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { navigationType ->
            when (navigationType) {
                EditDocumentViewModel.NavigationType.BACK -> popBackStack()
                EditDocumentViewModel.NavigationType.NEXT -> onNext()
            }
        }
    }

    // Only intended for selecting images on the gallery.
    val importActions = rememberDocumentImportController(
        onPdfSelected = {},
        onImagesSelected = viewModel::addNewImage,
        createTempImageUri = { null }
    )

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            ScreenTopAppBar(popBackStack = viewModel::onNavigateBack)
        }
    ) { innerPadding ->
        LoadingWrapper(
            uiState.draftDocument == null || uiState.documentImages.isEmpty()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ImagePager(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .weight(1f),
                    uiMode = uiState.uiMode,
                    selectedImageIndex = uiState.selectedImageIndex,
                    imageCount = uiState.documentImages.count(),
                    bitmapCache = bitmapCache,
                    cropControllers = uiState.cropControllers,
                    onLoadBitmap = viewModel::requestBitmapLoad,
                    onRequestImageKey = viewModel::requestImageKey,
                    onSelectImageIndex = viewModel::setSelectedImageIndex,
                    onLoadCropController = viewModel::loadCropController
                )

                val lazyListState = rememberLazyListState()
                val selectedImageIndex = uiState.selectedImageIndex

                var recentlyMoved by remember { mutableStateOf(false) }

                LaunchedEffect(selectedImageIndex) {
                    if (recentlyMoved) return@LaunchedEffect

                    if (selectedImageIndex < 0) return@LaunchedEffect

                    val totalItems = lazyListState.layoutInfo.totalItemsCount
                    if (selectedImageIndex >= totalItems) return@LaunchedEffect

                    if (!lazyListState.canScrollBackward && !lazyListState.canScrollForward) return@LaunchedEffect

                    lazyListState.animateScrollToItem(selectedImageIndex, -150)
                }

                LaunchedEffect(recentlyMoved) {
                    delay(100)
                    recentlyMoved = false
                }

                AnimatedVisibility(visible = uiState.uiMode == EditDocumentUIMode.SCROLL) {
                    ThumbnailRow(
                        modifier = Modifier.padding(bottom = 16.dp),
                        lazyListState = lazyListState,
                        imageCount = uiState.documentImages.count(),
                        bitmapCache = bitmapCache,
                        onLoadBitmap = viewModel::requestBitmapLoad,
                        onRequestImageKey = viewModel::requestImageKey,
                        selectedImageIndex = selectedImageIndex,
                        onSelectImage = {
                            recentlyMoved = true
                            viewModel.setSelectedImageIndex(it)
                        },
                        onNewImage = importActions.launchGallery
                    )
                }

                AnimatedVisibility(visible = uiState.uiMode == EditDocumentUIMode.COLOR) {
                    val imageFilters = uiState.imageFilters ?: return@AnimatedVisibility

                    val selectedImage = uiState.documentImages.getOrNull(uiState.selectedImageIndex)

                    EditColorRow(
                        modifier = Modifier.padding(bottom = 16.dp),
                        imageFilters = imageFilters,
                        activeFilterIndex = selectedImage?.filter?.toInt() ?: 0,
                        onSelectColorIndex = viewModel::setSelectedColorIndex,
                        bitmapCache = bitmapCache,
                        onLoadThumbnail = viewModel::requestThumbnailLoad,
                        onRequestThumbnailKey = viewModel::requestThumbnailKey
                    )
                }

                AnimatedVisibility(visible = uiState.uiMode == EditDocumentUIMode.CROP_ROTATE) {
                    CropRotateButtons(
                        onRotate = viewModel::rotateImage,
                    )
                }

                ToolBar(
                    modifier = Modifier
                        .padding(bottom = ScreenOffset),
                    uiMode = uiState.uiMode,
                    isSinglePage = uiState.documentImages.count() == 1,
                    onUpdateUiMode = viewModel::updateUIMode,
                    onDeleteImage = viewModel::deleteSelectedImage,
                    onSave = viewModel::onNavigateNext,
                )
            }
        }
    }

    if (uiState.isLoadingNewImage) {
        LoadingAlert(stringResource(R.string.adding_images))
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
    uiMode: EditDocumentUIMode,
    selectedImageIndex: Int,
    imageCount: Int,
    bitmapCache: Map<String, Bitmap>,
    cropControllers: Map<String, CropController>,
    onLoadBitmap: (pageNumber: Int) -> Unit,
    onRequestImageKey: (pageNumber: Int) -> String,
    onSelectImageIndex: (page: Int) -> Unit,
    onLoadCropController: (key: String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { imageCount })

    var isSelectedImageIndexRecent by rememberSaveable { mutableStateOf(false) }
    var isChangedFromCarouselRecent by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(selectedImageIndex) {
        if (isChangedFromCarouselRecent) return@LaunchedEffect

        if (pagerState.isScrollInProgress) return@LaunchedEffect

        if (pagerState.currentPage == selectedImageIndex) return@LaunchedEffect

        isSelectedImageIndexRecent = true
        pagerState.animateScrollToPage(selectedImageIndex)
        isSelectedImageIndexRecent = false
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (isSelectedImageIndexRecent) return@collect

                isChangedFromCarouselRecent = true
                onSelectImageIndex(page)
                isChangedFromCarouselRecent = false
            }
    }

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        pageSpacing = 16.dp,
        userScrollEnabled = uiMode == EditDocumentUIMode.SCROLL,
        modifier = modifier
    ) { page ->
        val key = onRequestImageKey(page)
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
            if (uiMode != EditDocumentUIMode.CROP_ROTATE) {
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
                val cropController = cropControllers[key]

                if (cropController == null) {
                    LaunchedEffect(key1 = cachedBitmap) {
                        if (cachedBitmap == null) return@LaunchedEffect
                        onLoadCropController(key)
                    }
                }
                LoadingWrapper(cropController == null) {
                    if (cropController == null) return@LoadingWrapper
                    ImageCropper(
                        modifier = Modifier
                            .fillMaxSize(),
                        cropController = cropController
                    )
                }
            }
        }
    }
}

@Composable
private fun ThumbnailRow(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    imageCount: Int,
    bitmapCache: Map<String, Bitmap>,
    onLoadBitmap: (pageNumber: Int) -> Unit,
    onRequestImageKey: (pageNumber: Int) -> String,
    selectedImageIndex: Int,
    onSelectImage: (Int) -> Unit,
    onNewImage: () -> Unit
) {
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
            items(
                count = imageCount,
                key = { index -> onRequestImageKey(index) }
            ) { index ->
                ThumbnailItem(
                    modifier = Modifier.animateItem(),
                    index = index,
                    isSelected = index == selectedImageIndex,
                    cacheKey = onRequestImageKey(index),
                    bitmapCache = bitmapCache,
                    onLoadBitmap = onLoadBitmap,
                    onSelect = onSelectImage
                )
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
private fun ThumbnailItem(
    modifier: Modifier,
    index: Int,
    isSelected: Boolean,
    cacheKey: String,
    bitmapCache: Map<String, Bitmap>,
    onLoadBitmap: (index: Int) -> Unit,
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

    Box(
        modifier = modifier,
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
                    .clickable { onSelect(index) }
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
    bitmapCache: Map<String, Bitmap>,
    onLoadThumbnail: suspend (filterNumber: Int) -> Unit,
    onRequestThumbnailKey: (filterNumber: Int) -> String
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
                val key = onRequestThumbnailKey(i)
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

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ToolBar(
    modifier: Modifier = Modifier,
    uiMode: EditDocumentUIMode,
    isSinglePage: Boolean,
    onUpdateUiMode: (uiMode: EditDocumentUIMode) -> Unit,
    onDeleteImage: () -> Unit,
    onSave: () -> Unit,
) {
    var isDeleteImageAlertExpanded by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalFloatingToolbar(
            expanded = true,
            content = {
                IconButton(
                    onClick = { onUpdateUiMode(EditDocumentUIMode.COLOR) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (uiMode == EditDocumentUIMode.COLOR) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                        contentColor = if (uiMode == EditDocumentUIMode.COLOR) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.monochrome_photos_24px),
                        contentDescription = stringResource(R.string.edit_image_colors)
                    )
                }
                IconButton(
                    onClick = { onUpdateUiMode(EditDocumentUIMode.CROP_ROTATE) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (uiMode == EditDocumentUIMode.CROP_ROTATE) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                        contentColor = if (uiMode == EditDocumentUIMode.CROP_ROTATE) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
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
                if (uiMode == EditDocumentUIMode.SCROLL) onSave() else onUpdateUiMode(
                    EditDocumentUIMode.SCROLL
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
                    EditDocumentUIMode.SCROLL -> {
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