package es.pile.features.editDocument.domain.useCases

import es.pile.DocumentModel
import es.pile.core.domain.models.DocumentStatusConstants
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.useCases.SaveImagesUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File
import java.time.LocalDateTime
import kotlin.test.assertEquals

class AddPageToDocumentUseCaseTest {

    private val saveImagesUseCase: SaveImagesUseCase = mockk()
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val addPageToDocumentUseCase = AddPageToDocumentUseCase(testDispatcher, saveImagesUseCase)

    @Test
    fun `invoke should save images and return updated document and images`() = runTest {
        // Given
        val doc = DocumentModel(
            id = "doc1",
            title = "Title",
            imageIds = listOf("oldImg"),
            creationDateTime = LocalDateTime.now(),
            modificationDateTime = LocalDateTime.now(),
            documentStatus = DocumentStatusConstants.SAVED,
            documentPileIds = emptyList(),
            documentDetails = emptyList(),
            documentNote = "",
            documentOrganizationIds = emptyList(),
            isIncomingPdf = false
        )
        val mockUris = listOf(mockk<android.net.Uri>())
        val mockFile = File("newImg.jpg")
        
        coEvery { saveImagesUseCase(FileRepository.StorageType.CACHE, mockUris, "doc1") } returns listOf(mockFile)

        // When
        val (updatedDoc, newImages) = addPageToDocumentUseCase(doc, mockUris)

        // Then
        assertEquals(1, newImages.size)
        assertEquals("newImg.jpg", newImages[0].id)
        assertEquals(listOf("oldImg", "newImg.jpg"), updatedDoc.imageIds)
    }
}
