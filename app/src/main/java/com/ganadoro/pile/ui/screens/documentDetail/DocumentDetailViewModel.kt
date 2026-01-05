package com.ganadoro.pile.ui.screens.documentDetail

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.R
import com.ganadoro.pile.models.DocumentDetail
import com.ganadoro.pile.models.StringDetail
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.PileModelRepository
import com.ganadoro.pile.util.renderAllPdfPages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class DocumentDetailUiState(
    var documentModel: DocumentModel? = null,
    var documentDetails: List<DocumentDetail>? = null,
    var bitmaps: List<Bitmap> = emptyList(),
    var documentPileModels: List<PileModel>? = null
)

sealed interface DocumentDetailEvent {
    data class UpdateText(val index: Int, val newName: String, val newValue: String) :
        DocumentDetailEvent

    data class Move(val fromIndex: Int, val toIndex: Int) : DocumentDetailEvent
    data object Add : DocumentDetailEvent
    data class Delete(val index: Int) : DocumentDetailEvent
    data object Restore : DocumentDetailEvent
}

@SuppressLint("StaticFieldLeak")
class DocumentDetailViewModel(
    private val context: Context, // Safe
    private val documentModelRepository: DocumentModelRepository,
    private val pileModelRepository: PileModelRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(DocumentDetailUiState())
    var uiState: StateFlow<DocumentDetailUiState> = _uiState.asStateFlow()

    private var recentlyDeletedDetails: List<DocumentDetail> = emptyList()


    fun loadDocument(documentId: String) {
        viewModelScope.launch {
            val documentModelFlow =
                documentModelRepository.getDocumentModelById(documentId)

            _uiState.update {
                it.copy(documentDetails = documentModelFlow.first()?.documentDetails)
            }

            documentModelFlow.combine(pileModelRepository.pileModels) { documentModel, allPiles ->
                if (documentModel == null) {
                    return@combine DocumentDetailUiState()
                }

                val bitmapsDeferred = async(Dispatchers.IO) {
                    val file = File(context.filesDir, documentId)
                    renderAllPdfPages(file)
                }

                val documentPileModels =
                    allPiles.filter { pile -> pile.id in documentModel.documentPileIds }

                DocumentDetailUiState(
                    documentModel = documentModel,
                    bitmaps = bitmapsDeferred.await(),
                    documentPileModels = documentPileModels
                )
            }.collect { finalState ->
                _uiState.update {
                    it.copy(
                        documentModel = finalState.documentModel,
                        bitmaps = finalState.bitmaps,
                        documentPileModels = finalState.documentPileModels
                    )
                }
            }
        }
    }

    fun updateDocumentNote(newDocumentNote: String) {
        viewModelScope.launch {
            val updatedDocumentModel =
                _uiState.value.documentModel?.copy(documentNote = newDocumentNote)
                    ?: return@launch

            documentModelRepository.updateDocumentModel(updatedDocumentModel)
        }
    }

    fun onDocumentDetailEvent(event: DocumentDetailEvent) {
        viewModelScope.launch {
            val newDetails =
                applyEvent(event, _uiState.value.documentDetails ?: return@launch)

            launch {
                _uiState.update { it.copy(documentDetails = newDetails) }
            }

            launch {
                val updatedDocumentModel = _uiState.value.documentModel?.copy(
                    documentDetails = newDetails
                )
                if (updatedDocumentModel == null) return@launch

                documentModelRepository.updateDocumentModel(updatedDocumentModel)
            }
        }
    }

    private fun applyEvent(
        event: DocumentDetailEvent,
        currentDetails: List<DocumentDetail>
    ) = when (event) {
        is DocumentDetailEvent.Move -> {
            currentDetails.toMutableList().apply {
                add(event.toIndex, removeAt(event.fromIndex))
            }
        }

        is DocumentDetailEvent.UpdateText -> {
            currentDetails.toMutableList().apply {
                if (event.index < 0 || event.index >= this.size) return@apply
                val oldItem = this[event.index] as? StringDetail ?: return@apply
                this[event.index] = oldItem.copy(name = event.newName, value = event.newValue)
            }
        }

        is DocumentDetailEvent.Add -> {
            currentDetails.toMutableList().apply {
                add(StringDetail(id = UUID.randomUUID().toString(), name = "", value = ""))
            }
        }

        is DocumentDetailEvent.Delete -> {
            currentDetails.toMutableList().apply {
                if (event.index < 0 || event.index >= this.size) return@apply
                recentlyDeletedDetails += this[event.index]

                remove(recentlyDeletedDetails.last())
            }
        }

        is DocumentDetailEvent.Restore -> {
            currentDetails.toMutableList().apply {
                if (recentlyDeletedDetails.isEmpty()) return@apply
                val documentDetail = recentlyDeletedDetails.first()
                add(recentlyDeletedDetails.first())
                recentlyDeletedDetails -= documentDetail
            }
        }
    }

    fun restoreDocumentDetail() {
        if (recentlyDeletedDetails.isEmpty()) return
        onDocumentDetailEvent(DocumentDetailEvent.Restore)
    }

    fun confirmErasureDocumentDetail() {
        if (recentlyDeletedDetails.isEmpty()) return
        recentlyDeletedDetails.toMutableList().apply {
            remove(recentlyDeletedDetails.first())
        }
    }

    fun openDocumentPDF() {
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

    fun openShareSheet() {
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

    fun downloadPDF() {
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
        viewModelScope.launch {
            val updatedDocumentModel = _uiState.value.documentModel?.copy(title = newDocumentName)
                ?: return@launch

            documentModelRepository.updateDocumentModel(updatedDocumentModel)
        }
    }

    fun deleteDocument() {
        viewModelScope.launch {
            val documentId = _uiState.value.documentModel?.id ?: return@launch
            documentModelRepository.deleteDocumentModel(documentId)
        }
    }
}