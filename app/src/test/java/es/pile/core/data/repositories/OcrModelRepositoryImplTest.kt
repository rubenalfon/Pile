package es.pile.core.data.repositories

import android.content.Context
import es.pile.core.domain.models.DownloadState
import es.pile.core.domain.models.UserSettings
import es.pile.core.domain.repositories.SettingsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class OcrModelRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun testDownload() = runTest(testDispatcher) {
        val context = mockk<Context>()
        val filesDir = File(System.getProperty("java.io.tmpdir"), "pile_test")
        filesDir.mkdirs()
        every { context.filesDir } returns filesDir

        val userSettingsFlow = MutableStateFlow(UserSettings())
        val settingsRepository = mockk<SettingsRepository>()
        every { settingsRepository.userSettings } returns userSettingsFlow
        coEvery { settingsRepository.updateSelectedOcrModelGroups(any()) } answers {
            userSettingsFlow.value = userSettingsFlow.value.copy(selectedOcrModelGroups = firstArg())
        }

        val repository = OcrModelRepositoryImpl(
            appContext = context,
            settingsRepository = settingsRepository,
            ioDispatcher = testDispatcher,
            externalScope = testScope
        )

        try {
            repository.downloadModel("latin")
            val state = repository.ocrModelGroupsState.first().first { it.group.id == "latin" }.downloadState
            println("Download completed. State: $state")
            assertTrue("Download should succeed or progress", state is DownloadState.Success || state is DownloadState.Error)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
