package com.ganadoro.pile.util

import android.graphics.ColorMatrix

fun ColorMatrix.createContrastBrightnessMatrix(contrast: Float, brightness: Float): ColorMatrix {
    this.postConcat(
        ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )
    return this
}