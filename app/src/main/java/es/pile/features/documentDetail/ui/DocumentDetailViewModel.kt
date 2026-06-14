package es.pile.features.documentDetail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pile.R
import es.pile.core.domain.models.DocumentDetail
import es.pile.core.domain.repositories.BitmapCacheRepository
import es.pile.core.domain.repositories.DocumentModelRepository
import es.pile.core.domain.useCases.CreatePileUseCase
import es.pile.core.domain.useCases.RequestBitmapLoadUseCase
import es.pile.core.ui.util.UiText
import es.pile.features.documentDetail.domain.helper.DocumentOpener
import es.pile.features.documentDetail.domain.useCases.DeleteDocumentUseCase
import es.pile.features.documentDetail.domain.useCases.GetDocumentDetailDataUseCase
import es.pile.features.documentDetail.domain.useCases.ManageDocumentPileUseCase
import es.pile.features.documentDetail.domain.useCases.UpdateDocumentDetailsUseCase
import es.pile.features.documentDetail.domain.useCases.export.ExportDocumentUseCase
import es.pile.features.documentDetail.domain.useCases.export.GetPdfUriUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@OptIn(ExperimentalCoroutinesApi::class)
class DocumentDetailViewModel(
    private val documentId: String,
    private val requestBitmapLoadUseCase: RequestBitmapLoadUseCase,
    private val deleteDocumentUseCase: DeleteDocumentUseCase,
    private val updateDocumentDetailsUseCase: UpdateDocumentDetailsUseCase,
    private val getDocumentDetailDataUseCase: GetDocumentDetailDataUseCase,
    private val createPileUseCase: CreatePileUseCase,
    private val manageDocumentPileUseCase: ManageDocumentPileUseCase,
    private val getPdfUriUseCase: GetPdfUriUseCase,
    private val documentOpener: DocumentOpener,
    private val exportDocumentUseCase: ExportDocumentUseCase,
    private val documentModelRepository: DocumentModelRepository,
    private val bitmapCacheRepository: BitmapCacheRepository
) : ViewModel() {
    private var _state = MutableStateFlow(DocumentDetailState())
    var state: StateFlow<DocumentDetailState> = _state.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    private var recentlyDeletedDetails: List<DocumentDetail> = emptyList()

    init {
        viewModelScope.launch {
            getDocumentDetailDataUseCase(documentId).collect { data ->
                if (data == null) return@collect

                val document = data.document
                val pdfPages = data.pdfPageCount

                val pageCacheKeys = if (document.isIncomingPdf) {
                    (0 until (pdfPages ?: 0)).map { index ->
                        bitmapCacheRepository.getImageKey(document, index)
                    }
                } else {
                    List(document.imageIds.size) { index ->
                        bitmapCacheRepository.getImageKey(document, index)
                    }
                }

                _state.update { currentState ->
                    currentState.copy(
                        documentModel = document,
                        documentPileModels = data.documentPiles,
                        pageCacheKeys = pageCacheKeys,
                        pdfPageCount = currentState.pdfPageCount ?: pdfPages,
                        localDocumentDetails = currentState.localDocumentDetails ?: document.documentDetails,
                        allPiles = data.allPiles
                    )
                }
            }
        }
    }

    fun handleEvent(event: DocumentDetailEvent) {
        when (event) {
            is DocumentDetailEvent.OnImageDisplayed -> requestBitmapLoad(event.pageNumber)
            is DocumentDetailEvent.OnUpdateEditingMode -> _state.update { it.copy(isDetailsEditing = event.isEditing) }

            is DocumentDetailEvent.OnRenameDocument -> renameDocument(event.newName)
            is DocumentDetailEvent.OnUpdateNote -> updateNote(event.newNote)
            is DocumentDetailEvent.OnUpdateDetails -> handleDetailsActionEvent(event.event)
            is DocumentDetailEvent.OnUpdatePileSelection -> updatePileSelection(event.pileId)
            is DocumentDetailEvent.OnNewPile -> newPile(event.pileName, event.iconId, event.color)
            DocumentDetailEvent.OnDeleteDocument -> deleteDocument()

            DocumentDetailEvent.OnOpenDocument -> openPDF()
            DocumentDetailEvent.OnShare -> openShareSheet()
            DocumentDetailEvent.OnDownload -> downloadPDF()

            DocumentDetailEvent.OnMessageDismissed -> _state.update { it.copy(userMessage = null) }
        }
    }

    private fun requestBitmapLoad(pageNumber: Int) {
        viewModelScope.launch {
            val document = state.value.documentModel ?: return@launch
            requestBitmapLoadUseCase(document, pageNumber)
        }
    }

    private fun renameDocument(newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            documentModelRepository.updateTitle(documentId, newName)
        }
    }

    private fun updateNote(newNote: String) {
        viewModelScope.launch {
            documentModelRepository.updateNote(documentId, newNote)
        }
    }

    private fun handleDetailsActionEvent(event: DetailsActionEvent) {
        val currentState = state.value

        val updatedCollectionDetails = updateDocumentDetailsUseCase(
            currentDetails = currentState.localDocumentDetails ?: emptyList(),
            deletedStack = recentlyDeletedDetails,
            event = event
        )

        _state.update { it.copy(localDocumentDetails = updatedCollectionDetails.updatedDetails) }
        recentlyDeletedDetails = updatedCollectionDetails.updatedDeletedStack

        viewModelScope.launch {
            documentModelRepository.updateDetails(
                id = documentId,
                details = updatedCollectionDetails.updatedDetails
            )
        }
    }

    private fun updatePileSelection(pileId: String) {
        viewModelScope.launch {
            val document = state.value.documentModel ?: return@launch
            manageDocumentPileUseCase(document, pileId)
        }
    }

    private fun newPile(pileName: String, iconId: String, color: Long) {
        viewModelScope.launch {
            val pileId = createPileUseCase(pileName, iconId, color)
            updatePileSelection(pileId)
        }
    }

    private fun deleteDocument() {
        viewModelScope.launch {
            val documentModel = state.value.documentModel ?: return@launch

            deleteDocumentUseCase(documentModel)
        }
    }

    private fun openPDF() {
        viewModelScope.launch {
            val document = state.value.documentModel ?: return@launch

            _state.update { it.copy(isExporting = true) }

            val uri = getPdfUriUseCase(document)

            _state.update { it.copy(isExporting = false) }

            documentOpener.openPdf(uri)
        }
    }

    private fun openShareSheet() {
        viewModelScope.launch {
            val document = state.value.documentModel ?: return@launch
            _state.update { it.copy(isExporting = true) }

            val uri = getPdfUriUseCase(document)

            _state.update { it.copy(isExporting = false) }

            documentOpener.sharePdf(uri)
        }
    }

    private fun downloadPDF() {
        viewModelScope.launch {
            val document = state.value.documentModel ?: return@launch
            try {
                _state.update { it.copy(isExporting = true) }

                exportDocumentUseCase(document)

                _state.update {
                    it.copy(
                        userMessage = UiText.StringResource(R.string.pdf_exported_successfully)
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        userMessage = UiText.StringResource(R.string.error_exporting_pdf)
                    )
                }
                e.printStackTrace()
            } finally {
                _state.update { it.copy(isExporting = false) }
            }
        }
    }
}