package es.pile.core.domain.models

import android.graphics.Bitmap

/**
 * Wrapper for [Bitmap] to include app-specific metadata.
 *
 * @property bitmap The original bitmap.
 * @property scaleFactor Custom scale factor used for internal coordinate calculations.
 */
data class ResizedBitmap (
    val bitmap: Bitmap,
    val scaleFactor: Float
)