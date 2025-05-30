package com.ganadoro.pile.ui.compostables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingComposable() { // TODO: Change to material 3 expressive loading

    val animation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animation.animateTo(1f)
    }
    AnimatedVisibility(
        visible = animation.value != 0f,
        enter = fadeIn(tween(durationMillis = 350, delayMillis = 1000)),
        exit = fadeOut(tween(200))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
//            Text(
//                text = stringResource(Res.string.loading),
//                style = MaterialTheme.typography.bodySmall
//            )
        }
    }
}
