package es.pile.features.settings.ui.libraries

import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
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
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Horizontal),
        topBar = {
            SettingsTopBar(
                title = stringResource(R.string.open_source_licences),
                popBackStack = popBackStack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current

        LibrariesContainer(
            libraries = libraries,
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection)
                )
                .fillMaxSize()
        )

    }
}