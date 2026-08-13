package es.pile.features.settings.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.pile.core.ui.theme.PileTheme

@Preview
@Composable
private fun SettingsSectionPrev() {
    PileTheme {
        Surface() {
            SettingsSection(
                title = "Appearance",
                modifier = Modifier.padding(16.dp)
            ) {
                SettingsItem(
                    itemPosition = ItemPosition.SINGLE,
                    title = "Use system theme color",
                    onAction = {}
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            content()
        }
    }
}