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
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.repositories.PileModelRepository
import com.ganadoro.pile.util.createSimplePdf
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
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

    init {
        viewModelScope.launch {
            launch {
                pileModelRepository.pileModels.collect { piles ->
                    _uiState.update { it.copy(pileModels = piles) }
                }
            }
            launch {
                if (documentModelRepository.getAllDocumentModels().isEmpty()) {
                    val newDocument = DocumentModel(
                        id = UUID.randomUUID().toString(),
                        title = "Mi documento",
                        creationDate = LocalDate.of(2025, 4, 1),
                        modificationDate = LocalDate.of(2025, 4, 1),
                        documentDetails = emptyList(),
                        documentOrganizationIds = emptyList(),
                        documentPileIds = emptyList()
                    )

                    documentModelRepository.insertDocumentModel(newDocument)
                }

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
        val fileName = "FILE3.pdf"
        val file = File(context.filesDir, fileName)

        if (file.exists()) {
            Napier.d("Archivo ya existe en: ${file.absolutePath}")
            return
        }

        try {
            val pdfDocument = createSimplePdf(context)

            FileOutputStream(file).use { output ->
                pdfDocument.writeTo(output)
            }

            pdfDocument.close()

            Napier.d("Archivo PDF creado: ${file.absolutePath}")

        } catch (e: Exception) {

            e.printStackTrace()
            Napier.e("Error al escribir el archivo PDF")
        }
    }

    fun importFromGalleryIntent(uriList: List<Uri>) {
        Napier.d { "importFromGalleryIntent" }


// TODO: Mover esto a donde se tenga que abrir el pdf del file
//
//        val file = File(context.filesDir, "FILE3.pdf")
//
//        if (!file.exists()) {
//            Napier.d("Archivo no existe en: ${file.absolutePath}")
//            return
//        }
//
//        val uri = FileProvider.getUriForFile(
//            context,
//            "${context.packageName}.provider",
//            file
//        )
//
//        val intent = Intent(Intent.ACTION_VIEW).apply {
//            setDataAndType(uri, "application/pdf")
//            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
//        }
//
//        context.startActivity(intent)
    }
}