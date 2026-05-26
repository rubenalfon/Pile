package com.pile.features.home.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pile.DocumentModel
import com.pile.R
import com.pile.core.domain.models.DocumentCoverItem
import com.pile.core.domain.models.DocumentStatusConstants.TEMPORARY
import com.pile.core.domain.repositories.BitmapCacheRepository
import com.pile.core.domain.repositories.DocumentModelRepository
import com.pile.core.domain.repositories.FileRepository
import com.pile.core.domain.repositories.PileModelRepository
import com.pile.core.domain.useCases.CreatePileUseCase
import com.pile.core.domain.useCases.RequestBitmapLoadUseCase
import com.pile.core.ui.util.UiText
import com.pile.features.home.domain.models.TemporaryDocumentBackup
import com.pile.features.home.domain.schedulers.CleanupScheduler
import com.pile.features.home.domain.useCases.CreateDocumentUseCase
import com.pile.features.home.domain.useCases.ManageTemporaryDocumentUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val createDocumentUseCase: CreateDocumentUseCase,
    private val manageTemporaryDocumentUseCase: ManageTemporaryDocumentUseCase,
    private val createPileUseCase: CreatePileUseCase,
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase,
    private val cleanupScheduler: CleanupScheduler,
    private val pileModelRepository: PileModelRepository,
    private val documentModelRepository: DocumentModelRepository,
    private val bitmapCacheRepository: BitmapCacheRepository,
    private val fileRepository: FileRepository
) : ViewModel() {
    private var _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    private val _navigationEvent = Channel<DocumentModel>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private var backupUnsavedDocument: TemporaryDocumentBackup? = null

    init {
        viewModelScope.launch {
            val pileModelsFlow = pileModelRepository.pileModels

            pileModelsFlow.combine(documentModelRepository.documentModels) { piles, documents ->
                val temporaryDocument = documents.find { it.documentStatus == TEMPORARY }
                val coloredPileIds = documents.flatMap { it.documentPileIds }.distinct()

                val documentCoverItems = documents.filter { it.documentStatus != TEMPORARY }
                    .map { documentModel ->
                        DocumentCoverItem(
                            document = documentModel,
                            coverImageCacheKey = bitmapCacheRepository.getImageKey(documentModel, 0)
                        )
                    }

                _state.update {
                    it.copy(
                        pileModels = piles,
                        documentCoverItems = documentCoverItems,
                        temporaryDocument = temporaryDocument,
                        coloredPileIds = coloredPileIds,
                        isInitialLoading = false
                    )
                }
            }.collect()
        }
    }

    override fun onCleared() {
        super.onCleared()
        purgeDraftDocument()
    }

    fun handleEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnImageDisplayed -> requestBitmapLoad(event.document)

            is HomeEvent.OnRemoveDraftDocument -> removeDraftDocument()
            HomeEvent.OnRestoreDraftDocument -> restoreDraftDocument()
            HomeEvent.OnPurgeDraftDocument -> purgeDraftDocument()

            is HomeEvent.OnCreatePile -> addPile(event.pileName, event.iconId, event.color)

            is HomeEvent.OnPdfImported -> importPDFIntent(event.uri)
            is HomeEvent.OnImagesImported -> importImagesIntent(event.uris)
            HomeEvent.OnCameraClick -> createCameraUri()
            HomeEvent.OnCameraUriConsumed -> dismissCameraUri()

            HomeEvent.OnErrorDismissed -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun requestBitmapLoad(document: DocumentModel) {
        viewModelScope.launch {
            requestBitmapLoadUseCase(document, 0)
        }
    }

    private fun addPile(pileName: String, iconId: String, color: Long) {
        viewModelScope.launch {
            createPileUseCase(pileName, iconId, color)
        }
    }

    private fun createCameraUri() {
        viewModelScope.launch {
            _state.update {
                it.copy(cameraUri = fileRepository.createTempImageUri())
            }
        }
    }

    private fun dismissCameraUri() = _state.update { it.copy(cameraUri = null) }

    private fun importPDFIntent(uri: Uri) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoadingNewDocument = true) }
                val newDoc = createDocumentUseCase.createFromPdf(uri)
                _navigationEvent.send(newDoc)
            } catch (e: Exception) {
                Napier.e("Error importing PDF", e)
                _state.update {
                    it.copy(errorMessage = UiText.StringResource(R.string.error_importing_pdf))
                }
            } finally {
                _state.update { it.copy(isLoadingNewDocument = false) }
            }
        }
    }

    private fun importImagesIntent(uriList: List<Uri>) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoadingNewDocument = true) }
                val newDoc = createDocumentUseCase.createFromImages(uriList)
                _navigationEvent.send(newDoc)
            } catch (e: Exception) {
                Napier.e("Error importing images", e)
                _state.update {
                    it.copy(errorMessage = UiText.StringResource(R.string.error_importing_images))
                }
            } finally {
                _state.update { it.copy(isLoadingNewDocument = false) }
            }
        }
    }

    private fun removeDraftDocument() {
        viewModelScope.launch {
            backupUnsavedDocument = manageTemporaryDocumentUseCase.deleteForUndo()
        }
    }

    private fun restoreDraftDocument() {
        val backup = backupUnsavedDocument ?: return

        viewModelScope.launch {
            try {
                backupUnsavedDocument = null
                manageTemporaryDocumentUseCase.restoreBackup(backup)
            } catch (e: Exception) {
                Napier.e { "Error restoring backup. Message: ${e.message}" }
                _state.update {
                    it.copy(
                        errorMessage = UiText.StringResource(R.string.error_restoring_draft_document)
                    )
                }
            }
        }
    }

    private fun purgeDraftDocument() {
        val backup = backupUnsavedDocument ?: return
        val documentId = backup.document.id

        backupUnsavedDocument = null

        cleanupScheduler.scheduleDocumentDeletion(documentId)
    }
}