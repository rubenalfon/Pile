package com.ganadoro.pile.ui.screens.documentDetail

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarDefaults.vibrantFloatingToolbarColors
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonDefaults.smallContainerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.R
import com.ganadoro.pile.models.DocumentDetail
import com.ganadoro.pile.models.StringDetail
import com.ganadoro.pile.ui.compostables.LoadingWrapper
import com.ganadoro.pile.ui.compostables.Pile
import com.ganadoro.pile.ui.screens.documentDetail.composables.SectionTitleBar
import com.ganadoro.pile.ui.screens.documentDetail.composables.SimpleTextField
import kotlinx.coroutines.delay
import org.koin.androidx.compose.getViewModel
import sh.calvin.reorderable.ReorderableColumn
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DocumentDetailScreen(
    modifier: Modifier = Modifier,
    documentId: String,
    navigateToPileDetail: (pileId: String) -> Unit,
    popBackStack: () -> Unit,
    viewModel: DocumentDetailViewModel = getViewModel<DocumentDetailViewModel>()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.documentModel == null) {
        viewModel.loadDocument(documentId)
    }

    val documentModel = uiState.documentModel

    var isRenameDocumentAlertExpanded by rememberSaveable { mutableStateOf(false) }
    var isDeleteDocumentAlertExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            ScreenTopAppBar(
                popBackStack = popBackStack,
                title = documentModel?.title ?: ""
            ) // TODO is this correct?
        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
        ) {
            LoadingWrapper(
                documentModel == null || uiState.documentPileModels == null
            ) {
                var isDocumentDetailsEditing by rememberSaveable { mutableStateOf(false) }

                var unsavedDocumentDetails by rememberSaveable {
                    mutableStateOf(
                        uiState.documentModel?.documentDetails ?: emptyList()
                    )
                }

                LaunchedEffect(key1 = unsavedDocumentDetails) {
                    delay(500)

                    if (unsavedDocumentDetails == uiState.documentModel?.documentDetails) return@LaunchedEffect

                    viewModel.updateDocumentDetails(unsavedDocumentDetails)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    item {
                        ImagePager(
                            images = uiState.bitmaps,
                            onClick = { viewModel.openDocumentPDF() }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }

                    documentDetailsSection(
                        documentDetails = unsavedDocumentDetails,
                        isEditingMode = isDocumentDetailsEditing,
                        updateEditingMode = { isDocumentDetailsEditing = it },
                        updateDocumentDetails = { unsavedDocumentDetails = it }
                    )

                    item { Spacer(Modifier.height(16.dp)) }

                    item {
                        DocumentNoteSection(
                            documentModel = uiState.documentModel,
                            onUpdateDocumentNote = viewModel::updateDocumentNote
                        )
                    }

                    item { Spacer(Modifier.height(16.dp)) }

                    documentPilesSection(
                        documentPileModels = uiState.documentPileModels ?: emptyList(),
                        onPileClick = navigateToPileDetail, // TODO No se actualiza el nombre de la pila. Que pasa si la pila se borra?
                        onEditDocumentPiles = { /*TODO Editar las pilas del documento*/ }
                    )

                    item { Spacer(Modifier.height(16.dp)) }

                    item {
                        AddedSection(documentModel = documentModel!!)
                    }
                    item { Spacer(Modifier.height(330.dp)) }
                }
            }

            ToolBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = -ScreenOffset),
                onRenameDocument = {
                    isRenameDocumentAlertExpanded = true
                },
                onDeleteDocument = {
                    isDeleteDocumentAlertExpanded = true
                },
                onDownloadDocument = { },
                onShareDocument = viewModel::openShareSheet,
                onEditDocument = { },
            )
        }
    }

    if (isRenameDocumentAlertExpanded) {
        AlertEditDocument(
            documentName = documentModel?.title ?: "",
            onDismiss = { isRenameDocumentAlertExpanded = false },
            onConfirm = { newDocumentName ->
                isRenameDocumentAlertExpanded = false
                viewModel.renameDocument(newDocumentName)
            }
        )
    }

    if (isDeleteDocumentAlertExpanded) {
        AlertDeleteDocument(
            onDismiss = { isDeleteDocumentAlertExpanded = false },
            onConfirm = {
                isDeleteDocumentAlertExpanded = false
                viewModel.deleteDocument()
                popBackStack()
            }
        )
    }
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
                maxLines = 1
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

@Composable
private fun ImagePager(
    modifier: Modifier = Modifier,
    images: List<Bitmap>,
    onClick: () -> Unit
) {
    val pagerState = rememberPagerState(
        pageCount = { images.size }
    )

    HorizontalPager(
        state = pagerState,
        pageSpacing = 8.dp,
        modifier = modifier
            .height(500.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable { onClick.invoke() }
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

private fun LazyListScope.documentDetailsSection(
    documentDetails: List<DocumentDetail>,
    isEditingMode: Boolean,
    updateEditingMode: (state: Boolean) -> Unit,
    updateDocumentDetails: (newDocumentDetails: List<DocumentDetail>) -> Unit
) {
    item {
        SectionTitleBar(
            title = stringResource(R.string.details),
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, bottom = 8.dp),
            onButtonCLick = {
                updateEditingMode.invoke(!isEditingMode)
            },
            isSaveMode = isEditingMode
        )
    }

    if (documentDetails.isEmpty()) {
        item {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    stringResource(if (isEditingMode) R.string.add_a_detail else R.string.no_document_details),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    item {
        ReorderableColumn(
            list = documentDetails,
            onSettle = { fromIndex, toIndex ->
                updateDocumentDetails.invoke(
                    documentDetails.toMutableList().apply {
                        add(toIndex, removeAt(fromIndex))
                    }
                )
            },
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) { index, documentDetail, _ ->
            key(documentDetail.name) { // TODO Add an id
                val interactionSource = remember { MutableInteractionSource() }

                val topCornersDp by animateDpAsState(
                    targetValue = if (index == 0) 14.dp else 4.dp,
                    label = "topCorners"
                )
                val bottomCornersDp by animateDpAsState(
                    targetValue = if (index == documentDetails.size - 1) 12.dp else 4.dp,
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

                val moveUpLabel = stringResource(R.string.move_up_detail)
                val moveDownLabel = stringResource(R.string.move_down_detail)

                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .semantics {
                            customActions = listOf(
                                CustomAccessibilityAction(label = moveUpLabel, action = {
                                    if (index > 0) {
                                        updateDocumentDetails.invoke(
                                            documentDetails.toMutableList().apply {
                                                add(index - 1, removeAt(index))
                                            }
                                        )
                                        true
                                    } else {
                                        false
                                    }
                                }),
                                CustomAccessibilityAction(label = moveDownLabel, action = {
                                    if (index < documentDetails.size - 1) {
                                        updateDocumentDetails.invoke(
                                            documentDetails.toMutableList().apply {
                                                add(index + 1, removeAt(index))
                                            }
                                        )
                                        true
                                    } else {
                                        false
                                    }
                                }),
                            )
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = containerColor,
                    ),
                    shape = RoundedCornerShape(
                        topStart = topCornersDp,
                        topEnd = topCornersDp,
                        bottomStart = bottomCornersDp,
                        bottomEnd = bottomCornersDp
                    )
                ) {
                    if (documentDetail !is StringDetail) return@Card
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .draggableHandle(interactionSource =  interactionSource, enabled = isEditingMode)
                            .clearAndSetSemantics { }
                    ) {
                        SimpleTextField(
                            value = documentDetail.name,
                            onValueChange = { newText ->
                                val updatedDocumentDetails = documentDetails.toMutableList()
                                updatedDocumentDetails[index] =
                                    documentDetail.copy(name = newText)
                                updateDocumentDetails.invoke(updatedDocumentDetails)
                            },
                            modifier = Modifier
                                .weight(1f),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = contentColor
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            singleLine = true,
                            enabled = isEditingMode,
                            hint = stringResource(R.string.detail_hint_title)
                        )

                        SimpleTextField(
                            value = documentDetail.value,
                            onValueChange = { newText ->
                                val updatedDocumentDetails = documentDetails.toMutableList()
                                updatedDocumentDetails[index] = documentDetail.copy(value = newText)
                                updateDocumentDetails.invoke(updatedDocumentDetails)
                            },
                            modifier = Modifier,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                textAlign = TextAlign.End,
                                color = contentColor
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            singleLine = true,
                            enabled = isEditingMode,
                            hint = stringResource(R.string.detail_hint_value),
                        )
                    }
                }
            }
        }
    }

    // Add a detail button
    item {
        AnimatedVisibility(
            visible = isEditingMode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Button(
                onClick = {
                    val updatedDocumentDetails = documentDetails + StringDetail(
                        name = "",
                        value = ""
                    )

                    updateDocumentDetails.invoke(updatedDocumentDetails)
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
private fun DocumentNoteSection(
    documentModel: DocumentModel?,
    onUpdateDocumentNote: (newText: String) -> Unit
) {
    var unsavedDocumentNoteDetail by rememberSaveable {
        mutableStateOf(
            documentModel?.documentNote ?: ""
        )
    }

    LaunchedEffect(unsavedDocumentNoteDetail) {
        delay(500)

        if (unsavedDocumentNoteDetail == documentModel?.documentNote) return@LaunchedEffect

        onUpdateDocumentNote.invoke(unsavedDocumentNoteDetail)
    }

    SectionTitleBar(
        title = stringResource(R.string.note),
        modifier = Modifier.padding(start = 16.dp, end = 8.dp, bottom = 8.dp)
    )

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
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
                modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, bottom = 8.dp)
        )
    }

    items(documentPileModels.size) { index ->
        val pileModel = documentPileModels[index]

        val topCornersDp = if (index == 0) 14.dp else 4.dp
        val bottomCornersDp =
            if (index == documentPileModels.size - 1) 12.dp else 4.dp

        Pile(
            pileModel = pileModel,
            modifier = Modifier
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
            Spacer(Modifier.height(4.dp))
        }
    }

    if (documentPileModels.isEmpty()) {
        item {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
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
    documentModel: DocumentModel
) {
    val locale = Locale.getDefault()
    val formatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        if (documentModel.modificationDate != documentModel.creationDate) {
            Text(
                text = stringResource(
                    R.string.modified_the,
                    documentModel.modificationDate.format(formatter)
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        Text(
            text = stringResource(
                R.string.added_the,
                documentModel.creationDate.format(formatter)
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ToolBar(
    modifier: Modifier = Modifier,
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


@Composable
private fun AlertEditDocument(
    modifier: Modifier = Modifier,
    documentName: String,
    onDismiss: () -> Unit,
    onConfirm: (documentName: String) -> Unit
) {
    var newDocumentName by rememberSaveable { mutableStateOf(documentName) }
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_document)) },
        text = {
            OutlinedTextField(
                value = newDocumentName,
                onValueChange = { newDocumentName = it },
                label = { Text(stringResource(R.string.document_name)) },
                trailingIcon = {
                    if (newDocumentName.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.delete_text),
                            modifier = Modifier.clickable { newDocumentName = "" })
                    }
                },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                enabled = newDocumentName.isNotEmpty(),
                onClick = {
                    onConfirm.invoke(newDocumentName)
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