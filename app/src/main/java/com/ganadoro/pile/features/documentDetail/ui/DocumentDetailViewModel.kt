package com.ganadoro.pile.features.documentDetail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.core.domain.models.DocumentDetail
import com.ganadoro.pile.core.domain.repositories.BitmapCacheRepository
import com.ganadoro.pile.core.domain.repositories.DocumentImageRepository
import com.ganadoro.pile.core.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.core.domain.repositories.FileRepository
import com.ganadoro.pile.core.domain.repositories.PileModelRepository
import com.ganadoro.pile.core.domain.useCases.CreatePileUseCase
import com.ganadoro.pile.core.domain.useCases.ManageDocumentPileUseCase
import com.ganadoro.pile.core.domain.useCases.RequestBitmapLoadUseCase
import com.ganadoro.pile.features.documentDetail.domain.helper.DocumentOpener
import com.ganadoro.pile.features.documentDetail.domain.useCases.DeleteDocumentUseCase
import com.ganadoro.pile.features.documentDetail.domain.useCases.UpdateDocumentDetailsUseCase
import com.ganadoro.pile.features.documentDetail.domain.useCases.export.ExportDocumentUseCase
import com.ganadoro.pile.features.documentDetail.domain.useCases.export.GetPdfUriUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DocumentDetailUiState(
    val documentModel: DocumentModel? = null,
    val localDocumentDetails: List<DocumentDetail>? = null,
    val documentPileModels: List<PileModel>? = null,
    val documentImages: List<DocumentImage>? = null,
    val pdfPageNumber: Int? = null,
    val allPiles: List<PileModel>? = null,
    val isDocumentDetailsEditing: Boolean = false
)

sealed interface DocumentDetailEvent {
    data class UpdateText(val id: String, val newName: String, val newValue: String) :
        DocumentDetailEvent

    data class MoveIndex(val fromIndex: Int, val toIndex: Int) : DocumentDetailEvent
    data class MoveId(val fromId: String, val toId: String) : DocumentDetailEvent
    data object Add : DocumentDetailEvent
    data class Delete(val index: Int) : DocumentDetailEvent
    data object Restore : DocumentDetailEvent
    data object ConfirmErasure : DocumentDetailEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentDetailViewModel(
    private val documentId: String,
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase,
    private val deleteDocumentUseCase: DeleteDocumentUseCase,
    private val updateDocumentDetailsUseCase: UpdateDocumentDetailsUseCase,
    private val createPileUseCase: CreatePileUseCase,
    private val manageDocumentPileUseCase: ManageDocumentPileUseCase,
    private val getPdfUriUseCase: GetPdfUriUseCase,
    private val documentOpener: DocumentOpener,
    private val exportDocumentUseCase: ExportDocumentUseCase,
    private val documentModelRepository: DocumentModelRepository,
    private val pileModelRepository: PileModelRepository,
    private val documentImageRepository: DocumentImageRepository,
    private val bitmapCacheRepository: BitmapCacheRepository,
    private val fileRepository: FileRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(DocumentDetailUiState())
    var uiState: StateFlow<DocumentDetailUiState> = _uiState.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    private var recentlyDeletedDetails: List<DocumentDetail> = emptyList()

    init {
        viewModelScope.launch {
            val documentFlow =
                documentModelRepository.getDocumentModelById(documentId).distinctUntilChanged()

            val documentPilesFlow = documentFlow
                .map { it?.documentPileIds ?: emptyList() }
                .distinctUntilChanged()
                .flatMapLatest { ids ->
                    if (ids.isEmpty()) flowOf(emptyList())
                    else pileModelRepository.getPileModelsByIds(ids)
                }

            val imagesFlow = documentFlow
                .map { it?.imageIds ?: emptyList() }
                .distinctUntilChanged()
                .flatMapLatest { ids ->
                    if (ids.isEmpty()) flowOf(emptyList())
                    else {
                        combine(ids.map { documentImageRepository.getDocumentImageById(it) }) { images ->
                            images.filterNotNull()
                        }
                    }
                }

            val pdfPagesFlow = documentFlow
                .distinctUntilChanged()
                .mapLatest { document ->
                    if (document != null && document.isIncomingPdf) {
                        getPdfPageCount(document)
                    } else {
                        null
                    }
                }

            combine(
                documentFlow,
                documentPilesFlow,
                imagesFlow,
                pdfPagesFlow
            ) { document, piles, images, pdfPages ->
                if (document == null) return@combine

                _uiState.update { currentState ->
                    currentState.copy(
                        documentModel = document,
                        documentPileModels = piles,
                        documentImages = images,
                        pdfPageNumber = currentState.pdfPageNumber
                            ?: if (document.isIncomingPdf) pdfPages else null,
                        localDocumentDetails = currentState.localDocumentDetails
                            ?: document.documentDetails,
                        allPiles = pileModelRepository.getAllPileModels()
                    )
                }
            }.collect()
        }
    }

    private suspend fun getPdfPageCount(document: DocumentModel): Int {
        val result = fileRepository.getPageCount(document.id)
        return result.getOrElse { error ->
            Napier.e("Error PDF", error)
            0
        }
    }

    fun requestBitmapLoad(pageNumber: Int) {
        viewModelScope.launch {
            val document = uiState.value.documentModel ?: return@launch
            requestBitmapLoadUseCase(document, pageNumber)
        }
    }

    fun requestImageKey(pageNumber: Int): String {
        val document = uiState.value.documentModel ?: return ""
        return bitmapCacheRepository.getImageKey(document, pageNumber)
    }

    fun updateDocumentNote(newDocumentNote: String) {
        viewModelScope.launch {
            val document = uiState.value.documentModel ?: return@launch
            val updatedDocumentModel = document.copy(documentNote = newDocumentNote)
            documentModelRepository.updateDocumentModel(updatedDocumentModel)
        }
    }

    fun onDocumentDetailEvent(event: DocumentDetailEvent) {
        val currentDetails = uiState.value.localDocumentDetails ?: emptyList()

        val detailsModificationResult = updateDocumentDetailsUseCase(
            currentDetails = currentDetails,
            deletedStack = recentlyDeletedDetails,
            event = event
        )

        _uiState.update { it.copy(localDocumentDetails = detailsModificationResult.updatedDetails) }
        recentlyDeletedDetails = detailsModificationResult.updatedDeletedStack

        viewModelScope.launch {
            val document = uiState.value.documentModel ?: return@launch
            val updatedDocumentModel =
                document.copy(documentDetails = detailsModificationResult.updatedDetails)

            documentModelRepository.updateDocumentModel(updatedDocumentModel)
        }
    }

    fun restoreDocumentDetail() {
        if (recentlyDeletedDetails.isEmpty()) return
        onDocumentDetailEvent(DocumentDetailEvent.Restore)
    }

    fun confirmErasureDocumentDetail() {
        if (recentlyDeletedDetails.isEmpty()) return
        onDocumentDetailEvent(DocumentDetailEvent.ConfirmErasure)
    }

    fun addRemoveDocumentPiles(pileId: String) {
        viewModelScope.launch {
            val document = uiState.value.documentModel ?: return@launch
            manageDocumentPileUseCase(document, pileId)
        }
    }

    fun addPile(pileName: String, iconId: String, color: Long) {
        viewModelScope.launch {
            val pileId = createPileUseCase(pileName, iconId, color)
            addRemoveDocumentPiles(pileId)
        }
    }

    fun openDocumentPDF() { // TODO: Show loading indicator
        viewModelScope.launch {
            val document = uiState.value.documentModel ?: return@launch
            val uri = getPdfUriUseCase(document)
            documentOpener.openPdf(uri)
        }
    }

    fun openShareSheet() {
        viewModelScope.launch {
            val document = uiState.value.documentModel ?: return@launch
            val uri = getPdfUriUseCase(document)
            documentOpener.sharePdf(uri)
        }
    }

    fun downloadPDF() {
        viewModelScope.launch {
            val document = uiState.value.documentModel ?: return@launch
            try {
                exportDocumentUseCase(document)
                // TODO: Show confirmation toast
            } catch (e: Exception) {
                // TODO: Handle exception
                e.printStackTrace()
            }
        }
    }

    fun renameDocument(newDocumentName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val documentModel = uiState.value.documentModel ?: return@launch
            val updatedDocumentModel = documentModel.copy(title = newDocumentName)

            documentModelRepository.updateDocumentModel(updatedDocumentModel)
        }
    }

    fun deleteDocument() {
        viewModelScope.launch {
            val documentModel = uiState.value.documentModel ?: return@launch

            deleteDocumentUseCase(documentModel)
        }
    }

    fun updateIsEditingMode(newMode: Boolean) {
        _uiState.update { it.copy(isDocumentDetailsEditing = newMode) }
    }
}