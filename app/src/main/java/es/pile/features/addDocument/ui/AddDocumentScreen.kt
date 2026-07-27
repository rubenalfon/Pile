package es.pile.features.addDocument.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pile.R
import es.pile.core.ui.composables.AlertNewPile
import es.pile.core.ui.composables.KeyboardAware
import es.pile.core.ui.composables.LoadingWrapper
import es.pile.core.ui.composables.itemPileGrid
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddDocumentScreen(
    modifier: Modifier = Modifier,
    documentId: String,
    popBackStack: () -> Unit,
    navigateToDocumentDetail: (String) -> Unit,
    viewModel: AddDocumentViewModel = koinViewModel { parametersOf(documentId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bitmapCache by viewModel.bitmapCache.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect {
            navigateToDocumentDetail(documentId)
        }
    }

    var isNewPileAlertExpanded by rememberSaveable { mutableStateOf(false) }

    val hapticFeedback = LocalHapticFeedback.current

    var hasRequestedFocus by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val documentName = state.documentName ?: ""

    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = documentName,
                selection = TextRange(documentName.length)
            )
        )
    }

    LaunchedEffect(state.documentName) {
        if (state.documentName != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = documentName,
                selection = TextRange(documentName.length)
            )
        }
    }

    LaunchedEffect(Unit) {
        if (!hasRequestedFocus) {
            delay(100.milliseconds) // Prevents errors
            focusRequester.requestFocus()
            hasRequestedFocus = true
        }
    }

    KeyboardAware {
        Scaffold(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                ScreenTopAppBar(popBackStack = popBackStack)
            },
            floatingActionButton = {
                MediumFloatingActionButton(
                    onClick = { viewModel.handleEvent(AddDocumentEvent.OnSaveDocument) },
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
                state.documentModel == null || state.allPileModels == null
            ) {
                val colorScheme = MaterialTheme.colorScheme
                val layoutDirection = LocalLayoutDirection.current

                BoxWithConstraints(
                    Modifier
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            start = innerPadding.calculateStartPadding(layoutDirection),
                            end = innerPadding.calculateEndPadding(layoutDirection)
                        )
                        .fillMaxSize()
                        .background(colorScheme.surfaceContainer)
                ) {
                    val availableWidth = maxWidth

                    LazyColumn(
                        modifier = Modifier
                            .padding(bottom = innerPadding.calculateBottomPadding())
                            .background(colorScheme.surface),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            ) {
                                val imageId = state.coverImageCacheKey
                                val cachedBitmap: Bitmap? = bitmapCache[imageId]

                                if (cachedBitmap == null) {
                                    LaunchedEffect(key1 = imageId) {
                                        viewModel.handleEvent(AddDocumentEvent.OnImageVisible)
                                    }
                                }

                                LoadingWrapper(cachedBitmap == null) {
                                    if (cachedBitmap == null) return@LoadingWrapper
                                    Image(
                                        bitmap = cachedBitmap.asImageBitmap(),
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
                                value = textFieldValue,
                                onValueChange = {
                                    textFieldValue = it
                                    viewModel.handleEvent(AddDocumentEvent.OnNameChanged(it.text))
                                },
                                label = { Text(stringResource(R.string.document_name)) },
                                trailingIcon = {
                                    if (textFieldValue.text.isNotEmpty()) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.delete_text),
                                            modifier = Modifier.clickable {
                                                textFieldValue = textFieldValue.copy(text = "")
                                                viewModel.handleEvent(AddDocumentEvent.OnNameChanged(""))
                                            })
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .focusRequester(focusRequester),
                                isError = state.noDocumentNameError,
                                supportingText = {
                                    AnimatedVisibility(
                                        visible = state.noDocumentNameError,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        Text(stringResource(R.string.document_no_name_error))
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                    }
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
                            piles = state.allPileModels!!,
                            onPileClick = { pileId ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                viewModel.handleEvent(AddDocumentEvent.OnAddPile(pileId))
                            },
                            onNewPileClick = { isNewPileAlertExpanded = true },
                            coloredPileIds = state.documentModel?.documentPileIds ?: emptyList(),
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
                viewModel.handleEvent(AddDocumentEvent.OnCreateNewPile(pileName, pileIconId, pileColorNumber))
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
