package com.ganadoro.pile.ui.screens.home.compostables

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.ui.theme.ExtendedTheme
import java.util.UUID

@Composable
fun Pile(
    modifier: Modifier = Modifier,
    pileModel: PileModel,
    onClick: (String) -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clip(RoundedCornerShape(if (pileModel.colorNumber != null) 8.dp else 100.dp))
            .clickable {
                onClick(pileModel.id)
            }
            .background(pileModel.colorNumber?.let {
                ExtendedTheme.colors.customColorList.getOrNull(
                    it.toInt()
                )?.colorContainer
            }
                ?: MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Icon(
            imageVector = pileModel.icon,
            contentDescription = null,
            tint = pileModel.colorNumber?.let { ExtendedTheme.colors.customColorList.getOrNull(it.toInt())?.onColorContainer }
                ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(32.dp)
                .alpha(0.8f)
        )
        Text(
            pileModel.name,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Left,
            color = pileModel.colorNumber?.let { ExtendedTheme.colors.customColorList.getOrNull(it.toInt())?.onColorContainer }
                ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
