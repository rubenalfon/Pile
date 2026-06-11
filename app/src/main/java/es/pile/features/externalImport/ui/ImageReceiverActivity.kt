package es.pile.features.externalImport.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pile.R
import es.pile.core.activities.MainActivity
import es.pile.core.ui.composables.LoadingComposable
import es.pile.core.ui.theme.PileTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class ImageReceiverActivity : ComponentActivity() {
    private val viewModel: ImportViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(state.successDocumentId) {
                state.successDocumentId?.let { id ->
                    navigateToMainApp(id, state.isPdf)
                }
            }

            LaunchedEffect(state.errorMessage) {
                state.errorMessage?.let {
                    Toast.makeText(this@ImageReceiverActivity, R.string.error_importing_images, Toast.LENGTH_LONG).show()
                    finish()
                }
            }

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
            Toast.makeText(this, R.string.no_images_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.handleEvent(ImportEvent.OnImportImages(imageUris))
    }

    private fun navigateToMainApp(documentId: String, isPdf: Boolean) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_NEW_DOCUMENT_ID, documentId)
            putExtra(MainActivity.EXTRA_IS_PDF, isPdf)
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
                intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
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
