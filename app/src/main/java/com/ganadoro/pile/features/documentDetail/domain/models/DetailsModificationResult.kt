package com.ganadoro.pile.features.documentDetail.domain.models

import com.ganadoro.pile.core.domain.models.DocumentDetail

/**
 * Result object containing the updated state after a document detail modification.
 */
data class DetailsModificationResult(
    val updatedDetails: List<DocumentDetail>,
    val updatedDeletedStack: List<DocumentDetail>
)