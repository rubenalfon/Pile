package es.pile.features.home.ui.compostables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.pile.R
import es.pile.core.domain.models.SyncState
import es.pile.core.ui.theme.PileTheme
import es.pile.core.ui.util.UiText

@Preview(showBackground = true)
@Composable
private fun SyncStatusIndicatorPreview() {
    val states = listOf(
        "Idle" to SyncState.Idle,
        "Success" to SyncState.Success(System.currentTimeMillis()),
        "No Provider" to SyncState.NoProvider,
        "Waiting for Wi-Fi" to SyncState.WaitingForWifi,
        "Syncing" to SyncState.Syncing,
        "Uploading" to SyncState.Uploading,
        "Downloading" to SyncState.Downloading,
        "Verifying Key" to SyncState.VerifyingKey,
        "Key Required" to SyncState.KeyRequired,
        "Invalid Key" to SyncState.InvalidKey,
        "Error" to SyncState.Error(UiText.DynamicString("Error"))
    )

    PileTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            states.forEach { (label, state) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    SyncStatusIndicator(
                        state = state,
                        onClick = {}
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SyncStatusIndicator(
    state: SyncState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        val isSyncing = state.isSyncing

        if (isSyncing) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        }

        AnimatedContent(
            targetState = state,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "SyncIconAnimation"
        ) { syncState ->
            val icon = when (syncState) {
                SyncState.Idle, is SyncState.Success -> painterResource(R.drawable.check_24px)
                SyncState.NoProvider, SyncState.WaitingForWifi -> painterResource(R.drawable.sync_disabled_24px)
                SyncState.Syncing -> painterResource(R.drawable.sync_24px)
                SyncState.VerifyingKey -> painterResource(R.drawable.sync_24px)
                SyncState.Uploading -> painterResource(R.drawable.backup)
                SyncState.Downloading -> painterResource(R.drawable.download_24px)
                is SyncState.Error, SyncState.InvalidKey, SyncState.KeyRequired -> painterResource(R.drawable.sync_disabled_24px)
            }

            val tint = when (syncState) {
                is SyncState.Error, SyncState.InvalidKey, SyncState.KeyRequired -> MaterialTheme.colorScheme.error
                SyncState.NoProvider, SyncState.WaitingForWifi -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                SyncState.Idle, is SyncState.Success -> MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.7f
                )

                else -> MaterialTheme.colorScheme.primary
            }

            Icon(
                painter = icon,
                contentDescription = null,
                tint = tint
            )
        }
    }
}
