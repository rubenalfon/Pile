package es.pile.features.backup.ui.wipe

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.pile.R
import es.pile.core.ui.theme.PileTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun WipeCloudScreen(
    viewModel: WipeCloudViewModel = koinViewModel(),
    popBackStack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            popBackStack()
        }
    }

    WipeCloudContent(
        state = state,
        onEvent = { event ->
            when (event) {
                WipeCloudEvent.OnBackClicked -> popBackStack()
                else -> viewModel.handleEvent(event)
            }
        }
    )
}

@Preview
@Composable
private fun WipeCloudScreenPreview() {
    PileTheme {
        WipeCloudContent(
            state = WipeCloudState(
                showConfirmationDialog = true
            ),
            onEvent = {}
        )
    }
}

@Composable
fun WipeCloudContent(
    state: WipeCloudState,
    onEvent: (WipeCloudEvent) -> Unit
) {
    BackHandler(enabled = state.isWiping) {
        // Intercept and prevent back navigation while cloud data is being wiped
    }

    if (state.showConfirmationDialog) {
        ConfirmationDialog(onEvent)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.error)
            .windowInsetsPadding(WindowInsets.displayCutout)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.warning_24px),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.onError
                )
                Text(
                    text = stringResource(R.string.wipe_cloud_data_alert_message),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onError
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            state.error?.let { errorUiText ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        text = errorUiText.asString(),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface
                            ),
                            endY = 320f
                        )
                    )
                    .padding(16.dp)
                    .safeDrawingPadding()
                    .padding(top = 46.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val size = ButtonDefaults.LargeContainerHeight
                Button(
                    onClick = { onEvent(WipeCloudEvent.ShowConfirmationDialog) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(size),
                    enabled = !state.isWiping,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    if (state.isWiping) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.wipe_cloud_data),
                            style = ButtonDefaults.textStyleFor(size)
                        )
                    }
                }

                OutlinedButton(
                    onClick = { onEvent(WipeCloudEvent.OnBackClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(size),
                    enabled = !state.isWiping
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = ButtonDefaults.textStyleFor(size)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = state.isWiping,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ConfirmationDialog(onEvent: (WipeCloudEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onEvent(WipeCloudEvent.HideConfirmationDialog) },
        icon = {
            Icon(
                painter = painterResource(R.drawable.warning_24px),
                contentDescription = null
            )
        },
        title = {
            Text(
                text = stringResource(R.string.are_you_sure),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = stringResource(R.string.wipe_cloud_dialog_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEvent(WipeCloudEvent.OnConfirmWipe) },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(stringResource(R.string.wipe))
            }
        },
        dismissButton = {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEvent(WipeCloudEvent.HideConfirmationDialog) }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}