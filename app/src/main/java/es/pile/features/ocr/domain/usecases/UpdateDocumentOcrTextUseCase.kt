package es.pile.features.ocr.domain.usecases

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import es.pile.core.data.util.PdfRenderHelper
import es.pile.core.data.util.calculateInSampleSize
import es.pile.core.domain.models.ocr.DocumentOcrData
import es.pile.core.domain.models.ocr.ImageOcrData
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.repositories.OcrModelRepository
import es.pile.features.ocr.data.engine.OnnxOcrEngine
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json


/**
 * Use case responsible for extracting text from a document using OCR (Optical Character Recognition)
 * and saving the generated OCR result to the database.
 *
 * Supports documents formed by individual page images as well as imported PDF files.
 * For PDFs, each page is rendered to a bitmap before performing OCR.
 */
class UpdateDocumentOcrTextUseCase(
    private val ioDispatcher: CoroutineDispatcher,
    private val documentModelRepository: DocumentModelRepository,
    private val fileRepository: FileRepository,
    private val pdfRenderHelper: PdfRenderHelper,
    private val ocrEngine: OnnxOcrEngine,
    private val ocrModelRepository: OcrModelRepository
) {
    /**
     * Executes the OCR text extraction process for a document specified by [documentId]
     * and updates the document record in the repository with the extracted JSON OCR data.
     *
     * @param documentId The unique identifier of the document to process.
     * @return A [Result] containing [Unit] on success, or an exception on failure.
     */
    suspend operator fun invoke(documentId: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            val documentModel =
                documentModelRepository.getDocumentModelById(documentId).firstOrNull()
                    ?: throw NoSuchElementException("Document with ID $documentId not found")

            val activeModels = ocrModelRepository.activeModelGroupIds.first()
            if (activeModels.isEmpty()) {
                return@withContext Result.success(Unit)
            }

            val imageOcrDataList = mutableListOf<ImageOcrData>()

            if (documentModel.isIncomingPdf) { // TODO: Check direct text inside the pdf, not just make images
                val file = fileRepository.getPDFFile(
                    storageType = FileRepository.StorageType.PERSISTENT,
                    documentId = documentModel.id
                )

                if (file.exists()) {
                    val pageCountResult = pdfRenderHelper.getPageCount(file)
                    val pageCount = pageCountResult.getOrDefault(0)

                    for (i in 0 until pageCount) {
                        val bitmap = pdfRenderHelper.renderPageToBitmap(file, i) ?: continue
                        try {
                            val ocrResult = ocrEngine.runOcr(bitmap, activeModels)
                            imageOcrDataList.add(
                                ImageOcrData(
                                    id = "$PDF_ID_PREFIX$i",
                                    text = ocrResult.fullText
                                )
                            )
                        } finally {
                            bitmap.recycle()
                        }
                    }
                }
            }

            documentModel.imageIds.forEach { imageId ->
                val file = fileRepository.getImageFile(
                    storageType = FileRepository.StorageType.PERSISTENT,
                    documentId = documentModel.id,
                    imageId = imageId
                )

                if (file.exists()) {
                    val bitmap = decodeSampledBitmapFromFile(file.absolutePath, TARGET_MAX_DIMENSION)
                        ?: return@forEach
                    try {
                        val ocrResult = ocrEngine.runOcr(bitmap, activeModels)
                        imageOcrDataList.add(
                            ImageOcrData(
                                id = imageId,
                                text = ocrResult.fullText
                            )
                        )
                    } finally {
                        bitmap.recycle()
                    }
                }
            }

            val ocrData = DocumentOcrData(images = imageOcrDataList)
            val jsonText = Json.encodeToString(ocrData)

            documentModelRepository.updateOcrText(documentModel.id, jsonText)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Napier.e("Error updating OCR note for document $documentId", e)
            Result.failure(e)
        }
    }

    private fun decodeSampledBitmapFromFile(path: String, maxDimension: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)

            options.inSampleSize = options.calculateInSampleSize(maxDimension)
            options.inJustDecodeBounds = false

            BitmapFactory.decodeFile(path, options)
        } catch (e: OutOfMemoryError) {
            Napier.e("OutOfMemoryError decoding image at path: $path", e)
            null
        } catch (e: Exception) {
            Napier.e("Error decoding image at path: $path", e)
            null
        }
    }

    companion object {
        private const val TARGET_MAX_DIMENSION = 960
        private const val PDF_ID_PREFIX = "pdf_page_"
    }
}
