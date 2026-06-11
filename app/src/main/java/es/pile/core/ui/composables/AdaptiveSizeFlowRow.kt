package es.pile.core.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.floor

fun <T> LazyListScope.adaptiveSizeItemsGrid(
    backgroundColor: Color,
    availableWidth: Dp,
    itemList: List<T>,
    minimumItemWidth: Dp,
    horizontalSpacing: Dp = 16.dp,
    verticalSpacing: Dp = 16.dp,
    horizontalPadding: Dp = 16.dp,
    content: @Composable (modifier: Modifier, document: T) -> Unit
) {
    val nItemsPerRow = floor(
        (availableWidth + horizontalSpacing + horizontalPadding * 2) / (minimumItemWidth + horizontalSpacing)
    ).toInt().coerceAtLeast(1)

    val rows = itemList.chunked(nItemsPerRow)

    rows.forEachIndexed { index, rowItems ->
        item {
            Row(
                modifier = Modifier
                    .background(backgroundColor)
                    .padding(horizontal = horizontalPadding)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
            ) {
                rowItems.forEach { document ->
                    content.invoke(Modifier.weight(1f), document)
                }
                repeat(nItemsPerRow - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (index < rows.size - 1) {
            item {
                Box(
                    Modifier
                        .background(backgroundColor)
                        .height(verticalSpacing)
                        .fillMaxWidth()
                )
            }
        }
    }
}