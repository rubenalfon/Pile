package es.pile.features.home.domain.useCases

import android.net.Uri
import es.pile.core.domain.models.DocumentStatusConstants
import es.pile.core.domain.repositories.DocumentImageRepository
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.useCases.SaveImagesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class CreateDocumentUseCaseTest {

    private val saveImagesUseCase: SaveImagesUseCase = mockk()
    private val fileRepository: FileRepository = mockk()
    private val documentModelRepository: DocumentModelRepository = mockk()
    private val documentImageRepository: DocumentImageRepository = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val createDocumentUseCase = CreateDocumentUseCase(
        ioDispatcher = testDispatcher,
        saveImagesUseCase = saveImagesUseCase,
        fileRepository = fileRepository,
        documentModelRepository = documentModelRepository,
        documentImageRepository = documentImageRepository
    )

    @Before
    fun setup() {
        mockkStatic(Uri::class)
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "new-doc-id"
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
        unmockkStatic(UUID::class)
    }

    @Test
    fun `createFromPdf cleans up, copies file, and updates document`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val fileName = "test.pdf"

        coEvery { documentModelRepository.getDocumentModelsByStatus(DocumentStatusConstants.TEMPORARY) } returns flowOf(
            emptyList()
        )
        coEvery { documentModelRepository.insertDocumentModel(any()) } returns Unit
        coEvery { fileRepository.getFileNameFromUri(mockUri) } returns fileName
        coEvery {
            fileRepository.copyPdfToInternalStorage(
                mockUri, "new-doc-id"
            )
        } returns File("path")
        coEvery { documentModelRepository.updateDocumentModel(any()) } returns Unit

        // When
        val result = createDocumentUseCase.createFromPdf(mockUri)

        // Then
        assertEquals("new-doc-id", result.id)
        assertEquals(fileName, result.title)
        coVerify { documentModelRepository.insertDocumentModel(match { it.id == "new-doc-id" && it.isIncomingPdf }) }
        coVerify { documentModelRepository.updateDocumentModel(match { it.id == "new-doc-id" && it.title == fileName }) }
    }

    @Test
    fun `createFromPdf performs rollback on failure`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        coEvery { documentModelRepository.getDocumentModelsByStatus(DocumentStatusConstants.TEMPORARY) } returns flowOf(
            emptyList()
        )
        coEvery { documentModelRepository.insertDocumentModel(any()) } returns Unit

        coEvery { fileRepository.getFileNameFromUri(mockUri) } throws RuntimeException("Storage error")
        coEvery { fileRepository.deleteDocumentStorage(documentId = "new-doc-id") } returns true
        coEvery { documentModelRepository.deleteDocumentModel("new-doc-id") } returns Unit

        // When & Then
        assertFailsWith<RuntimeException> {
            createDocumentUseCase.createFromPdf(mockUri)
        }

        coVerify { fileRepository.deleteDocumentStorage(documentId = "new-doc-id") }
        coVerify { documentModelRepository.deleteDocumentModel("new-doc-id") }
    }

    @Test
    fun `createFromPdf with initialPileIds should include them in the document`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val fileName = "test.pdf"
        val pileIds = listOf("pile1", "pile2")

        coEvery { documentModelRepository.getDocumentModelsByStatus(DocumentStatusConstants.TEMPORARY) } returns flowOf(emptyList())
        coEvery { documentModelRepository.insertDocumentModel(any()) } returns Unit
        coEvery { fileRepository.getFileNameFromUri(mockUri) } returns fileName
        coEvery { fileRepository.copyPdfToInternalStorage(mockUri, "new-doc-id") } returns File("path")
        coEvery { documentModelRepository.updateDocumentModel(any()) } returns Unit

        // When
        val result = createDocumentUseCase.createFromPdf(mockUri, initialPileIds = pileIds)

        // Then
        assertEquals(pileIds, result.documentPileIds)
        coVerify { documentModelRepository.insertDocumentModel(match { it.documentPileIds == pileIds }) }
    }

    @Test
    fun `createFromImages with initialPileIds should include them in the document`() = runTest {
        // Given
        val mockUris = listOf(mockk<Uri>())
        val mockFiles = listOf(File("img1.jpg"))
        val pileIds = listOf("pileA")

        coEvery { documentModelRepository.getDocumentModelsByStatus(DocumentStatusConstants.TEMPORARY) } returns flowOf(emptyList())
        coEvery { documentModelRepository.insertDocumentModel(any()) } returns Unit
        coEvery { saveImagesUseCase(FileRepository.StorageType.PERSISTENT, mockUris, "new-doc-id") } returns mockFiles
        coEvery { documentImageRepository.insertDocumentImage(any()) } returns Unit
        coEvery { documentModelRepository.updateDocumentModel(any()) } returns Unit

        // When
        val result = createDocumentUseCase.createFromImages(mockUris, initialPileIds = pileIds)

        // Then
        assertEquals(pileIds, result.documentPileIds)
        coVerify { documentModelRepository.insertDocumentModel(match { it.documentPileIds == pileIds }) }
    }
}
