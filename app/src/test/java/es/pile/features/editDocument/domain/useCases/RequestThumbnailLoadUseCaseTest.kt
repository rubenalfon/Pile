package es.pile.features.editDocument.domain.useCases

import es.pile.DocumentImage
import es.pile.core.domain.repositories.BitmapCacheRepository
import es.pile.core.domain.repositories.FileRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

class RequestThumbnailLoadUseCaseTest {

    private val bitmapCacheRepository: BitmapCacheRepository = mockk(relaxed = true)
    private val fileRepository: FileRepository = mockk()
    private val requestThumbnailLoadUseCase = RequestThumbnailLoadUseCase(
        bitmapCacheRepository,
        fileRepository
    )

    @Test
    fun `invoke should load thumbnail correctly`() = runTest {
        // Given
        val docId = "doc1"
        val img = mockk<DocumentImage> {
            every { id } returns "img1"
            every { isDraft } returns true
        }
        val mockFile = File("img1.jpg")
        every { fileRepository.getImageFile(FileRepository.StorageType.CACHE, docId, "img1") } returns mockFile

        // When
        requestThumbnailLoadUseCase(docId, img, 1)

        // Then
        coVerify { 
            bitmapCacheRepository.loadImageThumbnail(
                imageFile = mockFile,
                documentImage = img,
                filterId = 1
            ) 
        }
    }
}
