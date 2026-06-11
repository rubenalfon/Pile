package es.pile.core.ui.composables

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LoadingWrapper(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(),
    content: @Composable (() -> Unit)
) {
    Crossfade(
        targetState = isLoading,
        modifier = modifier,
        animationSpec = animationSpec
    ) { loading ->
        if (loading) LoadingComposable()
        else content()
    }
}