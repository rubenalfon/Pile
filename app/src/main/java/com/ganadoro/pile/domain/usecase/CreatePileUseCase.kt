package com.ganadoro.pile.domain.usecase

import com.ganadoro.pile.PileModel
import com.ganadoro.pile.repositories.PileModelRepository
import java.util.UUID

/**
 * Use case responsible for creating and persisting a new [PileModel].
 * Encapsulates the ID generation logic and data persistence.
 */
class CreatePileUseCase(
    private val pileModelRepository: PileModelRepository
) {
    /**
     * Creates a new Pile with a unique ID and saves it to the repository.
     *
     * @param name The display name of the pile.
     * @param iconId The identifier for the pile's icon.
     * @param color The color value (Long) associated with the pile.
     */
    suspend operator fun invoke(name: String, iconId: String, color: Long) {
        val pileModel = PileModel(
            id = UUID.randomUUID().toString(),
            name = "New Pile",
            iconId = iconId,
            colorNumber = color
        )

        pileModelRepository.insertPileModel(pileModel)
    }
}