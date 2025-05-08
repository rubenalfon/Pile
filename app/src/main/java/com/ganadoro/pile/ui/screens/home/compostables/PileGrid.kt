package com.ganadoro.pile.ui.screens.home.compostables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.CastForEducation
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.ui.compostables.AdaptiveSizeFlowRow
import com.ganadoro.pile.ui.compostables.Pile
import kotlin.math.floor


@Preview
@Composable
private fun PilePreview() {
    Surface {
        PileGrid(
            piles = listOf(
                Pile(name = "Mis Pilas 1", icon = Icons.Default.Add, color = Color.Red),
                Pile(name = "Mis Pilas 2", icon = Icons.Default.AddRoad),
                Pile(name = "Mis Pilas 3"),
                Pile(
                    name = "Mis Pilas 1",
                    icon = Icons.Default.CastForEducation,
                    color = Color.Red
                ),
                Pile(name = "Mis Pilas 2", icon = Icons.Default.CloudUpload),
                Pile(name = "Banco")
            )
        )
    }
}

@Composable
fun PileGrid(
    modifier: Modifier = Modifier,
    piles: List<Pile>
) {
    AdaptiveSizeFlowRow(
        modifier = modifier,
        minimumItemWidth = 70.dp,
        horizontalSpacing = 16.dp,
        verticalSpacing = 16.dp,
        horizontalAlignment = Alignment.Start
    ) { itemWidth ->
        piles.forEach { pile ->
            Pile(
                pile = pile,
                modifier = Modifier
                    .width(itemWidth)
            )
        }
    }
}