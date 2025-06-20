package com.ganadoro.pile.ui.screens.addDocument

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.PileModelRepository
import com.ganadoro.pile.util.renderFirstPDFPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class AddDocumentUiState(
    var documentModel: DocumentModel? = null,
    var firstPageBitmap: Bitmap? = null,
    var documentName: String = "",
    var allPileModels: List<PileModel>? = null,
    var selectedPileModels: List<PileModel> = emptyList()
)

@SuppressLint("StaticFieldLeak")
class AddDocumentViewModel(
    private val context: Context, // Safe
    private val documentModelRepository: DocumentModelRepository,
    private val pileModelRepository: PileModelRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(AddDocumentUiState())
    var uiState: StateFlow<AddDocumentUiState> = _uiState.asStateFlow()

    fun loadDocument(documentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                _uiState.value.documentModel =
                    documentModelRepository.getDocumentModelById(documentId)
            }
            launch {
                val file = File(context.filesDir, documentId)

                _uiState.value.firstPageBitmap = renderFirstPDFPage(file)
            }
        }
    }

    fun loadPiles() {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                _uiState.value.allPileModels = pileModelRepository.getAllPileModels()
            }
        }
    }

    fun setDocumentName(name: String) {
        _uiState.value = _uiState.value.copy(documentName = name)
    }

    fun setSelectedPiles(piles: List<PileModel>) {
        _uiState.value = _uiState.value.copy(selectedPileModels = piles)

    }
}