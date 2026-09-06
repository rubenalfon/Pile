package es.pile.core.domain.models

/**
 * Constants representing the types of deleted entities stored in the tombstone table.
 */
object DeletedEntityType {
    const val DOCUMENT = "DOCUMENT"
    const val PILE = "PILE"
    const val IMAGE = "IMAGE"
}
