package es.pile.features.externalImport.ui

import android.net.Uri
import es.pile.core.ui.util.UiText

data class ImportState(
    val successDocumentId: String? = null,
    val isPdf: Boolean = false,
    val errorMessage: UiText? = null
)

sealed interface ImportEvent {
    data class OnImportImages(val uris: List<Uri>) : ImportEvent
    data class OnImportPdf(val uri: Uri) : ImportEvent
    data object OnErrorDismissed : ImportEvent
}
