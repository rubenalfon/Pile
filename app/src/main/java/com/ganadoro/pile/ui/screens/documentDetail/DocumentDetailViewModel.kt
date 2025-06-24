package com.ganadoro.pile.ui.screens.documentDetail

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.PileModelRepository
import com.ganadoro.pile.util.renderPdfPages
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class DocumentDetailUiState(
    var documentModel: DocumentModel? = null,
    var bitmaps: List<Bitmap> = emptyList(),
    var documentPileModels: List<PileModel>? = null
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
                _uiState.value = _uiState.value.copy(
                    documentModel = documentModelRepository.getDocumentModelById(documentId)
                )
            }
            launch {
                val file = File(context.filesDir, documentId)
                _uiState.value = _uiState.value.copy(bitmaps = renderPdfPages(file))
            }
            launch {
                try {
                    _uiState.value = _uiState.value.copy(
                        documentPileModels = _uiState.value.documentModel!!.documentPileIds.map {
                            pileModelRepository.getPileModelById(it)!!
                        }
                    )
                } catch (ex: Exception) {
                    Napier.e("Error loading document piles", ex) // TODO error handling
                    _uiState.value = _uiState.value.copy(documentPileModels = emptyList())

                }
            }
        }
    }

    fun updateDocumentNote(newDocumentNote: String) {
       Napier.d { "DocumentDetailViewModel.updateDocumentNote: $newDocumentNote" }
        viewModelScope.launch {
            val updatedDocumentModel =
                _uiState.value.documentModel?.copy(documentNote = newDocumentNote)
                    ?: return@launch

            _uiState.value = _uiState.value.copy(documentModel = updatedDocumentModel)

            documentModelRepository.updateDocumentModel(updatedDocumentModel)
        }
    }

    fun renameDocument(newDocumentName: String) {
        viewModelScope.launch {
            val updatedDocumentModel = _uiState.value.documentModel?.copy(title = newDocumentName)
                ?: return@launch

            _uiState.value = _uiState.value.copy(documentModel = updatedDocumentModel)

            documentModelRepository.updateDocumentModel(updatedDocumentModel)
        }
    }

    fun deleteDocument() {
        viewModelScope.launch {
            val documentId = _uiState.value.documentModel?.id ?: return@launch
            documentModelRepository.deleteDocumentModel(documentId)
        }
    }
}