package es.pile.core.data.repositories

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import es.pile.DocumentImage
import es.pile.DocumentModel
import es.pile.core.data.util.ImageTransformationHelper
import es.pile.core.data.util.PdfRenderHelper
import es.pile.core.domain.models.ImageFilterType
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.repositories.FileRepository.StorageType
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


class FileRepositoryImpl(
    private val appContext: Context,
    private val appDirectory: File = appContext.filesDir,
    private val cacheDirectory: File = appContext.cacheDir,
    private val contentResolver: ContentResolver = appContext.contentResolver,
    private val ioDispatcher: CoroutineDispatcher,
    private val pdfRenderHelper: PdfRenderHelper,
    private val imageTransformationHelper: ImageTransformationHelper
) : FileRepository {
    private fun getPDFFileName(documentId: String): String {
        val cleanId = documentId.removeSuffix(".pdf")
        return "$cleanId.pdf"
    }

    private fun getImageFileName(imageId: String): String {
        val cleanId = imageId.removePrefix("img_").removeSuffix(".jpg")
        return "img_$cleanId.jpg"
    }

    private fun getStorage(storageType: StorageType): File = when (storageType) {
        StorageType.PERSISTENT -> appDirectory
        StorageType.CACHE -> cacheDirectory
    }

    override fun getDocumentDirectory(
        storageType: StorageType,
        documentId: String
    ): File = File(getStorage(storageType), documentId)

    override fun getPDFFile(storageType: StorageType, documentId: String): File =
        File(getDocumentDirectory(storageType, documentId), getPDFFileName(documentId))

    override fun getImageFile(
        storageType: StorageType,
        documentId: String,
        imageId: String
    ): File = File(getDocumentDirectory(storageType, documentId), getImageFileName(imageId))

    override suspend fun deleteDocumentStorage(
        storageType: StorageType,
        documentId: String
    ): Boolean = withContext(ioDispatcher) {
        getDocumentDirectory(storageType, documentId).deleteRecursively()
    }

    override suspend fun deleteDocumentImage(
        storageType: StorageType,
        documentId: String,
        imageId: String
    ): Boolean = withContext(ioDispatcher) {
        getImageFile(storageType, documentId, imageId).delete()
    }

    override suspend fun createTempImageUri(): Uri = withContext(ioDispatcher) {
        val imageDir = File(appContext.cacheDir, "images")
        if (!imageDir.exists()) imageDir.mkdirs()

        val imageFile = File.createTempFile("IMG_", ".jpg", imageDir)

        FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.provider",
            imageFile
        )
    }

    override suspend fun getUriForFile(file: File): Uri = withContext(ioDispatcher) {
        val authority = "${appContext.packageName}.provider"

        FileProvider.getUriForFile(
            appContext,
            authority,
            file
        )
    }

    override suspend fun isPdfOutdated(document: DocumentModel): Boolean =
        withContext(ioDispatcher) {
            if (document.isIncomingPdf) return@withContext false

            val pdfFile = getPDFFile(StorageType.PERSISTENT, document.id)
            if (!pdfFile.exists()) return@withContext true

            val pdfFileLastModification = Instant.ofEpochMilli(pdfFile.lastModified())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            val documentLastModification = document.modificationDateTime

            return@withContext pdfFileLastModification.isBefore(documentLastModification)
        }

    override suspend fun saveResizeRotateImagesToStorage(
        storageType: StorageType,
        uris: List<Uri>,
        documentId: String,
        maxSize: Int,
        quality: Int
    ): List<File> = withContext(ioDispatcher) {
        val storageDir = File(getStorage(storageType), documentId).apply { if (!exists()) mkdirs() }

        uris.map { uri ->
            async {
                saveResizeRotateImageToStorage(
                    uri = uri,
                    storageDir = storageDir,
                    maxSize = maxSize,
                    quality = quality
                )
            }
        }.awaitAll().filterNotNull()
    }

    override suspend fun saveImageToStorage(
        storageType: StorageType,
        uris: List<Uri>,
        documentId: String
    ): List<File> = withContext(ioDispatcher) {
        val storageDir = File(getStorage(storageType), documentId).apply { if (!exists()) mkdirs() }

        uris.map { uri ->
            async {
                val fileName = getImageFileName(UUID.randomUUID().toString())
                val destFile = File(storageDir, fileName)

                copyContentUriToFile(uri, destFile)

                destFile
            }
        }.awaitAll()
    }

    override suspend fun copyImageToInternalStorage(
        documentId: String,
        documentImage: DocumentImage
    ) = withContext(ioDispatcher) {
        val imageFile = getImageFile(StorageType.CACHE, documentId, documentImage.id)
        imageFile.copyTo(
            File(
                getDocumentDirectory(StorageType.PERSISTENT, documentId),
                imageFile.name
            )
        )
    }

    override suspend fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) fileName = cursor.getString(nameIndex)
            }
        }
        fileName = fileName?.substringBeforeLast('.')
        return fileName
    }

    override suspend fun getPageCount(documentId: String): Result<Int> =
        pdfRenderHelper.getPageCount(getPDFFile(StorageType.PERSISTENT, documentId))

    override suspend fun createPdfFromImages(
        documentId: String,
        images: List<DocumentImage>
    ): File = withContext(ioDispatcher) {
        val pdfDocument = PdfDocument()
        val generatedPdfFile = getPDFFile(StorageType.PERSISTENT, documentId)

        try {
            images.forEachIndexed { index, documentImage ->
                val imageFile = getImageFile(StorageType.PERSISTENT, documentId, documentImage.id)

                if (!imageFile.exists()) return@forEachIndexed

                val bitmap = imageTransformationHelper.transform(
                    file = imageFile,
                    rotation = documentImage.rotation.toInt(),
                    cropData = documentImage.crop,
                    filter = ImageFilterType.fromId(documentImage.filter.toInt()),
                    reqSize = 0 // When exporting full resolution
                )?.bitmap ?: return@forEachIndexed

                val pageInfo =
                    PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()

                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                canvas.drawColor(Color.WHITE)
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                pdfDocument.finishPage(page)
                bitmap.recycle()
            }

            FileOutputStream(generatedPdfFile).use { pdfDocument.writeTo(it) }
        } catch (e: Exception) {
            if (generatedPdfFile.exists()) generatedPdfFile.delete()
            throw e
        } finally {
            pdfDocument.close()
        }
        return@withContext generatedPdfFile
    }

    override suspend fun copyPdfToInternalStorage(uri: Uri, documentId: String): File =
        withContext(ioDispatcher) {
            val documentFolder = File(appDirectory, documentId)
            documentFolder.mkdir()

            val destinationFile = File(documentFolder, getPDFFileName(documentId))

            copyContentUriToFile(uri, destinationFile)

            destinationFile
        }

    override suspend fun createTempPdfCopyWithName(sourceFile: File, displayName: String): File =
        withContext(ioDispatcher) {
            val safeName = sanitizeFileName(displayName)

            val exportDir = File(appContext.cacheDir, "export_pdfs").apply { mkdirs() }

            exportDir.listFiles()?.forEach { it.delete() }

            val destinationFile = File(exportDir, safeName)

            sourceFile.copyTo(destinationFile, overwrite = true)

            return@withContext destinationFile
        }

    override suspend fun exportFileToDownloads(file: File, publicName: String): Result<String> =
        withContext(ioDispatcher) {
            runCatching {
                val safeName = sanitizeFileName(publicName)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, safeName)
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
                } else {
                    val downloadsDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (!downloadsDir.exists()) downloadsDir.mkdirs()

                    val destinationFile = File(downloadsDir, safeName)

                    file.copyTo(destinationFile, overwrite = true)

                    // Notify MediaScanner so the file shows up in Downloads/File Manager immediately
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, safeName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(MediaStore.MediaColumns.DATA, destinationFile.absolutePath)
                    }
                    contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

                    Uri.fromFile(destinationFile).toString()
                }
            }
        }

    override suspend fun getExifRotation(file: File): Int =
        imageTransformationHelper.getExifRotation(file)


    override suspend fun getExifRotation(uri: Uri): Int =
        imageTransformationHelper.getExifRotation(uri)

    /**
     * Helper function to save an image from a URI with resizing and rotating based on EXIF data.
     *
     * @param uri URI of the image to be saved.
     * @param storageDir Directory where the image will be saved.
     * @param maxSize Maximum size of the image in pixels (default: 1200).
     * @param quality Quality of the saved image (default: 85).
     * @return File object representing the saved image
     */
    private suspend fun saveResizeRotateImageToStorage(
        uri: Uri,
        storageDir: File,
        maxSize: Int = 1200,
        quality: Int = 85
    ): File? = withContext(ioDispatcher) {
        try {
            val rotation = getExifRotation(uri)

            val fileName = getImageFileName(UUID.randomUUID().toString())
            val destFile = File(storageDir, fileName)

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            options.inSampleSize = imageTransformationHelper.calculateInSampleSize(options, maxSize)
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
     * Copies the content of a URI to a destination file.
     *
     * @param sourceUri The URI to copy from.
     * @param destinationFile The file to copy to.
     * @throws IllegalStateException if the input stream could not be opened.
     */
    private fun copyContentUriToFile(sourceUri: Uri, destinationFile: File) {
        val inputStream = contentResolver.openInputStream(sourceUri)
            ?: throw IllegalStateException("The stream for URI $sourceUri could not be opened")

        inputStream.use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    /**
     * Sanitizes a file name by removing invalid characters.
     */
    private fun sanitizeFileName(name: String): String {
        val cleanName = name.replace("[\\\\/:*?\"<>|]".toRegex(), "_")
        return if (cleanName.endsWith(".pdf", ignoreCase = true)) cleanName else "$cleanName.pdf"
    }
}
