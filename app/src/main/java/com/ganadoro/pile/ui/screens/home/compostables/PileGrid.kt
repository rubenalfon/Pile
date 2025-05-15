package com.ganadoro.pile.ui.screens.home.compostables

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CircleNotifications
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.R
import com.ganadoro.pile.ui.compostables.AdaptiveSizeFlowRow
import com.ganadoro.pile.ui.compostables.Pile


@Preview
@Composable
private fun PilePreview() {
    Surface {
        PileGrid(
            piles = listOf(
                Pile(name = "Home", icon = Icons.Default.Home, colorNumber = 8),
                Pile(name = "Work", icon = Icons.Default.AddRoad),
                Pile(name = "Church", icon = Icons.Default.CircleNotifications, colorNumber = 10),
                Pile(name = "Nighttime", icon = Icons.Default.Bedtime),
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
        minimumItemWidth = 140.dp,
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
        Pile(
            pile = Pile(name = stringResource(R.string.new_pile), icon = Icons.Default.Add),
            modifier = Modifier.width(itemWidth)
        )
    }
}