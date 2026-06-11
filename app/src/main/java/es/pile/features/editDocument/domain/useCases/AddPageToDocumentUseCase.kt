package es.pile.features.editDocument.domain.useCases

import android.net.Uri
import es.pile.DocumentImage
import es.pile.DocumentModel
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.useCases.SaveImagesUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Use case responsible for orchestrating the addition of a new page to an existing document.
 *
 * This class handles the physical persistence of image files into temporary storage (CACHE)
 * and generates the corresponding domain models. It does not persist changes to the
 * database; instead, it returns the updated models for the caller to handle within
 * a single transaction.
 */
class AddPageToDocumentUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val saveImagesUseCase: SaveImagesUseCase,
) {
    /**
     * Processes a list of URIs and prepares them to be added to a document.
     *
     * The process involves:
     * 1. Saving the images from the [uris] into the internal cache storage.
     * 2. Creating [DocumentImage] metadata for each file with default values (draft mode).
     * 3. Producing an updated [DocumentModel] containing the new image IDs.
     *
     * @param document The original [DocumentModel] to which the pages will be attached.
     * @param uris A list of [Uri] pointing to the source images to be imported.
     * @return A [Pair] containing:
     *         - First: The updated [DocumentModel] with the new image IDs appended.
     *         - Second: A [List] of the newly created [DocumentImage] objects.
     * @throws Exception If any file I/O operation fails during the saving process.
     */
    suspend operator fun invoke(
        document: DocumentModel,
        uris: List<Uri>
    ): Pair<DocumentModel, List<DocumentImage>> = withContext(ioDispatcher) {
        val imageFiles = saveImagesUseCase(FileRepository.StorageType.CACHE, uris, document.id)

        val documentImages = imageFiles.map { file ->
            DocumentImage(
                id = file.name,
                isDraft = true,
                crop = null,
                filter = 0,
                rotation = 0
            )
        }

        val updatedDocument =
            document.copy(imageIds = document.imageIds + documentImages.map { it.id })

        return@withContext updatedDocument to documentImages
    }
}