package com.ganadoro.pile.ui.screens.documentDetail

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.PileModelRepository
import com.ganadoro.pile.util.renderPdfPages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class DocumentDetailUiState(
    var documentModel: DocumentModel? = null,
    var bitmaps: List<Bitmap> = emptyList()
)

@SuppressLint("StaticFieldLeak")
class DocumentDetailViewModel(
    private val context: Context, // Safe
    private val documentModelRepository: DocumentModelRepository,
    private val pileModelRepository: PileModelRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(DocumentDetailUiState())
    var uiState: StateFlow<DocumentDetailUiState> = _uiState.asStateFlow()

    fun loadDocument(documentId: String) {
        viewModelScope.launch {
            launch {
                _uiState.value.documentModel =
                    documentModelRepository.getDocumentModelById(documentId)
            }
            launch {
                val file = File(context.filesDir, documentId)

                _uiState.value.bitmaps = renderPdfPages(file)
            }
        }
    }

    fun renameDocument(newDocumentName: String) {
        viewModelScope.launch {
            val updatedDocumentModel =
                _uiState.value.documentModel?.copy(title = newDocumentName)

            _uiState.value.documentModel = updatedDocumentModel

            if (updatedDocumentModel != null) { // TODO error handling
                documentModelRepository.updateDocumentModel(updatedDocumentModel)
            }
        }
    }

    fun deleteDocument() {
        viewModelScope.launch {
            val documentId = _uiState.value.documentModel?.id
            if (documentId != null) { // TODO error handling
                documentModelRepository.deleteDocumentModel(documentId)
            }
        }

    }

}