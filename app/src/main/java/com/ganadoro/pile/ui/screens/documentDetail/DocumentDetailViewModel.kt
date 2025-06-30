package com.ganadoro.pile.ui.screens.documentDetail

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.models.DocumentDetail
import com.ganadoro.pile.models.StringDetail
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.PileModelRepository
import com.ganadoro.pile.util.renderPdfPages
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class DocumentDetailUiState(
    var documentModel: DocumentModel? = null,
    var bitmaps: List<Bitmap> = emptyList(),
    var documentPileModels: List<PileModel>? = null
)

sealed interface DocumentDetailEvent {
    data class UpdateText(val index: Int, val newName: String, val newValue: String) :
        DocumentDetailEvent

    data class Move(val fromIndex: Int, val toIndex: Int) : DocumentDetailEvent
    class Add : DocumentDetailEvent
    data class Delete(val index: Int) : DocumentDetailEvent
}

@SuppressLint("StaticFieldLeak")
class DocumentDetailViewModel(
    private val context: Context, // Safe
    private val documentModelRepository: DocumentModelRepository,
    private val pileModelRepository: PileModelRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(DocumentDetailUiState())
    var uiState: StateFlow<DocumentDetailUiState> = _uiState.asStateFlow()

    fun loadDocument(documentId: String) {
        viewModelScope.launch {
            launch {
                _uiState.value = _uiState.value.copy(
                    documentModel = documentModelRepository.getDocumentModelById(documentId)
                )
            }
            launch {
                val file = File(context.filesDir, documentId)
                _uiState.value = _uiState.value.copy(bitmaps = renderPdfPages(file))
            }
            launch {
                try {
                    _uiState.value = _uiState.value.copy(
                        documentPileModels = _uiState.value.documentModel!!.documentPileIds.map {
                            pileModelRepository.getPileModelById(it)!!
                        }
                    )
                } catch (ex: Exception) {
                    Napier.e("Error loading document piles", ex) // TODO error handling
                    _uiState.value = _uiState.value.copy(documentPileModels = emptyList())

                }
            }
        }
    }

    fun updateDocumentNote(newDocumentNote: String) {
        Napier.d { "DocumentDetailViewModel.updateDocumentNote: $newDocumentNote" }
        viewModelScope.launch {
            val updatedDocumentModel =
                _uiState.value.documentModel?.copy(documentNote = newDocumentNote)
                    ?: return@launch

            _uiState.value = _uiState.value.copy(documentModel = updatedDocumentModel)

            documentModelRepository.updateDocumentModel(updatedDocumentModel)
        }
    }

    fun onEvent(event: DocumentDetailEvent) {
        viewModelScope.launch {
            val newDetails =
                applyEvent(event, _uiState.value.documentModel?.documentDetails ?: return@launch)

            val updatedDocumentModel = _uiState.value.documentModel?.copy(
                documentDetails = newDetails
            )

            if (updatedDocumentModel == null) return@launch

            launch {
                _uiState.update { it.copy(documentModel = updatedDocumentModel) }
            }

            launch {
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
                val oldItem = this[event.index] as StringDetail
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
                removeAt(event.index)
            }
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

    fun renameDocument(newDocumentName: String) {
        viewModelScope.launch {
            val updatedDocumentModel = _uiState.value.documentModel?.copy(title = newDocumentName)
                ?: return@launch

            _uiState.value = _uiState.value.copy(documentModel = updatedDocumentModel)

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