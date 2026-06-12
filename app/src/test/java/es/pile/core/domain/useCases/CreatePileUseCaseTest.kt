package es.pile.core.domain.useCases

import es.pile.core.domain.repositories.PileModelRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertTrue

class CreatePileUseCaseTest {

    private val pileModelRepository: PileModelRepository = mockk()
    private val createPileUseCase = CreatePileUseCase(pileModelRepository)

    @Test
    fun `invoke should insert pile model and return its id`() = runTest {
        // Given
        val name = "New Pile"
        val iconId = "icon_id"
        val color = 0xFF0000L
        coEvery { pileModelRepository.insertPileModel(any()) } returns Unit

        // When
        val resultId = createPileUseCase(name, iconId, color)

        // Then
        assertTrue(resultId.isNotEmpty())
        coVerify { 
            pileModelRepository.insertPileModel(match { 
                it.id == resultId && it.name == name && it.iconId == iconId && it.colorNumber == color 
            }) 
        }
    }
}
