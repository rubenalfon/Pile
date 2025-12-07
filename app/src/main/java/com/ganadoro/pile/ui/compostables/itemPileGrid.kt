package com.ganadoro.pile.ui.compostables

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.R


fun LazyListScope.itemPileGrid(
    availableWidth: Dp,
    piles: List<PileModel>,
    coloredPileIds: List<String>,
    onPileClick: (id: String) -> Unit = {},
    onNewPileClick: (() -> Unit)? = null,
    backgroundColor: Color = Color.Transparent
) {
    val addPile = PileModel(
        id = "0",
        name = "",
        iconId = "",
        colorNumber = null
    )

    val pilesAddPile = if (onNewPileClick == null) piles else piles + addPile

    adaptiveSizeItemsGrid(
        backgroundColor = backgroundColor,
        availableWidth = availableWidth,
        itemList = pilesAddPile,
        minimumItemWidth = 190.dp,
        horizontalSpacing = 8.dp,
        verticalSpacing = 8.dp,
        horizontalPadding = 16.dp,
        content = { modifier, pileModel ->
            if (pileModel != addPile) {
                Pile(
                    pileModel = pileModel,
                    modifier = modifier.fillMaxHeight(),
                    onClick = onPileClick,
                    isColored = coloredPileIds.contains(pileModel.id)
                )
            } else {
                Pile(
                    pileModel = PileModel(
                        id = "0",
                        name = stringResource(R.string.new_pile),
                        iconId = "Add",
                        colorNumber = null
                    ),
                    modifier = modifier.fillMaxHeight(),
                    onClick = { onNewPileClick?.invoke() },
                    isColored = false
                )
            }
        }
    )
}