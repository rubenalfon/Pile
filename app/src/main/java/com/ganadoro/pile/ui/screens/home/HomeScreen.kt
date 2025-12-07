package com.ganadoro.pile.ui.screens.home

import android.net.Uri
import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ganadoro.pile.R
import com.ganadoro.pile.models.TEMP_DOCUMENT_ID
import com.ganadoro.pile.ui.compostables.AlertNewPile
import com.ganadoro.pile.ui.compostables.LoadingWrapper
import com.ganadoro.pile.ui.compostables.SwipeBox
import com.ganadoro.pile.ui.compostables.itemDocumentsCompleteList
import com.ganadoro.pile.ui.compostables.itemPileGrid
import com.ganadoro.pile.ui.screens.home.compostables.HomeScreenSectionTitle
import com.ganadoro.pile.ui.screens.search.SearchBarScreen
import com.ganadoro.pile.util.UriUtils
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateToPileDetail: (pileId: String) -> Unit,
    navigateToDocumentDetail: (documentId: String) -> Unit,
    navigateToEditPDF: (documentId: String) -> Unit
) {
    val viewModel: HomeViewModel = koinViewModel()

    viewModel.navigateToEditPDF = navigateToEditPDF

    val uiState by viewModel.uiState.collectAsState()

    val bitmapCache by viewModel.bitmapCache.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isNewPileAlertExpanded by rememberSaveable { mutableStateOf(false) }

    var isSearchBarExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.displayCutout,
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            AnimatedVisibility(!isSearchBarExpanded,
                enter = fadeIn(), exit = fadeOut()
            ) {
                FabMenu(
                    modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues()),
                    fabMenuExpanded = fabMenuExpanded,
                    updateFabMenuExpanded = { fabMenuExpanded = it },
                    onImportPDF = { uri ->
                        viewModel.importPDFIntent(uri)
                    },
                    onImportFromGallery = { uriList ->
                        viewModel.importFromGalleryIntent(uriList)
                    },
                    onTakeAPhoto = { uri ->
                        viewModel.takePhoto(uri)
                    }
                )
            }
        },
        topBar = {
            val horizontalPaddingAnimated by animateDpAsState(
                targetValue = if (isSearchBarExpanded) 0.dp else 16.dp,
            )
            val bottomPaddingAnimated by animateDpAsState(
                targetValue = if (isSearchBarExpanded) 0.dp else 8.dp,
            )
            val displayCutoutStartPaddingAnimated by animateDpAsState(
                targetValue = if (isSearchBarExpanded) 0.dp else WindowInsets.displayCutout.asPaddingValues().calculateStartPadding(LocalLayoutDirection.current)
            )
            val displayCutoutEndPaddingAnimated by animateDpAsState(
                targetValue = if (isSearchBarExpanded) 0.dp else WindowInsets.displayCutout.asPaddingValues().calculateEndPadding(LocalLayoutDirection.current)
            )

            SearchBarScreen(
                modifier = Modifier
                    .padding(horizontal = horizontalPaddingAnimated)
                    .padding(bottom = bottomPaddingAnimated)
                    .padding(start = displayCutoutStartPaddingAnimated)
                    .padding(end = displayCutoutEndPaddingAnimated),
                expanded = isSearchBarExpanded,
                onExpandedChange = { isSearchBarExpanded = it },
                onSettingsClick = { /*TODO: Add settings */ },
                navigateToDocumentDetail = navigateToDocumentDetail
            )
        }
    ) { innerPadding ->
        val documentsColorSection = MaterialTheme.colorScheme.surface

        var availableWidth by remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current

        val layoutDirection = LocalLayoutDirection.current

        LoadingWrapper(
            isLoading = uiState.pileModels == null || uiState.documentList == null || uiState.coloredPileIds == null
        ) {
            Box(
                Modifier
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        end = innerPadding.calculateEndPadding(layoutDirection)
                    )
                    .fillMaxSize()
                    .background(documentsColorSection)
            ) {
                LazyColumn(
                    Modifier
                        .padding(bottom = innerPadding.calculateBottomPadding())
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
                    item { Spacer(Modifier.height(8.dp)) }

                    item {
                        AnimatedVisibility(
                            visible = uiState.documentList!!.any { it.id == TEMP_DOCUMENT_ID },
                            enter = EnterTransition.None,
                            exit = fadeOut(tween(100)) + shrinkVertically()
                        ) {
                            UnsavedDocumentCard(
                                onNavigateUnsavedDocument = { navigateToEditPDF(TEMP_DOCUMENT_ID) },
                                onDismiss = { viewModel.deleteUnsavedDocument() }
                            )
                        }
                    }


                    item { Spacer(Modifier.height(16.dp)) }

                    item {
                        HomeScreenSectionTitle(
                            title = stringResource(R.string.your_piles),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }

                    itemPileGrid(
                        availableWidth = availableWidth,
                        piles = uiState.pileModels!!,
                        onPileClick = navigateToPileDetail,
                        onNewPileClick = { isNewPileAlertExpanded = true },
                        coloredPileIds = uiState.coloredPileIds!!
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
                        documents = uiState.documentList!!,
                        onDocumentClick = navigateToDocumentDetail,
                        bitmapCache = bitmapCache,
                        loadBitmap = { documentId ->
                            viewModel.requestBitmapLoad(documentId)
                            null
                        }
                    )

                    item {
                        Box(
                            Modifier
                                .height(100.dp)
                                .fillMaxWidth()
                                .background(documentsColorSection)
                        )
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
                viewModel.addPile(pileName = pileName, iconId = pileIconId, color = pileColorNumber)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FabMenu( // TODO: Move?
    modifier: Modifier = Modifier,
    fabMenuExpanded: Boolean,
    updateFabMenuExpanded: (Boolean) -> Unit = {},
    onImportPDF: (uri: Uri) -> Unit = {},
    onImportFromGallery: (uriList: List<Uri>) -> Unit = {},
    onTakeAPhoto: (uri: Uri) -> Unit = {}
) {
    val importPDFLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { onImportPDF.invoke(it) }
        }
    val mediaLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickMultipleVisualMedia()) { uriList ->
            if (uriList.isNotEmpty()) {
                onImportFromGallery.invoke(uriList)
            }
        }

    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageUri?.let { onTakeAPhoto.invoke(it) }
            }
        }


    val items: List<Triple<Painter, String, () -> Unit>> =
        listOf(
            Triple(
                painterResource(R.drawable.ic_clip),
                stringResource(R.string.import_pdf_file)
            ) {
                importPDFLauncher.launch(arrayOf("application/pdf"))
            },
            Triple(
                rememberVectorPainter(Icons.Filled.Photo),
                stringResource(R.string.import_from_gallery)
            ) {
                mediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            Triple(
                rememberVectorPainter(Icons.Filled.CameraAlt),
                stringResource(R.string.take_a_photo)
            ) {
                val uri = UriUtils.createImageUri(context)
                imageUri = uri

                cameraLauncher.launch(uri)
            }
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
private fun UnsavedDocumentCard(
    modifier: Modifier = Modifier,
    onNavigateUnsavedDocument: () -> Unit,
    onDismiss: () -> Unit = {},
) {
    SwipeBox(
        onDelete = onDismiss,
        modifier = Modifier,
        contentPaddingValues = PaddingValues(horizontal = 16.dp)
    ) {
        Card(
            modifier = modifier,
            onClick = { onNavigateUnsavedDocument() },
            shape = RoundedCornerShape(16.dp),
            colors = CardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Row(
                Modifier
                    .padding(16.dp)
            ) {
                Text(
                    stringResource(R.string.user_document_unsaved_changes),
                    Modifier.weight(1f)
                )

                IconButton({}) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.navigate_to_edit_unsaved_document),
                    )
                }
            }
        }
    }
}