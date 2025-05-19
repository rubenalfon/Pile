package com.ganadoro.pile.ui.screens.pileDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.ui.compostables.itemDocumentsCompleteList
import org.koin.androidx.compose.getViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PileDetailScreen(
    modifier: Modifier = Modifier,
    id: UUID,
    popBackStack: () -> Unit,
    viewModel: PileDetailViewModel = getViewModel<PileDetailViewModel>()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.pile == null) {
        viewModel.loadPile(id)
    }

    if (uiState.pile == null || uiState.documentList.isEmpty()) {
        return
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        uiState.pile!!.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                titleHorizontalAlignment = Alignment.Start,
                navigationIcon = {
                    IconButton(onClick = popBackStack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
                actions = {
                    Row {
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Localized description"
                            )
                        }
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Localized description"
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        content = { innerPadding ->

            var availableWidth by remember { mutableStateOf(0.dp) }
            val density = LocalDensity.current

            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .onGloballyPositioned { coordinates ->
                        val widthPx = coordinates.size.width
                        availableWidth = with(density) { widthPx.toDp() }.value.dp
                    }
            ) {
                itemDocumentsCompleteList(
                    availableWidth = availableWidth,
                    documents = uiState.documentList
                )
            }
        }
    )
}