package es.pile.features.documentDetail.domain.useCases

import es.pile.DocumentModel
import es.pile.core.domain.repositories.DocumentImageRepository
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.FileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteDocumentUseCaseTest {

    private val documentModelRepository: DocumentModelRepository = mockk()
    private val documentImageRepository: DocumentImageRepository = mockk()
    private val fileRepository: FileRepository = mockk()
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val deleteDocumentUseCase = DeleteDocumentUseCase(
        testDispatcher,
        documentModelRepository,
        documentImageRepository,
        fileRepository
    )

    @Test
    fun `invoke should delete document and images from DB and storage`() = runTest {
        // Given
        val doc = mockk<DocumentModel> {
            every { id } returns "doc1"
            every { imageIds } returns listOf("img1", "img2")
        }
        coEvery { documentModelRepository.deleteDocumentModel("doc1") } returns Unit
        coEvery { documentImageRepository.deleteDocumentImage(any()) } returns Unit
        coEvery { fileRepository.deleteDocumentStorage(any(), any()) } returns true

        // When
        deleteDocumentUseCase(doc)

        // Then
        coVerify { documentModelRepository.deleteDocumentModel("doc1") }
        coVerify { documentImageRepository.deleteDocumentImage("img1") }
        coVerify { documentImageRepository.deleteDocumentImage("img2") }
        coVerify { fileRepository.deleteDocumentStorage(FileRepository.StorageType.PERSISTENT, "doc1") }
    }
}
