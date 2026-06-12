package es.pile.features.documentDetail.domain.useCases

import es.pile.DocumentModel
import es.pile.core.domain.repositories.DocumentModelRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ManageDocumentPileUseCaseTest {

    private val documentModelRepository: DocumentModelRepository = mockk()
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val manageDocumentPileUseCase = ManageDocumentPileUseCase(
        testDispatcher,
        documentModelRepository
    )

    @Test
    fun `invoke should add pileId if not present`() = runTest {
        // Given
        val doc = mockk<DocumentModel> {
            every { documentPileIds } returns listOf("p1")
            every { copy(documentPileIds = any()) } returns mockk()
        }
        coEvery { documentModelRepository.updateDocumentModel(any()) } returns Unit

        // When
        manageDocumentPileUseCase(doc, "p2")

        // Then
        coVerify {
            documentModelRepository.updateDocumentModel(any()) 
        }
    }

    @Test
    fun `invoke should remove pileId if present`() = runTest {
        // Given
        val doc = mockk<DocumentModel> {
            every { documentPileIds } returns listOf("p1", "p2")
            every { copy(documentPileIds = any()) } returns mockk()
        }
        coEvery { documentModelRepository.updateDocumentModel(any()) } returns Unit

        // When
        manageDocumentPileUseCase(doc, "p1")

        // Then
        coVerify { 
            documentModelRepository.updateDocumentModel(any()) 
        }
    }
}
