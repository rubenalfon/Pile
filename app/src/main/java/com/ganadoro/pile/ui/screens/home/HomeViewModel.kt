package com.ganadoro.pile.ui.screens.home

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentImage
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.models.TEMP_DOCUMENT_ID
import com.ganadoro.pile.repositories.BitmapCacheRepository
import com.ganadoro.pile.repositories.DocumentImageRepository
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.PileModelRepository
import com.ganadoro.pile.util.saveResizedImagesToInternalStorage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.util.UUID

data class HomeUiState(
    var pileModels: List<PileModel>? = null,
    var documentList: List<DocumentModel>? = null,
    var coloredPileIds: List<String>? = null,
)

@SuppressLint("StaticFieldLeak")
class HomeViewModel(
    private val context: Context, // Is safe,
    private val pileModelRepository: PileModelRepository,
    private val documentModelRepository: DocumentModelRepository,
    private val bitmapCacheRepository: BitmapCacheRepository,
    private val documentImageRepository: DocumentImageRepository
) : ViewModel() {
    private var _uiState = MutableStateFlow(HomeUiState())
    var uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val bitmapCache = bitmapCacheRepository.bitmapCache

    lateinit var navigateToEditPDF: (pileId: String) -> Unit

    private var unsavedDeletedDocument: DocumentModel? = null

    init {
        viewModelScope.launch {

            val pileModelsFlow = pileModelRepository.pileModels

            pileModelsFlow.combine(documentModelRepository.documentModels) { piles, documents ->
                val coloredPileIds = documents.flatMap { it.documentPileIds }.distinct()

                HomeUiState(
                    pileModels = piles,
                    documentList = documents,
                    coloredPileIds = coloredPileIds
                )
            }.collect { finalState ->
                _uiState.update {
                    finalState
                }
            }
        }
    }

    fun requestBitmapLoad(documentId: String) {
        bitmapCacheRepository.ensureBitmapIsLoaded(documentId)
    }

    fun addPile(pileName: String, iconId: String, color: Long) {
        viewModelScope.launch {
            val newPile = PileModel(
                id = UUID.randomUUID().toString(),
                name = pileName,
                iconId = iconId,
                colorNumber = color
            )

            pileModelRepository.insertPileModel(newPile)
        }
    }


    private suspend fun processNewDocument(
        initialTitle: String? = null,
        processFileAction: suspend (document: DocumentModel) -> Unit
    ) {
        val documentId = UUID.randomUUID().toString()
        val document = DocumentModel(
            id = TEMP_DOCUMENT_ID + documentId,
            title = initialTitle ?: "",
            imageIds = emptyList(),
            creationDate = LocalDate.now(),
            modificationDate = LocalDate.now(),
            documentDetails = emptyList(),
            documentOrganizationIds = emptyList(),
            documentNote = "",
            documentPileIds = emptyList()
        )
        try {
            documentModelRepository.insertDocumentModel(document)

            withContext(Dispatchers.IO) {
                processFileAction(document)
            }

            navigateToEditPDF.invoke(document.id)

        } catch (e: Exception) {
            Napier.e("Error procesando el nuevo documento", e) // TODO: Error handling
            try {
                val file = File(context.filesDir, documentId)
                file.deleteRecursively()

                documentModelRepository.deleteDocumentModel(document.id)
            } catch (_: Exception) {
            }
        }
    }

    fun importPDFIntent(uri: Uri) {
//        viewModelScope.launch {
//            processNewDocument(
//                initialTitle = FileUtils.getFileNameFromUri(
//                    context,
//                    uri
//                )
//            ) { destinationFile ->
//                destinationFile.copyUriFile(context, uri)
//            }
//        }
    }

    fun importFromGalleryIntent(uriList: List<Uri>) {
        viewModelScope.launch (Dispatchers.IO) {
            processNewDocument { document ->
                val imageFileList = saveResizedImagesToInternalStorage(
                    context = context,
                    uris = uriList,
                    documentId = document.id.removePrefix(TEMP_DOCUMENT_ID)
                )

                for (image in imageFileList) {
                    val documentImage = DocumentImage(
                        id = image.name,
                        crop = null,
                        filter = 0,
                        rotation = 0
                    )

                    documentImageRepository.insertDocumentImage(documentImage)
                }

                documentModelRepository.updateDocumentModel(
                    document.copy(
                        imageIds = imageFileList.map { it.name }
                    )
                )
            }
        }
    }

    fun takePhoto(uri: Uri) {
//        viewModelScope.launch {
//            processNewDocument { destinationFile ->
//                createPdfWithImages(
//                    context = context,
//                    imageUris = listOf(uri),
//                    outputFile = destinationFile
//                )
//            }
//        }
    }

    fun deleteUnsavedDocument() {
        unsavedDeletedDocument = _uiState.value.documentList?.first { it.id == TEMP_DOCUMENT_ID }

        _uiState.update {
            it.copy(documentList = _uiState.value.documentList?.filter { document -> document != unsavedDeletedDocument })
        }

        viewModelScope.launch {
            documentModelRepository.deleteDocumentModel(TEMP_DOCUMENT_ID)
        }
    }

    fun restoreUnsavedDeletedDocument() {
        if (unsavedDeletedDocument == null) return

        _uiState.update {
            it.copy(documentList = _uiState.value.documentList?.plus(unsavedDeletedDocument!!))
        }

        viewModelScope.launch {
            documentModelRepository.insertDocumentModel(unsavedDeletedDocument!!)
        }
    }

    fun confirmErasureUnsavedDeletedDocument() {
        unsavedDeletedDocument = null

        viewModelScope.launch(Dispatchers.IO) {
            val file = File(context.filesDir, TEMP_DOCUMENT_ID)
            file.delete()
        }
    }
}