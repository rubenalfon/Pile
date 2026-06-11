package es.pile.features.home.domain.models

import es.pile.DocumentImage
import es.pile.DocumentModel

/**
 * Data class representing a snapshot of a deleted document for restoration purposes.
 */
data class TemporaryDocumentBackup(
    val document: DocumentModel,
    val images: List<DocumentImage>
)