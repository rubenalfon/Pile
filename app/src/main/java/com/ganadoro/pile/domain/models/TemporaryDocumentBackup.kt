package com.ganadoro.pile.domain.models

import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel


/**
 * Data class representing a snapshot of a deleted document for restoration purposes.
 */
data class TemporaryDocumentBackup(
    val document: DocumentModel,
    val images: List<DocumentImage>
)