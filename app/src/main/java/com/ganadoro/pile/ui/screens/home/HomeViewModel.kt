package com.ganadoro.pile.ui.screens.home

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Quiz
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.DocumentModel
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.models.TEMP_DOCUMENT_ID
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.PileModelRepository
import com.ganadoro.pile.util.createPdfWithImages
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.util.UUID
import kotlin.random.Random

data class HomeUiState(
    var pileModels: List<PileModel> = emptyList(),
    var documentList: List<DocumentModel> = emptyList()
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
                    _uiState.update { it.copy(documentList = documents) }
                }
            }
        }
    }

    fun addPile(pileName: String) {
        viewModelScope.launch {
            val newPile = PileModel( // TODO icono y color seleccionado por el usuario
                id = UUID.randomUUID().toString(),
                name = pileName,
                icon = Icons.Default.Quiz,
                colorNumber = Random.nextInt(31).toLong()
            )

            pileModelRepository.insertPileModel(newPile)
        }
    }

    fun importPDFIntent() {
    }

    fun importFromGalleryIntent(uriList: List<Uri>) {
        val document = DocumentModel(
            id = TEMP_DOCUMENT_ID,
            title = "",
            creationDate = LocalDate.now(),
            modificationDate = LocalDate.now(),
            documentDetails = emptyList(),
            documentOrganizationIds = emptyList(),
            documentNote = "",
            documentPileIds = emptyList()
        )

        val file = File(context.filesDir, document.id)

        viewModelScope.launch {
            if (file.exists() || documentModelRepository.getDocumentModelById(document.id) != null) { // TODO: Gestionar error
//                Napier.d("No se puede guardar el archivo, ya existe con la ruta: ${file.absolutePath}")
//                return@launch

                file.delete()
                documentModelRepository.deleteDocumentModel(document.id)

            }
        }

        viewModelScope.launch { //TODO Add a loading indicator maybe in the repository and navigate to editpdf directly
            launch {
                documentModelRepository.insertDocumentModel(document)
            }
            launch(Dispatchers.IO) {
                createPdfWithImages(
                    context = context,
                    imageUris = uriList,
                    outputFile = file
                )

                Napier.d { "File created: ${file.absolutePath}\nNavigating to edit pdf" }
                launch(Dispatchers.Main) {
                    navigateToEditPDF.invoke(document.id) // Has to run on main thread
                }
            }
        }
    }

    fun deleteUnsavedDocument() { // TODO: Delete
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




