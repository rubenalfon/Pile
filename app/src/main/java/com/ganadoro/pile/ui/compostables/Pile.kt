package com.ganadoro.pile.ui.compostables

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.util.getContainerColor
import com.ganadoro.pile.util.getOnContainerColor
import kotlinx.coroutines.delay
import java.util.UUID

data class Pile(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val icon: ImageVector,
    val color: Color? = null
)

@Preview
@Composable
private fun PileFolder() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            val pile1 =
                Pile(name = "Mis Pilas 1", icon = Icons.Default.Add, color = Color.Red)
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
            .clip(RoundedCornerShape(if (pile.color != null) 8.dp else 100.dp))
            .clickable {
                isBeingClicked = true
                onClick(pile.id)
            }
            .background(
                pile.color?.getContainerColor(isDark)
                    ?: MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(12.dp)
    ) {
        Icon(
            imageVector = pile.icon,
            contentDescription = null,
            tint = pile.color?.getOnContainerColor(isDark)
                ?: MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(40.dp)
                .alpha(0.8f)
        )
        Text(
            pile.name,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Left,
            color = pile.color?.getOnContainerColor(isDark)
                ?: MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
