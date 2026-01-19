package com.ganadoro.pile.domain.usecases

import com.ganadoro.pile.domain.models.ImageFilterType

/**
 * Use case responsible for retrieving a list of available image filters.
 *
 */
class GetAvailableFiltersUseCase {
    /**
     * Retrieves a list of available image filters.
     *
     * @return A list of [ImageFilterType] representing the available filters.
     */
    operator fun invoke(): List<ImageFilterType> {
        return ImageFilterType.entries
    }
}