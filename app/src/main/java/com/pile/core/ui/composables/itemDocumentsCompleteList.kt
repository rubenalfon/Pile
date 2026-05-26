package com.pile.core.ui.composables

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
import com.pile.DocumentModel
import com.pile.core.domain.models.DocumentCoverItem
import com.pile.core.domain.models.DocumentStatusConstants
import java.time.LocalDate

fun LazyListScope.itemDocumentsCompleteList(
    availableWidth: Dp,
    backgroundColor: Color = Color.Transparent,
    documents: List<DocumentCoverItem>,
    bitmapCache: Map<String, Bitmap>,
    onLoadBitmap: suspend (document: DocumentModel) -> Unit,
    onDocumentClick: (documentId: String) -> Unit = {}
) {
    val groupedDocuments: List<Pair<LocalDate, List<DocumentCoverItem>>> =
        documents
            .filter { it.document.documentStatus == DocumentStatusConstants.SAVED }
            .groupBy { it.document.modificationDateTime.toLocalDate() }
            .toSortedMap(compareByDescending { it })
            .map { (date, docs) -> date to docs.sortedByDescending { it.document.modificationDateTime } }

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
                val key = document.coverImageCacheKey

                val cachedBitmap: Bitmap? = bitmapCache[key]

                if (cachedBitmap == null) {
                    LaunchedEffect(key1 = key) {
                        onLoadBitmap(document.document)
                    }
                }

                Document(
                    documentModel = document.document,
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
