package es.pile.features.pileDetail.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pile.DocumentModel
import es.pile.R
import es.pile.core.domain.models.DocumentCoverItem
import es.pile.core.domain.models.DocumentStatusConstants.TEMPORARY
import es.pile.core.domain.repositories.BitmapCacheRepository
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.repositories.FileRepository
import es.pile.core.domain.repositories.PileModelRepository
import es.pile.core.domain.useCases.RequestBitmapLoadUseCase
import es.pile.core.ui.util.UiText
import es.pile.features.home.domain.useCases.CreateDocumentUseCase
import es.pile.features.pileDetail.domain.usecases.DeletePileUseCase
import es.pile.features.pileDetail.domain.usecases.UpdatePileUseCase
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

class PileDetailViewModel(
    private val pileId: String,
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase,
    private val createDocumentUseCase: CreateDocumentUseCase,
    private val updatePileUseCase: UpdatePileUseCase,
    private val deletePileUseCase: DeletePileUseCase,
    private val pileModelRepository: PileModelRepository,
    private val documentModelRepository: DocumentModelRepository,
    private val bitmapCacheRepository: BitmapCacheRepository,
    private val fileRepository: FileRepository
) : ViewModel() {
    private val _state = MutableStateFlow(PileDetailState())
    val state: StateFlow<PileDetailState> = _state.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    private val _navigationEvent = Channel<DocumentModel>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private var pendingImportAction: (() -> Unit)? = null

    init {
        viewModelScope.launch {
            val pileFlow = pileModelRepository.getPileModelById(pileId)
            val allDocumentsFlow = documentModelRepository.documentModels

            combine(
                pileFlow,
                allDocumentsFlow
            ) { pile, allDocuments ->

                val pileDocuments = allDocuments.filter { it.documentPileIds.contains(pileId) && it.documentStatus != TEMPORARY }
                val temporaryDocument = allDocuments.find { it.documentStatus == TEMPORARY }

                val documentCoverItems = pileDocuments.map { documentModel ->
                    DocumentCoverItem(
                        document = documentModel,
                        coverImageCacheKey = bitmapCacheRepository.getImageKey(documentModel, 0)
                    )
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        pile = pile,
                        documentCoverItems = documentCoverItems,
                        temporaryDocument = temporaryDocument
                    )
                }
            }.collect()
        }
    }

    fun handleEvent(event: PileDetailEvent) {
        when (event) {
            is PileDetailEvent.OnImageDisplayed -> requestBitmapLoad(event.document)
            PileDetailEvent.OnDeletePile -> deletePile()
            is PileDetailEvent.OnPileChange -> updatePile(event.name, event.iconId, event.color)

            is PileDetailEvent.OnPdfImported -> {
                if (state.value.temporaryDocument != null) {
                    pendingImportAction = { importPDF(event.uri) }
                    _state.update { it.copy(showDraftWarning = true) }
                } else {
                    importPDF(event.uri)
                }
            }

            is PileDetailEvent.OnImagesImported -> {
                if (state.value.temporaryDocument != null) {
                    pendingImportAction = { importImages(event.uris) }
                    _state.update { it.copy(showDraftWarning = true) }
                } else {
                    importImages(event.uris)
                }
            }

            PileDetailEvent.OnCameraClick -> {
                if (state.value.temporaryDocument != null) {
                    pendingImportAction = { createCameraUri() }
                    _state.update { it.copy(showDraftWarning = true) }
                } else {
                    createCameraUri()
                }
            }

            PileDetailEvent.OnCameraUriConsumed -> dismissCameraUri()

            PileDetailEvent.OnConfirmImport -> {
                _state.update { it.copy(showDraftWarning = false) }
                pendingImportAction?.invoke()
                pendingImportAction = null
            }

            PileDetailEvent.OnDismissDraftWarning -> {
                _state.update { it.copy(showDraftWarning = false) }
                pendingImportAction = null
            }

            PileDetailEvent.OnErrorDismissed -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun requestBitmapLoad(document: DocumentModel) {
        viewModelScope.launch {
            requestBitmapLoadUseCase(document, 0)
        }
    }

    private fun updatePile(name: String, iconId: String, color: Long) {
        val pile = state.value.pile ?: return

        viewModelScope.launch {
            updatePileUseCase(id = pile.id, name = name, iconId = iconId, color = color)
        }
    }

    private fun deletePile() {
        val pile = state.value.pile ?: return

        viewModelScope.launch {
            deletePileUseCase(pile.id)
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

    private fun importPDF(uri: Uri) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoadingNewDocument = true) }
                val newDoc = createDocumentUseCase.createFromPdf(uri, listOf(pileId))
                _navigationEvent.send(newDoc)
            } catch (e: Exception) {
                Napier.e("Error importing PDF from Pile Detail", e)
                _state.update {
                    it.copy(errorMessage = UiText.StringResource(R.string.error_importing_pdf))
                }
            } finally {
                _state.update { it.copy(isLoadingNewDocument = false) }
            }
        }
    }

    private fun importImages(uris: List<Uri>) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoadingNewDocument = true) }
                val newDoc = createDocumentUseCase.createFromImages(uris, listOf(pileId))
                _navigationEvent.send(newDoc)
            } catch (e: Exception) {
                Napier.e("Error importing images from Pile Detail", e)
                _state.update {
                    it.copy(errorMessage = UiText.StringResource(R.string.error_importing_images))
                }
            } finally {
                _state.update { it.copy(isLoadingNewDocument = false) }
            }
        }
    }
}
