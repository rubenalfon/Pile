package com.ganadoro.pile.ui.controllers

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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
 * @param createTempImageUri Function to create a temporary URI for the camera.
 * @param onPdfSelected Callback when a PDF is selected.
 * @param onImagesSelected Callback when images are selected.
 * @return An ImportActions object with callbacks to trigger document import actions.
 */
@Composable
fun rememberDocumentImportController(
    createTempImageUri: () -> Uri,
    onPdfSelected: (Uri) -> Unit,
    onImagesSelected: (List<Uri>) -> Unit
): ImportActions {
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            onImagesSelected(listOf(tempCameraUri!!))
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


    return remember(createTempImageUri) {
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
                val uri = createTempImageUri()
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            }
        )
    }
}