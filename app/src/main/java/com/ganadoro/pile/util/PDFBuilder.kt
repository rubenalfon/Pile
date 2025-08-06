package com.ganadoro.pile.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Size
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Crea un archivo PDF con imágenes adjuntas.
 *
 * @param context Contexto de la aplicación.
 * @param imageUris Lista de URIs de imágenes a adjuntar.
 * @param outputFile Archivo PDF de salida.
 *
 * @throws IOException Si ocurre un error al escribir el archivo PDF.
 */
suspend fun createPdfWithImages(context: Context, imageUris: List<Uri>, outputFile: File) = withContext(Dispatchers.IO) {
    val pdfDocument = PdfDocument()

    imageUris.forEachIndexed { index, uri ->
        val bitmap = prepareBitmapFromUri(context, uri, Size(595, 842)) ?: return@forEachIndexed

        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawBitmap(bitmap, 0f, 0f, null)
        pdfDocument.finishPage(page)
    }

    outputFile.outputStream().use {
        pdfDocument.writeTo(it)
    }

    pdfDocument.close()
}

/**
 * Renderiza la primera página de un archivo PDF a un Bitmap.
 *
 * @param pdfFile El archivo PDF a renderizar.
 * @return Un Bitmap de la primera página, o null si el PDF no tiene páginas o hay un error.
 */
suspend fun renderFirstPdfPage(pdfFile: File): Bitmap? {
    return try {
        renderPdfPageAtIndex(pdfFile, 0)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

/**
 * Renderiza todas las páginas de un archivo PDF a una lista de Bitmaps.
 *
 * @param pdfFile El archivo PDF a renderizar.
 * @return Una lista de Bitmaps, una por cada página del PDF. La lista estará vacía si hay un error.
 */
suspend fun renderAllPdfPages(pdfFile: File): List<Bitmap> = withContext(Dispatchers.IO) {
    val bitmaps = mutableListOf<Bitmap>()
    try {
        ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
            PdfRenderer(fd).use { renderer ->
                for (i in 0 until renderer.pageCount) {
                    val bitmap = renderPage(renderer, i)
                    bitmaps.add(bitmap)
                }
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return@withContext bitmaps
}

/**
 * Función de ayuda principal que renderiza una ÚNICA página de un PDF por su índice.
 * Esta función es la que contiene la lógica central.
 */
@Throws(IOException::class)
private suspend fun renderPdfPageAtIndex(file: File, pageIndex: Int): Bitmap? = withContext(Dispatchers.IO) {
    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
        PdfRenderer(fd).use { renderer ->
            if (pageIndex < 0 || pageIndex >= renderer.pageCount) {
                return@withContext null // O lanzar una excepción, ej: IndexOutOfBoundsException
            }
            return@withContext renderPage(renderer, pageIndex)
        }
    }
}

/**
 * Helper final y privado. Renderiza una página específica usando un PdfRenderer ya abierto.
 * AQUÍ ESTÁ LA LÓGICA CENTRAL REPETIDA.
 */
private fun renderPage(renderer: PdfRenderer, pageIndex: Int): Bitmap {
    return renderer.openPage(pageIndex).use { page ->
        val bitmap = createBitmap(page.width, page.height)

        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        bitmap
    }
}