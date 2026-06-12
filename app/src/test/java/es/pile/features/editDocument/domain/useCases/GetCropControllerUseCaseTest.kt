package es.pile.features.editDocument.domain.useCases

import android.graphics.Bitmap
import com.tanishranjan.cropkit.CropDefaults
import es.pile.DocumentImage
import es.pile.core.data.util.ImageTransformationHelper
import es.pile.core.domain.models.ResizedBitmap
import es.pile.core.domain.repositories.FileRepository
import es.pile.features.editDocument.domain.helper.CropControllerFactory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GetCropControllerUseCaseTest {

    private val fileRepository: FileRepository = mockk()
    private val imageTransformationHelper: ImageTransformationHelper = mockk()
    private val cropControllerFactory: CropControllerFactory = mockk()
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val getCropControllerUseCase = GetCropControllerUseCase(
        testDispatcher,
        fileRepository,
        imageTransformationHelper,
        cropControllerFactory
    )

    @Before
    fun setup() {
        mockkObject(CropDefaults)
        every { CropDefaults.cropColors(any(), any(), any(), any(), any()) } returns mockk(relaxed = true)
        every { CropDefaults.cropOptions(any(), any(), any(), any(), any(), any(), any()) } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invoke should return extended crop controller using factory`() = runTest {
        // Given
        val docId = "doc1"
        val img = mockk<DocumentImage> {
            every { id } returns "img1"
            every { isDraft } returns true
            every { rotation } returns 0L
            every { filter } returns 0L
            every { crop } returns null
        }
        val mockFile = File("img1.jpg")
        val mockBitmap = mockk<Bitmap>(relaxed = true)
        val resizedBitmap = ResizedBitmap(mockBitmap, 0.5f)

        every { fileRepository.getImageFile(any(), docId, "img1") } returns mockFile
        coEvery { imageTransformationHelper.transform(mockFile, 0, null, any()) } returns resizedBitmap
        every { cropControllerFactory.create(any(), any(), any()) } returns mockk()

        // When
        val result = getCropControllerUseCase(docId, img)

        // Then
        assertNotNull(result)
        assertEquals(0.5f, result.scaleFactor)
    }
}
