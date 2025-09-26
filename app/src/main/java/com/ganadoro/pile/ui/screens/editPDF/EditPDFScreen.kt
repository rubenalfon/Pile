package com.ganadoro.pile.ui.screens.editPDF

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.R
import com.ganadoro.pile.ui.compostables.LoadingWrapper
import com.ganadoro.pile.ui.screens.editPDF.composables.AddItemCarousel
import com.tanishranjan.cropkit.CropController
import com.tanishranjan.cropkit.CropDefaults
import com.tanishranjan.cropkit.CropShape
import com.tanishranjan.cropkit.ImageCropper
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditPDFScreen(
    modifier: Modifier = Modifier,
    documentId: String,
    popBackStack: () -> Unit,
    onNext: () -> Unit,
    viewModel: EditPDFViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = documentId) {
        if (uiState.documentModel == null || viewModel.onNext == null) {
            viewModel.loadDocument(documentId)
            viewModel.onNext = onNext
        }
    }

    val displayBitmaps = remember(
        uiState.bitmaps,
        uiState.colorEditedBitmaps,
        uiState.cropEditedBitmaps,
        uiState.lastEditType
    ) {
        List(uiState.bitmaps.size) { i ->
            when (uiState.lastEditType[i]) {
                EditType.CROP -> uiState.cropEditedBitmaps[i] ?: uiState.bitmaps[i]
                EditType.COLOR -> uiState.colorEditedBitmaps[i] ?: uiState.bitmaps[i]
                else -> uiState.bitmaps[i]
            }
        }
    }


    val colorScheme = MaterialTheme.colorScheme
    val cropControllers = remember(displayBitmaps) {
        mutableStateListOf<CropController>().apply {
            displayBitmaps.forEach { bitmap ->
                add(
                    CropController(
                        bitmap = bitmap,
                        cropColors = CropDefaults.cropColors(
                            gridlines = colorScheme.tertiary.copy(0.5f),
                            cropRectangle = colorScheme.tertiary.copy(0.5f),
                            handle = colorScheme.tertiary
                        ),
                        cropOptions = CropDefaults.cropOptions(
                            cropShape = CropShape.FreeForm,
                            touchPadding = 30.dp
                        )
                    )
                )
            }
        }
    }


    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            ScreenTopAppBar(popBackStack = popBackStack)
        }
    ) { innerPadding ->
        LoadingWrapper(
            uiState.documentModel == null || displayBitmaps.isEmpty()
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
                    images = displayBitmaps,
                    cropControllers = cropControllers,
                    uiMode = uiState.uiMode,
                    selectedImageIndex = uiState.selectedImageIndex,
                    onSelectImage = { index ->
                        viewModel.setSelectedImageIndex(index)
                    }
                )

                val thumbnailCarouselState = rememberCarouselState { displayBitmaps.count() + 1 }
                AnimatedVisibility(visible = uiState.uiMode == EditPDFUIMode.SCROLL) {
                    ThumbnailCarousel(
                        modifier = Modifier.padding(bottom = 16.dp),
                        state = thumbnailCarouselState,
                        images = displayBitmaps,
                        selectedImageIndex = uiState.selectedImageIndex,
                        onSelectImage = { index ->
                            viewModel.setSelectedImageIndex(index)
                        },
                        onNewImage = { viewModel.addNewImage() }
                    )
                }

                AnimatedVisibility(visible = uiState.uiMode == EditPDFUIMode.COLOR) {
                    LoadingWrapper(
                        modifier = modifier.height(84.dp),
                        isLoading = uiState.colorModifiedBitmaps == null
                    ) {
                        if (uiState.colorModifiedBitmaps == null) return@LoadingWrapper
                        EditColorRow(
                            modifier = Modifier.padding(bottom = 16.dp),
                            colorModifiedImages = uiState.colorModifiedBitmaps!!,
                            selectedImageIndex = uiState.selectedColorIndex[uiState.selectedImageIndex]
                                ?: 0,
                            onSelectImage = { index ->
                                viewModel.setSelectedColorIndex(index)
                            }
                        )
                    }
                }

                AnimatedVisibility(visible = uiState.uiMode == EditPDFUIMode.CROP_ROTATE) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                cropControllers[uiState.selectedImageIndex].rotateAntiClockwise()
                            }
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
                        OutlinedButton(
                            onClick = {
                                cropControllers[uiState.selectedImageIndex] = CropController(
                                    bitmap = displayBitmaps[uiState.selectedImageIndex],
                                    cropColors = CropDefaults.cropColors(
                                        gridlines = colorScheme.tertiary.copy(0.5f),
                                        cropRectangle = colorScheme.tertiary.copy(0.5f),
                                        handle = colorScheme.tertiary
                                    ),
                                    cropOptions = CropDefaults.cropOptions(
                                        cropShape = CropShape.FreeForm,
                                        touchPadding = 30.dp
                                    )
                                )
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.undo_24px),
                                    contentDescription = stringResource(R.string.reset)
                                )
                                Text(stringResource(R.string.reset))
                            }
                        }
                    }
                }

                ToolBar(
                    modifier = Modifier
                        .padding(bottom = ScreenOffset),
                    uiMode = uiState.uiMode,
                    bitmapCount = displayBitmaps.count(),
                    onEditImageColors = {
                        if (uiState.uiMode == EditPDFUIMode.CROP_ROTATE) {
                            viewModel.cropImage(cropControllers[uiState.selectedImageIndex].crop())
                        }
                        viewModel.updateUIMode(
                            if (uiState.uiMode != EditPDFUIMode.COLOR) EditPDFUIMode.COLOR
                            else EditPDFUIMode.SCROLL
                        )
                    },
                    onResizeImage = {
                        if (uiState.uiMode == EditPDFUIMode.CROP_ROTATE) {
                            viewModel.cropImage(cropControllers[uiState.selectedImageIndex].crop())
                        }
                        viewModel.updateUIMode(
                            if (uiState.uiMode != EditPDFUIMode.CROP_ROTATE) EditPDFUIMode.CROP_ROTATE
                            else EditPDFUIMode.SCROLL
                        )
                    },
                    onDeleteImage = {
                        viewModel.deleteSelectedImage()
                        viewModel.updateUIMode(EditPDFUIMode.SCROLL)
                    },
                    onAddDocument = viewModel::onNext
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
    images: List<Bitmap>,
    cropControllers: List<CropController>,
    uiMode: EditPDFUIMode,
    selectedImageIndex: Int,
    onSelectImage: (Int) -> Unit
) {
    val pagerState = rememberPagerState(
        pageCount = { images.size }
    )

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
                onSelectImage(page)
                isChangedFromCarouselRecent = false
            }
    }

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        pageSpacing = 16.dp,
        userScrollEnabled = uiMode == EditPDFUIMode.SCROLL,
        modifier = modifier
    ) { page ->
        if (uiMode != EditPDFUIMode.CROP_ROTATE) {
            Image(
                bitmap = images[page].asImageBitmap(),
                contentDescription = stringResource(R.string.image_number, page + 1),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentScale = ContentScale.Fit
            )
        } else {
            ImageCropper(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                cropController = cropControllers[page]
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThumbnailCarousel(
    modifier: Modifier = Modifier,
    state: CarouselState,
    images: List<Bitmap>,
    selectedImageIndex: Int,
    onSelectImage: (Int) -> Unit,
    onNewImage: () -> Unit
) {
    HorizontalMultiBrowseCarousel(
        state = state,
        modifier = modifier
            .wrapContentHeight()
            .height(84.dp),
        preferredItemWidth = 84.dp,
        flingBehavior = CarouselDefaults.noSnapFlingBehavior(),
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { i ->
        if (i == images.count()) {
            AddItemCarousel(
                modifier = Modifier
                    .maskClip(RoundedCornerShape(20.dp)),
                onItemClick = { onNewImage() }
            )
            return@HorizontalMultiBrowseCarousel
        }

        val isSelected = i == selectedImageIndex

        val animatedCornerRadius by animateDpAsState(
            if (isSelected) 42.dp else 20.dp,
            animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
        )

        Box(
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier
                    .aspectRatio(1f)
                    .then(
                        key(animatedCornerRadius) {
                            Modifier.maskClip(RoundedCornerShape(animatedCornerRadius))
                        }
                    )
                    .clickable {
                        onSelectImage.invoke(i)
                    }
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                bitmap = images[i].asImageBitmap(),
                contentDescription = stringResource(R.string.image_number, i + 1),
                contentScale = ContentScale.Crop
            )

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
    colorModifiedImages: List<Bitmap>,
    selectedImageIndex: Int,
    onSelectImage: (Int) -> Unit,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(colorModifiedImages.size) { i ->
            Box(
                contentAlignment = Alignment.Center
            ) {
                val isSelected = i == selectedImageIndex

                val animatedCornerRadius by animateDpAsState(
                    if (isSelected) 42.dp else 20.dp,
                    animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                )

                Image(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .size(84.dp)
                        .clip(RoundedCornerShape(animatedCornerRadius))
                        .clickable {
                            onSelectImage.invoke(i)
                        }
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    bitmap = colorModifiedImages[i].asImageBitmap(),
                    contentDescription = stringResource(R.string.image_number, i + 1),
                    contentScale = ContentScale.Crop
                )

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
    uiMode: EditPDFUIMode,
    bitmapCount: Int,
    onEditImageColors: () -> Unit,
    onResizeImage: () -> Unit,
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
                    onClick = onEditImageColors,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (uiMode == EditPDFUIMode.COLOR) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                        contentColor = if (uiMode == EditPDFUIMode.COLOR) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.monochrome_photos_24px),
                        contentDescription = stringResource(R.string.edit_image_colors)
                    )
                }
                IconButton(
                    onClick = onResizeImage, colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (uiMode == EditPDFUIMode.CROP_ROTATE) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                        contentColor = if (uiMode == EditPDFUIMode.CROP_ROTATE) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.transform_24px),
                        contentDescription = stringResource(R.string.resize_image)
                    )
                }
                IconButton(
                    enabled = bitmapCount > 1,
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
            visible = uiMode == EditPDFUIMode.SCROLL,
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