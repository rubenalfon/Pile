package com.ganadoro.pile.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CircleNotifications
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Work
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.ganadoro.pile.models.DocumentModel
import com.ganadoro.pile.models.PileModel
import com.ganadoro.pile.util.createSimplePdf
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

data class HomeUiState(
    var pileModels: List<PileModel> = emptyList(),
    var documentList: List<DocumentModel> = emptyList()
)

class HomeViewModel(
    private val context: Context // TODO: Revisar
) : ViewModel() {
    private var _uiState = MutableStateFlow(HomeUiState())
    var uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        Napier.d { "HomeViewModel init" }
        _uiState.value.pileModels = listOf(
            PileModel(name = "Home", icon = Icons.Default.Home, colorNumber = 0),
            PileModel(name = "Work", icon = Icons.Default.AddRoad, colorNumber = 1),
            PileModel(name = "Church", icon = Icons.Default.CircleNotifications, colorNumber = 23),
            PileModel(name = "Legal", icon = Icons.Default.Work, colorNumber = 7),
            PileModel(name = "Vehicles", icon = Icons.Default.DirectionsCar, colorNumber = 29),
            PileModel(name = "Nightime", icon = Icons.Default.Bedtime)

        )

        _uiState.value.documentList = listOf(
            DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                documentRoute = ""
            ),
            DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 1),
                documentRoute = ""
            ), DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 1),
                documentRoute = ""
            ),
            DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 1),
                documentRoute = ""
            ), DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 1),
                documentRoute = ""
            ),
            DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 1),
                documentRoute = ""
            ), DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 1),
                documentRoute = ""
            ),
            DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 1),
                documentRoute = ""
            ), DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 1),
                documentRoute = ""
            ),
            DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 5),
                documentRoute = ""
            ), DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 5),
                documentRoute = ""
            ),
            DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                date = LocalDate.of(2025, 4, 5),
                documentRoute = ""
            ),
            DocumentModel(
                id = java.util.UUID.randomUUID(),
                title = "Mi documento",
                documentRoute = ""
            )
        )

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

        val file = File(context.filesDir, "FILE3.pdf")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
    }
}