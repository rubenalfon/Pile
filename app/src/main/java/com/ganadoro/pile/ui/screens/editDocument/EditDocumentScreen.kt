package com.ganadoro.pile.ui.screens.editDocument

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ganadoro.pile.R
import com.ganadoro.pile.domain.models.ImageFilterType
import com.ganadoro.pile.ui.composables.LoadingWrapper
import com.ganadoro.pile.ui.controllers.rememberDocumentImportController
import com.ganadoro.pile.ui.screens.editDocument.EditDocumentUIMode.COLOR
import com.ganadoro.pile.ui.screens.editDocument.EditDocumentUIMode.CROP_ROTATE
import com.ganadoro.pile.ui.screens.editDocument.EditDocumentUIMode.SCROLL
import com.ganadoro.pile.ui.screens.editDocument.composables.AddItemCarousel
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
    viewModel: EditPDFViewModel = koinViewModel { parametersOf(documentId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bitmapCache by viewModel.bitmapCache.collectAsStateWithLifecycle()

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
            ScreenTopAppBar(popBackStack = popBackStack)
        }
    ) { innerPadding ->
        LoadingWrapper(
            uiState.documentModel == null || uiState.documentImages.isEmpty()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ImagePager(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 16.dp),
                    imageCount = uiState.documentImages.count(),
                    bitmapCache = bitmapCache,
                    onLoadBitmap = viewModel::requestBitmapLoad,
                    onRequestImageKey = viewModel::requestImageKey,
                    uiMode = uiState.uiMode,
                    selectedImageIndex = uiState.selectedImageIndex,
                    onSelectImageIndex = { viewModel.setSelectedImageIndex(it) }
                )

                AnimatedVisibility(visible = uiState.uiMode == SCROLL) {
                    ThumbnailCarousel(
                        modifier = Modifier.padding(bottom = 16.dp),
                        imageCount = uiState.documentImages.count(),
                        bitmapCache = bitmapCache,
                        onLoadBitmap = viewModel::requestBitmapLoad,
                        onRequestImageKey = viewModel::requestImageKey,
                        selectedImageIndex = uiState.selectedImageIndex,
                        onSelectImage = { viewModel.setSelectedImageIndex(it) },
                        onNewImage = importActions.launchGallery
                    )
                }

                AnimatedVisibility(visible = uiState.uiMode == COLOR) {
                    LoadingWrapper(
                        modifier = modifier.height(84.dp),
                        isLoading = /*TODO uiState.colorModifiedBitmaps == null*/ false
                    ) {
                        val imageFilters = uiState.imageFilters ?: return@LoadingWrapper

                        val selectedDocumentImage =
                            uiState.documentImages.getOrNull(uiState.selectedImageIndex)

                        EditColorRow(
                            modifier = Modifier.padding(bottom = 16.dp),
                            imageFilters = imageFilters,
                            documentImage = selectedDocumentImage?.filter?.toInt() ?: 0,
                            onSelectColorIndex = viewModel::setSelectedColorIndex
                        )
                    }
                }

//                AnimatedVisibility(visible = uiState.uiMode == EditDocumentUIMode.CROP_ROTATE) {
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        modifier = Modifier.padding(bottom = 16.dp)
//                    ) {
//                        OutlinedButton(
//                            onClick = {
//                                cropControllers[uiState.selectedImageIndex].rotateAntiClockwise()
//                            }
//                        ) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.spacedBy(8.dp)
//                            ) {
//                                Icon(
//                                    painter = painterResource(R.drawable.rotate_24px),
//                                    contentDescription = stringResource(R.string.rotate_image_counterclockwise)
//                                )
//                                Text(stringResource(R.string.rotate))
//                            }
//                        }
//                        OutlinedButton(
//                            onClick = {
//                                cropControllers[uiState.selectedImageIndex] = CropController(
//                                    bitmap = displayBitmaps[uiState.selectedImageIndex],
//                                    cropColors = CropDefaults.cropColors(
//                                        gridlines = colorScheme.tertiary.copy(0.5f),
//                                        cropRectangle = colorScheme.tertiary.copy(0.5f),
//                                        handle = colorScheme.tertiary
//                                    ),
//                                    cropOptions = CropDefaults.cropOptions(
//                                        cropShape = CropShape.FreeForm,
//                                        touchPadding = 30.dp
//                                    )
//                                )
//                            }
//                        ) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.spacedBy(8.dp)
//                            ) {
//                                Icon(
//                                    painter = painterResource(R.drawable.undo_24px),
//                                    contentDescription = stringResource(R.string.reset)
//                                )
//                                Text(stringResource(R.string.reset))
//                            }
//                        }
//                    }
//                }

                ToolBar(
                    modifier = Modifier
                        .padding(bottom = ScreenOffset),
                    uiMode = uiState.uiMode,
                    imageCount = uiState.documentImages.count(),
                    onUpdateUiMode = viewModel::updateUIMode,
                    onDeleteImage = viewModel::deleteSelectedImage,
                    onAddDocument = onNext
                )
            }
        }
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
    imageCount: Int,
    bitmapCache: Map<String, Bitmap>,
    onLoadBitmap: suspend (pageNumber: Int) -> Unit,
    onRequestImageKey: (pageNumber: Int) -> String,
    uiMode: EditDocumentUIMode,
    selectedImageIndex: Int,
    onSelectImageIndex: (Int) -> Unit
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
        userScrollEnabled = uiMode == SCROLL,
        modifier = modifier
    ) { page ->
        val key = onRequestImageKey(page)
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
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThumbnailCarousel(
    modifier: Modifier = Modifier,
    imageCount: Int,
    bitmapCache: Map<String, Bitmap>,
    onLoadBitmap: suspend (pageNumber: Int) -> Unit,
    onRequestImageKey: (pageNumber: Int) -> String,
    selectedImageIndex: Int,
    onSelectImage: (Int) -> Unit,
    onNewImage: () -> Unit
) {
    val thumbnailCarouselState = rememberCarouselState { imageCount + 1 }

    HorizontalMultiBrowseCarousel(
        state = thumbnailCarouselState,
        modifier = modifier
            .wrapContentHeight()
            .height(84.dp),
        preferredItemWidth = 84.dp,
        flingBehavior = CarouselDefaults.noSnapFlingBehavior(),
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { page ->
        if (page == imageCount) {
            AddItemCarousel(
                modifier = Modifier
                    .width(84.dp)
                    .maskClip(RoundedCornerShape(20.dp)),
                onItemClick = onNewImage
            )
            return@HorizontalMultiBrowseCarousel
        }

        val isSelected = page == selectedImageIndex

        val animatedCornerRadius by animateDpAsState(
            if (isSelected) 42.dp else 20.dp,
            animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
        )

        Box(
            contentAlignment = Alignment.Center
        ) {
            val key = onRequestImageKey(page)
            val cachedBitmap: Bitmap? = bitmapCache[key]

            if (cachedBitmap == null) {
                LaunchedEffect(key1 = key) {
                    onLoadBitmap(page)
                }
            }
            LoadingWrapper(cachedBitmap == null) {
                if (cachedBitmap == null) return@LoadingWrapper
                Image(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .then(
                            key(animatedCornerRadius) {
                                Modifier.maskClip(RoundedCornerShape(animatedCornerRadius))
                            }
                        )
                        .clickable {
                            onSelectImage.invoke(page)
                        }
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    bitmap = cachedBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.image_number, page + 1),
                    contentScale = ContentScale.Crop
                )
            }

            AnimatedVisibility(
                isSelected,
                enter = fadeIn(animationSpec = MaterialTheme.motionScheme.slowEffectsSpec()),
                exit = fadeOut(animationSpec = MaterialTheme.motionScheme.slowEffectsSpec())
            ) {
                val infiniteTransition = rememberInfiniteTransition()

                val angle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 15000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .graphicsLayer {
                            rotationZ = angle
                        }
                        .clip(MaterialShapes.Cookie9Sided.toShape())
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EditColorRow(
    modifier: Modifier = Modifier,
    imageFilters: List<ImageFilterType>,
    documentImage: Int,
    onSelectColorIndex: (Int) -> Unit,
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
                val isSelected = i == documentImage

                val animatedCornerRadius by animateDpAsState(
                    if (isSelected) 42.dp else 20.dp,
                    animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                )

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .size(84.dp)
                        .clip(RoundedCornerShape(animatedCornerRadius))
                        .clickable {
                            onSelectColorIndex.invoke(i)
                        }
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                )
//       TODO         Image(
//                    modifier = Modifier
//                        .aspectRatio(1f)
//                        .size(84.dp)
//                        .clip(RoundedCornerShape(animatedCornerRadius))
//                        .clickable {
//                            onSelectColorIndex.invoke(i)
//                        }
//                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
//                    bitmap = colorModifiedImages[i].asImageBitmap(),
//                    contentDescription = stringResource(R.string.image_number, i + 1),
//                    contentScale = ContentScale.Crop
//                )

                AnimatedVisibility(
                    isSelected,
                    enter = fadeIn(animationSpec = MaterialTheme.motionScheme.slowEffectsSpec()),
                    exit = fadeOut(animationSpec = MaterialTheme.motionScheme.slowEffectsSpec())
                ) {
                    val infiniteTransition = rememberInfiniteTransition()

                    val angle by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 15000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        )
                    )

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .graphicsLayer {
                                rotationZ = angle
                            }
                            .clip(MaterialShapes.Cookie9Sided.toShape())
                            .background(MaterialTheme.colorScheme.surface)
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ToolBar(
    modifier: Modifier = Modifier,
    uiMode: EditDocumentUIMode,
    imageCount: Int,
    onUpdateUiMode: (uiMode: EditDocumentUIMode) -> Unit,
    onDeleteImage: () -> Unit,
    onAddDocument: () -> Unit
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
                    onClick = { onUpdateUiMode(COLOR) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (uiMode == COLOR) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                        contentColor = if (uiMode == COLOR) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.monochrome_photos_24px),
                        contentDescription = stringResource(R.string.edit_image_colors)
                    )
                }
                IconButton(
                    onClick = { onUpdateUiMode(CROP_ROTATE) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (uiMode == CROP_ROTATE) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                        contentColor = if (uiMode == CROP_ROTATE) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.transform_24px),
                        contentDescription = stringResource(R.string.resize_image)
                    )
                }
                IconButton(
                    enabled = imageCount > 1,
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
        AnimatedVisibility(
            visible = uiMode == SCROLL,
        ) {
            FloatingActionButton(
                onClick = onAddDocument,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    painter = painterResource(R.drawable.check_24px),
                    contentDescription = stringResource(R.string.add_document)
                )
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