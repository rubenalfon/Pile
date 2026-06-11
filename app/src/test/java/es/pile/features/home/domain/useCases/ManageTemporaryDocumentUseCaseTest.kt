package es.pile.features.home.domain.useCases

import es.pile.DocumentImage
import es.pile.DocumentModel
import es.pile.core.domain.models.DocumentStatusConstants
import es.pile.core.domain.repositories.DocumentImageRepository
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.FileRepository
import es.pile.features.home.domain.models.TemporaryDocumentBackup
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ManageTemporaryDocumentUseCaseTest {

    private val documentModelRepository: DocumentModelRepository = mockk()
    private val documentImageRepository: DocumentImageRepository = mockk()
    private val fileRepository: FileRepository = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val manageTemporaryDocumentUseCase = ManageTemporaryDocumentUseCase(
        ioDispatcher = testDispatcher,
        documentModelRepository = documentModelRepository,
        documentImageRepository = documentImageRepository,
        fileRepository = fileRepository
    )

    private val mockDocument = DocumentModel(
        id = "doc123",
        title = "Test Doc",
        imageIds = listOf("img1", "img2"),
        creationDateTime = LocalDateTime.now(),
        modificationDateTime = LocalDateTime.now(),
        documentStatus = DocumentStatusConstants.TEMPORARY,
        documentDetails = emptyList(),
        documentOrganizationIds = emptyList(),
        documentNote = "",
        documentPileIds = emptyList(),
        isIncomingPdf = false
    )

    private val mockImages = listOf(
        DocumentImage("img1", false, null, 0, 0),
        DocumentImage("img2", false, null, 0, 0)
    )

    @Test
    fun `deleteForUndo returns backup when temporary document exists`() = runTest {
        // Given
        coEvery { documentModelRepository.getDocumentModelsByStatus(DocumentStatusConstants.TEMPORARY) } returns flowOf(
            listOf(mockDocument)
        )
        every { documentImageRepository.getDocumentImageById("img1") } returns flowOf(mockImages[0])
        every { documentImageRepository.getDocumentImageById("img2") } returns flowOf(mockImages[1])
        coEvery { documentModelRepository.deleteDocumentModel(any()) } returns Unit
        coEvery { documentImageRepository.deleteDocumentImage(any()) } returns Unit

        // When
        val result = manageTemporaryDocumentUseCase.deleteForUndo()

        // Then
        val expectedBackup = TemporaryDocumentBackup(mockDocument, mockImages)
        assertEquals(expectedBackup, result)
        coVerify { documentModelRepository.deleteDocumentModel("doc123") }
        coVerify { documentImageRepository.deleteDocumentImage("img1") }
        coVerify { documentImageRepository.deleteDocumentImage("img2") }
    }

    @Test
    fun `deleteForUndo returns null when no temporary document exists`() = runTest {
        // Given
        coEvery { documentModelRepository.getDocumentModelsByStatus(DocumentStatusConstants.TEMPORARY) } returns flowOf(
            emptyList()
        )

        // When
        val result = manageTemporaryDocumentUseCase.deleteForUndo()

        // Then
        assertNull(result)
        coVerify(exactly = 0) { documentModelRepository.deleteDocumentModel(any()) }
    }

    @Test
    fun `restoreBackup inserts document and images from backup`() = runTest {
        // Given
        val backup = TemporaryDocumentBackup(mockDocument, mockImages)
        coEvery { documentModelRepository.insertDocumentModel(any()) } returns Unit
        coEvery { documentImageRepository.insertDocumentImage(any()) } returns Unit

        // When
        manageTemporaryDocumentUseCase.restoreBackup(backup)

        // Then
        coVerify { documentModelRepository.insertDocumentModel(mockDocument) }
        coVerify { documentImageRepository.insertDocumentImage(mockImages[0]) }
        coVerify { documentImageRepository.insertDocumentImage(mockImages[1]) }
    }

    @Test
    fun `confirmPermanentDeletion calls fileRepository`() = runTest {
        // Given
        val docId = "doc123"
        coEvery { fileRepository.deleteDocumentStorage(documentId = docId) } returns true

        // When
        manageTemporaryDocumentUseCase.confirmPermanentDeletion(docId)

        // Then
        coVerify { fileRepository.deleteDocumentStorage(documentId = docId) }
    }
}
