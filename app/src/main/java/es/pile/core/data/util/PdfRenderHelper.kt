package es.pile.core.data.util

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Helper class responsible for low-level PDF rendering operations.
 * Wraps Android's [android.graphics.pdf.PdfRenderer] API to provide clean access to page counts and bitmap rendering.
 */
class PdfRenderHelper(
    private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * Gets the total number of pages in a PDF file.
     *
     * @param pdfFile The source PDF file.
     * @return A [Result] containing the page count or an error.
     */
    suspend fun getPageCount(pdfFile: File): Result<Int> =
        withContext(ioDispatcher) {
            runCatching {
                getRenderer(pdfFile).use { (renderer, _) ->
                    renderer.pageCount
                }
            }
        }

    /**
     * Renders a specific page of a PDF file into a Bitmap.
     *
     * @param file The source PDF file.
     * @param pageIndex The 0-based index of the page.
     * @param width (Optional) Targeted width for scaling. If null, uses actual page width.
     * @return The rendered [android.graphics.Bitmap], or null if failed.
     */
    suspend fun renderPageToBitmap(
        file: File,
        pageIndex: Int,
        width: Int? = null
    ): Bitmap? = withContext(ioDispatcher) {
        try {
            getRenderer(file).use { (renderer, _) ->
                if (pageIndex >= renderer.pageCount) return@withContext null

                renderer.openPage(pageIndex).use { page ->
                    val destinationWidth = width ?: page.width
                    val aspectRatio = page.height.toFloat() / page.width.toFloat()
                    val destinationHeight = (destinationWidth * aspectRatio).toInt()

                    val bitmap = Bitmap.createBitmap(
                        destinationWidth,
                        destinationHeight,
                        Bitmap.Config.ARGB_8888
                    )

                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    bitmap
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Opens a PDF file and returns a [Pair] of [PdfRenderer] and [android.os.ParcelFileDescriptor].
     *
     * @param file The source PDF file.
     * @return A [Pair] of [PdfRenderer] and [android.os.ParcelFileDescriptor].
     */
    private fun getRenderer(file: File): Pair<PdfRenderer, ParcelFileDescriptor> {
        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fileDescriptor)
        return Pair(renderer, fileDescriptor)
    }

    // Extension function to allow destructuring Pair in 'use' block
    private inline fun <R> Pair<PdfRenderer, ParcelFileDescriptor>.use(block: (Pair<PdfRenderer, ParcelFileDescriptor>) -> R): R {
        return try {
            block(this)
        } finally {
            try {
                this.first.close()
            } catch (_: Exception) {
            }
            try {
                this.second.close()
            } catch (_: Exception) {
            }
        }
    }
}