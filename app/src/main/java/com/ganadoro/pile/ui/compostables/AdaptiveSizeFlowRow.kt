package com.ganadoro.pile.ui.compostables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.floor


@Composable
fun AdaptiveSizeFlowRow(
    modifier: Modifier = Modifier,
    minimumItemWidth: Dp,
    horizontalSpacing: Dp,
    verticalSpacing: Dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable (itemWidth: Dp) -> Unit
) {
    var widthDp by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    val nItemsPerRow =
        floor((widthDp + horizontalSpacing) / (minimumItemWidth + horizontalSpacing)).toInt()
            .coerceAtLeast(1)
    val itemWidth = (widthDp - horizontalSpacing * (nItemsPerRow - 1)) / nItemsPerRow

    FlowRow(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val widthPx = coordinates.size.width
                widthDp = with(density) { widthPx.toDp() }.value.dp
            }
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = horizontalSpacing,
            alignment = horizontalAlignment
        ),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        content = { content(itemWidth) }
    )
}
