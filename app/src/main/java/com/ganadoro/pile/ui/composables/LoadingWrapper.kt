package com.ganadoro.pile.ui.composables

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LoadingWrapper(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)
) {
    Crossfade(targetState = isLoading, modifier = modifier) { targetState ->
        when (targetState) {
            true -> LoadingComposable()
            false -> content()
        }
    }
}