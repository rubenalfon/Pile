package com.ganadoro.pile.ui.screens.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Quiz
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganadoro.pile.PileModel
import com.ganadoro.pile.models.DocumentModelLocal
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
    var documentList: List<DocumentModelLocal> = emptyList()
)

@SuppressLint("StaticFieldLeak")
class HomeViewModel(
    private val context: Context, // Is safe,
    private val pileModelRepository: PileModelRepository
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
        }

        _uiState.value.documentList = listOf(
            DocumentModelLocal(
                id = UUID.randomUUID(),
                title = "Mi documento",
                documentRoute = ""
            ),
            DocumentModelLocal(
                id = UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 1),
                documentRoute = ""
            ), DocumentModelLocal(
                id = UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 1),
                documentRoute = ""
            ),
            DocumentModelLocal(
                id = UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 1),
                documentRoute = ""
            ), DocumentModelLocal(
                id = UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 1),
                documentRoute = ""
            ),
            DocumentModelLocal(
                id = UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 1),
                documentRoute = ""
            ), DocumentModelLocal(
                id = UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 1),
                documentRoute = ""
            ),
            DocumentModelLocal(
                id = UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 1),
                documentRoute = ""
            ), DocumentModelLocal(
                id = UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 1),
                documentRoute = ""
            ),
            DocumentModelLocal(
                id = UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 5),
                documentRoute = ""
            ), DocumentModelLocal(
                id = UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 5),
                documentRoute = ""
            ),
            DocumentModelLocal(
                id = UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 5),
                documentRoute = ""
            ),
            DocumentModelLocal(
                id = UUID.randomUUID(),
                title = "Mi documento",
                documentRoute = ""
            )
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

    fun importFromGalleryIntent() {
        Napier.d { "importFromGalleryIntent" }
// TODO: Mover esto a donde se tenga que abrir el pdf del file
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