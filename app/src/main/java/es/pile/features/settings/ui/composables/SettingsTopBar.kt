package es.pile.features.settings.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import es.pile.R
import es.pile.core.ui.theme.PileTheme

@Preview
@Composable
private fun SettingsTopBarPreview() {
    PileTheme {
        SettingsTopBar(
            title = "Settings",
            popBackStack = {},
            onLibrariesClick = {},
            modifier = Modifier,
            scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        )
    }
}


@Composable
fun SettingsTopBar(
    modifier: Modifier = Modifier,
    title: String,
    popBackStack: () -> Unit,
    onLibrariesClick: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior
) {
    LargeFlexibleTopAppBar(
        modifier = modifier,
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = popBackStack) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_24px),
                    contentDescription = stringResource(R.string.return_)
                )
            }
        },
        actions = {
            if (onLibrariesClick != null) {
                Box {
                    var menuOpened by remember { mutableStateOf(false) }

                    IconButton(onClick = { menuOpened = true }) {
                        Icon(
                            painter = painterResource(R.drawable.burger_menu),
                            contentDescription = stringResource(R.string.open_menu)
                        )
                    }

                    DropdownMenuPopup(
                        expanded = menuOpened,
                        onDismissRequest = { menuOpened = false }
                    ) {
                        DropdownMenuGroup(
                            shapes = MenuDefaults.groupShape(0, 1),
                        ) {

                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.open_source_licences)) },
                                onClick = {
                                    onLibrariesClick()
                                    menuOpened = false

                                },
                            )
                        }
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}