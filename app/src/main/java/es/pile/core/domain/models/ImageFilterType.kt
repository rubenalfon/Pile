package es.pile.core.domain.models

/**
 * Enum representing different types of image filters.
 *
 * @property id Integer ID representing the filter type.
 */
enum class ImageFilterType(val id: Int) {
    ORIGINAL(0),
    GRAYSCALE(1),
    HIGH_CONTRAST(2);

    companion object {
        /**
         * Retrieves the [ImageFilterType] from its ID.
         *
         * @param id Integer ID representing the filter type.
         * @return The corresponding [ImageFilterType] or [ORIGINAL] if not found.
         */
        fun fromId(id: Int): ImageFilterType = entries.find { it.id == id } ?: ORIGINAL
    }
}