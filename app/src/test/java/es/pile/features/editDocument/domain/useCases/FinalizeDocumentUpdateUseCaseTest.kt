package es.pile.features.editDocument.domain.useCases

import es.pile.DocumentImage
import es.pile.DocumentModel
import es.pile.core.domain.models.DocumentStatusConstants
import es.pile.core.domain.repositories.DocumentImageRepository
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.FileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDateTime

class FinalizeDocumentUpdateUseCaseTest {

    private val fileRepository: FileRepository = mockk()
    private val documentModelRepository: DocumentModelRepository = mockk()
    private val documentImageRepository: DocumentImageRepository = mockk()
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val finalizeDocumentUpdateUseCase = FinalizeDocumentUpdateUseCase(
        testDispatcher,
        fileRepository,
        documentModelRepository,
        documentImageRepository
    )

    @Test
    fun `invoke should finalize document update correctly`() = runTest {
        // Given
        val docId = "doc1"
        val originalDoc = DocumentModel(
            id = docId,
            title = "Old Title",
            imageIds = listOf("oldImg"),
            creationDateTime = LocalDateTime.now(),
            modificationDateTime = LocalDateTime.now(),
            documentStatus = DocumentStatusConstants.SAVED,
            documentDetails = emptyList(),
            documentOrganizationIds = emptyList(),
            documentNote = "",
            documentPileIds = emptyList(),
            isIncomingPdf = false
        )
        val newDoc = originalDoc.copy(title = "Updated Title", imageIds = listOf("newImg"))

        val draftImage = DocumentImage(
            id = "newImg",
            isDraft = true,
            crop = null,
            filter = 0,
            rotation = 0
        )
        val imageList = listOf(draftImage)

        coEvery { documentModelRepository.getDocumentModelById(docId) } returns flowOf(originalDoc)
        coEvery { fileRepository.copyImageToInternalStorage(docId, draftImage) } returns mockk()
        coEvery { fileRepository.deleteDocumentImage(FileRepository.StorageType.CACHE, docId, "newImg") } returns true
        coEvery { fileRepository.deleteDocumentImage(FileRepository.StorageType.PERSISTENT, docId, "oldImg") } returns true
        coEvery { documentModelRepository.updateDocumentModel(any()) } returns Unit
        coEvery { documentImageRepository.insertDocumentImage(any()) } returns Unit
        coEvery { documentImageRepository.updateDocumentImage(any()) } returns Unit

        // When
        finalizeDocumentUpdateUseCase(newDoc, imageList)

        // Then
        coVerify { fileRepository.copyImageToInternalStorage(docId, draftImage) }
        coVerify { fileRepository.deleteDocumentImage(FileRepository.StorageType.CACHE, docId, "newImg") }
        coVerify { fileRepository.deleteDocumentImage(FileRepository.StorageType.PERSISTENT, docId, "oldImg") }
        coVerify { documentModelRepository.updateDocumentModel(match { it.id == docId && it.title == "Updated Title" }) }
        coVerify { documentImageRepository.insertDocumentImage(match { it.id == "newImg" && !it.isDraft }) }
    }
}
