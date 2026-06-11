package es.pile.features.pileDetail.domain.usecases

import es.pile.PileModel
import es.pile.core.domain.repositories.PileModelRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext


/**
 * Use case responsible for updating an existing [es.pile.PileModel].
 */
class UpdatePileUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val pileModelRepository: PileModelRepository
) {
    /**
     * Updates an existing Pile and saves it to the repository.
     *
     * @param id The unique identifier of the pile.
     * @param name The display name of the pile.
     * @param iconId The identifier for the pile's icon.
     * @param color The color value (Long) associated with the pile.
     */
    suspend operator fun invoke(id: String, name: String, iconId: String, color: Long) =
        withContext(ioDispatcher) {
            val pileModel = PileModel(
                id = id,
                name = name,
                iconId = iconId,
                colorNumber = color
            )

            pileModelRepository.updatePileModel(pileModel)
        }
}