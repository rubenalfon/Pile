package es.pile.features.home.ui

import es.pile.DocumentModel
import es.pile.PileModel
import es.pile.core.domain.repositories.BitmapCacheRepository
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.useCases.CreatePileUseCase
import es.pile.core.domain.useCases.RequestBitmapLoadUseCase
import es.pile.features.home.domain.schedulers.CleanupScheduler
import es.pile.features.home.domain.useCases.CreateDocumentUseCase
import es.pile.features.home.domain.useCases.GetHomeDataUseCase
import es.pile.features.home.domain.useCases.ManageTemporaryDocumentUseCase
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
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val createDocumentUseCase: CreateDocumentUseCase = mockk()
    private val manageTemporaryDocumentUseCase: ManageTemporaryDocumentUseCase = mockk()
    private val getHomeDataUseCase: GetHomeDataUseCase = mockk()
    private val createPileUseCase: CreatePileUseCase = mockk()
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase = mockk()
    private val cleanupScheduler: CleanupScheduler = mockk()
    private val bitmapCacheRepository: BitmapCacheRepository = mockk(relaxed = true)
    private val fileRepository: FileRepository = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load home data`() = runTest {
        // Given
        val homeData = GetHomeDataUseCase.HomeData(
            piles = listOf(PileModel("p1", "Pile 1", "icon1", 1)),
            documents = listOf(mockk<DocumentModel>(relaxed = true) {
                every { id } returns "d1"
            }),
            temporaryDocument = null,
            coloredPileIds = listOf("p1")
        )
        every { getHomeDataUseCase() } returns flowOf(homeData)

        // When
        val viewModel = HomeViewModel(
            createDocumentUseCase,
            manageTemporaryDocumentUseCase,
            getHomeDataUseCase,
            createPileUseCase,
            requestBitmapLoadUseCase,
            cleanupScheduler,
            bitmapCacheRepository,
            fileRepository
        )

        // Then
        val state = viewModel.state.value
        assertEquals(1, state.pileModels.size)
        assertEquals(1, state.documentCoverItems.size)
        assertFalse(state.isInitialLoading)
    }
}
