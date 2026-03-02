package com.ganadoro.pile.core.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectPilesBottomSheet(
    modifier: Modifier = Modifier,
    title: String,
    pileList: List<PileModel>?,
    selectedFilterPiles: List<String> = emptyList(),
    onDismissBottomSheet: () -> Unit,
    onPileClick: (pileId: String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissBottomSheet
    ) {
        Column(modifier.verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp).padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onDismissBottomSheet) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close_pile_selection_bottom_sheet)
                    )
                }
            }

            pileList?.forEach { pileModel ->
                Pile(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    pileModel = pileModel,
                    isColored = selectedFilterPiles.contains(pileModel.id),
                    onClick = onPileClick
                )
                Spacer(Modifier.height(4.dp))
            }
            Spacer(Modifier.height(50.dp))
        }
    }
}
