package com.ganadoro.pile.ui.screens.editPDF

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.util.renderAllPdfPages
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File


data class EditPDFUiState(
    var documentModel: DocumentModel? = null,
    var bitmaps: List<Bitmap> = emptyList(),
    var selectedImageIndex: Int = 0
)

@SuppressLint("StaticFieldLeak")
class EditPDFViewModel(
    private val context: Context, // Safe
    private val documentModelRepository: DocumentModelRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(EditPDFUiState())
    var uiState: StateFlow<EditPDFUiState> = _uiState.asStateFlow()

    fun loadDocument(documentId: String) {
        Napier.d { "EditPDFViewModel.loadDocument: $documentId" }
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                _uiState.update {
                    it.copy(
                        documentModel = documentModelRepository.getDocumentModelById(documentId)
                    )
                }
            }
            launch {
                val file = File(context.filesDir, documentId)
                _uiState.update { it.copy(bitmaps = renderAllPdfPages(file)) }
            }
        }
    }

    fun setSelectedImageIndex(index: Int) {
        viewModelScope.launch {
            Napier.d { "EditPDFViewModel.setSelectedImageIndex: $index" }
            _uiState.value = _uiState.value.copy(selectedImageIndex = index)
        }
    }

    fun addNewImage() {

    }

    fun deleteSelectedImage() {
        Napier.d { "DeleteImage selectedImageIndex: ${uiState.value.selectedImageIndex}" }

        val filteredBitmaps = _uiState.value.bitmaps.filterIndexed { index, _ ->
            index != uiState.value.selectedImageIndex
        }

        _uiState.value = _uiState.value.copy(bitmaps = filteredBitmaps)
    }
}