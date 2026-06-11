package es.pile.features.documentDetail.data.helper

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import es.pile.features.documentDetail.domain.helper.DocumentOpener

/**
 * Android Framework implementation of [DocumentOpener].
 *
 * Uses [Intent] flags to grant temporary read permissions to external
 * applications, ensuring secure file sharing.
 *
 * @property appContext The application context used to start external activities.
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
        }
    }
}
