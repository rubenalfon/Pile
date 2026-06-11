package es.pile.features.documentDetail.domain.helper

import android.content.Intent
import android.net.Uri

/**
 * Interface responsible for handling document-related external actions.
 */
interface DocumentOpener {
    /**
     * Triggers an [Intent.ACTION_VIEW] to open a PDF file with an external viewer.
     *
     * @param uri The FileProvider [Uri] of the PDF. Must have read permissions.
     */
    fun openPdf(uri: Uri)

    /**
     * Launches the system share sheet using [Intent.ACTION_SEND] to distribute
     * the document to other installed applications.
     *
     * @param uri The FileProvider [Uri] of the PDF to be shared.
     */
    fun sharePdf(uri: Uri)
}
