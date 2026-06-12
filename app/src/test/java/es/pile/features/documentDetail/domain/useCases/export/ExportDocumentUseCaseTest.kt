package es.pile.features.documentDetail.domain.useCases.export

import es.pile.DocumentModel
import es.pile.core.domain.repositories.FileRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExportDocumentUseCaseTest {

    private val getUpToDatePdfUseCase: GetUpToDatePdfUseCase = mockk()
    private val fileRepository: FileRepository = mockk()
    private val exportDocumentUseCase = ExportDocumentUseCase(getUpToDatePdfUseCase, fileRepository)

    @Test
    fun `invoke should export document pdf correctly`() = runTest {
        // Given
        val doc = mockk<DocumentModel> {
            every { title } returns "Document Title"
        }
        val sourceFile = File("path/to/source.pdf")
        coEvery { getUpToDatePdfUseCase(doc) } returns sourceFile
        coEvery { fileRepository.exportFileToDownloads(sourceFile, "Document Title") } returns Result.success("ExportedPath")

        // When
        val result = exportDocumentUseCase(doc)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("ExportedPath", result.getOrNull())
    }
}
