package com.ganadoro.pile.ui.screens.documentDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.domain.models.DocumentDetail
import com.ganadoro.pile.domain.usecase.DeleteDocumentUseCase
import com.ganadoro.pile.domain.usecase.RequestBitmapLoadUseCase
import com.ganadoro.pile.domain.usecase.UpdateDocumentDetailsUseCase
import com.ganadoro.pile.repositories.BitmapCacheRepository
import com.ganadoro.pile.repositories.DocumentImageRepository
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.FileRepository
import com.ganadoro.pile.repositories.PileModelRepository
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
import kotlinx.coroutines.withContext

data class DocumentDetailUiState(
    val documentModel: DocumentModel? = null,
    val localDocumentDetails: List<DocumentDetail>? = null,
    val documentPileModels: List<PileModel>? = null,
    val documentImages: List<DocumentImage>? = null,
    val pdfPageNumber: Int? = null,
    val allPiles: List<PileModel>? = null,
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
            deletedStack = emptyList(),
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

    fun addRemoveDocumentPiles(pileId: String) { // TODO: UseCase
        viewModelScope.launch {
            val documentModel = uiState.value.documentModel ?: return@launch
            val documentPiles = documentModel.documentPileIds

            val updatedDocumentPiles = documentPiles.toMutableList().apply {
                if (contains(pileId)) {
                    remove(pileId)
                } else {
                    add(pileId)
                }
            }

            withContext(Dispatchers.IO) {
                documentModelRepository.updateDocumentModel(
                    documentModel.copy(
                        documentPileIds = updatedDocumentPiles
                    )
                )
            }
        }
    }

//    /** // TODO: UseCase
//     * Creates or replaces a pdf containing all the document images.
//     */
//    private suspend fun createOrReplacePDF(): Boolean {
//        viewModelScope.launch(Dispatchers.IO) {
//            val documentModel = uiState.value.documentModel ?: return@launch
//            val folderFile = File(context.filesDir, documentModel.id)
//            val pdfFile = File(folderFile, "${documentModel.id}.pdf")
//
//            val finalBitmapList = uiState.value.originalBitmaps.mapIndexed { index, original ->
//                uiState.value.cropEditedBitmaps[index]
//                    ?: uiState.value.modifiedBitmaps[index]
//                    ?: original
//            }
//
//            try {
//                createPdfWithImages(
//                    bitmaps = finalBitmapList, outputFile = documentFile
//                )
//            } catch (ex: Exception) {
//                Napier.e { "EditPDFViewModel.updateDocumentPDF: ${ex.message}" }
//            }
//        }
//
//
//        return false
//    }

    fun openDocumentPDF() { // TODO: Redo // TODO: UseCase
//        if (!isDocumentPDFUpdated()) {
////            createOrReplacePDF()
//        }
//
//        val documentFolder = File(context.filesDir, documentId)
//        val pdfFile = File(documentFolder, "$documentId.pdf")
//
//        val uri = FileProvider.getUriForFile(
//            context,
//            "${context.packageName}.provider",
//            pdfFile
//        )
//
//        val openIntent = Intent(Intent.ACTION_VIEW).apply {
//            setDataAndType(uri, "application/pdf")
//            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
//        }
//
//        context.startActivity(openIntent)
    }

    fun openShareSheet() { // TODO: Redo // TODO: UseCase
//        if (_uiState.value.documentModel == null) return
//
//        val originalFile = File(context.filesDir, _uiState.value.documentModel!!.id)
//
//        val tempFile = File(context.cacheDir, "${_uiState.value.documentModel!!.title}.pdf")
//        originalFile.copyTo(tempFile, overwrite = true)
//
//        val uri = FileProvider.getUriForFile(
//            context,
//            "${context.packageName}.provider",
//            tempFile
//        )
//
//        val shareIntent = Intent(Intent.ACTION_SEND).apply {
//            type = "application/pdf"
//            putExtra(Intent.EXTRA_STREAM, uri)
//            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        }
//
//        val chooser = Intent.createChooser(shareIntent, null)
//        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // TODO: Not the best way to do this
//        context.startActivity(chooser)
    }

    fun downloadPDF() { // TODO: Redo // TODO: UseCase
//        if (_uiState.value.documentModel == null) return
//
//        val originalFile = File(context.filesDir, _uiState.value.documentModel!!.id)
//
//        try {
//            val resolver = context.contentResolver
//
//            val contentValues = ContentValues().apply {
//                put(MediaStore.MediaColumns.DISPLAY_NAME, _uiState.value.documentModel!!.title)
//                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
//                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/")
//            }
//
//            val uri =
//                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) ?: return
//
//            resolver.openOutputStream(uri)?.use { outputStream ->
//                originalFile.inputStream().use { inputStream ->
//                    inputStream.copyTo(outputStream)
//                }
//            }
//
//            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//
//            try {
//                context.startActivity(intent)
//            } catch (ex: ActivityNotFoundException) {
//                ex.printStackTrace()
//                Toast.makeText(
//                    context,
//                    context.getString(R.string.error_saving_document),
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//
//
//        } catch (ex: Exception) {
//            ex.printStackTrace()
//            Toast.makeText(
//                context,
//                context.getString(R.string.error_opening_files_explorer),
//                Toast.LENGTH_SHORT
//            ).show()
//        }
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
}