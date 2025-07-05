package com.ganadoro.pile.ui.compostables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.ganadoro.pile.R


@Composable
fun AlertNewPile(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: (pileName: String) -> Unit
) {
    var pileName by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_pile)) },
        text = {
            OutlinedTextField(
                value = pileName,
                onValueChange = { pileName = it },
                label = { Text(stringResource(R.string.pile_name)) },
                trailingIcon = {
                    if (pileName.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.delete_text),
                            modifier = Modifier.clickable { pileName = "" })
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )
        },
        confirmButton = {
            TextButton(
                enabled = pileName.isNotEmpty(),
                onClick = {
                    onConfirm.invoke(pileName)
                }
            ) {
                Text(stringResource(R.string.new_))
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