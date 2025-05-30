package com.ganadoro.pile.ui.screens.home

import android.net.Uri
import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.R
import com.ganadoro.pile.ui.compostables.itemDocumentsCompleteList
import com.ganadoro.pile.ui.screens.home.compostables.HomeScreenSectionTitle
import com.ganadoro.pile.ui.screens.home.compostables.SearchBar
import com.ganadoro.pile.ui.screens.home.compostables.itemPileGrid
import io.github.aakira.napier.Napier
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateToEditPiles: (pileId: String) -> Unit,
    navigateToEditPDF: (documentId: String) -> Unit
) {

    val viewModel = getViewModel<HomeViewModel>()

    viewModel.navigateToEditPDF = navigateToEditPDF

    val uiState by viewModel.uiState.collectAsState()

    val listState = rememberLazyListState() // TODO: Send to viewmodel

    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isNewPileAlertExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        floatingActionButtonPosition = FabPosition.EndOverlay,
        floatingActionButton = {
            FabMenu(
                fabMenuExpanded = fabMenuExpanded,
                updateFabMenuExpanded = { fabMenuExpanded = it },
                onImportPDF = {
                    viewModel.importPDFIntent()
                },
                onImportFromGallery = { uriList ->
                    viewModel.importFromGalleryIntent(uriList)
                },
                onTakeAPhoto = {

                }
            )
        },
        topBar = {
            SearchBar(
                Modifier
                    .padding(horizontal = 16.dp)
            )
        }
    ) { innerPadding ->
        val documentsColorSection = MaterialTheme.colorScheme.surface

        var availableWidth by remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current

        Box(
            Modifier
                .fillMaxSize()
                .background(documentsColorSection)
        ) {
            LazyColumn(
                Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .onGloballyPositioned { coordinates ->
                        val widthPx = coordinates.size.width
                        availableWidth = with(density) { widthPx.toDp() }.value.dp
                    }
                    .pointerInteropFilter {
                        when (it.action) {
                            MotionEvent.ACTION_DOWN -> {
                                fabMenuExpanded = false

                            }
                        }
                        false
                    },
                state = listState
            ) {
                item { Spacer(Modifier.height(innerPadding.calculateTopPadding())) }
                item { Spacer(Modifier.height(24.dp)) }
                item {
                    HomeScreenSectionTitle(
                        title = stringResource(R.string.your_piles),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }

                itemPileGrid(
                    availableWidth = availableWidth,
                    piles = uiState.pileModels,
                    onPileClick = navigateToEditPiles,
                    onNewPileClick = { isNewPileAlertExpanded = true }
                )

                item { Spacer(Modifier.height(30.dp)) }
                item {
                    Column(
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 24.dp,
                                    topEnd = 24.dp
                                )
                            )
                            .background(documentsColorSection)
                            .padding(top = 16.dp)
                    ) {
                        HomeScreenSectionTitle(
                            title = stringResource(R.string.all_documents),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
                itemDocumentsCompleteList(
                    availableWidth = availableWidth,
                    backgroundColor = documentsColorSection,
                    documents = uiState.documentList,
                    onDocumentClick = navigateToEditPDF
                )
                item {
                    Box(
                        Modifier
                            .height(52.dp)
                            .fillMaxWidth()
                            .background(documentsColorSection)
                    )
                }
            }
        }


        if (isNewPileAlertExpanded) {
            AlertNewPile(
                onDismiss = { isNewPileAlertExpanded = false },
                onConfirm = { pileName ->
                    isNewPileAlertExpanded = false
                    viewModel.addPile(pileName)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FabMenu( // TODO: Move
    modifier: Modifier = Modifier,
    fabMenuExpanded: Boolean,
    updateFabMenuExpanded: (Boolean) -> Unit = {},
    onImportPDF: () -> Unit = {},
    onImportFromGallery: (uriList: List<Uri>) -> Unit = {},
    onTakeAPhoto: () -> Unit = {}
) {

    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uriList ->
            if (uriList == null) {
                Napier.d { "PhotoPicker: No media selected" }
                return@rememberLauncherForActivityResult
            }

            Napier.d { "PhotoPicker: $uriList" }
            onImportFromGallery.invoke(uriList)
        }

    val items: List<Triple<Painter, String, () -> Unit>> =
        listOf(
            Triple(
                painterResource(R.drawable.ic_clip),
                stringResource(R.string.import_pdf_file),
                onImportPDF
            ),
            Triple(
                rememberVectorPainter(Icons.Filled.Photo),
                stringResource(R.string.import_from_gallery)
            ) {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            Triple(
                rememberVectorPainter(Icons.Filled.CameraAlt),
                stringResource(R.string.take_a_photo),
                onTakeAPhoto
            )
        )

    BackHandler(fabMenuExpanded) { updateFabMenuExpanded(false) }

    val expandedString = stringResource(R.string.expanded)
    val collapsedString = stringResource(R.string.collapsed)
    val toggleMenuString = stringResource(R.string.toggle_menu)
    val closeMenuString = stringResource(R.string.close_menu)

    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = fabMenuExpanded,
        button = {
            ToggleFloatingActionButton(
                modifier = Modifier
                    .semantics {
                        traversalIndex = -1f
                        stateDescription =
                            if (fabMenuExpanded) expandedString else collapsedString
                        contentDescription = toggleMenuString
                    },
                checked = fabMenuExpanded,
                onCheckedChange = { updateFabMenuExpanded(it) }
            ) {
                val imageVector by remember {
                    derivedStateOf {
                        if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                    }
                }
                Icon(
                    painter = rememberVectorPainter(imageVector),
                    contentDescription = null,
                    modifier = Modifier.animateIcon({ checkedProgress })
                )
            }
        }
    ) {
        items.forEachIndexed { i, item ->
            FloatingActionButtonMenuItem(
                modifier =
                    Modifier.semantics {
                        isTraversalGroup = true
                        if (i == items.size - 1) {
                            customActions = listOf(
                                CustomAccessibilityAction(
                                    label = closeMenuString,
                                    action = {
                                        item.third
                                        updateFabMenuExpanded(false)
                                        true
                                    }
                                )
                            )
                        }
                    },
                onClick = {
                    item.third.invoke()
                    updateFabMenuExpanded(false)
                },
                icon = { Icon(item.first, contentDescription = null) },
                text = { Text(text = item.second) },
            )
        }
    }

}

@Composable
private fun AlertNewPile(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: (pileName: String) -> Unit
) {
    var pileName by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_pile)) },
        text = {
            OutlinedTextField(
                value = pileName,
                onValueChange = { pileName = it },
                label = { Text(stringResource(R.string.pile_name)) },
                trailingIcon = {
                    if (pileName.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.clickable { pileName = "" })
                    }
                }
            )
        },
        confirmButton = {
            TextButton(
                enabled = pileName.isNotEmpty(),
                onClick = {
                    onConfirm.invoke(pileName)
                }
            ) {
                Text(stringResource(R.string.new_))
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