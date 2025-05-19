package com.ganadoro.pile.ui.screens.home.compostables

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.R
import com.ganadoro.pile.models.PileModel
import com.ganadoro.pile.ui.compostables.Pile
import com.ganadoro.pile.ui.compostables.adaptiveSizeItemsGrid
import java.util.UUID


fun LazyListScope.itemPileGrid(
    availableWidth: Dp,
    piles: List<PileModel>,
    onPileClick: (id: UUID) -> Unit = {},
    onNewPileClick: () -> Unit = {}
) {
    val addPile = PileModel(
        name = "",
        icon = Icons.Default.Add
    )
    val pilesAddPile = piles + addPile
    adaptiveSizeItemsGrid(
        backgroundColor = Color.Transparent,
        availableWidth = availableWidth,
        itemList = pilesAddPile,
        minimumItemWidth = 190.dp,
        horizontalSpacing = 8.dp,
        verticalSpacing = 8.dp,
        horizontalPadding = 16.dp,
        content = { modifier, document ->
            if (document != addPile) {
                Pile(
                    pileModel = document,
                    modifier = modifier,
                    onClick = onPileClick
                )
            } else {
                Pile(
                    pileModel = PileModel(
                        name = stringResource(R.string.new_pile),
                        icon = Icons.Default.Add
                    ),
                    modifier = modifier,
                    onClick = { onNewPileClick.invoke() }
                )
            }
        }
    )
}