package com.ganadoro.pile.ui.screens.documentDetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.R
import com.ganadoro.pile.ui.compostables.LoadingWrapper
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DocumentDetailScreen(
    modifier: Modifier = Modifier,
    documentId: String,
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
                uiState.documentModel == null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

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