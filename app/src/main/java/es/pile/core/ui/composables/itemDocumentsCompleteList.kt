package es.pile.core.ui.composables

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import es.pile.DocumentModel
import es.pile.core.domain.models.DocumentCoverItem
import es.pile.core.domain.models.DocumentStatusConstants
import es.pile.core.ui.theme.PileTheme
import java.time.LocalDateTime

@Preview(showBackground = true)
@Composable
fun ItemDocumentsCompleteListPreview() {
    val mockDocs = listOf(
        DocumentCoverItem(
            document = DocumentModel(
                id = "1",
                title = "Document 1",
                imageIds = emptyList(),
                creationDateTime = LocalDateTime.now(),
                modificationDateTime = LocalDateTime.now(),
                documentStatus = DocumentStatusConstants.SAVED,
                documentPileIds = emptyList(),
                documentDetails = emptyList(),
                documentNote = "",
                documentOrganizationIds = emptyList(),
                isIncomingPdf = false
            ),
            coverImageCacheKey = "key1"
        ),
        DocumentCoverItem(
            document = DocumentModel(
                id = "2",
                title = "Document 2",
                imageIds = emptyList(),
                creationDateTime = LocalDateTime.now().minusDays(1),
                modificationDateTime = LocalDateTime.now().minusDays(1),
                documentStatus = DocumentStatusConstants.SAVED,
                documentPileIds = emptyList(),
                documentDetails = emptyList(),
                documentNote = "",
                documentOrganizationIds = emptyList(),
                isIncomingPdf = false
            ),
            coverImageCacheKey = "key2"
        )
    )

    PileTheme {
        LazyColumn {
            itemDocumentsCompleteList(
                availableWidth = 400.dp,
                documents = mockDocs,
                bitmapCache = emptyMap(),
                onLoadBitmap = {}
            )
        }
    }
}

fun LazyListScope.itemDocumentsCompleteList(
    availableWidth: Dp,
    backgroundColor: Color = Color.Transparent,
    documents: List<DocumentCoverItem>,
    bitmapCache: Map<String, Bitmap>,
    onLoadBitmap: suspend (document: DocumentModel) -> Unit,
    onDocumentClick: (documentId: String) -> Unit = {}
) {
    val groupedDocuments = documents
        .filter { it.document.documentStatus == DocumentStatusConstants.SAVED }
        .groupBy { it.document.modificationDateTime.toLocalDate() }
        .toSortedMap(compareByDescending { it })

    groupedDocuments.forEach { (date, docs) ->
        val sortedDocs = docs.sortedByDescending { it.document.modificationDateTime }

        item(key = "header_$date") {
            DocumentsDivider(
                date = date,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(horizontal = 16.dp)
            )
        }

        item(key = "spacer_top_$date") {
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
            itemList = sortedDocs,
            minimumItemWidth = 125.dp,
            horizontalSpacing = 16.dp,
            verticalSpacing = 16.dp,
            horizontalPadding = 16.dp,
            content = { modifier, documentItem ->
                val key = documentItem.coverImageCacheKey
                val cachedBitmap: Bitmap? = bitmapCache[key]

                if (cachedBitmap == null) {
                    LaunchedEffect(key1 = key) {
                        onLoadBitmap(documentItem.document)
                    }
                }

                Document(
                    documentModel = documentItem.document,
                    imageBitmap = cachedBitmap?.asImageBitmap(),
                    modifier = modifier,
                    onClick = onDocumentClick
                )
            }
        )

        item(key = "spacer_bottom_$date") {
            Box(
                Modifier
                    .height(16.dp)
                    .fillMaxWidth()
                    .background(backgroundColor)
            )
        }
    }
}
