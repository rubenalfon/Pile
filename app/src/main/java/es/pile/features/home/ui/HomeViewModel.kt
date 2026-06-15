package es.pile.features.home.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pile.DocumentModel
import es.pile.R
import es.pile.core.domain.models.DocumentCoverItem
import es.pile.core.domain.repositories.BitmapCacheRepository
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.useCases.CreatePileUseCase
import es.pile.core.domain.useCases.RequestBitmapLoadUseCase
import es.pile.core.ui.util.UiText
import es.pile.features.home.domain.models.TemporaryDocumentBackup
import es.pile.features.home.domain.schedulers.CleanupScheduler
import es.pile.features.home.domain.useCases.CreateDocumentUseCase
import es.pile.features.home.domain.useCases.GetHomeDataUseCase
import es.pile.features.home.domain.useCases.ManageTemporaryDocumentUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val createDocumentUseCase: CreateDocumentUseCase,
    private val manageTemporaryDocumentUseCase: ManageTemporaryDocumentUseCase,
    private val getHomeDataUseCase: GetHomeDataUseCase,
    private val createPileUseCase: CreatePileUseCase,
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase,
    private val cleanupScheduler: CleanupScheduler,
    private val bitmapCacheRepository: BitmapCacheRepository,
    private val fileRepository: FileRepository
) : ViewModel() {
    private var _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    private val _navigationEvent = Channel<DocumentModel>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private var pendingImportAction: (() -> Unit)? = null

    private var backupUnsavedDocument: TemporaryDocumentBackup? = null

    init {
        viewModelScope.launch {
            getHomeDataUseCase().collect { homeData ->
                val documentCoverItems = homeData.documents.map { documentModel ->
                    DocumentCoverItem(
                        document = documentModel,
                        coverImageCacheKey = bitmapCacheRepository.getImageKey(documentModel, 0)
                    )
                }

                _state.update {
                    it.copy(
                        pileModels = homeData.piles,
                        documentCoverItems = documentCoverItems,
                        temporaryDocument = homeData.temporaryDocument,
                        coloredPileIds = homeData.coloredPileIds,
                        isInitialLoading = false
                    )
                }
            }
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

            is HomeEvent.OnPdfImported -> {
                if (state.value.temporaryDocument != null) {
                    pendingImportAction = { importPDFIntent(event.uri) }
                    _state.update { it.copy(showDraftWarning = true) }
                } else {
                    importPDFIntent(event.uri)
                }
            }

            is HomeEvent.OnImagesImported -> {
                if (state.value.temporaryDocument != null) {
                    pendingImportAction = { importImagesIntent(event.uris) }
                    _state.update { it.copy(showDraftWarning = true) }
                } else {
                    importImagesIntent(event.uris)
                }
            }

            HomeEvent.OnCameraClick -> {
                if (state.value.temporaryDocument != null) {
                    pendingImportAction = { createCameraUri() }
                    _state.update { it.copy(showDraftWarning = true) }
                } else {
                    createCameraUri()
                }
            }

            HomeEvent.OnCameraUriConsumed -> dismissCameraUri()

            HomeEvent.OnConfirmImport -> {
                _state.update { it.copy(showDraftWarning = false) }
                pendingImportAction?.invoke()
                pendingImportAction = null
            }

            HomeEvent.OnDismissDraftWarning -> {
                _state.update { it.copy(showDraftWarning = false) }
                pendingImportAction = null
            }

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