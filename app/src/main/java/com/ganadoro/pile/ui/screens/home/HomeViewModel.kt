package com.ganadoro.pile.ui.screens.home

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.models.TEMP_DOCUMENT_ID
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.PileModelRepository
import com.ganadoro.pile.util.FileUtils
import com.ganadoro.pile.util.copyUriFile
import com.ganadoro.pile.util.createPdfWithImages
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.util.UUID

data class HomeUiState(
    var pileModels: List<PileModel> = emptyList(),
    var documentList: List<DocumentModel> = emptyList(),
    var coloredPileIds: List<String> = emptyList()
)

@SuppressLint("StaticFieldLeak")
class HomeViewModel(
    private val context: Context, // Is safe,
    private val pileModelRepository: PileModelRepository,
    private val documentModelRepository: DocumentModelRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(HomeUiState())
    var uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    lateinit var navigateToEditPDF: (pileId: String) -> Unit

    init {
        viewModelScope.launch {
            launch {
                pileModelRepository.pileModels.collect { piles ->
                    _uiState.update { it.copy(pileModels = piles) }
                }
            }
            launch {
                documentModelRepository.documentModels.collect { documents ->
                    val coloredPileIds = documents.flatMap { it.documentPileIds }.distinct()

                    _uiState.update {
                        it.copy(
                            documentList = documents,
                            coloredPileIds = coloredPileIds
                        )
                    }
                }
            }
        }
    }

    fun addPile(pileName: String, iconId: String, color: Int) {
        viewModelScope.launch {
            val newPile = PileModel(
                id = UUID.randomUUID().toString(),
                name = pileName,
                iconId = iconId,
                colorNumber = color.toLong()
            )

            pileModelRepository.insertPileModel(newPile)
        }
    }


    private suspend fun processNewDocument(
        initialTitle: String? = null,
        processFileAction: suspend (destinationFile: File) -> Unit
    ) {
        val tempDocument = DocumentModel(
            id = TEMP_DOCUMENT_ID,
            title = initialTitle ?: "",
            creationDate = LocalDate.now(),
            modificationDate = LocalDate.now(),
            documentDetails = emptyList(),
            documentOrganizationIds = emptyList(),
            documentNote = "",
            documentPileIds = emptyList()
        )
        val tempFile = File(context.filesDir, tempDocument.id)

        try {
            if (tempFile.exists()) {
                tempFile.delete()
            }
            if (documentModelRepository.getDocumentModelById(tempDocument.id) != null) {
                documentModelRepository.deleteDocumentModel(tempDocument.id)
            }

            documentModelRepository.insertDocumentModel(tempDocument)

            withContext(Dispatchers.IO) {
                processFileAction(tempFile)
            }

            navigateToEditPDF.invoke(tempDocument.id)

        } catch (e: Exception) {
            Napier.e("Error procesando el nuevo documento", e) // TODO: Error handling
            try {
                tempFile.delete()
                documentModelRepository.deleteDocumentModel(tempDocument.id)
            } catch (_: Exception) {
            }
        }
    }

    fun importPDFIntent(uri: Uri) {
        viewModelScope.launch {
            processNewDocument(
                initialTitle = FileUtils.getFileNameFromUri(
                    context,
                    uri
                )
            ) { destinationFile ->
                destinationFile.copyUriFile(context, uri)
            }
        }
    }

    fun importFromGalleryIntent(uriList: List<Uri>) {
        viewModelScope.launch {
            processNewDocument { destinationFile ->
                createPdfWithImages(
                    context = context,
                    imageUris = uriList,
                    outputFile = destinationFile
                )
            }
        }
    }

    fun takePhoto(uri: Uri) {
        viewModelScope.launch {
            processNewDocument { destinationFile ->
                createPdfWithImages(
                    context = context,
                    imageUris = listOf(uri),
                    outputFile = destinationFile
                )
            }
        }
    }

    fun deleteUnsavedDocument() { // TODO: safe Delete
        _uiState.update {
            it.copy(documentList = _uiState.value.documentList.filter { document -> document.id != TEMP_DOCUMENT_ID })
        }

//        viewModelScope.launch {
//            launch {
//                documentModelRepository.deleteDocumentModel(TEMP_DOCUMENT_ID)
//            }
//            launch(Dispatchers.IO) {
//                val file = File(context.filesDir, TEMP_DOCUMENT_ID)
//                file.delete()
//            }
//        }
    }
}