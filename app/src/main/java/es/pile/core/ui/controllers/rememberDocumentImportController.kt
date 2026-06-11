package es.pile.core.ui.controllers

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

/**
 * Data class holding the callbacks to trigger document import actions.
 * Passed down to UI components like FABs or Menus.
 */
data class ImportActions(
    val launchPdfPicker: () -> Unit,
    val launchGallery: () -> Unit,
    val launchCamera: () -> Unit
)

/**
 * A headless composable controller that manages ActivityResultLaunchers
 * for document imports.
 *
 * @param cameraUri URI for the camera.
 * @param onUriConsumed Callback when the URI is consumed.
 * @param onCameraClick Callback when the camera button is clicked.
 * @param onPdfSelected Callback when a PDF is selected.
 * @param onImagesSelected Callback when images are selected.
 * @return An ImportActions object with callbacks to trigger document import actions.
 */
@Composable
fun rememberDocumentImportController(
    cameraUri: Uri? = null,
    onUriConsumed: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    onPdfSelected: (Uri) -> Unit = {},
    onImagesSelected: (List<Uri>) -> Unit = {}
): ImportActions {
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraUri != null) {
            onImagesSelected(listOf(cameraUri))
        }
        onUriConsumed()
    }

    // REACCIÓN: Cuando el ViewModel genera el URI, lanzamos la cámara automáticamente
    LaunchedEffect(cameraUri) {
        cameraUri?.let {
            cameraLauncher.launch(it)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImagesSelected(uris)
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let(onPdfSelected)
    }


    return remember {
        ImportActions(
            launchPdfPicker = {
                pdfLauncher.launch(arrayOf("application/pdf"))
            },
            launchGallery = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            launchCamera = {
                onCameraClick()
            }
        )
    }
}
