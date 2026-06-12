package es.pile.core.domain.useCases

import android.net.Uri
import es.pile.core.domain.models.ImageResolution
import es.pile.core.domain.models.UserSettings
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.repositories.SettingsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class SaveImagesUseCaseTest {

    private val fileRepository: FileRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val saveImagesUseCase = SaveImagesUseCase(testDispatcher, fileRepository, settingsRepository)

    @Test
    fun `invoke should save original images when resolution is ORIGINAL`() = runTest {
        // Given
        val storageType = FileRepository.StorageType.CACHE
        val uris = listOf(mockk<Uri>())
        val docId = "doc1"
        val mockFiles = listOf(File("img1.jpg"))
        
        every { settingsRepository.userSettings } returns flowOf(UserSettings(imageResolution = ImageResolution.ORIGINAL))
        coEvery { fileRepository.saveImageToStorage(storageType, uris, docId) } returns mockFiles

        // When
        val result = saveImagesUseCase(storageType, uris, docId)

        // Then
        assertEquals(mockFiles, result)
    }

    @Test
    fun `invoke should save resized images when resolution is NOT ORIGINAL`() = runTest {
        // Given
        val storageType = FileRepository.StorageType.PERSISTENT
        val uris = listOf(mockk<Uri>())
        val docId = "doc2"
        val mockFiles = listOf(File("img2.jpg"))
        
        every { settingsRepository.userSettings } returns flowOf(UserSettings(imageResolution = ImageResolution.LOW))
        coEvery { fileRepository.saveResizeRotateImagesToStorage(storageType, uris, docId) } returns mockFiles

        // When
        val result = saveImagesUseCase(storageType, uris, docId)

        // Then
        assertEquals(mockFiles, result)
    }
}
