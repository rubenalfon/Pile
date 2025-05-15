package com.ganadoro.pile.ui.screens.home.compostables

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.R
import com.ganadoro.pile.models.PileModel
import com.ganadoro.pile.ui.compostables.AdaptiveSizeFlowRow
import com.ganadoro.pile.ui.compostables.Pile
import java.util.UUID

@Composable
fun PileGrid(
    modifier: Modifier = Modifier,
    pileModels: List<PileModel>,
    onPileClick: (id: UUID) -> Unit = {}
) {
    AdaptiveSizeFlowRow(
        modifier = modifier,
        minimumItemWidth = 140.dp,
        horizontalSpacing = 8.dp,
        verticalSpacing = 8.dp,
        horizontalAlignment = Alignment.Start
    ) { itemWidth ->
        pileModels.forEach { pile ->
            Pile(
                pileModel = pile,
                modifier = Modifier
                    .width(itemWidth),
                onClick = onPileClick
            )
        }
        Pile(
            pileModel = PileModel(
                name = stringResource(R.string.new_pile),
                icon = Icons.Default.Add
            ),
            modifier = Modifier.width(itemWidth),
            onClick = onPileClick
        )
    }
}