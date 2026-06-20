package es.pile.features.onboarding.ui

import es.pile.core.domain.repositories.AppPreferencesRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val appPreferencesRepository: AppPreferencesRepository = mockk(relaxed = true)
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
    fun `initial state should be 0`() {
        val viewModel = OnboardingViewModel(appPreferencesRepository)
        assertEquals(0, viewModel.state.value.currentPage)
    }

    @Test
    fun `OnNextClicked should increment currentPage`() {
        val viewModel = OnboardingViewModel(appPreferencesRepository)
        viewModel.handleEvent(OnboardingEvent.OnNextClicked)
        assertEquals(1, viewModel.state.value.currentPage)
    }

    @Test
    fun `OnBackClicked should decrement currentPage`() {
        val viewModel = OnboardingViewModel(appPreferencesRepository)
        viewModel.handleEvent(OnboardingEvent.OnNextClicked)
        viewModel.handleEvent(OnboardingEvent.OnBackClicked)
        assertEquals(0, viewModel.state.value.currentPage)
    }

    @Test
    fun `OnFinished should call updateOnboardingCompleted`() = runTest {
        val viewModel = OnboardingViewModel(appPreferencesRepository)
        viewModel.handleEvent(OnboardingEvent.OnFinished)
        coVerify { appPreferencesRepository.updateOnboardingCompleted(true) }
    }
}
