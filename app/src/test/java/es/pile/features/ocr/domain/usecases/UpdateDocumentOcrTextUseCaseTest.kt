package es.pile.features.ocr.domain.usecases

import es.pile.core.data.util.PdfRenderHelper
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.repositories.OcrModelRepository
import es.pile.features.ocr.data.engine.OnnxOcrEngine
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateDocumentOcrTextUseCaseTest {

    private val documentModelRepository: DocumentModelRepository = mockk()
    private val fileRepository: FileRepository = mockk()
    private val pdfRenderHelper: PdfRenderHelper = mockk()
    private val ocrEngine: OnnxOcrEngine = mockk()
    private val ocrModelRepository: OcrModelRepository = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val useCase = UpdateDocumentOcrTextUseCase(
        testDispatcher,
        documentModelRepository,
        fileRepository,
        pdfRenderHelper,
        ocrEngine,
        ocrModelRepository
    )

    @Test
    fun `invoke should rethrow CancellationException`() = runTest {
        coEvery { documentModelRepository.getDocumentModelById("doc1") } returns flow {
            throw CancellationException("Cancelled")
        }

        assertFailsWith<CancellationException> {
            useCase("doc1")
        }
    }

    @Test
    fun `invoke should return failure result when document is not found`() = runTest {
        coEvery { documentModelRepository.getDocumentModelById("doc1") } returns flowOf(null)

        val result = useCase("doc1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoSuchElementException)
    }
}
