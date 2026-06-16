package es.pile.features.settings.ui.resolution

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pile.R
import es.pile.core.domain.models.ImageResolution
import es.pile.core.ui.composables.LoadingWrapper
import es.pile.core.ui.theme.PileTheme
import es.pile.features.settings.ui.composables.SettingsIconicItem
import es.pile.features.settings.ui.composables.SettingsTopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsResolutionScreen(
    viewModel: SettingsResolutionViewModel = koinViewModel(),
    popBackStack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsResolutionContent(
        state = state,
        onEvent = { event ->
            when (event) {
                is SettingsResolutionEvent.OnBackClicked -> popBackStack()
                else -> viewModel.handleEvent(event)
            }
        }
    )
}

@Preview
@Composable
private fun SettingsResolutionPrev() {
    PileTheme {
        SettingsResolutionContent(
            state = SettingsResolutionState(),
            onEvent = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsResolutionContent(
    modifier: Modifier = Modifier,
    state: SettingsResolutionState,
    onEvent: (SettingsResolutionEvent) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.displayCutout,
        topBar = {
            SettingsTopBar(
                title = stringResource(R.string.document_resolution),
                popBackStack = { onEvent(SettingsResolutionEvent.OnBackClicked) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LoadingWrapper(state.isLoading) {
            Column(
                modifier = modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsIconicItem(
                    title = stringResource(R.string.original_quality_tittle),
                    description = stringResource(R.string.original_quality_body),
                    isSelected = state.imageResolution == ImageResolution.ORIGINAL,
                    onClick = { onEvent(SettingsResolutionEvent.OnResolutionChanged(ImageResolution.ORIGINAL)) }
                )

                SettingsIconicItem(
                    title = stringResource(R.string.storage_saver_tittle),
                    description = stringResource(R.string.storage_saver_body),
                    isSelected = state.imageResolution == ImageResolution.LOW,
                    onClick = { onEvent(SettingsResolutionEvent.OnResolutionChanged(ImageResolution.LOW)) }
                )
            }
        }
    }
}