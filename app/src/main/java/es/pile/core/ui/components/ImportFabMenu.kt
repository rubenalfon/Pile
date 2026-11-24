package es.pile.core.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import es.pile.R
import es.pile.core.ui.controllers.ImportActions

@Composable
fun ImportFabMenu(
    modifier: Modifier = Modifier,
    fabMenuExpanded: Boolean,
    updateFabMenuExpanded: (Boolean) -> Unit = {},
    importActions: ImportActions
) {
    val items = listOf(
        Triple(
            painterResource(R.drawable.ic_clip),
            stringResource(R.string.import_pdf_file),
            importActions.launchPdfPicker
        ),
        Triple(
            rememberVectorPainter(Icons.Filled.Photo),
            stringResource(R.string.import_from_gallery),
            importActions.launchGallery
        ),
        Triple(
            rememberVectorPainter(Icons.Filled.CameraAlt),
            stringResource(R.string.take_a_photo),
            importActions.launchCamera
        )
    )

    BackHandler(fabMenuExpanded) { updateFabMenuExpanded(false) }

    val expandedString = stringResource(R.string.expanded)
    val collapsedString = stringResource(R.string.collapsed)
    val toggleMenuString = stringResource(R.string.toggle_menu)
    val closeMenuString = stringResource(R.string.close_menu)

    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = fabMenuExpanded,
        button = {
            ToggleFloatingActionButton(
                modifier = Modifier
                    .semantics {
                        traversalIndex = -1f
                        stateDescription =
                            if (fabMenuExpanded) expandedString else collapsedString
                        contentDescription = toggleMenuString
                    },
                checked = fabMenuExpanded,
                onCheckedChange = { updateFabMenuExpanded(it) }
            ) {
                val imageVector by remember {
                    derivedStateOf {
                        if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                    }
                }
                Icon(
                    painter = rememberVectorPainter(imageVector),
                    contentDescription = null,
                    modifier = Modifier.animateIcon({ checkedProgress })
                )
            }
        }
    ) {
        items.forEachIndexed { i, (icon, label, action) ->
            FloatingActionButtonMenuItem(
                modifier =
                    Modifier.semantics {
                        isTraversalGroup = true
                        if (i == items.size - 1) {
                            customActions = listOf(
                                CustomAccessibilityAction(
                                    label = closeMenuString,
                                    action = {
                                        action()
                                        updateFabMenuExpanded(false)
                                        true
                                    }
                                )
                            )
                        }
                    },
                onClick = {
                    action()
                    updateFabMenuExpanded(false)
                },
                icon = { Icon(icon, contentDescription = null) },
                text = { Text(text = label) }
            )
        }
    }
}