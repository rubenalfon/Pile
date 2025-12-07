package comganadoro.pile.ui.screens.editDocumentPiles

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonDefaults.smallContainerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.R
import com.ganadoro.pile.ui.compostables.AlertNewPile
import com.ganadoro.pile.ui.compostables.LoadingWrapper
import com.ganadoro.pile.ui.compostables.itemPileGrid
import com.ganadoro.pile.ui.screens.editDocumentPiles.EditDocumentPilesViewModel
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditDocumentPilesScreen(
    modifier: Modifier = Modifier,
    documentId: String,
    popBackStack: () -> Unit,
    viewModel: EditDocumentPilesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.documentModel == null) {
        viewModel.customInnit(documentId)
    }

    var isNewPileAlertExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.displayCutout,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            ScreenTopAppBar(
                popBackStack = popBackStack,
                title = uiState.documentModel?.title ?: ""
            )
        }
    ) { innerPadding ->
        var availableWidth by remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current

        LoadingWrapper(
            uiState.documentModel == null || uiState.allPileModels == null || uiState.selectedPileModelIds == null,
            modifier = Modifier
                .padding(innerPadding)
                .onGloballyPositioned { coordinates ->
                    val widthPx = coordinates.size.width
                    availableWidth = with(density) { widthPx.toDp() }.value.dp
                }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start
            ) {
                itemPileGrid(
                    availableWidth = availableWidth,
                    piles = uiState.allPileModels!!,
                    onPileClick = viewModel::updatePileSelectState,
                    onNewPileClick = { isNewPileAlertExpanded = true },
                    coloredPileIds = uiState.selectedPileModelIds!!
                )

                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
    if (isNewPileAlertExpanded) {
        AlertNewPile(
            onDismiss = { isNewPileAlertExpanded = false },
            onConfirm = { pileName, pileIconId, pileColorNumber ->
                isNewPileAlertExpanded = false
                viewModel.addPile(pileName, pileIconId, pileColorNumber)
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 16.dp)
            )
        },
        actions = {
            FilledIconButton(
                modifier = Modifier
                    .padding(start = 14.dp, end = 12.dp)
                    .size(smallContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide)),
                onClick = popBackStack
            ) {
                Icon(
                    painter = painterResource(R.drawable.check_24px),
                    contentDescription = stringResource(R.string.done)
                )
            }
        },
        colors = topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}