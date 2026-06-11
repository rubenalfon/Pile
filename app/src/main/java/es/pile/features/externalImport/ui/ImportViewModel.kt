package es.pile.features.externalImport.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pile.R
import es.pile.core.ui.util.UiText
import es.pile.features.home.domain.useCases.CreateDocumentUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ImportViewModel(
    private val createDocumentUseCase: CreateDocumentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ImportState())
    val state: StateFlow<ImportState> = _state.asStateFlow()

    fun handleEvent(event: ImportEvent) {
        when (event) {
            is ImportEvent.OnImportImages -> importImages(event.uris)
            is ImportEvent.OnImportPdf -> importPdf(event.uri)
            ImportEvent.OnErrorDismissed -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun importImages(uris: List<Uri>) {
        if (uris.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(successDocumentId = null, errorMessage = null) }
            try {
                val document = createDocumentUseCase.createFromImages(uris)
                _state.update { it.copy(successDocumentId = document.id, isPdf = false) }
            } catch (e: Exception) {
                Napier.e("Error importing images", e)
                _state.update {
                    it.copy(errorMessage = UiText.StringResource(R.string.error_importing_images))
                }
            }
        }
    }

    private fun importPdf(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(successDocumentId = null, errorMessage = null) }
            try {
                val document = createDocumentUseCase.createFromPdf(uri)
                _state.update { it.copy(successDocumentId = document.id, isPdf = true) }
            } catch (e: Exception) {
                Napier.e("Error importing PDF", e)
                _state.update {
                    it.copy(errorMessage = UiText.StringResource(R.string.error_importing_pdf))
                }
            }
        }
    }
}
