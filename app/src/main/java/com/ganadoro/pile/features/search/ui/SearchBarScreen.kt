package com.ganadoro.pile.features.search.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults.InputField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.R
import com.ganadoro.pile.core.ui.composables.Document
import com.ganadoro.pile.core.ui.composables.LoadingWrapper
import com.ganadoro.pile.core.ui.composables.SelectPilesBottomSheet
import com.ganadoro.pile.core.ui.composables.adaptiveSizeItemsGrid
import com.ganadoro.pile.core.ui.util.horizontalPaddingValues
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarScreen(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: SearchBarViewModel = koinViewModel(),
    navigateToDocumentDetail: (documentId: String) -> Unit,
    focusRequester: FocusRequester? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bitmapCache by viewModel.bitmapCache.collectAsStateWithLifecycle()

    if (uiState.pileList == null || uiState.documentList == null) {
        viewModel.init()
    }

    var showFilterPilesBottomSheet by remember { mutableStateOf(false) }
    var showFilterDateAlert by remember { mutableStateOf(false) }

    SearchBar(
        modifier = modifier
            .fillMaxWidth(),
        inputField = {
            val focusRequester = focusRequester ?: remember { FocusRequester() }

            SearchInputField(
                searchQuery = uiState.searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                onSearch = viewModel::filterResults,
                expanded = expanded,
                onExpandedChange = {
                    if (!it) viewModel.deinit()
                    onExpandedChange(it)
                },
                onSettingsClick = onSettingsClick,
                focusRequester = focusRequester
            )
        },
        expanded = expanded,
        onExpandedChange = {
            if (!it) viewModel.deinit()
            onExpandedChange(it)
        }
    ) {
        Column(
            modifier = Modifier.padding(
                WindowInsets.displayCutout.asPaddingValues()
                    .horizontalPaddingValues(LocalLayoutDirection.current)
            )
        ) {
            FilterChipsRow(
                selectedFilterPiles = uiState.selectedFilterPiles,
                pileList = uiState.pileList,
                selectedFilterDate = uiState.selectedFilterDate,
                onShowFilterPilesBottomSheet = { showFilterPilesBottomSheet = true },
                onSHowFilterDateAlert = { showFilterDateAlert = true }
            )

            var availableWidth by remember { mutableStateOf(0.dp) }
            val density = LocalDensity.current

            LoadingWrapper(uiState.pileList == null || uiState.documentList == null) {
                LazyColumn(
                    modifier = Modifier
                        .imePadding()
                        .fillMaxSize()
                        .onGloballyPositioned { coordinates ->
                            val widthPx = coordinates.size.width
                            availableWidth = with(density) { widthPx.toDp() }.value.dp
                        }
                ) {
                    itemDocumentsCustomList(
                        availableWidth = availableWidth,
                        documents = uiState.filteredDocumentList,
                        onDocumentClick = { documentId ->
                            navigateToDocumentDetail(documentId)
                        },
                        bitmapCache = bitmapCache,
                        onLoadBitmap = viewModel::requestBitmapLoad,
                        onRequestImageKey = viewModel::requestImageKey
                    )
                    item {
                        Spacer(Modifier.height(50.dp))
                    }
                }
            }
        }
    }

    if (showFilterPilesBottomSheet) {
        SelectPilesBottomSheet(
            title = stringResource(R.string.piles),
            pileList = uiState.pileList,
            selectedFilterPiles = uiState.selectedFilterPiles,
            onDismissBottomSheet = { showFilterPilesBottomSheet = false },
            onPileClick = viewModel::addRemoveFilterPiles,
        )
    }


    if (showFilterDateAlert) {
        FilterDateAlert(
            onDateSelected = viewModel::updateFilterDate,
            selectedFilterDate = uiState.selectedFilterDate,
            documentList = uiState.documentList,
            onDismiss = { showFilterDateAlert = false }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SearchInputField(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    focusRequester: FocusRequester
) {
    InputField(
        query = searchQuery,
        onQueryChange = onQueryChange,
        onSearch = { onSearch() },
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        placeholder = { Text(stringResource(R.string.search_your_documents)) },
        leadingIcon = {
            Crossfade(
                targetState = expanded,
                animationSpec = tween(durationMillis = 150)
            ) { targetState ->
                when (targetState) {
                    true -> {
                        IconButton(
                            onClick = { onExpandedChange(false) }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_back_24px),
                                contentDescription = stringResource(R.string.return_)
                            )
                        }
                    }

                    false -> {
                        Box(
                            modifier = Modifier.minimumInteractiveComponentSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.search_24px),
                                contentDescription = null
                            )
                        }

                    }
                }

            }
        },
        trailingIcon = {
            AnimatedVisibility(!expanded, enter = fadeIn(), exit = fadeOut()) {
                IconButton(
                    onClick = onSettingsClick
                ) {
                    Icon(
                        painter = painterResource(R.drawable.settings_24px),
                        contentDescription = stringResource(R.string.settings)
                    )
                }
            }
            AnimatedVisibility(
                expanded && searchQuery.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = { onQueryChange("") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.settings)
                    )
                }
            }
        },
        modifier = modifier
            .padding(
                WindowInsets.displayCutout.asPaddingValues()
                    .horizontalPaddingValues(LocalLayoutDirection.current)

            )
            .focusRequester(focusRequester)
    )
}

@Composable
private fun FilterChipsRow(
    modifier: Modifier = Modifier,
    pileList: List<PileModel>?,
    selectedFilterPiles: List<String>,
    selectedFilterDate: LocalDate?,
    onShowFilterPilesBottomSheet: () -> Unit,
    onSHowFilterDateAlert: () -> Unit

) {
    Row(
        modifier
            .horizontalScroll(rememberScrollState())
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(Modifier.width(8.dp))
        FilterChip(
            onClick = onShowFilterPilesBottomSheet,
            selected = selectedFilterPiles.isNotEmpty(),
            label = {
                val chipText = if (selectedFilterPiles.isEmpty())
                    stringResource(R.string.piles)
                else
                    "${pileList?.firstOrNull { it.id == selectedFilterPiles.firstOrNull() }?.name ?: ""} ${if (selectedFilterPiles.size > 1) " + ${selectedFilterPiles.size - 1}" else ""}"

                Text(chipText)
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
        )
        FilterChip(
            onClick = onSHowFilterDateAlert,
            selected = selectedFilterDate != null,
            label = {
                val chipText = if (selectedFilterDate == null)
                    stringResource(R.string.date)
                else {
                    selectedFilterDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                }
                Text(chipText)
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
        )
        Spacer(Modifier.width(8.dp))
    }
}

private fun LazyListScope.itemDocumentsCustomList(
    availableWidth: Dp,
    documents: List<DocumentModel>,
    bitmapCache: Map<String, Bitmap>,
    onLoadBitmap: suspend (document: DocumentModel, pageNumber: Int) -> Unit,
    onRequestImageKey: (document: DocumentModel, pageNumber: Int) -> String,
    onDocumentClick: (documentId: String) -> Unit = {}
) {
    adaptiveSizeItemsGrid(
        backgroundColor = Color.Transparent,
        availableWidth = availableWidth,
        itemList = documents,
        minimumItemWidth = 125.dp,
        horizontalSpacing = 16.dp,
        verticalSpacing = 16.dp,
        horizontalPadding = 16.dp,
        content = { modifier, document ->
            val key = onRequestImageKey(document, 0)

            val cachedBitmap: Bitmap? = bitmapCache[key]

            if (cachedBitmap == null) {
                LaunchedEffect(key1 = key) {
                    onLoadBitmap(document, 0)
                }
            }

            Document(
                documentModel = document,
                imageBitmap = cachedBitmap?.asImageBitmap(),
                modifier = modifier,
                onClick = onDocumentClick
            )
        }
    )
    item { Spacer(Modifier.height(16.dp)) }
}

@Composable
private fun FilterDateAlert(
    modifier: Modifier = Modifier,
    selectedFilterDate: LocalDate?,
    documentList: List<DocumentModel>?,
    onDateSelected: (LocalDate?) -> Unit,
    onDismiss: () -> Unit
) {
    val millis = selectedFilterDate?.atTime(0, 0, 0)?.toEpochSecond(ZoneOffset.UTC)
    val years = documentList?.flatMap { listOf(it.creationDateTime.year, it.modificationDateTime.year) }
        ?.distinct()

    val datePickerState: DatePickerState

    if (years != null && years.isNotEmpty()) {
        val yearRange = years.min()..years.max()
        datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = millis?.times(1000),
            yearRange = yearRange
        )
    } else {
        datePickerState = rememberDatePickerState(initialSelectedDateMillis = millis?.times(1000))
    }

    DatePickerDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextButton(
                    onClick = {
                        onDateSelected(null)
                        onDismiss()
                    },
                    enabled = selectedFilterDate != null
                ) {
                    Text(stringResource(R.string.clear))
                }

                Spacer(Modifier.weight(1f))

                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }

                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate =
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onDateSelected(selectedDate)
                    }
                    onDismiss()
                }) {
                    Text(stringResource(R.string.ok))
                }

            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}