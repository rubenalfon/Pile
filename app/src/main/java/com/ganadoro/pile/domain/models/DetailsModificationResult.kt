package com.ganadoro.pile.domain.models

/**
 * Result object containing the updated state after a document detail modification.
 */
data class DetailsModificationResult(
    val updatedDetails: List<DocumentDetail>,
    val updatedDeletedStack: List<DocumentDetail>
)