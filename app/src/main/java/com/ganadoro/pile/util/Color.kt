package com.ganadoro.pile.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.sasikanth.material.color.utilities.hct.Hct

fun Color.getContainerColor(isSystemInDarkTheme: Boolean): Color {
    val hctColor = Hct.fromInt(this.toArgb())

    if (isSystemInDarkTheme) {
        hctColor.setTone(30.0)
    } else {
        hctColor.setTone(90.0)
    }
    return Color(hctColor.toInt())
}

fun Color.getOnContainerColor(isSystemInDarkTheme: Boolean): Color {
    val hctColor = Hct.fromInt(this.toArgb())

    if (isSystemInDarkTheme) {
        hctColor.setTone(90.0)
    } else {
        hctColor.setTone(30.0)
    }
    return Color(hctColor.toInt())
}


