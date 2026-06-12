package es.pile.features.editDocument.domain.useCases

import es.pile.DocumentImage
import es.pile.DocumentModel
import es.pile.core.domain.repositories.BitmapCacheRepository
import es.pile.core.domain.repositories.FileRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

class RequestDraftBitmapLoadUseCaseTest {

    private val bitmapCacheRepository: BitmapCacheRepository = mockk(relaxed = true)
    private val fileRepository: FileRepository = mockk()
    private val requestDraftBitmapLoadUseCase = RequestDraftBitmapLoadUseCase(
        bitmapCacheRepository,
        fileRepository
    )

    @Test
    fun `invoke should load bitmap from CACHE for draft image`() = runTest {
        // Given
        val doc = mockk<DocumentModel> {
            every { id } returns "doc1"
            every { imageIds } returns listOf("img1")
        }
        val img = mockk<DocumentImage> {
            every { id } returns "img1"
            every { isDraft } returns true
        }
        val mockFile = File("draft.jpg")
        every { fileRepository.getImageFile(FileRepository.StorageType.CACHE, "doc1", "img1") } returns mockFile

        // When
        requestDraftBitmapLoadUseCase(doc, img)

        // Then
        coVerify { 
            bitmapCacheRepository.loadBitmap(
                file = mockFile,
                document = doc,
                pageNumber = 0,
                documentImage = img
            ) 
        }
    }

    @Test
    fun `invoke should load bitmap from PERSISTENT for non-draft image`() = runTest {
        // Given
        val doc = mockk<DocumentModel> {
            every { id } returns "doc1"
            every { imageIds } returns listOf("img1")
        }
        val img = mockk<DocumentImage> {
            every { id } returns "img1"
            every { isDraft } returns false
        }
        val mockFile = File("persistent.jpg")
        every { fileRepository.getImageFile(FileRepository.StorageType.PERSISTENT, "doc1", "img1") } returns mockFile

        // When
        requestDraftBitmapLoadUseCase(doc, img)

        // Then
        coVerify { 
            bitmapCacheRepository.loadBitmap(
                file = mockFile,
                document = doc,
                pageNumber = 0,
                documentImage = img
            ) 
        }
    }
}
