package com.ganadoro.pile.ui.screens.home.compostables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CastForEducation
import androidx.compose.material.icons.filled.CircleNotifications
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Home
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
                Pile(name = "Home", icon = Icons.Default.Home, color = Color.Red),
                Pile(name = "Work", icon = Icons.Default.AddRoad),
                Pile(name = "Church", icon = Icons.Default.CircleNotifications, color = Color.Red),
                Pile(name = "Nightime", icon = Icons.Default.Bedtime),
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
        minimumItemWidth = 160.dp,
        horizontalSpacing = 8.dp,
        verticalSpacing = 8.dp,
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