package es.pile.core.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun SwipeBox(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    contentPaddingValues: PaddingValues,
    backgroundContent: (@Composable (state: SwipeToDismissBoxState, offset: Dp) -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val swipeState = rememberSwipeToDismissBoxState()
    val density = LocalDensity.current
    var itemOffset by remember { mutableStateOf(0.dp) }

    SwipeToDismissBox(
        modifier = modifier.animateContentSize(),
        state = swipeState,
        gesturesEnabled = enabled,
        backgroundContent = {
            if (backgroundContent != null) {
                backgroundContent(swipeState, itemOffset)
            } else {
                SwipeBoxDefaultBackgroundContent(
                    swipeState = swipeState,
                    itemOffset = itemOffset,
                    contentPaddingValues = contentPaddingValues
                )
            }
        },
        onDismiss = {
            onDelete()
        }
    ) {
        Box(
            Modifier
                .padding(contentPaddingValues)
                .onGloballyPositioned { coordinates ->
                    val positionInRoot = coordinates.positionInRoot()
                    itemOffset = with(density) { positionInRoot.x.toDp() }
                }
        ) {
            content()
        }
    }
}


@Composable
fun SwipeBoxDefaultBackgroundContent(
    swipeState: SwipeToDismissBoxState,
    icon: ImageVector? = Icons.Outlined.Delete,
    color: Color = MaterialTheme.colorScheme.errorContainer,
    itemOffset: Dp,
    contentPaddingValues: PaddingValues,
) {
    val alignment = when (swipeState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        else -> Alignment.Center
    }
    Box(
        contentAlignment = alignment,
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (icon == null) return@Box

        val dismissIndicatorWidth = abs((itemOffset - 16.dp).value).dp

        Box(
            contentAlignment = alignment,
            modifier = Modifier
                .padding(contentPaddingValues)
                .clip(MaterialTheme.shapes.medium)
                .fillMaxHeight()
                .width(dismissIndicatorWidth)
                .background(color)
        ) {
            AnimatedVisibility(
                visible = dismissIndicatorWidth >= 50.dp,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(100))
            ) {
                Icon(
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    imageVector = icon, contentDescription = null
                )
            }
        }
    }
}
