package com.ganadoro.pile.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.LayoutDirection

@Stable
fun PaddingValues.horizontalPaddingValues(layoutDirection: LayoutDirection): PaddingValues {
    return PaddingValues(
        start = this.calculateStartPadding(layoutDirection),
        end = this.calculateEndPadding(layoutDirection)
    )
}