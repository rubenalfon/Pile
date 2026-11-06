package es.pile.core.data.util

import android.graphics.BitmapFactory

/**
 * Calculates the optimal sample size (inSampleSize) for decoding a bitmap given a maximum dimension.
 *
 * @param maxSize The maximum width or height of the target bitmap in pixels.
 * @return The optimal power-of-two sample size.
 */
fun BitmapFactory.Options.calculateInSampleSize(maxSize: Int): Int {
    val height = outHeight
    val width = outWidth
    var inSampleSize = 1

    if (height > maxSize || width > maxSize) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while (halfHeight / inSampleSize >= maxSize || halfWidth / inSampleSize >= maxSize) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}
