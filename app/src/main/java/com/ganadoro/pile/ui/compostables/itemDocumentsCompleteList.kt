package com.ganadoro.pile.ui.compostables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.DocumentModel
import java.time.LocalDate

fun LazyListScope.itemDocumentsCompleteList(
    availableWidth: Dp,
    backgroundColor: Color = Color.Transparent,
    documents: List<DocumentModel>,
) {
    val groupedDocuments: List<Pair<LocalDate, List<DocumentModel>>> =
        documents
            .groupBy { it.modificationDate }
            .toSortedMap(compareByDescending { it })
            .map { (date, docs) -> date to docs }

    for ((index, entry) in groupedDocuments.withIndex()) {
        val (date, docs) = entry

        item {
            DocumentsDivider(
                date = date,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(horizontal = 16.dp)
            )
        }

        item {
            Box(
                Modifier
                    .height(16.dp)
                    .fillMaxWidth()
                    .background(backgroundColor)
            )
        }

        adaptiveSizeItemsGrid(
            backgroundColor = backgroundColor,
            availableWidth = availableWidth,
            itemList = docs,
            minimumItemWidth = 125.dp,
            horizontalSpacing = 16.dp,
            verticalSpacing = 16.dp,
            horizontalPadding = 16.dp,
            content = { modifier, document ->
                Document(
                    documentModel = document,
                    modifier = modifier
                )
            }
        )

        if (index < groupedDocuments.size - 1) {
            item {
                Box(
                    Modifier
                        .height(16.dp)
                        .fillMaxWidth()
                        .background(backgroundColor)
                )
            }
        }
    }
}