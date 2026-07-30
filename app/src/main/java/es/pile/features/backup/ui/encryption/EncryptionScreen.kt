package es.pile.features.backup.ui.encryption

import android.content.ClipData
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.pile.R
import es.pile.core.ui.theme.PileTheme
import es.pile.core.ui.util.SecureWindow
import es.pile.features.settings.ui.composables.ItemPosition
import es.pile.features.settings.ui.composables.SettingsItem
import es.pile.features.settings.ui.composables.SettingsSection
import es.pile.features.settings.ui.composables.SettingsTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.seconds

@Composable
fun EncryptionScreen(
    viewModel: EncryptionViewModel = koinViewModel(),
    popBackStack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    EncryptionContent(
        state = state,
        onBack = popBackStack,
        onEvent = viewModel::handleEvent
    )
}

@Preview
@Composable
private fun EncryptionScreenPreview() {
    PileTheme {
        EncryptionContent(
            state = EncryptionState(isEncryptionOn = true),
            onBack = {},
            onEvent = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncryptionContent(
    state: EncryptionState,
    onBack: () -> Unit,
    onEvent: (EncryptionEvent) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SettingsTopBar(
                title = stringResource(R.string.encryption_settings),
                popBackStack = onBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = if (state.isEncryptionOn) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = if (state.isEncryptionOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Text(
                    text = stringResource(R.string.encryption_description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SettingsSection(title = stringResource(R.string.encryption)) {
                SettingsItem(
                    itemPosition = if (state.isEncryptionOn) ItemPosition.TOP else ItemPosition.SINGLE,
                    title = stringResource(R.string.end_to_end_encrypted_backup),
                    subtitle = if (state.isEncryptionOn) stringResource(R.string.on) else stringResource(
                        R.string.off
                    ),
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.backup),
                            contentDescription = null
                        )
                    },
                    onAction = {
                        onEvent(EncryptionEvent.OnToggleEncryption)
                    }
                )
                AnimatedVisibility(
                    visible = state.isEncryptionOn,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                    label = "RecoveryItemAppear"
                ) {
                    SettingsItem(
                        itemPosition = ItemPosition.BOTTOM,
                        title = stringResource(R.string.show_recovery_key),
                        subtitle = stringResource(R.string.show_recovery_key_subtitle),
                        onAction = {
                            onEvent(EncryptionEvent.OnShowRecoveryKeyClicked)
                        }
                    )
                }
            }
        }

        if (state.isRecoveryKeyVisible && state.recoveryKey != null) {
            RecoveryKeyDialog(
                recoveryKey = state.recoveryKey,
                isEnabling = !state.isEncryptionOn,
                onConfirm = { onEvent(EncryptionEvent.OnRecoveryKeyConfirmed) },
                onDismiss = { onEvent(EncryptionEvent.OnHideRecoveryKey) }
            )
        }

        if (state.isDisableAlertVisible) {
            DisableEncryptionAlert(
                onDismiss = { onEvent(EncryptionEvent.OnHideDisableAlert) },
                onConfirm = { onEvent(EncryptionEvent.OnConfirmDisable) }
            )
        }
    }
}

@Composable
private fun DisableEncryptionAlert(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.disable_encryption_confirmation_title)) },
        text = { Text(stringResource(R.string.disable_encryption_confirmation_message)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.disable_encryption))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun RecoveryKeyDialog(
    recoveryKey: String,
    isEnabling: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2.seconds)
            isCopied = false
        }
    }

    SecureWindow()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painterResource(R.drawable.warning_24px),
                contentDescription = null
            )
        },
        title = { Text(stringResource(R.string.recovery_key_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.recovery_key_message))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = recoveryKey,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        textAlign = TextAlign.Center
                    )
                }

                val containerColor by animateColorAsState(
                    targetValue = if (isCopied) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondary,
                    label = "CopyButtonColor"
                )

                Button(
                    onClick = {
                        scope.launch {
                            clipboard.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        "Recovery Key",
                                        recoveryKey
                                    )
                                )
                            )
                        }
                        isCopied = true
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = containerColor)
                ) {
                    AnimatedContent(
                        targetState = isCopied,
                        label = "CopyButtonContent",
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        contentAlignment = Alignment.Center
                    ) { copied ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(24.dp)
                        ) {
                            Icon(
                                painter = if (copied) painterResource(R.drawable.check_24px) else painterResource(
                                    R.drawable.copy_24px
                                ),
                                contentDescription = null,
                                tint = if (copied) MaterialTheme.colorScheme.onPrimaryContainer else LocalContentColor.current
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = if (copied) stringResource(R.string.selected_) else stringResource(
                                    R.string.copy_key
                                ),
                                color = if (copied) MaterialTheme.colorScheme.onPrimaryContainer else LocalContentColor.current
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = if (isEnabling) stringResource(R.string.confirm_saved) else stringResource(
                        R.string.ok
                    )
                )
            }
        },
        dismissButton = {
            if (isEnabling) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )
}
