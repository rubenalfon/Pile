package com.ganadoro.pile.ui.screens.documentDetail

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.R
import com.ganadoro.pile.ui.compostables.LoadingWrapper
import com.ganadoro.pile.ui.compostables.Pile
import com.ganadoro.pile.ui.screens.documentDetail.composables.SectionTitleBar
import com.ganadoro.pile.util.TextFieldValueSaver
import org.koin.androidx.compose.getViewModel
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
        },
        floatingActionButton = {

        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .padding(top = 8.dp)
        ) {
            LoadingWrapper(
                documentModel == null || uiState.documentPileModels == null
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Carousel


                    // Details

                    // Note

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
                onShareDocument = { },
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
private fun DocumentNoteSection(
    documentModel: DocumentModel?,
    onUpdateDocumentNote: (newText: String) -> Unit
) {
    var isEditing by rememberSaveable { mutableStateOf(false) }

    var uneditedDocumentNoteDetail by rememberSaveable(stateSaver = TextFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(documentModel?.documentNote ?: "")
        )
    }

    SectionTitleBar(
        title = stringResource(R.string.note),
        modifier = Modifier.padding(start = 16.dp, end = 8.dp, bottom = 8.dp),
        isSaveMode = isEditing,
        onButtonCLick = {
            if (isEditing) {
                onUpdateDocumentNote.invoke(uneditedDocumentNoteDetail.text)
            }

            isEditing = !isEditing
        }
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
        if (isEditing) {
            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                uneditedDocumentNoteDetail = uneditedDocumentNoteDetail.copy(
                    selection = TextRange(uneditedDocumentNoteDetail.text.length)
                )
            }

            BasicTextField(
                value = uneditedDocumentNoteDetail,
                onValueChange = { newText ->
                    uneditedDocumentNoteDetail = newText
                },
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .padding(16.dp)
                    .fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )
        } else {
            Text(
                text = documentModel?.documentNote ?: "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
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