package com.ganadoro.pile.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Size
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

private val DEFAULT_PAGE_SIZE = Size(595, 842) // A4 en puntos (72 dpi)

/**
 * Crea un archivo PDF con imágenes (URIs).
 *
 * @param context Contexto de la aplicación.
 * @param imageUris Lista de URIs de imágenes a adjuntar.
 * @param outputFile Archivo PDF de salida.
 * @param pageSize Tamaño de página/canvas en puntos (por defecto A4 595x842).
 *
 * @throws IOException Si ocurre un error al escribir el archivo PDF.
 */
suspend fun createPdfWithImages(
    context: Context,
    imageUris: List<Uri>,
    outputFile: File,
    pageSize: Size = DEFAULT_PAGE_SIZE
) = withContext(Dispatchers.IO) {
    val pdfDocument = PdfDocument()
    try {
        imageUris.forEachIndexed { index, uri ->
            val bitmap = prepareBitmapFromUri(context, uri, pageSize) ?: return@forEachIndexed

            val pageInfo =
                PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)
        }

        outputFile.outputStream().use { pdfDocument.writeTo(it) }
    } finally {
        pdfDocument.close()
    }
}

/**
 * Crea un archivo PDF con Bitmaps.
 *
 * @param bitmaps Lista de Bitmaps a adjuntar.
 * @param outputFile Archivo PDF de salida.
 *
 * @throws IOException Si ocurre un error al escribir el archivo PDF.
 */
suspend fun createPdfWithImages(
    bitmaps: List<Bitmap>,
    outputFile: File,
) = withContext(Dispatchers.IO) {
    val pdfDocument = PdfDocument()
    try {
        bitmaps.forEachIndexed { index, bitmap ->
            val pageInfo =
                PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)
        }

        outputFile.outputStream().use { pdfDocument.writeTo(it) }
    } finally {
        pdfDocument.close()
    }
}

suspend fun renderPdfPage(pdfFile: File, pageNumber: Int): Bitmap? = withContext(Dispatchers.IO) {
    try {
        ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
            PdfRenderer(fd).use { renderer ->
                if (pageNumber in 0 until renderer.pageCount) {
                    renderPage(renderer, pageNumber)
                } else null
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

private fun renderPage(renderer: PdfRenderer, pageIndex: Int): Bitmap {
    return renderer.openPage(pageIndex).use { page ->
        val bitmap = createBitmap(page.width, page.height)

        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        bitmap
    }
}

/**
 * Crea un archivo PDF a partir de una lista de archivos de imagen locales.
 *
 * @param imageFiles Lista de archivos (File) que contienen las imágenes.
 * @param outputFile Archivo PDF de salida.
 */
suspend fun createPdfFromFiles(
    imageFiles: List<File>,
    outputFile: File
) = withContext(Dispatchers.IO) {

    // 1. Convertimos los archivos a Bitmaps en paralelo para que sea rápido
    val bitmaps = imageFiles.map { file ->
        async {
            try {
                // Usamos decodeFile que es directo para objetos File
                BitmapFactory.decodeFile(file.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }.awaitAll().filterNotNull()

    // 2. Usamos tu función existente que ya sabe manejar una lista de Bitmaps
    if (bitmaps.isNotEmpty()) {
        createPdfWithImages(bitmaps, outputFile)

        // Importante: Liberar la memoria de los bitmaps después de crear el PDF
        bitmaps.forEach { it.recycle() }
    }
}