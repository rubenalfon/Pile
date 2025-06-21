package com.ganadoro.pile.ui.screens.addDocument

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.models.TEMP_DOCUMENT_ID
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.PileModelRepository
import com.ganadoro.pile.util.renderFirstPDFPage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.util.UUID

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

    fun saveDocument() {
        viewModelScope.launch(Dispatchers.IO) {
            val newDocumentId = UUID.randomUUID().toString()
            val newDocumentFile = File(context.filesDir, newDocumentId)

            if (newDocumentFile.exists()) {
                Napier.e  { "No se puede crear el documento, ya existe" } // TODO Error handling
                return@launch
            }

            launch {
                var documentModel = _uiState.value.documentModel ?: return@launch

                documentModel = documentModel.copy(
                    id = newDocumentId,
                    title = _uiState.value.documentName,
                    creationDate = LocalDate.now(),
                    modificationDate = LocalDate.now(),
                    documentPileIds = _uiState.value.selectedPileModels.map { it.id },
                    documentDetails = emptyList(),
                    documentOrganizationIds = emptyList()
                )

                documentModelRepository.insertDocumentModel(documentModel)

                documentModelRepository.deleteDocumentModel(TEMP_DOCUMENT_ID)
            }

            launch {
                val oldDocumentFile = File(context.filesDir, TEMP_DOCUMENT_ID)

                oldDocumentFile.renameTo(newDocumentFile)
            }
        }
    }

    fun setSelectedPiles(piles: List<PileModel>) {
        _uiState.value = _uiState.value.copy(selectedPileModels = piles)

    }
}