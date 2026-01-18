package com.ganadoro.pile.repositories

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.data.util.PdfRenderHelper
import com.ganadoro.pile.util.copyUriFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import kotlin.io.copyTo
import kotlin.io.deleteRecursively
import kotlin.io.inputStream
import kotlin.use

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

class FileRepositoryImpl(
    private val appContext: Context,
    private val appDirectory: File = appContext.filesDir,
    private val contentResolver: ContentResolver = appContext.contentResolver,
    private val ioDispatcher: CoroutineDispatcher,
    private val pdfRenderHelper: PdfRenderHelper
) : FileRepository {
    /**
     * Asegura que el nombre del PDF termine en .pdf sin duplicarlo.
     */
    private fun getPDFFileName(documentId: String): String {
        val cleanId = documentId.removeSuffix(".pdf")
        return "$cleanId.pdf"
    }

    /**
     * Asegura que el nombre de la imagen tenga el prefijo img_ y la extensión .jpg
     * de forma única.
     */
    private fun getImageFileName(imageId: String): String {
        val cleanId = imageId.removePrefix("img_").removeSuffix(".jpg")
        return "img_$cleanId.jpg"
    }

    override fun getDocumentDirectory(documentId: String): File = File(appDirectory, documentId)

    override fun getPDFFile(documentId: String): File =
        File(getDocumentDirectory(documentId), getPDFFileName(documentId))

    override fun getImageFile(documentId: String, imageId: String): File =
        File(getDocumentDirectory(documentId), getImageFileName(imageId))

    override suspend fun deleteDocumentStorage(documentId: String): Boolean =
        withContext(ioDispatcher) {
            getDocumentDirectory(documentId).deleteRecursively()
        }

    override fun getUriForFile(file: File): Uri {
        val authority = "${appContext.packageName}.provider"

        return FileProvider.getUriForFile(
            appContext,
            authority,
            file
        )
    }

    override suspend fun isPdfOutdated(document: DocumentModel): Boolean =
        withContext(ioDispatcher) {
            if (document.isIncomingPdf) return@withContext true

            val pdfFile = getPDFFile(document.id)
            if (!pdfFile.exists()) return@withContext false

            val pdfFileLastModification = Instant.ofEpochMilli(pdfFile.lastModified())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            val documentLastModification = document.modificationDateTime

            return@withContext !pdfFileLastModification.isBefore(documentLastModification)
        }

    override suspend fun saveImagesToInternalStorage(
        uris: List<Uri>,
        documentId: String,
        maxSize: Int,
        quality: Int
    ): List<File> = withContext(ioDispatcher) {
        val storageDir = File(appDirectory, documentId).apply { if (!exists()) mkdirs() }

        uris.map { uri ->
            async {
                saveImageToInternalStorage(
                    uri = uri,
                    storageDir = storageDir,
                    maxSize = maxSize,
                    quality = quality
                )
            }
        }.awaitAll().filterNotNull()
    }

    override suspend fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        fileName = fileName?.substringBeforeLast('.')
        return fileName
    }

    override suspend fun getPageCount(documentId: String): Result<Int> =
        pdfRenderHelper.getPageCount(getPDFFile(documentId))

    override suspend fun createPdfFromImages(documentId: String, bitmaps: List<Bitmap>): File? {
        TODO("Not yet implemented")
    }

    override suspend fun copyPdfToInternalStorage(uri: Uri, documentId: String): File =
        withContext(ioDispatcher) {
            val documentFolder = File(appDirectory, documentId)
            documentFolder.mkdir()

            val destinationFile = File(documentFolder, getPDFFileName(documentId))

            destinationFile.copyUriFile(appContext, uri)
        }

    override suspend fun exportFileToDownloads(file: File, publicName: String): Result<String> =
        withContext(ioDispatcher) {
            runCatching {
                val nameWithExt =
                    if (publicName.endsWith(".pdf", true)) publicName else "$publicName.pdf"

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, nameWithExt)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: throw IOException("Failed to create new MediaStore record.")

                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    file.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: throw IOException("The output stream for URI $uri could not be opened")
                uri.toString()
            }
        }


    /**
     * Helper function to save an image from an URI with resizing and rotation correction.
     *
     * @param uri URI of the image to be saved.
     * @param storageDir Directory where the image will be saved.
     * @param maxSize Maximum size of the image in pixels (default: 1200).
     * @param quality Quality of the saved image (default: 85).
     * @return File object representing the saved image
     */
    private suspend fun saveImageToInternalStorage(
        uri: Uri,
        storageDir: File,
        maxSize: Int = 1200,
        quality: Int = 85
    ): File? = withContext(ioDispatcher) {
        try {
            val rotation = getRotationDegrees(uri)

            val fileName = getImageFileName(UUID.randomUUID().toString())
            val destFile = File(storageDir, fileName)

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            options.inSampleSize = calculateInSampleSize(options, maxSize)
            options.inJustDecodeBounds = false

            val bitmap = contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            bitmap?.let { original ->
                var finalBitmap = scaleBitmap(original, maxSize)

                if (rotation != 0) finalBitmap = rotateBitmap(finalBitmap, rotation)

                FileOutputStream(destFile).use { out ->
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                }
                if (finalBitmap != original) finalBitmap.recycle()
                original.recycle()
            }

            destFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Retrieves the rotation degrees of an image from its URI.
     *
     * @param uri The URI of the image.
     * @return The rotation degrees
     */
    private fun getRotationDegrees(uri: Uri): Int {
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                when (exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (_: Exception) {
            0
        }
    }

    /**
     * Rotates a [Bitmap] by a specified number of degrees.
     *
     * @param source The [Bitmap] to be rotated.
     * @param degrees The number of degrees to rotate the [Bitmap].
     * @return The rotated [Bitmap].
     */
    private fun rotateBitmap(source: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return source
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        source.recycle()
        return rotated
    }

    /**
     * Calculates the optimal sample size for decoding a bitmap.
     *
     * @param options The [BitmapFactory.Options] object containing bitmap metadata.
     * @param maxSize The maximum size of the bitmap in pixels.
     * @return The optimal sample size.
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, maxSize: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > maxSize || width > maxSize) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= maxSize || halfWidth / inSampleSize >= maxSize) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Scales a [Bitmap] to a specified maximum size.
     *
     * @param source The [Bitmap] to be scaled.
     * @param maxSize The maximum size of the bitmap in pixels.
     * @return The scaled [Bitmap].
     */
    private fun scaleBitmap(source: Bitmap, maxSize: Int): Bitmap {
        val width = source.width
        val height = source.height

        if (width <= maxSize && height <= maxSize) return source

        val ratio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int

        if (width > height) {
            targetWidth = maxSize
            targetHeight = (maxSize / ratio).toInt()
        } else {
            targetHeight = maxSize
            targetWidth = (maxSize * ratio).toInt()
        }

        return source.scale(targetWidth, targetHeight)
    }
}
