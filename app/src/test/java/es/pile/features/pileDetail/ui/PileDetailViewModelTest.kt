package es.pile.features.pileDetail.ui

import android.net.Uri
import app.cash.turbine.test
import es.pile.DocumentModel
import es.pile.PileModel
import es.pile.core.domain.repositories.BitmapCacheRepository
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.repositories.PileModelRepository
import es.pile.core.domain.useCases.RequestBitmapLoadUseCase
import es.pile.features.home.domain.useCases.CreateDocumentUseCase
import es.pile.features.pileDetail.domain.usecases.DeletePileUseCase
import es.pile.features.pileDetail.domain.usecases.UpdatePileUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class PileDetailViewModelTest {

    private val pileId = "test-pile-id"
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase = mockk()
    private val createDocumentUseCase: CreateDocumentUseCase = mockk()
    private val updatePileUseCase: UpdatePileUseCase = mockk()
    private val deletePileUseCase: DeletePileUseCase = mockk()
    private val pileModelRepository: PileModelRepository = mockk()
    private val documentModelRepository: DocumentModelRepository = mockk()
    private val bitmapCacheRepository: BitmapCacheRepository = mockk(relaxed = true)
    private val fileRepository: FileRepository = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { pileModelRepository.getPileModelById(pileId) } returns flowOf(PileModel(pileId, "Pile", "icon", 0))
        every { documentModelRepository.documentModels } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `OnPdfImported should call createFromPdf with pileId and navigate`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val mockDoc = mockk<DocumentModel> {
            every { id } returns "new-doc-id"
        }
        coEvery { createDocumentUseCase.createFromPdf(mockUri, initialPileIds = listOf(pileId)) } returns mockDoc

        val viewModel = PileDetailViewModel(
            pileId,
            requestBitmapLoadUseCase,
            createDocumentUseCase,
            updatePileUseCase,
            deletePileUseCase,
            pileModelRepository,
            documentModelRepository,
            bitmapCacheRepository,
            fileRepository
        )

        // When & Then
        viewModel.navigationEvent.test {
            viewModel.handleEvent(PileDetailEvent.OnPdfImported(mockUri))
            assertEquals(mockDoc, awaitItem())
        }
    }

    @Test
    fun `OnImagesImported should call createFromImages with pileId and navigate`() = runTest {
        // Given
        val mockUris = listOf(mockk<Uri>())
        val mockDoc = mockk<DocumentModel> {
            every { id } returns "new-doc-id"
        }
        coEvery { createDocumentUseCase.createFromImages(mockUris, initialPileIds = listOf(pileId)) } returns mockDoc

        val viewModel = PileDetailViewModel(
            pileId,
            requestBitmapLoadUseCase,
            createDocumentUseCase,
            updatePileUseCase,
            deletePileUseCase,
            pileModelRepository,
            documentModelRepository,
            bitmapCacheRepository,
            fileRepository
        )

        // When & Then
        viewModel.navigationEvent.test {
            viewModel.handleEvent(PileDetailEvent.OnImagesImported(mockUris))
            assertEquals(mockDoc, awaitItem())
        }
    }
}
