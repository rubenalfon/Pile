package com.ganadoro.pile.ui.compostables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.ui.theme.ExtendedTheme
import kotlinx.coroutines.delay
import java.util.UUID

data class Pile(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val icon: ImageVector,
    val colorNumber: Int? = null // max = 30
)

@Preview
@Composable
private fun PileFolder() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            val pile1 =
                Pile(name = "Mis Pilas 1", icon = Icons.Default.Add, colorNumber = 1)
            val pile2 = Pile(name = "Mis Pilas 2", icon = Icons.Default.Add)

            Pile(modifier = Modifier, pile = pile1)
            Pile(modifier = Modifier, pile = pile2)
        }
    }
}

@Composable
fun Pile(
    modifier: Modifier = Modifier,
    pile: Pile,
    onClick: (UUID) -> Unit = {}
) {
    var isBeingClicked by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    val scale by animateFloatAsState(
        targetValue = if (isBeingClicked) 0.96f else 1f,
        label = "ClickScale"
    )

    LaunchedEffect(isBeingClicked) {
        if (isBeingClicked) {
            delay(120)
            isBeingClicked = false
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(if (pile.colorNumber != null) 8.dp else 100.dp))
            .clickable {
                isBeingClicked = true
                onClick(pile.id)
            }
            .background(pile.colorNumber?.let { ExtendedTheme.colors.customColorList.getOrNull(it)?.colorContainer }
                ?: MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Icon(
            imageVector = pile.icon,
            contentDescription = null,
            tint = pile.colorNumber?.let { ExtendedTheme.colors.customColorList.getOrNull(it)?.onColorContainer }
                ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(40.dp)
                .alpha(0.8f)
        )
        Text(
            pile.name,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Left,
            color = pile.colorNumber?.let { ExtendedTheme.colors.customColorList.getOrNull(it)?.onColorContainer }
                ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
