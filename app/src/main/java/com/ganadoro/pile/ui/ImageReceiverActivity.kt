package com.ganadoro.pile.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ganadoro.pile.R
import com.ganadoro.pile.repositories.DocumentModelRepository
import com.ganadoro.pile.ui.composables.LoadingComposable
import com.ganadoro.pile.ui.theme.PileTheme
import org.koin.android.ext.android.inject

class ImageReceiverActivity(
) : ComponentActivity() {
    private val documentModelRepository: DocumentModelRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PileTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingComposable()
                }
            }
        }

        handleIntent()
    }

    private fun handleIntent() {
        val intent = this.intent ?: run { finish(); return }
        if (intent.action != Intent.ACTION_SEND && intent.action != Intent.ACTION_SEND_MULTIPLE) {
            finish(); return
        }

        val imageUris = extractUrisFromIntent(intent)
        if (imageUris.isEmpty()) {
            Toast.makeText(
                this,
                R.string.no_images_found,
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

//        lifecycleScope.launch {
//            val tempDocument = DocumentModel(
//                id = TEMP_DOCUMENT_ID,
//                title = "",
//                creationDate = LocalDate.now(),
//                modificationDate = LocalDate.now(),
//                documentDetails = emptyList(),
//                documentOrganizationIds = emptyList(),
//                documentNote = "",
//                documentPileIds = emptyList()
//            )
//            val tempFile = File(filesDir, tempDocument.id)
//
//
//            try {
//                if (tempFile.exists()) {
//                    tempFile.delete()
//                }
//
//                if (documentModelRepository.getDocumentModelById(tempDocument.id).first() != null) {
//                    documentModelRepository.deleteDocumentModel(tempDocument.id)
//                }
//
//                documentModelRepository.insertDocumentModel(tempDocument)
//
//                createPdfWithImages(
//                    context = applicationContext,
//                    imageUris = imageUris,
//                    outputFile = tempFile
//                )
//
//                navigateToMainApp(newPdfId = tempFile.name)
//
//            } catch (e: IOException) {
//                e.printStackTrace()
//                Toast.makeText(
//                    applicationContext,
//                    R.string.error_creating_pdf,
//                    Toast.LENGTH_LONG
//                ).show()
//                finish()
//            }
//        }
    }

    private fun navigateToMainApp(newPdfId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("NEW_PDF_ID", newPdfId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun extractUrisFromIntent(intent: Intent): List<Uri> {
        val uris = mutableListOf<Uri>()
        if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
            val uriList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)

            }
            uriList?.let { uris.addAll(it) }

        } else if (intent.action == Intent.ACTION_SEND) {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            uri?.let { uris.add(it) }
        }
        return uris
    }
}