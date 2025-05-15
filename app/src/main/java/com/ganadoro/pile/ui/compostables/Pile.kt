package com.ganadoro.pile.ui.compostables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.models.PileModel
import com.ganadoro.pile.ui.theme.ExtendedTheme
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import java.util.UUID

@Composable
fun Pile(
    modifier: Modifier = Modifier,
    pileModel: PileModel,
    onClick: (UUID) -> Unit = {}
) {
    var isBeingClicked by remember { mutableStateOf(false) }

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
            .clip(RoundedCornerShape(if (pileModel.colorNumber != null) 8.dp else 100.dp))
            .clickable {
                isBeingClicked = true
                onClick(pileModel.id)
            }
            .background(pileModel.colorNumber?.let { ExtendedTheme.colors.customColorList.getOrNull(it)?.colorContainer }
                ?: MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Icon(
            imageVector = pileModel.icon,
            contentDescription = null,
            tint = pileModel.colorNumber?.let { ExtendedTheme.colors.customColorList.getOrNull(it)?.onColorContainer }
                ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(32.dp)
                .alpha(0.8f)
        )
        Text(
            pileModel.name,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Left,
            color = pileModel.colorNumber?.let { ExtendedTheme.colors.customColorList.getOrNull(it)?.onColorContainer }
                ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
