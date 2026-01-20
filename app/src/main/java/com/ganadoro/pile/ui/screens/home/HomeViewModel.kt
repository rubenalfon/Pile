package com.ganadoro.pile.ui.screens.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.domain.models.DocumentStatusConstants.TEMPORARY
import com.ganadoro.pile.domain.models.TemporaryDocumentBackup
import com.ganadoro.pile.domain.repositories.BitmapCacheRepository
import com.ganadoro.pile.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.domain.repositories.PileModelRepository
import com.ganadoro.pile.domain.usecases.CreateDocumentUseCase
import com.ganadoro.pile.domain.usecases.CreatePileUseCase
import com.ganadoro.pile.domain.usecases.ManageTemporaryDocumentUseCase
import com.ganadoro.pile.domain.usecases.RequestBitmapLoadUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val pileModels: List<PileModel>? = null,
    val documentList: List<DocumentModel>? = null,
    val temporaryDocument: DocumentModel? = null,
    val coloredPileIds: List<String>? = null,
)

class HomeViewModel(
    private val createDocumentUseCase: CreateDocumentUseCase,
    private val manageTemporaryDocumentUseCase: ManageTemporaryDocumentUseCase,
    private val createPileUseCase: CreatePileUseCase,
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase,
    private val pileModelRepository: PileModelRepository,
    private val documentModelRepository: DocumentModelRepository,
    private val bitmapCacheRepository: BitmapCacheRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(HomeUiState())
    var uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    private val _navigationEvent = Channel<DocumentModel>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private var backupUnsavedDocument: TemporaryDocumentBackup? = null

    init {
        viewModelScope.launch {
            val pileModelsFlow = pileModelRepository.pileModels

            pileModelsFlow.combine(documentModelRepository.documentModels) { piles, documents ->
                val coloredPileIds = documents.flatMap { it.documentPileIds }.distinct()
                val temporaryDocument = documents.find { it.documentStatus == TEMPORARY }

                HomeUiState(
                    pileModels = piles,
                    documentList = documents.filter { it.documentStatus != TEMPORARY },
                    temporaryDocument = temporaryDocument,
                    coloredPileIds = coloredPileIds
                )
            }.collect { finalState ->
                _uiState.update {
                    finalState
                }
            }
        }
    }

    override fun onCleared() { // TODO: Work manager ?
        super.onCleared()
        confirmErasureUnsavedDeletedDocument()
    }

    fun requestBitmapLoad(document: DocumentModel, pageNumber: Int) {
        viewModelScope.launch {
            requestBitmapLoadUseCase(document, pageNumber)
        }
    }

    fun requestImageKey(document: DocumentModel, pageNumber: Int): String =
        bitmapCacheRepository.getImageKey(document, pageNumber)

    fun addPile(pileName: String, iconId: String, color: Long) {
        viewModelScope.launch {
            createPileUseCase(pileName, iconId, color)
        }
    }

    fun importPDFIntent(uri: Uri) {
        viewModelScope.launch {
            try {
                val newDoc = createDocumentUseCase.createFromPdf(uri)
                _navigationEvent.send(newDoc)
            } catch (e: Exception) {
                Napier.e("Error importing PDF", e)
                // TODO: show in ui, toast
            }
        }
    }

    fun importFromGalleryIntent(uriList: List<Uri>) {
        viewModelScope.launch {
            try {
                val newDoc = createDocumentUseCase.createFromImages(uriList)
                _navigationEvent.send(newDoc)
            } catch (e: Exception) {
                Napier.e("Error importing images", e)
                // TODO: show in ui, toast
            }
        }
    }

    fun takePhoto(uri: Uri) {
        importFromGalleryIntent(listOf(uri))
    }

    fun partialDeleteUnsavedDocument() {
        viewModelScope.launch {
            backupUnsavedDocument = manageTemporaryDocumentUseCase.deleteForUndo()
        }
    }

    fun restoreUnsavedDeletedDocument() {
        val backup = backupUnsavedDocument ?: return

        viewModelScope.launch {
            try {
                backupUnsavedDocument = null
                manageTemporaryDocumentUseCase.restoreBackup(backup)
            } catch (e: Exception) {
                Napier.e { "Error restoring backup. Message: ${e.message}" }
                // TODO: show in ui, toast
            }
        }
    }

    fun confirmErasureUnsavedDeletedDocument() {
        val backup = backupUnsavedDocument ?: return
        val documentId = backup.document.id

        backupUnsavedDocument = null

        viewModelScope.launch {
            try {
                manageTemporaryDocumentUseCase.confirmPermanentDeletion(documentId)
            } catch (e: Exception) {
                Napier.e { "Error deleting document. Message: ${e.message}" }
                // TODO: show in ui, toast
            }
        }
    }
}