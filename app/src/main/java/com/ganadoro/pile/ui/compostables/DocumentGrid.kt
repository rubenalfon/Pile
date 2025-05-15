package com.ganadoro.pile.ui.compostables

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.models.DocumentModel


@Preview
@Composable
private fun DocumentGridPreview() {
    Surface {
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
        ) {
            item {
                DocumentGrid(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    documentModels = listOf(
                        DocumentModel(
                            id = java.util.UUID.randomUUID(),
                            title = "Mi documento",
                            documentRoute = ""
                        ),
                        DocumentModel(
                            id = java.util.UUID.randomUUID(),
                            title = "Mi documento",
                            documentRoute = ""
                        ),
                        DocumentModel(
                            id = java.util.UUID.randomUUID(),
                            title = "Mi documento",
                            documentRoute = ""
                        )
                    )
                )
            }
        }
    }
}

@Composable
fun DocumentGrid(
    modifier: Modifier = Modifier,
    documentModels: List<DocumentModel>
) {
    AdaptiveSizeFlowRow(
        modifier = modifier,
        minimumItemWidth = 120.dp,
        horizontalSpacing = 16.dp,
        verticalSpacing = 16.dp,
        horizontalAlignment = Alignment.Start
    ) { itemWidth ->
        documentModels.forEach { document ->
            Document(
                documentModel = document,
                modifier = Modifier.width(itemWidth)
            )
        }
    }
}