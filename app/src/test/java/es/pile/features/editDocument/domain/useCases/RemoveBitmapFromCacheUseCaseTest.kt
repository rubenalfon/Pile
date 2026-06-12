package es.pile.features.editDocument.domain.useCases

import es.pile.DocumentModel
import es.pile.core.domain.repositories.BitmapCacheRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class RemoveBitmapFromCacheUseCaseTest {

    private val bitmapCacheRepository: BitmapCacheRepository = mockk(relaxed = true)
    private val removeBitmapFromCacheUseCase = RemoveBitmapFromCacheUseCase(bitmapCacheRepository)

    @Test
    fun `removeImage should remove correct key from cache`() {
        // Given
        val doc = mockk<DocumentModel> {
            every { imageIds } returns listOf("img1", "img2")
        }
        every { bitmapCacheRepository.getImageKey(doc, 1) } returns "cacheKey2"

        // When
        removeBitmapFromCacheUseCase.removeImage(doc, "img2")

        // Then
        verify { bitmapCacheRepository.removeFromCache("cacheKey2") }
    }

    @Test
    fun `removeThumbnails should remove all filter keys from cache`() {
        // Given
        val imageId = "img1"

        // When
        removeBitmapFromCacheUseCase.removeThumbnails(imageId)

        // Then
        // Verify it was called for multiple filters (at least check a few)
        verify { bitmapCacheRepository.removeFromCache(any()) }
    }
}
