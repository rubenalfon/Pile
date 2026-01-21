package com.ganadoro.pile.data.repositories

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.data.util.ImageTransformationHelper
import com.ganadoro.pile.data.util.PdfRenderHelper
import com.ganadoro.pile.domain.models.ImageFilterType
import com.ganadoro.pile.domain.repositories.FileRepository
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
    private val contentResolver: ContentResolver = appContext.contentResolver,
    private val ioDispatcher: CoroutineDispatcher,
    private val pdfRenderHelper: PdfRenderHelper,
    private val imageTransformationHelper: ImageTransformationHelper
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

    override suspend fun deleteDocumentImage(
        documentId: String,
        imageId: String
    ): Boolean = withContext(ioDispatcher) {
        getImageFile(documentId, imageId).delete()
    }

    override fun createTempImageUri(): Uri {
        val imageDir = File(appContext.cacheDir, "images")
        if (!imageDir.exists()) imageDir.mkdirs()

        val imageFile = File.createTempFile("IMG_", ".jpg", imageDir)

        return FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.provider",
            imageFile
        )
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
            if (!pdfFile.exists()) return@withContext true

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
                if (nameIndex != -1) fileName = cursor.getString(nameIndex)
            }
        }
        fileName = fileName?.substringBeforeLast('.')
        return fileName
    }

    override suspend fun getPageCount(documentId: String): Result<Int> =
        pdfRenderHelper.getPageCount(getPDFFile(documentId))

    override suspend fun createPdfFromImages(
        documentId: String,
        images: List<DocumentImage>
    ): File = withContext(ioDispatcher) {
        val pdfDocument = PdfDocument()
        val generatedPdfFile = getPDFFile(documentId)

        try {
            images.forEachIndexed { index, documentImage ->
                val imageFile = getImageFile(documentId, documentImage.id)

                if (!imageFile.exists()) return@forEachIndexed

                val bitmap = imageTransformationHelper.transform(
                    file = imageFile,
                    rotation = documentImage.rotation.toInt(),
                    cropData = documentImage.crop,
                    filter = ImageFilterType.fromId(documentImage.filter.toInt())
                ) ?: return@forEachIndexed

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
