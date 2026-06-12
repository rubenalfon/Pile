package es.pile.features.addDocument.domain.useCases

import es.pile.DocumentModel
import es.pile.core.domain.models.DocumentStatusConstants
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.features.addDocument.domain.models.DocumentSaveException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertTrue

class SaveDocumentUseCaseTest {

    private val documentModelRepository: DocumentModelRepository = mockk()
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val saveDocumentUseCase = SaveDocumentUseCase(testDispatcher, documentModelRepository)

    @Test
    fun `invoke should update document when valid`() = runTest {
        // Given
        val doc = DocumentModel(
            id = "doc1",
            title = "Old Title",
            imageIds = emptyList(),
            creationDateTime = LocalDateTime.now(),
            modificationDateTime = LocalDateTime.now(),
            documentStatus = DocumentStatusConstants.TEMPORARY,
            documentDetails = emptyList(),
            documentNote = "",
            documentPileIds = emptyList(),
            documentOrganizationIds = emptyList(),
            isIncomingPdf = false
        )
        val name = "My Document"
        coEvery { documentModelRepository.updateDocumentModel(any()) } returns Unit

        // When
        val result = saveDocumentUseCase(doc, name)

        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            documentModelRepository.updateDocumentModel(match { 
                it.title == name && it.documentStatus == DocumentStatusConstants.SAVED 
            }) 
        }
    }

    @Test
    fun `invoke should return EmptyName error when name is blank`() = runTest {
        // Given
        val doc = DocumentModel(
            id = "doc1",
            title = "Old Title",
            imageIds = emptyList(),
            creationDateTime = LocalDateTime.now(),
            modificationDateTime = LocalDateTime.now(),
            documentStatus = DocumentStatusConstants.TEMPORARY,
            documentDetails = emptyList(),
            documentNote = "",
            documentPileIds = emptyList(),
            documentOrganizationIds = emptyList(),
            isIncomingPdf = false
        )
        val name = "  "

        // When
        val result = saveDocumentUseCase(doc, name)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DocumentSaveException.EmptyName)
    }

    @Test
    fun `invoke should return AlreadySaved error when status is SAVED`() = runTest {
        // Given
        val doc = mockk<DocumentModel>(relaxed = true) {
            every { documentStatus } returns DocumentStatusConstants.SAVED
        }
        val name = "Some name"

        // When
        val result = saveDocumentUseCase(doc, name)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DocumentSaveException.AlreadySaved)
    }
}
