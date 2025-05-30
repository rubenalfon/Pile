package com.ganadoro.pile.ui.compostables

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ganadoro.pile.ui.compostables.LoadingComposable

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