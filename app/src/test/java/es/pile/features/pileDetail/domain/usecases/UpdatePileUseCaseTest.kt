package es.pile.features.pileDetail.domain.usecases

import es.pile.PileModel
import es.pile.core.domain.repositories.PileModelRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UpdatePileUseCaseTest {

    private val pileModelRepository: PileModelRepository = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()
    
    private val updatePileUseCase = UpdatePileUseCase(
        ioDispatcher = testDispatcher,
        pileModelRepository = pileModelRepository
    )

    @Test
    fun `when invoke is called, then it should call updatePileModel on repository with correct data`() = runTest {
        // Given
        val id = "1"
        val name = "Test Pile"
        val iconId = "icon_1"
        val color = 0xFF0000L
        val expectedPileModel = PileModel(id, name, iconId, color)
        
        coEvery { pileModelRepository.updatePileModel(any()) } returns Unit

        // When
        updatePileUseCase(id, name, iconId, color)

        // Then
        coVerify { pileModelRepository.updatePileModel(expectedPileModel) }
    }
}
