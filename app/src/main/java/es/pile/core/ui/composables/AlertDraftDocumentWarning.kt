package es.pile.core.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import es.pile.R
import es.pile.core.ui.theme.PileTheme

@Preview
@Composable
private fun AlertDraftDocumentWarningPrev() {
    PileTheme {
        Surface(Modifier.fillMaxSize()) {
            AlertDraftDocumentWarning(
                onDismiss = {},
                onDiscardAndContinue = {},
                onNavigateToDraft = {}
            )
        }
    }
}


@Composable
fun AlertDraftDocumentWarning(
    onDismiss: () -> Unit,
    onDiscardAndContinue: () -> Unit,
    onNavigateToDraft: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.draft_warning_title)) },
        text = { Text(stringResource(R.string.draft_warning_body)) },
        confirmButton = {
            TextButton(onClick = onNavigateToDraft) {
                Text(stringResource(R.string.draft_warning_navigate))
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.cancel))
                }
                TextButton(onClick = onDiscardAndContinue) {
                    Text(stringResource(R.string.draft_warning_discard))
                }
            }
        }
    )
}