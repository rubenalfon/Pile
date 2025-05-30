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

        _uiState.value.documentList = listOf(
            DocumentModel(
                id = UUID.randomUUID().toString(),
                title = "Mi documento",
                creationDate = LocalDate.of(2025, 4, 1),
                modificationDate = LocalDate.of(2025, 4, 1),
                documentDetails = emptyList(),
                documentOrganizationIds = emptyList(),
                documentPileIds = emptyList()
            ),
        )
    }

    fun addPile(pileName: String) {
        viewModelScope.launch {
            val newPile = PileModel( // TODO
                id = UUID.randomUUID().toString(),
                name = pileName,
                icon = Icons.Default.Quiz,
                colorNumber = null
            )

            pileModelRepository.insertPileModel(newPile)
            Napier.d("Pile added: $pileName")

            _uiState.value =
                _uiState.value.copy(pileModels = pileModelRepository.getAllPileModels())
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
            documentPileIds = emptyList()
        )

        val file = File(context.filesDir, document.id)

//        if (file.exists()) { // TODO: Gestionar error
//            Napier.d("No se puede guardar el archivo, ya existe con la ruta: ${file.absolutePath}")
//            return
//        }

        viewModelScope.launch { // Add a loading indicator maybe in the repository and navigate to editpdf directly
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
}




