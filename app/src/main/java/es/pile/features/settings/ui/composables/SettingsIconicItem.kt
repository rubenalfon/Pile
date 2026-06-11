package es.pile.features.settings.ui.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.pile.R
import es.pile.core.ui.theme.PileTheme

@Preview
@Composable
private fun SettingsIconicItemPrev() {
    PileTheme {
        Surface {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                var isSelected by remember { mutableStateOf(true) }
                SettingsIconicItem(
                    title = stringResource(R.string.original_quality_tittle),
                    description = stringResource(R.string.original_quality_body),
                    isSelected = isSelected,
                    onClick = { isSelected = !isSelected }
                )

                SettingsIconicItem(
                    title = stringResource(R.string.storage_saver_tittle),
                    description = stringResource(R.string.storage_saver_body),
                    isSelected = !isSelected,
                    onClick = { isSelected = !isSelected }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsIconicItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val transition = updateTransition(isSelected, label = "selected_state")

    val borderColor by transition.animateColor(label = "border_color") { selected ->
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    }

    val borderWidth by transition.animateDp(label = "border_width") { selected ->
        if (selected) 3.dp else 1.dp
    }

    val shapeSize by transition.animateDp(label = "shape_size") { selected ->
        if (selected) 12.dp else 22.dp
    }

    val containerColor by transition.animateColor(label = "container_color") { selected ->
        if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else Color.Transparent
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(shapeSize),
        border = BorderStroke(width = borderWidth, color = borderColor),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(text = title, style = MaterialTheme.typography.titleMedium)

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            ToggleButton(
                checked = isSelected,
                onCheckedChange = { onClick() }
            ) {
                AnimatedContent(
                    targetState = isSelected,
                    label = "text_animation",
                    transitionSpec = {
                        if (targetState) (fadeIn()).togetherWith(fadeOut())
                        else (fadeIn()).togetherWith(fadeOut())
                    }
                ) { selected ->
                    Text(
                        text = stringResource(if (selected) R.string.selected_ else R.string.select),
                        modifier = Modifier.animateContentSize()
                    )
                }
            }
        }
    }

}