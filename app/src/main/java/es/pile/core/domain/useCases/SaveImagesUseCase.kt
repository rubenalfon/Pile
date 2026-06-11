package es.pile.core.domain.useCases

import android.net.Uri
import es.pile.DocumentImage
import es.pile.core.domain.models.ImageResolution
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.repositories.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Use case responsible for saving images to the internal storage of the app.
 *
 * It checks the user's preferred image resolution setting and saves the images accordingly.
 */
class SaveImagesUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val fileRepository: FileRepository,
    private val settingsRepository: SettingsRepository
) {
    /**
     * Saves a list of images to the internal storage of the app. And creates a [DocumentImage] list
     * with the file name.
     *
     * @param storageType The type of storage (PERSISTENT or CACHE).
     * @param uris The list of URIs of the images to be saved.
     * @param documentId The unique identifier of the document where the images will be stored.
     * @return A list of [File] objects representing the saved images.
     */
    suspend operator fun invoke(
        storageType: FileRepository.StorageType,
        uris: List<Uri>,
        documentId: String
    ): List<File> = withContext(ioDispatcher) {
        val imageResolution = settingsRepository.userSettings.first().imageResolution

        return@withContext if (imageResolution == ImageResolution.ORIGINAL) {
            fileRepository.saveImageToStorage(
                storageType = storageType,
                uris = uris,
                documentId = documentId
            )
        } else {
            fileRepository.saveResizeRotateImagesToStorage(
                storageType = storageType,
                uris = uris,
                documentId = documentId
            )
        }
    }
}