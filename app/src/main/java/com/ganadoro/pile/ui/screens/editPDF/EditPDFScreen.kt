package com.ganadoro.pile.ui.screens.editPDF

import android.view.MotionEvent
import androidx.compose.foundation.background
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
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.R
import com.ganadoro.pile.ui.compostables.itemDocumentsCompleteList
import com.ganadoro.pile.ui.screens.home.FabMenu
import com.ganadoro.pile.ui.screens.home.HomeViewModel
import com.ganadoro.pile.ui.screens.home.compostables.HomeScreenSectionTitle
import com.ganadoro.pile.ui.screens.home.compostables.SearchBar
import com.ganadoro.pile.ui.screens.home.compostables.itemPileGrid
import org.koin.androidx.compose.getViewModel
import java.util.UUID


@Composable
fun EditPFDScreen(
    modifier: Modifier = Modifier,
    documentID: String,
    popBackStack: () -> Unit,
    viewModel: EditPDFViewModel = getViewModel<EditPDFViewModel>()
) {
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
                    documents = uiState.documentList
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