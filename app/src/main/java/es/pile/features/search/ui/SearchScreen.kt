package es.pile.features.search.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pile.DocumentModel
import es.pile.PileModel
import es.pile.R
import es.pile.core.ui.composables.Document
import es.pile.core.ui.composables.KeyboardAware
import es.pile.core.ui.composables.LoadingWrapper
import es.pile.core.ui.composables.SelectPilesBottomSheet
import es.pile.core.ui.composables.adaptiveSizeItemsGrid
import es.pile.core.ui.theme.PileTheme
import es.pile.core.ui.util.horizontalPaddingValues
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    pileId: String? = null,
    onBack: () -> Unit,
    navigateToDocumentDetail: (documentId: String) -> Unit
) {
    val showKeyboard = remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        if (showKeyboard.value) {
            delay(100.milliseconds) // Prevents errors
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.displayCutout,
        topBar = {
            SearchContent(
                pileId = pileId,
                expanded = true,
                onExpandedChange = { expanded ->
                    if (!expanded) onBack()
                },
                onSettingsClick = {}, // Do not
                navigateToDocumentDetail = navigateToDocumentDetail,
                focusRequester = focusRequester
            )
        },
        content = {}
    )
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    val document = DocumentModel(
        id = "1",
        title = "Factura de la luz",
        imageIds = emptyList(),
        creationDateTime = LocalDateTime.now(),
        modificationDateTime = LocalDateTime.now(),
        documentStatus = 0,
        documentPileIds = emptyList(),
        documentDetails = emptyList(),
        documentNote = "Nota de prueba",
        documentOrganizationIds = emptyList(),
        isIncomingPdf = false
    )

    PileTheme {
        SearchContent(
            state = SearchState(
                isLoading = false,
                searchQuery = "",
                filteredDocumentList = listOf(SearchItem(document, "")),
                pileList = listOf(PileModel("1", "Pilas", "icon", 0xFF0000L))
            ),
            bitmapCache = emptyMap(),
            expanded = true,
            onExpandedChange = {},
            onSettingsClick = {},
            onEvent = {},
            navigateToDocumentDetail = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    modifier: Modifier = Modifier,
    pileId: String? = null,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: SearchViewModel = koinViewModel { parametersOf(pileId) },
    navigateToDocumentDetail: (documentId: String) -> Unit,
    focusRequester: FocusRequester? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bitmapCache by viewModel.bitmapCache.collectAsStateWithLifecycle()

    SearchContent(
        modifier = modifier,
        state = state,
        bitmapCache = bitmapCache,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        onSettingsClick = onSettingsClick,
        onEvent = { viewModel.handleEvent(it) },
        navigateToDocumentDetail = navigateToDocumentDetail,
        focusRequester = focusRequester
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    modifier: Modifier = Modifier,
    state: SearchState,
    bitmapCache: Map<String, Bitmap>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    onEvent: (SearchEvent) -> Unit,
    navigateToDocumentDetail: (documentId: String) -> Unit,
    focusRequester: FocusRequester? = null
) {
    LaunchedEffect(expanded) {
        if (!expanded) {
            delay(300.milliseconds)
            onEvent(SearchEvent.OnCloseSearch)
        }
    }

    var showFilterPilesBottomSheet by remember { mutableStateOf(false) }
    var showFilterDateAlert by remember { mutableStateOf(false) }

    SearchBar(
        modifier = modifier
            .fillMaxWidth(),
        inputField = {
            val focusRequester = focusRequester ?: remember { FocusRequester() }

            SearchInputField(
                searchQuery = state.searchQuery,
                onQueryChange = { onEvent(SearchEvent.OnUpdateSearchQuery(it)) },
                onSearch = { onEvent(SearchEvent.OnSearch) },
                expanded = expanded,
                onExpandedChange = { onExpandedChange(it) },
                onSettingsClick = onSettingsClick,
                focusRequester = focusRequester
            )
        },
        expanded = expanded,
        onExpandedChange = { onExpandedChange(it) }
    ) {
        Column(
            modifier = Modifier.padding(
                WindowInsets.displayCutout.asPaddingValues()
                    .horizontalPaddingValues(LocalLayoutDirection.current)
            )
        ) {
            FilterChipsRow(
                selectedFilterPiles = state.selectedFilterPiles,
                pileList = state.pileList,
                selectedFilterDate = state.selectedFilterDate,
                onShowFilterPilesBottomSheet = { showFilterPilesBottomSheet = true },
                onSHowFilterDateAlert = { showFilterDateAlert = true }
            )

            LoadingWrapper(state.isLoading) {
                val isSearchEmpty =
                    state.searchQuery.isNotEmpty() && state.filteredDocumentList.isEmpty()

                KeyboardAware {
                    AnimatedContent(
                        targetState = isSearchEmpty,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(300, delayMillis = 90)) +
                                    slideInVertically(
                                        initialOffsetY = { it / 12 },
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )).togetherWith(fadeOut(animationSpec = tween(150)))
                        },
                        label = "SearchContentAnimation"
                    ) { empty ->
                        if (empty) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.search_24px),
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(R.string.no_search_results),
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            BoxWithConstraints {
                                val availableWidth = maxWidth

                                LazyColumn(
                                    modifier = Modifier
                                        .imePadding()
                                        .fillMaxSize()
                                ) {
                                    itemDocumentsCustomList(
                                        availableWidth = availableWidth,
                                        documents = state.filteredDocumentList,
                                        onDocumentClick = { documentId ->
                                            navigateToDocumentDetail(documentId)
                                        },
                                        bitmapCache = bitmapCache,
                                        onLoadBitmap = { document ->
                                            onEvent(SearchEvent.OnImageDisplayed(document))
                                        }
                                    )
                                    item {
                                        Spacer(Modifier.height(50.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterPilesBottomSheet) {
        SelectPilesBottomSheet(
            title = stringResource(R.string.piles),
            pileList = state.pileList,
            selectedFilterPiles = state.selectedFilterPiles,
            onDismissBottomSheet = { showFilterPilesBottomSheet = false },
            onPileClick = { onEvent(SearchEvent.OnUpdateFilterPiles(it)) }
        )
    }


    if (showFilterDateAlert) {
        FilterDateAlert(
            onDateSelected = { onEvent(SearchEvent.OnUpdateFilterDate(it)) },
            selectedFilterDate = state.selectedFilterDate,
            documentList = state.documentList,
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
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(Modifier.width(8.dp))
        val chipModifier = Modifier.animateContentSize(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        if (pileList?.isNotEmpty() == true) {
            FilterChip(
                modifier = chipModifier,
                onClick = onShowFilterPilesBottomSheet,
                selected = selectedFilterPiles.isNotEmpty(),
                label = {
                    val chipText = if (selectedFilterPiles.isEmpty())
                        stringResource(R.string.piles)
                    else
                        "${pileList.firstOrNull { it.id == selectedFilterPiles.firstOrNull() }?.name ?: ""} ${if (selectedFilterPiles.size > 1) " + ${selectedFilterPiles.size - 1}" else ""}"

                    AnimatedContent(
                        targetState = chipText,
                        transitionSpec = {
                            (fadeIn(tween(220, delayMillis = 90)) + scaleIn(initialScale = 0.92f))
                                .togetherWith(fadeOut(animationSpec = tween(90)))
                        },
                        label = "PileChipTextAnimation"
                    ) { targetText ->
                        Text(targetText, modifier)
                    }
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            )
        }
        FilterChip(
            modifier = chipModifier,
            onClick = onSHowFilterDateAlert,
            selected = selectedFilterDate != null,
            label = {
                val chipText = if (selectedFilterDate == null)
                    stringResource(R.string.date)
                else {
                    selectedFilterDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                }

                AnimatedContent(
                    targetState = chipText,
                    transitionSpec = {
                        (fadeIn(tween(220, delayMillis = 90)) + scaleIn(initialScale = 0.92f))
                            .togetherWith(fadeOut(animationSpec = tween(90)))
                    },
                    label = "DateChipTextAnimation"
                ) { targetText ->
                    Text(targetText)
                }
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
    documents: List<SearchItem>,
    bitmapCache: Map<String, Bitmap>,
    onLoadBitmap: suspend (document: DocumentModel) -> Unit,
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
        content = { modifier, documentItem ->
            val key = documentItem.coverImageCacheKey

            val cachedBitmap: Bitmap? = bitmapCache[key]

            if (cachedBitmap == null) {
                LaunchedEffect(key1 = key) {
                    onLoadBitmap(documentItem.document)
                }
            }

            Document(
                documentModel = documentItem.document,
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
    val years =
        documentList?.flatMap { listOf(it.creationDateTime.year, it.modificationDateTime.year) }
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