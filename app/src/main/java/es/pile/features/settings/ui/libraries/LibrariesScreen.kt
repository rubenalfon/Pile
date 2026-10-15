package es.pile.features.settings.ui.libraries

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import es.pile.R
import es.pile.core.ui.theme.PileTheme
import es.pile.features.settings.ui.composables.SettingsTopBar

@Composable
fun LibrariesScreen(
    popBackStack: () -> Unit,
) {
    val libraries by produceLibraries(R.raw.aboutlibraries)

    LibrariesContent(
        libraries = libraries,
        popBackStack = popBackStack
    )
}

@Preview
@Composable
private fun LibrariesScreenPreview() {
    val libraries by produceLibraries(R.raw.aboutlibraries)

    PileTheme {
        LibrariesContent(
            libraries = libraries,
            popBackStack = {}
        )
    }
}

@Composable
fun LibrariesContent(
    libraries: Libs?,
    popBackStack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.displayCutout,
        topBar = {
            SettingsTopBar(
                title = stringResource(R.string.open_source_licences),
                popBackStack = popBackStack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LibrariesContainer(
            libraries, Modifier
                .padding(paddingValues)
                .fillMaxSize()
        )

    }
}