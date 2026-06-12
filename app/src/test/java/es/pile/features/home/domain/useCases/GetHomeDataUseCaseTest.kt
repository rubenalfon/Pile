package es.pile.features.home.domain.useCases

import es.pile.DocumentModel
import es.pile.PileModel
import es.pile.core.domain.models.DocumentStatusConstants
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.PileModelRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GetHomeDataUseCaseTest {

    private val pileModelRepository: PileModelRepository = mockk()
    private val documentModelRepository: DocumentModelRepository = mockk()
    private val getHomeDataUseCase = GetHomeDataUseCase(pileModelRepository, documentModelRepository)

    @Test
    fun `invoke should aggregate data correctly`() = runTest {
        // Given
        val mockPiles = listOf(PileModel("p1", "Pile 1", "icon1", 1))
        val mockDocuments = listOf(
            mockk<DocumentModel> {
                every { id } returns "d1"
                every { documentStatus } returns DocumentStatusConstants.SAVED
                every { documentPileIds } returns listOf("p1")
            },
            mockk<DocumentModel> {
                every { id } returns "temp"
                every { documentStatus } returns DocumentStatusConstants.TEMPORARY
                every { documentPileIds } returns emptyList()
            }
        )

        every { pileModelRepository.pileModels } returns flowOf(mockPiles)
        every { documentModelRepository.documentModels } returns flowOf(mockDocuments)

        // When
        val result = getHomeDataUseCase().first()

        // Then
        assertEquals(mockPiles, result.piles)
        assertEquals(1, result.documents.size)
        assertEquals("d1", result.documents[0].id)
        assertNotNull(result.temporaryDocument)
        assertEquals("temp", result.temporaryDocument.id)
        assertEquals(listOf("p1"), result.coloredPileIds)
    }

    @Test
    fun `invoke should handle no temporary document`() = runTest {
        // Given
        val mockPiles = emptyList<PileModel>()
        val mockDocuments = listOf(
            mockk<DocumentModel> {
                every { id } returns "d1"
                every { documentStatus } returns DocumentStatusConstants.SAVED
                every { documentPileIds } returns emptyList()
            }
        )

        every { pileModelRepository.pileModels } returns flowOf(mockPiles)
        every { documentModelRepository.documentModels } returns flowOf(mockDocuments)

        // When
        val result = getHomeDataUseCase().first()

        // Then
        assertNull(result.temporaryDocument)
        assertEquals(1, result.documents.size)
    }
}
