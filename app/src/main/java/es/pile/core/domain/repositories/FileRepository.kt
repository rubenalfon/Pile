package es.pile.core.domain.repositories

import android.graphics.Bitmap
import android.net.Uri
import es.pile.DocumentImage
import es.pile.DocumentModel
import java.io.File

/**
 * Repository interface responsible for managing all file-related operations within the application.
 * This includes handling internal storage for documents, images, and PDF generation/manipulation.
 */
interface FileRepository {
    /**
     * Enum that represents the different types of storage available in the application.
     */
    enum class StorageType {
        PERSISTENT,
        CACHE,
    }

    /**
     * Gets the directory [File] where all assets for a specific document are stored.
     *
     * @param storageType The type of storage (PERSISTENT or CACHE).
     * @param documentId The unique identifier of the document.
     * @return A [File] representing the document's folder.
     */
    fun getDocumentDirectory(
        storageType: StorageType = StorageType.PERSISTENT,
        documentId: String
    ): File

    /**
     * Retrieves the PDF file for a specific document.
     *
     * @param storageType The type of storage (PERSISTENT or CACHE).
     * @param documentId The unique identifier of the document.
     * @return A [File] object pointing to the document's PDF file.
     */
    fun getPDFFile(storageType: StorageType = StorageType.PERSISTENT, documentId: String): File

    /**
     * Retrieves a specific image file for a document.
     *
     * @param storageType The type of storage (PERSISTENT or CACHE).
     * @param documentId The unique identifier of the document.
     * @param imageId The unique identifier the image file.
     * @return A [File] object pointing to the image file.
     */
    fun getImageFile(
        storageType: StorageType = StorageType.PERSISTENT,
        documentId: String,
        imageId: String
    ): File

    /**
     * Deletes all stored files and the directory associated with a specific document.
     *
     * @param storageType The type of storage (PERSISTENT or CACHE).
     * @param documentId The unique identifier of the document to be removed from storage.
     * @return A [Boolean] indicating whether the operation was successful.
     */
    suspend fun deleteDocumentStorage(
        storageType: StorageType = StorageType.PERSISTENT,
        documentId: String
    ): Boolean

    /**
     * Deletes a specific image file from the document folder.
     *
     * @param storageType The type of storage (PERSISTENT or CACHE).
     * @param documentId The unique identifier for the document.
     * @param imageId The unique identifier for the image file.
     * @return A [Boolean] indicating whether the operation was successful.
     */
    suspend fun deleteDocumentImage(
        storageType: StorageType = StorageType.PERSISTENT,
        documentId: String,
        imageId: String
    ): Boolean

    /**
     * Creates a temporary [Uri] for a image file.
     *
     * @return A [Uri] object representing the temporary image file.
     */
    suspend fun createTempImageUri(): Uri

    /**
     * Provides a secure [Uri] for a given [File] using a FileProvider.
     * Useful for sharing files with external applications.
     *
     * @param file The [File] to generate a URI for.
     * @return A content [Uri] that can be used for sharing or external access.
     */
    suspend fun getUriForFile(file: File): Uri

    /**
     * Checks if a PDF file for a document is outdated based on the modification date.
     *
     * @param document The [DocumentModel] representing the document.
     * @return A [Boolean] indicating true if the PDF is outdated or doesn't exist, false otherwise.
     */
    suspend fun isPdfOutdated(document: DocumentModel): Boolean

    /**
     * Saves a list of [Bitmap] objects, resized to a specific size and rotated based on their EXIF data,
     * within the document's storage.
     *
     * @param storageType The type of storage (PERSISTENT or CACHE).
     * @param uris List of URIs of images to be saved.
     * @param documentId Unique identifier of the document where the images will be stored.
     * @param maxSize Maximum size of the images in pixels (default: 1200).
     * @param quality Quality of the saved images (default: 85).
     * @return List of File objects representing the saved images.
     */
    suspend fun saveResizeRotateImagesToStorage(
        storageType: StorageType,
        uris: List<Uri>,
        documentId: String,
        maxSize: Int = 1200,
        quality: Int = 85
    ): List<File>

    /**
     * Saves a list of [Uri] objects without any changes as images within the document's storage.
     *
     * @param storageType The type of storage (PERSISTENT or CACHE).
     * @param uris List of URIs of images to be saved.
     * @param documentId Unique identifier of the document where the images will be stored.
     * @return List of File objects representing the saved images.
     */
    suspend fun saveImageToStorage(
        storageType: StorageType,
        uris: List<Uri>,
        documentId: String
    ): List<File>

    /**
     * Copies an image to the internal storage of the app.
     *
     * @param documentId The unique identifier of the document where the image will be stored.
     * @param documentImage The [DocumentImage] object representing the image to be copied.
     * @return A [File] object representing the copied image in internal storage.
     */
    suspend fun copyImageToInternalStorage(
        documentId: String,
        documentImage: DocumentImage
    ): File


    /**
     * Obtains the name of the file for a given URI.
     *
     * @param uri The Uri of the file.
     * @return A String containing the name of the file or null if not found.
     */
    suspend fun getFileNameFromUri(uri: Uri): String?

    /**
     * Retrieves the page count of a PDF file.
     *
     * @param documentId The unique identifier of the document.
     * @return A [Result] containing the page count or an error.
     */
    suspend fun getPageCount(documentId: String): Result<Int>

    /**
     * Generates a PDF file from a provided list of images.
     *
     * @param documentId The unique identifier of the document.
     * @param images A list of [DocumentImage] objects representing the images to be included in the PDF.
     * @return The generated PDF [File].
     */
    suspend fun createPdfFromImages(documentId: String, images: List<DocumentImage>): File

    /**
     * Copies a PDF from an external [Uri] into the app's internal storage.
     *
     * @param uri The source [Uri] of the PDF file.
     * @param documentId The unique identifier of the document where it will be stored.
     * @return The new [File] location in internal storage.
     */
    suspend fun copyPdfToInternalStorage(uri: Uri, documentId: String): File

    /**
     * Creates a temporary PDF file with a custom name.
     *
     * @param sourceFile The source [File] to be copied.
     * @param displayName The desired name for the new file.
     * @return A [File] object representing the newly created temporary PDF file.
     */
    suspend fun createTempPdfCopyWithName(sourceFile: File, displayName: String): File

    /**
     * Exports a file from internal storage to the public "Downloads" folder.
     *
     * @param file The source [File] to be exported.
     * @param publicName The name the file will have in the public storage.
     * @return A [Result] indicating the final name of the file or a failure.
     */
    suspend fun exportFileToDownloads(file: File, publicName: String): Result<String>

    /**
     * Retrieves the rotation degrees of an image from its physical [File].
     *
     * @param file The image file.
     * @return The rotation degrees (0, 90, 180, or 270).
     */
    suspend fun getExifRotation(file: File): Int
    /**
     * Retrieves the rotation degrees of an image from its [Uri].
     *
     * @param uri The image URI.
     * @return The rotation degrees (0, 90, 180, or 270).
     */
    suspend fun getExifRotation(uri: Uri): Int
}