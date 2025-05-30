package com.ganadoro.pile.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Size
import androidx.core.graphics.createBitmap
import java.io.File


suspend fun createPdfWithImages(context: Context, imageUris: List<Uri>, outputFile: File) {
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


suspend fun renderPdfPages(pdfFile: File): List<Bitmap> { // TODO: Rename
    val bitmaps = mutableListOf<Bitmap>()

    val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
    val renderer = PdfRenderer(fileDescriptor)

    for (i in 0 until renderer.pageCount) {
        renderer.openPage(i).use { page ->
            val bitmap = createBitmap(page.width, page.height)

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmaps.add(bitmap)
        }
    }

    renderer.close()
    fileDescriptor.close()

    return bitmaps
}

