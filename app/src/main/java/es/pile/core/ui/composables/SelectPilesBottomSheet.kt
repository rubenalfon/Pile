package es.pile.core.ui.composables

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.pile.PileModel
import es.pile.R

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

            val hapticFeedback = LocalHapticFeedback.current

            pileList?.forEach { pileModel ->
                Pile(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    pileModel = pileModel,
                    isColored = selectedFilterPiles.contains(pileModel.id),
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onPileClick(it)
                    }
                )
                Spacer(Modifier.height(4.dp))
            }
            if (pileList?.isNotEmpty() == true && onNewPile != null) {
                Pile(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    pileModel = PileModel(
                        id = "0",
                        name = stringResource(R.string.new_pile),
                        iconId = "Add",
                        colorNumber = null
                    ),
                    isColored = false,
                    onClick = { onNewPile() }
                )
                Spacer(Modifier.height(4.dp))
            }
            if (pileList?.isEmpty() == true && onNewPile != null) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.user_ic_category_24px),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )

                    Text(
                        stringResource(R.string.no_piles_saved),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = onNewPile,
                        modifier = Modifier.heightIn(ButtonDefaults.MediumContainerHeight),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                        )
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.new_pile))
                    }
                }
            }
            Spacer(Modifier.height(50.dp))
        }
    }
}
