package com.ganadoro.pile.ui.navigation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Interface responsible for handling document-related external actions.
 * 
 * It abstracts the logic for opening files with external viewers and sharing 
 * them with other applications.
 */
interface DocumentOpener {
    /**
     * Opens a PDF file using an external application.
     * 
     * @param uri The secure [Uri] of the PDF file to open.
     */
    fun openPdf(uri: Uri)

    /**
     * Shares a PDF file with other applications using the system share sheet.
     * 
     * @param uri The secure [Uri] of the PDF file to share.
     */
    fun sharePdf(uri: Uri)
}

/**
 * Implementation of [DocumentOpener] that uses Android Intents to perform actions.
 */
class DocumentOpenerImpl(private val appContext: Context) : DocumentOpener {
    
    override fun openPdf(uri: Uri) {
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivitySafely(openIntent)
    }

    override fun sharePdf(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Compartir documento")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivitySafely(chooser)
    }

    /**
     * Safely attempts to start an activity, handling cases where no suitable 
     * application is installed to handle the [intent].
     */
    private fun startActivitySafely(intent: Intent) {
        try {
            appContext.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            // Optional: Show a message to the user that no app can open this file
        }
    }
}
