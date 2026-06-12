package es.pile.features.documentDetail.domain.useCases.export

import es.pile.DocumentImage
import es.pile.DocumentModel
import es.pile.core.domain.repositories.DocumentImageRepository
import es.pile.core.domain.repositories.FileRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GeneratePdfUseCaseTest {

    private val fileRepository: FileRepository = mockk()
    private val documentImageRepository: DocumentImageRepository = mockk()
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val generatePdfUseCase = GeneratePdfUseCase(
        testDispatcher,
        fileRepository,
        documentImageRepository
    )

    @Test
    fun `invoke should generate PDF when images exist`() = runTest {
        // Given
        val doc = mockk<DocumentModel> {
            every { id } returns "doc1"
            every { imageIds } returns listOf("img1")
        }
        val mockImage = mockk<DocumentImage>()
        val mockFile = File("output.pdf")
        
        every { documentImageRepository.getDocumentImageById("img1") } returns flowOf(mockImage)
        coEvery { fileRepository.createPdfFromImages("doc1", listOf(mockImage)) } returns mockFile

        // When
        val result = generatePdfUseCase(doc)

        // Then
        assertEquals(mockFile, result)
    }

    @Test
    fun `invoke should throw exception when no images exist`() = runTest {
        // Given
        val doc = mockk<DocumentModel> {
            every { imageIds } returns emptyList()
        }

        // When & Then
        assertFailsWith<IllegalStateException> {
            generatePdfUseCase(doc)
        }
    }
}
