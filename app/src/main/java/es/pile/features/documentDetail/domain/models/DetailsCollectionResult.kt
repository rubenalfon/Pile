package es.pile.features.documentDetail.domain.models

import es.pile.core.domain.models.DocumentDetail

/**
 * Result object containing the updated state after a document detail modification.
 */
data class DetailsCollectionResult(
    val updatedDetails: List<DocumentDetail>,
    val updatedDeletedStack: List<DocumentDetail>
)