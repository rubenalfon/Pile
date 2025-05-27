package com.ganadoro.pile.ui.screens.editPDF

import androidx.lifecycle.ViewModel
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.ui.screens.home.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


data class EditPDFUiState(
    var documentModel: DocumentModel? = null,
    var documentList: List<DocumentModel> = emptyList()
)

class EditPDFViewModel(
    private val documentModelRepository: DocumentModelRepository
): ViewModel() {
    private var _uiState = MutableStateFlow(EditPDFUiState())
    var uiState: StateFlow<EditPDFUiState> = _uiState.asStateFlow()
}