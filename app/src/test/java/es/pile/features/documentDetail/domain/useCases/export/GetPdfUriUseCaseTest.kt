package es.pile.features.documentDetail.domain.useCases.export

import android.net.Uri
import es.pile.DocumentModel
import es.pile.core.domain.repositories.FileRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class GetPdfUriUseCaseTest {

    private val getUpToDatePdfUseCase: GetUpToDatePdfUseCase = mockk()
    private val fileRepository: FileRepository = mockk()
    private val getPdfUriUseCase = GetPdfUriUseCase(getUpToDatePdfUseCase, fileRepository)

    @Test
    fun `invoke should return URI for temporary PDF copy`() = runTest {
        // Given
        val doc = mockk<DocumentModel> {
            every { title } returns "Document Title"
        }
        val mockFile = File("source.pdf")
        val mockTempFile = File("temp_Document_Title.pdf")
        val mockUri = mockk<Uri>()
        
        coEvery { getUpToDatePdfUseCase(doc) } returns mockFile
        coEvery { fileRepository.createTempPdfCopyWithName(mockFile, "Document Title") } returns mockTempFile
        coEvery { fileRepository.getUriForFile(mockTempFile) } returns mockUri

        // When
        val result = getPdfUriUseCase(doc)

        // Then
        assertEquals(mockUri, result)
    }
}
