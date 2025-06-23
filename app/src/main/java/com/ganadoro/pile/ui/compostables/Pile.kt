package com.ganadoro.pile.ui.compostables

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.ui.theme.ExtendedTheme

@Composable
fun Pile(
    modifier: Modifier = Modifier,
    pileModel: PileModel,
    isColored: Boolean,
    customShape: Shape? = null,
    onClick: (String) -> Unit = {}
) {
    var backgroundColor: Color? = null
    var foregroundColor: Color? = null

    if (isColored && pileModel.colorNumber != null) {
        backgroundColor =
            ExtendedTheme.colors.customColorList.getOrNull(pileModel.colorNumber.toInt())?.colorContainer
    }
    if (isColored && pileModel.colorNumber != null) {
        foregroundColor =
            ExtendedTheme.colors.customColorList.getOrNull(pileModel.colorNumber.toInt())?.onColorContainer
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clip(customShape ?: RoundedCornerShape(if (isColored) 8.dp else 100.dp))
            .clickable {
                onClick(pileModel.id)
            }
            .background(color = backgroundColor ?: MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Icon(
            imageVector = pileModel.icon,
            contentDescription = null,
            tint = foregroundColor ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(32.dp)
                .alpha(0.8f)
        )
        Text(
            pileModel.name,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Left,
            color = foregroundColor ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
