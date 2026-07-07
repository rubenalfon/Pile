package es.pile.core.ui.composables

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.pile.PileModel
import es.pile.R
import es.pile.core.ui.theme.AppIcons
import es.pile.core.ui.theme.ExtendedTheme
import es.pile.core.ui.theme.PileTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds


@Preview
@Composable
internal fun AlertEditPilePreview() {
    PileTheme {
        Surface(Modifier.fillMaxSize()) {
            AlertEditPile(
                pileModel = PileModel(
                    id = "1",
                    name = "Sample Piles",
                    iconId = "Bank",
                    colorNumber = 1L
                ),
                onDismiss = {},
                onConfirm = { _, _, _ -> }
            )
        }
    }
}

@Composable
fun AlertNewPile(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: (pileName: String, pileIconId: String, pileColorNumber: Long) -> Unit
) {
    AlertNewEditPile(
        modifier = modifier,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Composable
fun AlertEditPile(
    modifier: Modifier = Modifier,
    pileModel: PileModel,
    onDismiss: () -> Unit,
    onConfirm: (pileName: String, pileIconId: String, pileColorNumber: Long) -> Unit
) {
    AlertNewEditPile(
        modifier = modifier,
        pileModel = pileModel,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}


@Composable
private fun AlertNewEditPile(
    modifier: Modifier = Modifier,
    pileModel: PileModel? = null,
    onDismiss: () -> Unit,
    onConfirm: (pileName: String, pileIconId: String, pileColorNumber: Long) -> Unit
) {
    var pileNameValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = pileModel?.name ?: "",
                selection = TextRange(pileModel?.name?.length ?: 0)
            )
        )
    }
    var pileIconId by rememberSaveable { mutableStateOf(pileModel?.iconId ?: "Add") }
    var pileColorNumber by rememberSaveable { mutableLongStateOf(pileModel?.colorNumber ?: 0) }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (pileModel == null) R.string.add_pile else R.string.edit_pile)) },
        text = {
            BodyAlertNewPile(
                pileNameValue = pileNameValue,
                pileColorNumber = pileColorNumber,
                pileIconId = pileIconId,
                onUpdatePileName = { pileNameValue = it },
                onUpdatePileIcon = { pileIconId = it },
                onUpdatePileColor = { pileColorNumber = it }
            )
        },
        confirmButton = {
            TextButton(
                enabled = pileNameValue.text.isNotEmpty(),
                onClick = {
                    onConfirm.invoke(pileNameValue.text, pileIconId, pileColorNumber)
                }
            ) {
                Text(stringResource(if (pileModel == null) R.string.new_ else R.string.edit))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss.invoke()
            }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BodyAlertNewPile(
    modifier: Modifier = Modifier,
    pileNameValue: TextFieldValue,
    pileColorNumber: Long,
    pileIconId: String,
    onUpdatePileName: (TextFieldValue) -> Unit,
    onUpdatePileIcon: (iconId: String) -> Unit,
    onUpdatePileColor: (colorNumber: Long) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var hasRequestedFocus by rememberSaveable { mutableStateOf(false) }

    val customColorList = ExtendedTheme.colors.customColorList
    val icons = AppIcons.entries
    val density = LocalDensity.current

    val colorLazyListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        if (!hasRequestedFocus) {
            delay(100.milliseconds) // Prevents errors
            focusRequester.requestFocus()
            hasRequestedFocus = true
        }
    }

    val targetForegroundColor = customColorList.getOrNull(pileColorNumber.toInt())?.onColorContainer
        ?: MaterialTheme.colorScheme.onSurfaceVariant
    val targetBackgroundColor = customColorList.getOrNull(pileColorNumber.toInt())?.colorContainer
        ?: MaterialTheme.colorScheme.surfaceContainerHighest

    val animatedForegroundColor by animateColorAsState(
        targetValue = targetForegroundColor,
        label = "foregroundColor"
    )
    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        label = "backgroundColor"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
            focusManager.clearFocus()
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(animatedBackgroundColor)
                .padding(12.dp)
        ) {
            Crossfade(
                targetState = pileIconId,
                label = "iconPreview",
                modifier = Modifier.size(32.dp),
                animationSpec = tween(durationMillis = 150)
            ) { id ->
                Icon(
                    painter = painterResource(AppIcons.getById(id)),
                    contentDescription = null,
                    tint = animatedForegroundColor,
                    modifier = Modifier.fillMaxSize()
                )
            }

            OutlinedTextField(
                value = pileNameValue,
                onValueChange = { onUpdatePileName(it) },
                modifier = Modifier.focusRequester(focusRequester),
                trailingIcon = {
                    if (pileNameValue.text.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.delete_text),
                            modifier = Modifier.clickable {
                                onUpdatePileName(pileNameValue.copy(text = ""))
                            },
                            tint = animatedForegroundColor
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = targetForegroundColor,
                    unfocusedTextColor = targetForegroundColor,
                    focusedBorderColor = targetForegroundColor,
                    unfocusedBorderColor = targetForegroundColor,
                    cursorColor = targetForegroundColor
                )
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp, 14.dp, 4.dp, 4.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .fillMaxWidth()
            ) {
                val maxWidthPx = constraints.maxWidth
                val itemWidthPx = with(density) { 42.dp.toPx() }

                LaunchedEffect(Unit) {
                    val centerOffset = (maxWidthPx - itemWidthPx) / 2
                    colorLazyListState.scrollToItem(pileColorNumber.toInt(), -centerOffset.toInt())
                }

                LazyRow(
                    state = colorLazyListState,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    items(customColorList.size) { index ->
                        val isSelected = pileColorNumber.toInt() == index
                        val colorScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.15f else 1f,
                            label = "colorScale"
                        )
                        val checkAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0f,
                            label = "checkAlpha"
                        )
                        val checkScale by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0.5f,
                            label = "checkScale"
                        )

                        FilledIconButton(
                            onClick = {
                                onUpdatePileColor(index.toLong())
                            },
                            colors = IconButtonColors(
                                containerColor = customColorList[index].onColorContainer.copy(),
                                contentColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                disabledContentColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .size(42.dp)
                                .aspectRatio(1f)
                                .alpha(if (isSelected) 1f else 0.8f)
                                .graphicsLayer(scaleX = colorScale, scaleY = colorScale)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.check_24px),
                                contentDescription = null,
                                tint = customColorList[index].colorContainer,
                                modifier = Modifier.graphicsLayer(
                                    alpha = checkAlpha,
                                    scaleX = checkScale,
                                    scaleY = checkScale
                                )
                            )
                        }
                    }
                }
            }


            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp, 4.dp, 14.dp, 14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(icons.size) { index ->
                        val isSelected = pileIconId == icons[index].id
                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.15f else 1f,
                            label = "iconScale"
                        )

                        FilledIconButton(
                            onClick = {
                                onUpdatePileIcon(icons[index].id)
                            },
                            colors = IconButtonColors(
                                containerColor = if (isSelected) animatedForegroundColor else Color.Transparent,
                                contentColor = Color.Transparent,
                                disabledContainerColor = Color.Red,
                                disabledContentColor = Color.Red
                            ),
                            modifier = Modifier
                                .aspectRatio(1f)
                                .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                        ) {
                            Icon(
                                painter = painterResource(icons[index].resourceId),
                                contentDescription = null,
                                tint = if (isSelected) animatedBackgroundColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
