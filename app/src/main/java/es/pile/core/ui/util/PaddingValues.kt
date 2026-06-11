package es.pile.core.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.LayoutDirection

/**
 * Extension function to calculate the horizontal padding values based on the layout direction, ignoring the vertical ones.
 *
 */
@Stable
fun PaddingValues.horizontalPaddingValues(layoutDirection: LayoutDirection): PaddingValues {
    return PaddingValues(
        start = this.calculateStartPadding(layoutDirection),
        end = this.calculateEndPadding(layoutDirection)
    )
}