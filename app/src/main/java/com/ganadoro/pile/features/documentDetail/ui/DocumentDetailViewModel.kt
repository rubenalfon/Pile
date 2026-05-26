package com.ganadoro.pile.features.documentDetail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.R
import com.ganadoro.pile.core.domain.models.DocumentDetail
import com.ganadoro.pile.core.domain.repositories.BitmapCacheRepository
import com.ganadoro.pile.core.domain.repositories.DocumentModelRepository
import com.ganadoro.pile.core.domain.repositories.FileRepository
import com.ganadoro.pile.core.domain.repositories.PileModelRepository
import com.ganadoro.pile.core.domain.useCases.CreatePileUseCase
import com.ganadoro.pile.core.domain.useCases.RequestBitmapLoadUseCase
import com.ganadoro.pile.core.ui.util.UiText
import com.ganadoro.pile.features.documentDetail.domain.helper.DocumentOpener
import com.ganadoro.pile.features.documentDetail.domain.useCases.DeleteDocumentUseCase
import com.ganadoro.pile.features.documentDetail.domain.useCases.ManageDocumentPileUseCase
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
    private val bitmapCacheRepository: BitmapCacheRepository,
    private val fileRepository: FileRepository
) : ViewModel() {
    private var _state = MutableStateFlow(DocumentDetailState())
    var state: StateFlow<DocumentDetailState> = _state.asStateFlow()

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
                pdfPagesFlow
            ) { document, piles, pdfPages ->
                if (document == null) return@combine

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
                        documentPileModels = piles,
                        pageCacheKeys = pageCacheKeys,
                        pdfPageCount = currentState.pdfPageCount
                            ?: if (document.isIncomingPdf) pdfPages else null,
                        localDocumentDetails = currentState.localDocumentDetails,
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
            val documentModel = state.value.documentModel ?: return@launch
            val updatedDocumentModel = documentModel.copy(title = newName)

            documentModelRepository.updateDocumentModel(updatedDocumentModel)
        }
    }

    private fun updateNote(newNote: String) {
        viewModelScope.launch {
            val document = state.value.documentModel ?: return@launch
            val updatedDocumentModel = document.copy(documentNote = newNote)
            documentModelRepository.updateDocumentModel(updatedDocumentModel)
        }
    }

    private fun handleDetailsActionEvent(event: DetailsActionEvent) {
        val currentState = state.value

        val updatedCollectionDetails = updateDocumentDetailsUseCase(
            currentDetails = currentState.localDocumentDetails,
            deletedStack = recentlyDeletedDetails,
            event = event
        )

        _state.update { it.copy(localDocumentDetails = updatedCollectionDetails.updatedDetails) }
        recentlyDeletedDetails = updatedCollectionDetails.updatedDeletedStack

        val document = currentState.documentModel ?: return
        val updatedDocumentModel =
            document.copy(documentDetails = updatedCollectionDetails.updatedDetails)

        viewModelScope.launch {
            documentModelRepository.updateDocumentModel(updatedDocumentModel)
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