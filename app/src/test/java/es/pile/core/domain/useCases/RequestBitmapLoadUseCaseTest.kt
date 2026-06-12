package es.pile.core.domain.useCases

import es.pile.DocumentImage
import es.pile.DocumentModel
import es.pile.core.domain.repositories.BitmapCacheRepository
import es.pile.core.domain.repositories.DocumentImageRepository
import es.pile.core.domain.repositories.FileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

class RequestBitmapLoadUseCaseTest {

    private val documentImageRepository: DocumentImageRepository = mockk()
    private val bitmapCacheRepository: BitmapCacheRepository = mockk(relaxed = true)
    private val fileRepository: FileRepository = mockk()
    private val requestBitmapLoadUseCase = RequestBitmapLoadUseCase(
        documentImageRepository,
        bitmapCacheRepository,
        fileRepository
    )

    @Test
    fun `invoke should load bitmap for PDF document`() = runTest {
        // Given
        val doc = mockk<DocumentModel> {
            every { id } returns "doc1"
            every { isIncomingPdf } returns true
        }
        val mockFile = File("test.pdf")
        coEvery { fileRepository.getPDFFile(FileRepository.StorageType.PERSISTENT, "doc1") } returns mockFile

        // When
        requestBitmapLoadUseCase(doc, 0)

        // Then
        coVerify { 
            bitmapCacheRepository.loadBitmap(
                file = mockFile,
                document = doc,
                pageNumber = 0
            ) 
        }
    }

    @Test
    fun `invoke should load bitmap for image document`() = runTest {
        // Given
        val doc = mockk<DocumentModel> {
            every { id } returns "doc1"
            every { isIncomingPdf } returns false
            every { imageIds } returns listOf("img1")
        }
        val mockFile = File("img1.jpg")
        val mockImage = mockk<DocumentImage>()
        coEvery { fileRepository.getImageFile(FileRepository.StorageType.PERSISTENT, "doc1", "img1") } returns mockFile
        every { documentImageRepository.getDocumentImageById("img1") } returns flowOf(mockImage)

        // When
        requestBitmapLoadUseCase(doc, 0)

        // Then
        coVerify { 
            bitmapCacheRepository.loadBitmap(
                file = mockFile,
                document = doc,
                pageNumber = 0,
                documentImage = mockImage
            ) 
        }
    }
}
