package es.pile.features.documentDetail.domain.useCases.export

import es.pile.DocumentModel
import es.pile.core.domain.repositories.FileRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class GetUpToDatePdfUseCaseTest {

    private val fileRepository: FileRepository = mockk()
    private val generatePdfUseCase: GeneratePdfUseCase = mockk()
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val getUpToDatePdfUseCase = GetUpToDatePdfUseCase(
        testDispatcher,
        fileRepository,
        generatePdfUseCase
    )

    @Test
    fun `invoke should return existing PDF if isIncomingPdf is true`() = runTest {
        // Given
        val doc = mockk<DocumentModel> {
            every { id } returns "doc1"
            every { isIncomingPdf } returns true
        }
        val mockFile = File("incoming.pdf")
        every { fileRepository.getPDFFile(any(), "doc1") } returns mockFile

        // When
        val result = getUpToDatePdfUseCase(doc)

        // Then
        assertEquals(mockFile, result)
    }

    @Test
    fun `invoke should generate new PDF if outdated`() = runTest {
        // Given
        val doc = mockk<DocumentModel> {
            every { id } returns "doc2"
            every { isIncomingPdf } returns false
        }
        val mockFile = File("generated.pdf")
        coEvery { fileRepository.isPdfOutdated(doc) } returns true
        coEvery { generatePdfUseCase(doc) } returns mockFile

        // When
        val result = getUpToDatePdfUseCase(doc)

        // Then
        assertEquals(mockFile, result)
    }

    @Test
    fun `invoke should return existing PDF if not outdated`() = runTest {
        // Given
        val doc = mockk<DocumentModel> {
            every { id } returns "doc3"
            every { isIncomingPdf } returns false
        }
        val mockFile = File("existing.pdf")
        coEvery { fileRepository.isPdfOutdated(doc) } returns false
        every { fileRepository.getPDFFile(any(), "doc3") } returns mockFile

        // When
        val result = getUpToDatePdfUseCase(doc)

        // Then
        assertEquals(mockFile, result)
    }
}
