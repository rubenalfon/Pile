package com.ganadoro.pile.ui.screens.documentDetail

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.R
import com.ganadoro.pile.models.DocumentDetail
import com.ganadoro.pile.models.StringDetail
import com.ganadoro.pile.repositories.BitmapCacheRepository
import com.ganadoro.pile.repositories.DocumentImageRepository
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.PileModelRepository
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class DocumentDetailUiState(
    var documentModel: DocumentModel? = null,
    var localDocumentDetails: List<DocumentDetail>? = null,
    var documentPileModels: List<PileModel>? = null,
    var documentImages: List<DocumentImage>? = null,
    var allPiles: List<PileModel>? = null,
)

sealed interface DocumentDetailEvent {
    data class UpdateText(val id: String, val newName: String, val newValue: String) :
        DocumentDetailEvent

    data class MoveIndex(val fromIndex: Int, val toIndex: Int) : DocumentDetailEvent
    data class MoveId(val fromId: String, val toId: String) : DocumentDetailEvent
    data object Add : DocumentDetailEvent
    data class Delete(val index: Int) : DocumentDetailEvent
    data object Restore : DocumentDetailEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@SuppressLint("StaticFieldLeak")
class DocumentDetailViewModel(
    private val documentId: String,
    private val context: Context, // Safe
    private val documentModelRepository: DocumentModelRepository,
    private val pileModelRepository: PileModelRepository,
    private val documentImageRepository: DocumentImageRepository,
    private val bitmapCacheRepository: BitmapCacheRepository
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

            combine(documentFlow, documentPilesFlow, imagesFlow) { document, piles, images ->
                if (document == null) return@combine

                _uiState.update { currentState ->
                    currentState.copy(
                        documentModel = document,
                        documentPileModels = piles,
                        documentImages = images,
                        localDocumentDetails = currentState.localDocumentDetails
                            ?: document.documentDetails,
                        allPiles = pileModelRepository.getAllPileModels()
                    )
                }
            }.collect()
        }
    }

    fun requestBitmapLoad(pageNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val documentModel = uiState.value.documentModel ?: return@launch

            bitmapCacheRepository.loadBitmap(document = documentModel, pageNumber)
        }
    }

    fun updateDocumentNote(newDocumentNote: String) {
        viewModelScope.launch {
            val document = uiState.value.documentModel ?: return@launch

            val updatedDocumentModel = document.copy(documentNote = newDocumentNote)

            withContext(Dispatchers.IO) {
                documentModelRepository.updateDocumentModel(updatedDocumentModel)
            }
        }
    }

    fun onDocumentDetailEvent(event: DocumentDetailEvent) {
        viewModelScope.launch {
            val document = uiState.value.documentModel ?: return@launch

            val newDetails = applyEvent(event, document.documentDetails)

            val updatedDocumentModel = document.copy(documentDetails = newDetails)

            _uiState.update { it.copy(localDocumentDetails = newDetails) }

            withContext(Dispatchers.IO) {
                documentModelRepository.updateDocumentModel(updatedDocumentModel)
            }
        }
    }

    private fun applyEvent(
        event: DocumentDetailEvent,
        currentDetails: List<DocumentDetail>
    ): List<DocumentDetail> = when (event) {
        is DocumentDetailEvent.MoveIndex -> {
            currentDetails.toMutableList().apply {
                add(event.toIndex, removeAt(event.fromIndex))
            }.toList()
        }


        is DocumentDetailEvent.MoveId -> {
            currentDetails.toMutableList().apply {
                val fromIndex = indexOfFirst { it.id == event.fromId }
                val toIndex = indexOfFirst { it.id == event.toId }
                add(toIndex, removeAt(fromIndex))
            }.toList()
        }

        is DocumentDetailEvent.UpdateText -> {
            currentDetails.map { item ->
                if (item.id == event.id && item is StringDetail) {
                    item.copy(name = event.newName, value = event.newValue)
                } else item
            }
        }

        is DocumentDetailEvent.Add -> {
            currentDetails + StringDetail(id = UUID.randomUUID().toString(), name = "", value = "")
        }

        is DocumentDetailEvent.Delete -> {
            val item = currentDetails.getOrNull(event.index)
            if (item != null) recentlyDeletedDetails += item
            currentDetails.filterIndexed { i, _ -> i != event.index }
        }

        is DocumentDetailEvent.Restore -> {
            if (recentlyDeletedDetails.isEmpty()) currentDetails
            else {
                val lastDeleted = recentlyDeletedDetails.first()
                val restoredDocumentDetail = when (lastDeleted) {
                    is StringDetail -> lastDeleted.copy(id = UUID.randomUUID().toString())
                    else -> lastDeleted
                }

                recentlyDeletedDetails -= restoredDocumentDetail
                currentDetails + restoredDocumentDetail
            }
        }
    }

    fun restoreDocumentDetail() {
        if (recentlyDeletedDetails.isEmpty()) return
        onDocumentDetailEvent(DocumentDetailEvent.Restore)
    }

    fun confirmErasureDocumentDetail() {
        viewModelScope.launch {
            if (recentlyDeletedDetails.isEmpty()) return@launch
            recentlyDeletedDetails.toMutableList().apply {
                remove(recentlyDeletedDetails.first())
            }
        }
    }

    fun addRemoveDocumentPiles(pileId: String) {
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

    fun openDocumentPDF() { // TODO: Redo
        if (_uiState.value.documentModel == null) return

        val file = File(context.filesDir, _uiState.value.documentModel?.id!!)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(openIntent)
    }

    fun openShareSheet() { // TODO: Redo
        if (_uiState.value.documentModel == null) return

        val originalFile = File(context.filesDir, _uiState.value.documentModel!!.id)

        val tempFile = File(context.cacheDir, "${_uiState.value.documentModel!!.title}.pdf")
        originalFile.copyTo(tempFile, overwrite = true)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            tempFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, null)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // TODO: Not the best way to do this
        context.startActivity(chooser)
    }

    fun downloadPDF() { // TODO: Redo
        if (_uiState.value.documentModel == null) return

        val originalFile = File(context.filesDir, _uiState.value.documentModel!!.id)

        try {
            val resolver = context.contentResolver

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, _uiState.value.documentModel!!.title)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/")
            }

            val uri =
                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) ?: return

            resolver.openOutputStream(uri)?.use { outputStream ->
                originalFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            try {
                context.startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                ex.printStackTrace()
                Toast.makeText(
                    context,
                    context.getString(R.string.error_saving_document),
                    Toast.LENGTH_SHORT
                ).show()
            }


        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(
                context,
                context.getString(R.string.error_opening_files_explorer),
                Toast.LENGTH_SHORT
            ).show()
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
        viewModelScope.launch(Dispatchers.IO) {
            documentModelRepository.deleteDocumentModel(documentId)
        }
    }
}