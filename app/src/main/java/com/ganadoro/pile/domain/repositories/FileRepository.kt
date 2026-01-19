package com.ganadoro.pile.domain.repositories

import android.graphics.Bitmap
import android.net.Uri
import com.ganadoro.pile.DocumentModel
import java.io.File

/**
 * Repository interface responsible for managing all file-related operations within the application.
 * This includes handling internal storage for documents, images, and PDF generation/manipulation.
 */
interface FileRepository {
    /**
     * Gets the directory [File] where all assets for a specific document are stored.
     *
     * @param documentId The unique identifier of the document.
     * @return A [File] representing the document's folder.
     */
    fun getDocumentDirectory(documentId: String): File

    /**
     * Retrieves the PDF file for a specific document.
     *
     * @param documentId The unique identifier of the document.
     * @return A [File] object pointing to the document's PDF file.
     */
    fun getPDFFile(documentId: String): File

    /**
     * Retrieves a specific image file for a document.
     *
     * @param documentId The unique identifier of the document.
     * @param imageId The unique identifier the image file.
     * @return A [File] object pointing to the image file.
     */
    fun getImageFile(documentId: String, imageId: String): File

    /**
     * Deletes all stored files and the directory associated with a specific document.
     *
     * @param documentId The unique identifier of the document to be removed from storage.
     * @return A [Boolean] indicating whether the operation was successful.
     */
    suspend fun deleteDocumentStorage(documentId: String): Boolean

    /**
     * Provides a secure [Uri] for a given [File] using a FileProvider.
     * Useful for sharing files with external applications.
     *
     * @param file The [File] to generate a URI for.
     * @return A content [Uri] that can be used for sharing or external access.
     */
    fun getUriForFile(file: File): Uri

    /**
     * Checks if a PDF file for a document is outdated based on the modification date.
     *
     * @param document The [DocumentModel] representing the document.
     * @return A [Boolean] indicating whether the PDF file is outdated.
     */
    suspend fun isPdfOutdated(document: DocumentModel): Boolean

    /**
     * Saves a list of [Bitmap] objects as images within the document's storage.
     *
     * @param uris List of URIs of images to be saved.
     * @param documentId Unique identifier of the document where the images will be stored.
     * @param maxSize Maximum size of the images in pixels (default: 1200).
     * @param quality Quality of the saved images (default: 85).
     * @return List of File objects representing the saved images.
     */
    suspend fun saveImagesToInternalStorage(
        uris: List<Uri>,
        documentId: String,
        maxSize: Int = 1200,
        quality: Int = 85
    ): List<File>

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
     * Generates a PDF file from a provided list of [Bitmap] images.
     *
     * @param documentId The unique identifier of the document.
     * @param bitmaps The list of images to be converted into PDF pages.
     * @return The generated PDF [File], or null if the operation failed.
     */
    suspend fun createPdfFromImages(documentId: String, bitmaps: List<Bitmap>): File?

    /**
     * Copies a PDF from an external [Uri] into the app's internal storage.
     *
     * @param uri The source [Uri] of the PDF file.
     * @param documentId The unique identifier of the document where it will be stored.
     * @return The new [File] location in internal storage.
     */
    suspend fun copyPdfToInternalStorage(uri: Uri, documentId: String): File

    /**
     * Exports a file from internal storage to the public "Downloads" folder.
     *
     * @param file The source [File] to be exported.
     * @param publicName The name the file will have in the public storage.
     * @return A [Result] indicating the final name of the file or a failure.
     */
    suspend fun exportFileToDownloads(file: File, publicName: String): Result<String>
}