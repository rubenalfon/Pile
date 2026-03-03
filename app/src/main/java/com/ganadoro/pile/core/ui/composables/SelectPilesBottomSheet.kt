package com.ganadoro.pile.core.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SelectPilesBottomSheet(
    modifier: Modifier = Modifier,
    title: String,
    pileList: List<PileModel>?,
    selectedFilterPiles: List<String> = emptyList(),
    onDismissBottomSheet: () -> Unit,
    onPileClick: (pileId: String) -> Unit,
    onNewPile: (() -> Unit)? = null
) {
    ModalBottomSheet(
        onDismissRequest = onDismissBottomSheet
    ) {
        Column(modifier.verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .padding(start = 8.dp),
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

            if (pileList?.isEmpty() == true && onNewPile != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        stringResource(R.string.no_piles_saved),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = onNewPile,
                        modifier = Modifier.heightIn(ButtonDefaults.MediumContainerHeight),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.add_a_detail),
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                stringResource(R.string.add_a_detail),
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(50.dp))
        }
    }
}
