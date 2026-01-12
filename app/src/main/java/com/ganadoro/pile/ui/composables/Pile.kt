package com.ganadoro.pile.ui.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.R
import com.ganadoro.pile.ui.theme.ExtendedTheme
import com.ganadoro.pile.util.IconPack

@Composable
fun Pile(
    modifier: Modifier = Modifier,
    pileModel: PileModel,
    isColored: Boolean,
    customShape: Shape? = null,
    onClick: (String) -> Unit = {}
) {
    val colorIndex = remember(pileModel.colorNumber) {
        pileModel.colorNumber?.toInt()
    }
    val colored = isColored && colorIndex != null

    val backgroundColor by animateColorAsState(
        targetValue = if (colored) {
            ExtendedTheme.colors.customColorList.getOrNull(colorIndex)?.colorContainer
                ?: MaterialTheme.colorScheme.surface
        } else MaterialTheme.colorScheme.surface,
        label = "backgroundColor"
    )
    val foregroundColor by animateColorAsState(
        targetValue = if (colored) {
            ExtendedTheme.colors.customColorList.getOrNull(colorIndex)?.onColorContainer
                ?: MaterialTheme.colorScheme.onSurface
        } else MaterialTheme.colorScheme.onSurface,
        label = "foregroundColor"
    )

    val cornerShape = if (customShape != null) customShape
    else {
        val cornerSize by animateDpAsState(
            targetValue = if (isColored) 8.dp else 24.dp,
            label = "cornerShape"
        )
        RoundedCornerShape(cornerSize)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clip(cornerShape)
            .clickable { onClick(pileModel.id) }
            .background(backgroundColor)
            .padding(12.dp)
    ) {
        Icon(
            painter = painterResource(
                IconPack.getIcon(pileModel.iconId) ?: R.drawable.warning_24px
            ),
            contentDescription = null,
            tint = foregroundColor,
            modifier = Modifier
                .size(32.dp)
                .alpha(0.8f)
        )
        Text(
            pileModel.name,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Left,
            color = foregroundColor,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
