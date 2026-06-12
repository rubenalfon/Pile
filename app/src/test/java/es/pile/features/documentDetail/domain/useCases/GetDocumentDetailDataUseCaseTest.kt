package es.pile.features.documentDetail.domain.useCases

import es.pile.DocumentModel
import es.pile.PileModel
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.repositories.PileModelRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetDocumentDetailDataUseCaseTest {

    private val documentModelRepository: DocumentModelRepository = mockk()
    private val pileModelRepository: PileModelRepository = mockk()
    private val fileRepository: FileRepository = mockk()
    private val getDocumentDetailDataUseCase = GetDocumentDetailDataUseCase(
        documentModelRepository,
        pileModelRepository,
        fileRepository
    )

    @Test
    fun `invoke should aggregate document data correctly`() = runTest {
        // Given
        val docId = "doc1"
        val mockDocument = mockk<DocumentModel> {
            every { id } returns docId
            every { documentPileIds } returns listOf("p1")
            every { isIncomingPdf } returns true
            every { imageIds } returns emptyList()
        }
        val mockPiles = listOf(PileModel("p1", "Pile 1", "icon1", 1))
        val allPiles = listOf(PileModel("p1", "Pile 1", "icon1", 1), PileModel("p2", "Pile 2", "icon2", 2))

        every { documentModelRepository.getDocumentModelById(docId) } returns flowOf(mockDocument)
        every { pileModelRepository.getPileModelsByIds(listOf("p1")) } returns flowOf(mockPiles)
        every { pileModelRepository.pileModels } returns flowOf(allPiles)
        io.mockk.coEvery { fileRepository.getPageCount(docId) } returns Result.success(5)

        // When
        val result = getDocumentDetailDataUseCase(docId).first()

        // Then
        assertEquals(mockDocument, result?.document)
        assertEquals(mockPiles, result?.documentPiles)
        assertEquals(5, result?.pdfPageCount)
        assertEquals(allPiles, result?.allPiles)
    }

    @Test
    fun `invoke should return null if document not found`() = runTest {
        // Given
        val docId = "unknown"
        every { documentModelRepository.getDocumentModelById(docId) } returns flowOf(null)
        every { pileModelRepository.pileModels } returns flowOf(emptyList())

        // When
        val result = getDocumentDetailDataUseCase(docId).first()

        // Then
        assertNull(result)
    }
}
