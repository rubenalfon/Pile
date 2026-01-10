package com.ganadoro.pile.ui.compostables

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.models.TEMP_DOCUMENT_ID
import java.time.LocalDate

fun LazyListScope.itemDocumentsCompleteList(
    availableWidth: Dp,
    backgroundColor: Color = Color.Transparent,
    documents: List<DocumentModel>,
    bitmapCache: Map<String, Bitmap>,
    onLoadBitmap: suspend (documentId: String, imageId: String) -> Unit,
    onDocumentClick: (documentId: String) -> Unit = {}
) {
    val groupedDocuments: List<Pair<LocalDate, List<DocumentModel>>> =
        documents
            .filter { it.id != TEMP_DOCUMENT_ID }
            .groupBy { it.modificationDate }
            .toSortedMap(compareByDescending { it })
            .map { (date, docs) -> date to docs }

    for (entry in groupedDocuments) {
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
                val cachedBitmap = bitmapCache[document.imageIds.first()]

                if (cachedBitmap == null) {
                    LaunchedEffect(key1 = document.id) {
                        onLoadBitmap(document.id, document.imageIds.first())
                    }
                }

                Document(
                    documentModel = document,
                    imageBitmap = cachedBitmap?.asImageBitmap(),
                    modifier = modifier,
                    onClick = onDocumentClick
                )
            }
        )

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
