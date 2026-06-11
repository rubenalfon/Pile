package es.pile.features.settings.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import es.pile.R
import es.pile.core.ui.theme.PileTheme

@Preview
@Composable
private fun SettingsItemPrev() {
    PileTheme {
        Surface {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                SettingsItem(
                    itemPosition = ItemPosition.TOP,
                    title = "Dark theme",
                    subtitle = "Follow system",
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.dark_mode_24px),
                            contentDescription = null
                        )
                    },
                    onAction = {}
                )
                SettingsItem(
                    itemPosition = ItemPosition.MIDDLE,
                    title = "Use system theme color",
                    onAction = {},
                    checked = false
                )
                SettingsItem(
                    itemPosition = ItemPosition.BOTTOM,
                    title = "Activate local AI features",
                    subtitle = "Use AI in device to extract data from your documents.",
                    checked = true,
                    onAction = {}
                )
                SettingsItem(
                    enabled = false,
                    itemPosition = ItemPosition.TOP,
                    title = "Dark theme",
                    subtitle = "Follow system",
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.dark_mode_24px),
                            contentDescription = null
                        )
                    },
                    onAction = {}
                )
                SettingsItem(
                    enabled = false,
                    itemPosition = ItemPosition.BOTTOM,
                    title = "Activate local AI features",
                    subtitle = "Use AI in device to extract data from your documents.",
                    checked = true,
                    onAction = {}
                )
            }
        }
    }
}

enum class ItemPosition {
    TOP, MIDDLE, BOTTOM, SINGLE;

    @Composable
    fun getShape(cornerSize: Dp = 12.dp, innerCornerSize: Dp = 4.dp): RoundedCornerShape {
        return when (this) {
            TOP -> RoundedCornerShape(
                topStart = cornerSize,
                topEnd = cornerSize,
                bottomStart = innerCornerSize,
                bottomEnd = innerCornerSize
            )

            BOTTOM -> RoundedCornerShape(
                topStart = innerCornerSize,
                topEnd = innerCornerSize,
                bottomStart = cornerSize,
                bottomEnd = cornerSize
            )

            MIDDLE -> RoundedCornerShape(innerCornerSize)
            SINGLE -> RoundedCornerShape(cornerSize)
        }
    }
}

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    itemPosition: ItemPosition,
    title: String,
    subtitle: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    checked: Boolean? = null,
    onAction: () -> Unit
) {
    val containerColor = if (enabled) {
        MaterialTheme.colorScheme.surfaceContainerHighest
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    }
    val contentAlpha = if (enabled) 1f else 0.38f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled) { onAction() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = itemPosition.getShape()
    ) {
        val paddingValues = if (subtitle != null)
            PaddingValues(16.dp)
        else PaddingValues(horizontal = 16.dp, vertical = 8.dp)

        Row(
            modifier = Modifier.padding(paddingValues),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            leadingIcon?.let {
                CompositionLocalProvider(
                    LocalContentColor provides LocalContentColor.current.copy(alpha = contentAlpha)
                ) {
                    it()
                }
            }

            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                    )
                }
            }

            checked?.let {
                Switch(
                    enabled = enabled,
                    checked = it,
                    onCheckedChange = { onAction() }
                )
            }
        }
    }
}