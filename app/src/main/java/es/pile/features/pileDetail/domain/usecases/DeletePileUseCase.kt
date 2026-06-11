package es.pile.features.pileDetail.domain.usecases

import es.pile.core.domain.repositories.PileModelRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext


/**
 * Use case responsible for deleting an existing [es.pile.PileModel].
 */
class DeletePileUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val pileModelRepository: PileModelRepository
) {
    /**
     * Deletes an existing Pile from the repository.
     *
     * @param id The unique identifier of the pile.
     */
    suspend operator fun invoke(id: String) = withContext(ioDispatcher) {
        pileModelRepository.deletePileModel(id)
    }
}