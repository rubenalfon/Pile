package com.pile.core.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pile.PileModel
import com.pile.R
import com.pile.core.ui.theme.AppIcons
import com.pile.core.ui.theme.ExtendedTheme
import com.pile.core.ui.theme.PileTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds


@Preview
@Composable
internal fun AlertEditPilePreview() {
    PileTheme {
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
    var pileName by rememberSaveable { mutableStateOf(pileModel?.name ?: "") }
    var pileIconId by rememberSaveable { mutableStateOf(pileModel?.iconId ?: "Add") }
    var pileColorNumber by rememberSaveable { mutableLongStateOf(pileModel?.colorNumber ?: 0) }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (pileModel == null) R.string.add_pile else R.string.edit_pile)) },
        text = {
            BodyAlertNewPile(
                pileName = pileName,
                pileColorNumber = pileColorNumber,
                pileIconId = pileIconId,
                onUpdatePileName = { pileName = it },
                onUpdatePileIcon = { pileIconId = it },
                onUpdatePileColor = { pileColorNumber = it }
            )
        },
        confirmButton = {
            TextButton(
                enabled = pileName.isNotEmpty(),
                onClick = {
                    onConfirm.invoke(pileName, pileIconId, pileColorNumber)
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
    pileName: String,
    pileColorNumber: Long,
    pileIconId: String,
    onUpdatePileName: (name: String) -> Unit,
    onUpdatePileIcon: (iconId: String) -> Unit,
    onUpdatePileColor: (colorNumber: Long) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var hasRequestedFocus by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasRequestedFocus) {
            delay(100.milliseconds) // Prevents errors
            focusRequester.requestFocus()
            hasRequestedFocus = true
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
            focusManager.clearFocus()
        }
    ) {
        var selectedMode by remember { mutableIntStateOf(0) }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val foregroundColor =
                ExtendedTheme.colors.customColorList.getOrNull(
                    pileColorNumber.toInt()
                )?.onColorContainer

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(
                        width = 4.dp,
                        color = foregroundColor ?: MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        selectedMode = 1 - selectedMode
                        focusManager.clearFocus()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        AppIcons.getById(pileIconId)
                    ),
                    contentDescription = null,
                    tint = foregroundColor ?: MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(32.dp)
                )
            }

            OutlinedTextField(
                value = pileName,
                onValueChange = { onUpdatePileName(it) },
                modifier = Modifier.focusRequester(focusRequester),
                label = { Text(stringResource(R.string.pile_name)) },
                trailingIcon = {
                    if (pileName.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.delete_text),
                            modifier = Modifier.clickable { onUpdatePileName("") })
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
        ) {
            val options = listOf(stringResource(R.string.color), stringResource(R.string.icon))

            options.forEachIndexed { index, label ->
                ToggleButton(
                    checked = selectedMode == index,
                    onCheckedChange = {
                        selectedMode = if (selectedMode == index) 1 - index else index
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.semantics { role = Role.RadioButton },
                    shapes =
                        when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                ) {
                    Text(label)
                }
            }
        }

        Box(
            Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(16.dp)
        ) {
            val customColorList = ExtendedTheme.colors.customColorList
            when (selectedMode) {
                0 -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(customColorList.size) { index ->
                            FilledIconButton(
                                onClick = {
                                    onUpdatePileColor(index.toLong())
                                    focusManager.clearFocus()
                                },
                                colors = IconButtonColors(
                                    containerColor = customColorList[index].onColorContainer,
                                    contentColor = Color.Red,
                                    disabledContainerColor = Color.Red,
                                    disabledContentColor = Color.Red
                                ),
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .alpha(0.8f)
                            ) {
                                if (pileColorNumber.toInt() == index)
                                    Icon(
                                        painter = painterResource(R.drawable.check_24px),
                                        contentDescription = null,
                                        tint = customColorList[index].colorContainer
                                    )
                            }
                        }
                    }
                }

                1 -> {
                    val iconList = AppIcons.entries
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(iconList.size) { index ->
                            val isColorful = pileIconId == iconList[index].id

                            FilledIconButton(
                                onClick = {
                                    onUpdatePileIcon(iconList[index].id)
                                    focusManager.clearFocus()
                                },
                                colors = IconButtonColors(
                                    containerColor = if (isColorful) customColorList[pileColorNumber.toInt()].onColorContainer else Color.Transparent,
                                    contentColor = Color.Red,
                                    disabledContainerColor = Color.Red,
                                    disabledContentColor = Color.Red
                                ),
                                modifier = Modifier.aspectRatio(1f)
                            ) {
                                Icon(
                                    painter = painterResource(iconList[index].resourceId),
                                    contentDescription = null,
                                    tint = if (isColorful) customColorList[pileColorNumber.toInt()].colorContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}