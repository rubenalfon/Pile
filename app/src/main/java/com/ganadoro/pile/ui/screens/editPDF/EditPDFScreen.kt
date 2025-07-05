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
import androidx.compose.runtime.collectAsState
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
import com.ganadoro.pile.R
import com.ganadoro.pile.ui.compostables.LoadingWrapper
import com.ganadoro.pile.ui.screens.editPDF.composables.AddItemCarousel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.getViewModel


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditPDFScreen(
    modifier: Modifier = Modifier,
    documentId: String,
    popBackStack: () -> Unit,
    onNext: () -> Unit,
    viewModel: EditPDFViewModel = getViewModel<EditPDFViewModel>()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = documentId) {
        if (uiState.documentModel == null) {
            Napier.d { "EditPDFScreen: $documentId" }
            viewModel.loadDocument(documentId)
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
            uiState.documentModel == null || uiState.bitmaps.isEmpty()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ImagePager(
                    modifier = Modifier.weight(1f),
                    images = uiState.bitmaps,
                    selectedImageIndex = uiState.selectedImageIndex,
                    onSelectImage = { index ->
                        viewModel.setSelectedImageIndex(index)
                    }
                )
                ThumbnailCarousel(
                    images = uiState.bitmaps,
                    selectedImageIndex = uiState.selectedImageIndex,
                    onSelectImage = { index ->
                        viewModel.setSelectedImageIndex(index)
                    },
                    onNewImage = { viewModel.addNewImage() }
                )

                ToolBar(
                    modifier = Modifier
                        .padding(bottom = ScreenOffset),
                    bitmapCount = uiState.bitmaps.count(),
                    onEditImageColors = { },
                    onResizeImage = { },
                    onDeleteImage = {
                        viewModel.deleteSelectedImage()
                    },
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
    images: List<Bitmap>,
    selectedImageIndex: Int,
    onSelectImage: (Int) -> Unit,
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

        snapshotFlow { pagerState.currentPage } // TODO: Fix Performance hit
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
        modifier = modifier
    ) { page ->
        Image(
            bitmap = images[page].asImageBitmap(),
            contentDescription = stringResource(R.string.image_number, page + 1),
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentScale = ContentScale.Fit
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThumbnailCarousel(
    modifier: Modifier = Modifier,
    images: List<Bitmap>,
    selectedImageIndex: Int,
    onSelectImage: (Int) -> Unit,
    onNewImage: () -> Unit
) {
    val state = rememberCarouselState { images.count() + 1 }
    // TODO: Navigate to selectedImageIndex when changed from pager

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

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ToolBar(
    modifier: Modifier = Modifier,
    bitmapCount: Int,
    onEditImageColors: () -> Unit,
    onResizeImage: () -> Unit,
    onDeleteImage: () -> Unit,
    onAddDocument: () -> Unit
) {
    var isDeleteImageAlertExpanded by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalFloatingToolbar(
            expanded = true,
            content = {
                IconButton(onClick = onEditImageColors) {
                    Icon(
                        painter = painterResource(R.drawable.monochrome_photos_24px),
                        contentDescription = stringResource(R.string.edit_image_colors)
                    )
                }
                IconButton(onClick = onResizeImage) {
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
            }
        )
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