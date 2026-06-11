package es.pile.features.editDocument.domain.models

import com.tanishranjan.cropkit.CropController

/**
 * Wrapper for [CropController] to include screen-specific metadata.
 *
 * @property cropController The original controller from the library.
 * @property scaleFactor Custom scale factor used for internal coordinate calculations.
 */
data class ExtendedCropController (
    val cropController: CropController,
    val scaleFactor: Float
)