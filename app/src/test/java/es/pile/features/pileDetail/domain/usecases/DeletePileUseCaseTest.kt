package es.pile.features.pileDetail.domain.usecases

import es.pile.core.domain.repositories.PileModelRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeletePileUseCaseTest {

    private val pileModelRepository: PileModelRepository = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()
    
    private val deletePileUseCase = DeletePileUseCase(
        ioDispatcher = testDispatcher,
        pileModelRepository = pileModelRepository
    )

    @Test
    fun `when invoke is called, then it should call deletePileModel on repository with correct id`() = runTest {
        // Given
        val id = "123"
        coEvery { pileModelRepository.deletePileModel(any()) } returns Unit

        // When
        deletePileUseCase(id)

        // Then
        coVerify { pileModelRepository.deletePileModel(id) }
    }
}
