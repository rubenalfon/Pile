package com.ganadoro.pile.ui.screens.home.compostables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.CastForEducation
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.ui.compostables.PileFolder
import com.ganadoro.pile.ui.compostables.Pile


@Preview
@Composable
private fun PileFolderPreview() {
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
    LazyVerticalStaggeredGrid(
        modifier = modifier,
        columns = StaggeredGridCells.Adaptive(minSize = 70.dp),
        horizontalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.Start
        ),
        verticalItemSpacing = 16.dp
    ) {
        items(piles.size) { index ->
            PileFolder(
                pile = piles[index]
            )
        }
    }

}
