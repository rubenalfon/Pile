package com.ganadoro.pile.ui.screens.addDocument

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonDefaults.smallContainerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ganadoro.pile.R
import com.ganadoro.pile.ui.compostables.AlertNewPile
import com.ganadoro.pile.ui.compostables.KeyboardAware
import com.ganadoro.pile.ui.compostables.LoadingWrapper
import com.ganadoro.pile.ui.compostables.itemPileGrid
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddDocumentScreen(
    modifier: Modifier = Modifier,
    documentId: String,
    popBackStack: () -> Unit,
    navigateToDocumentDetail: (String) -> Unit,
    viewModel: AddDocumentViewModel = koinViewModel { parametersOf(documentId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val bitmapCache by viewModel.bitmapCache.collectAsStateWithLifecycle()

    var isNewPileAlertExpanded by rememberSaveable { mutableStateOf(false) }

    KeyboardAware {
        Scaffold(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                ScreenTopAppBar(popBackStack = popBackStack)
            },
            floatingActionButton = {
                MediumFloatingActionButton(
                    onClick = { viewModel.saveDocument(onSuccess = {
                        navigateToDocumentDetail(documentId)
                    }) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        painter = painterResource(R.drawable.check_24px),
                        contentDescription = stringResource(R.string.add_document)
                    )
                }
            }
        ) { innerPadding ->
            LoadingWrapper(
                uiState.documentModel == null || uiState.allPileModels == null
            ) {
                var availableWidth by remember { mutableStateOf(0.dp) }
                val density = LocalDensity.current

                val colorScheme = MaterialTheme.colorScheme
                val layoutDirection = LocalLayoutDirection.current

                Box(
                    Modifier
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            start = innerPadding.calculateStartPadding(layoutDirection),
                            end = innerPadding.calculateEndPadding(layoutDirection)
                        )
                        .fillMaxSize()
                        .background(colorScheme.surfaceContainer)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(bottom = innerPadding.calculateBottomPadding())
                            .background(colorScheme.surface)
                            .onGloballyPositioned { coordinates ->
                                val widthPx = coordinates.size.width
                                availableWidth = with(density) { widthPx.toDp() }.value.dp
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            ) {
                                val imageId = uiState.frontPageDocumentImage?.id ?: return@Box

                                val cachedBitmap = bitmapCache[imageId]

                                if (cachedBitmap == null) {
                                    LaunchedEffect(key1 = imageId) {
                                        viewModel.requestBitmapLoad(documentId, imageId)
                                    }
                                }

                                LoadingWrapper(cachedBitmap == null) {
                                    Image(
                                        bitmap = cachedBitmap!!.asImageBitmap(),
                                        contentDescription = stringResource(R.string.document_first_image),
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }

                        item { Spacer(Modifier.height(16.dp)) }

                        item {
                            OutlinedTextField(
                                value = uiState.documentName,
                                onValueChange = { viewModel.setDocumentName(it) },
                                label = { Text(stringResource(R.string.document_name)) },
                                trailingIcon = {
                                    if (uiState.documentName.isNotEmpty()) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.delete_text),
                                            modifier = Modifier.clickable {
                                                viewModel.setDocumentName(
                                                    ""
                                                )
                                            })
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                isError = uiState.noDocumentNameError,
                                supportingText = {
                                    AnimatedVisibility(
                                        visible = uiState.noDocumentNameError,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        Text(stringResource(R.string.document_no_name_error))
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences
                                )
                            )
                        }

                        item { Spacer(Modifier.height(16.dp)) }

                        item {
                            Text(
                                text = stringResource(R.string.add_to_piles),
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                    .background(colorScheme.surfaceContainer)
                                    .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                            )
                        }

                        itemPileGrid(
                            availableWidth = availableWidth,
                            piles = uiState.allPileModels!!,
                            onPileClick = { pileId ->
                                viewModel.updatePileSelectState(pileId)
                            },
                            onNewPileClick = { isNewPileAlertExpanded = true },
                            coloredPileIds = uiState.selectedPileModelIds,
                            backgroundColor = colorScheme.surfaceContainer
                        )

                        item {
                            Box(
                                Modifier
                                    .height(104.dp)
                                    .fillMaxWidth()
                                    .background(colorScheme.surfaceContainer)
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
                viewModel.addPile(pileName, pileIconId, pileColorNumber)
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
            Text(stringResource(R.string.add_document))
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
