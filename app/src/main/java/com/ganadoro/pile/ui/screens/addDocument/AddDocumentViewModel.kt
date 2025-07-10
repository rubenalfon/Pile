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
import com.ganadoro.pile.util.renderFirstPdfPage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.util.UUID
import kotlin.random.Random

data class AddDocumentUiState(
    var documentModel: DocumentModel? = null,
    var firstPageBitmap: Bitmap? = null,
    var documentName: String = "",
    var allPileModels: List<PileModel>? = null,
    var selectedPileModelIds: List<String> = emptyList(),
    var noDocumentNameError: Boolean = false
)

@SuppressLint("StaticFieldLeak")
class AddDocumentViewModel(
    private val context: Context, // Safe
    private val documentModelRepository: DocumentModelRepository,
    private val pileModelRepository: PileModelRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(AddDocumentUiState())
    var uiState: StateFlow<AddDocumentUiState> = _uiState.asStateFlow()

    var navigateToDocumentDetail: ((String) -> Unit)? = null

    fun loadDocument(documentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                _uiState.update {
                    it.copy(
                        documentModel = documentModelRepository.getDocumentModelById(
                            documentId
                        )
                    )
                }
                _uiState.update {
                    it.copy(
                        documentName = _uiState.value.documentModel!!.title
                    )
                }
            }
            launch {
                val file = File(context.filesDir, documentId)
                _uiState.update { it.copy(firstPageBitmap = renderFirstPdfPage(file)) }
            }
        }
    }

    fun loadPiles() {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                pileModelRepository.pileModels.collect { piles ->
                    _uiState.update { it.copy(allPileModels = piles) }
                }
            }
        }
    }

    fun setDocumentName(name: String) {
        _uiState.update { it.copy(documentName = name) }
    }

    fun saveDocument() {
        if (_uiState.value.documentName.isBlank()) {
            _uiState.update { it.copy(noDocumentNameError = true) }
            return
        }
        _uiState.update { it.copy(noDocumentNameError = false) }

        viewModelScope.launch(Dispatchers.IO) {
            val newDocumentId = UUID.randomUUID().toString()
            val newDocumentFile = File(context.filesDir, newDocumentId)

            if (newDocumentFile.exists()) {
                Napier.e { "No se puede crear el documento, ya existe" } // TODO Error handling
                return@launch
            }

            val dbJob = launch dbJob@{
                var documentModel = _uiState.value.documentModel ?: return@dbJob

                documentModel = documentModel.copy(
                    id = newDocumentId,
                    title = _uiState.value.documentName,
                    creationDate = LocalDate.now(),
                    modificationDate = LocalDate.now(),
                    documentPileIds = _uiState.value.selectedPileModelIds,
                    documentDetails = emptyList(),
                    documentOrganizationIds = emptyList()
                )

                documentModelRepository.insertDocumentModel(documentModel)

                documentModelRepository.deleteDocumentModel(TEMP_DOCUMENT_ID)
            }

            val fileJob = launch {
                val oldDocumentFile = File(context.filesDir, TEMP_DOCUMENT_ID)

                oldDocumentFile.renameTo(newDocumentFile)
            }

            listOf(dbJob, fileJob).forEach { it.join() }

            launch(Dispatchers.Main) {
                navigateToDocumentDetail?.invoke(newDocumentId)
            }
        }
    }

    fun updatePileSelectState(pileId: String) {
        val piles = _uiState.value.selectedPileModelIds.toMutableList()

        if (piles.contains(pileId)) {
            piles.remove(pileId)
        } else {
            piles.add(pileId)
        }

        _uiState.update { it.copy(selectedPileModelIds = piles) }
    }


    fun addPile(pileName: String) {
        viewModelScope.launch {
            val newPile = PileModel( // TODO icono y color seleccionado por el usuario
                id = UUID.randomUUID().toString(),
                name = pileName,
                iconId = "Pet",
                colorNumber = Random.nextInt(31).toLong()
            )

            pileModelRepository.insertPileModel(newPile)
        }
    }
}