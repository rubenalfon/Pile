package es.pile.core.domain.useCases

import es.pile.PileModel
import es.pile.core.domain.repositories.PileModelRepository
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
     * @return The id of the created pile.
     */
    suspend operator fun invoke(name: String, iconId: String, color: Long): String {
        val pileModel = PileModel(
            id = UUID.randomUUID().toString(),
            name = name,
            iconId = iconId,
            colorNumber = color
        )

        pileModelRepository.insertPileModel(pileModel)

        return pileModel.id
    }
}