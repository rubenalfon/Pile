package com.ganadoro.pile.util

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.graphics.Canvas
import android.graphics.Paint
import java.io.File
import java.io.FileOutputStream

fun createSimplePdf(context: Context, fileName: String = "FILE.pdf") : PdfDocument {
    val file = File(context.filesDir, fileName)

    // Crea un nuevo documento PDF
    val pdfDocument = PdfDocument()

    // Define el tamaño de página (A4 estándar, 595x842 puntos)
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()

    // Crea una página
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas

    val paint = Paint().apply {
        textSize = 16f
        isAntiAlias = true
    }

    // Dibuja texto en el canvas
    canvas.drawText("¡Hola, esto es un PDF!", 100f, 100f, paint)

    // Finaliza la página
    pdfDocument.finishPage(page)

    return pdfDocument
}
