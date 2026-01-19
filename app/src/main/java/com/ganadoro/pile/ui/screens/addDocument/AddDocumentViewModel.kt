package com.ganadoro.pile.ui.screens.addDocument

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.domain.models.DocumentStatusConstants
import com.ganadoro.pile.domain.repositories.BitmapCacheRepository
import com.ganadoro.pile.domain.repositories.DocumentImageRepository
import com.ganadoro.pile.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.domain.repositories.PileModelRepository
import com.ganadoro.pile.domain.usecases.RequestBitmapLoadUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID

data class AddDocumentUiState(
    val documentModel: DocumentModel? = null,
    val frontPageDocumentImage: DocumentImage? = null,
    val documentName: String = "",
    val allPileModels: List<PileModel>? = null,
    val selectedPileModelIds: List<String> = emptyList(),
    val noDocumentNameError: Boolean = false
)

class AddDocumentViewModel(
    private val documentId: String,
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase,
    private val documentModelRepository: DocumentModelRepository,
    private val pileModelRepository: PileModelRepository,
    private val documentImageRepository: DocumentImageRepository,
    private val bitmapCacheRepository: BitmapCacheRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(AddDocumentUiState())
    var uiState: StateFlow<AddDocumentUiState> = _uiState.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    init {
        viewModelScope.launch {
            launch {
                loadDocument(documentId)
            }
            launch {
                loadPiles()
            }
        }
    }

    private suspend fun loadDocument(documentId: String) {
        try {
            val document = withContext(Dispatchers.IO) {
                documentModelRepository.getDocumentModelById(documentId).first()
            }

            if (document == null) return

            _uiState.update {
                it.copy(
                    documentModel = document,
                    documentName = document.title
                )
            }

            val firstPageId = document.imageIds.firstOrNull() ?: return

            documentImageRepository.getDocumentImageById(firstPageId).collect { image ->
                _uiState.update { it.copy(frontPageDocumentImage = image) }
            }
        } catch (e: Exception) {
            Napier.e(e) { "Error al cargar documento: $documentId" }
        }
    }

    private suspend fun loadPiles() {
        pileModelRepository.pileModels.collect { piles ->
            _uiState.update { it.copy(allPileModels = piles) }
        }
    }

    fun requestBitmapLoad(pageNumber: Int) {
        viewModelScope.launch {
            val document = uiState.value.documentModel ?: return@launch
            requestBitmapLoadUseCase(document, pageNumber)
        }
    }

    fun setDocumentName(name: String) {
        _uiState.update {
            it.copy(
                documentName = name,
                noDocumentNameError = false
            )
        }
    }

    fun updatePileSelectState(pileId: String) {
        _uiState.update { state ->
            val currentPiles = state.selectedPileModelIds

            val newPiles = if (currentPiles.contains(pileId)) {
                currentPiles - pileId
            } else {
                currentPiles + pileId
            }

            state.copy(selectedPileModelIds = newPiles)
        }
    }

    fun addPile(pileName: String, iconId: String, color: Long) {
        viewModelScope.launch {
            val newPile = PileModel(
                id = UUID.randomUUID().toString(),
                name = pileName,
                iconId = iconId,
                colorNumber = color
            )

            withContext(Dispatchers.IO) {
                pileModelRepository.insertPileModel(newPile)
            }
        }
    }

    fun saveDocument(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        if (currentState.documentName.isBlank()) {
            _uiState.update { it.copy(noDocumentNameError = true) }
            return
        }

        _uiState.update { it.copy(noDocumentNameError = false) }

        viewModelScope.launch {
            try {
                val documentModel = currentState.documentModel ?: return@launch

                val updatedDocument = documentModel.copy(
                    title = currentState.documentName,
                    creationDateTime = LocalDateTime.now(),
                    modificationDateTime = LocalDateTime.now(),
                    documentStatus = DocumentStatusConstants.SAVED,
                    documentPileIds = currentState.selectedPileModelIds,
                    documentDetails = emptyList(),
                    documentOrganizationIds = emptyList()
                )

                withContext(Dispatchers.IO) {
                    documentModelRepository.updateDocumentModel(updatedDocument)
                }

                onSuccess()

            } catch (e: Exception) {
                Napier.e(e) { "Error al guardar documento" }
                // TODO Opcional: mostrar un Toast o mensaje de error al usuario
            }
        }
    }
}