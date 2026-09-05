package es.pile.features.backup.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CheckableDropdownMenuItem
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.core.net.toUri
import es.pile.R
import es.pile.core.domain.backup.BackupProviderInfo
import es.pile.core.domain.backup.UserCancelledException
import es.pile.core.domain.models.SyncState
import es.pile.core.ui.composables.LoadingWrapper
import es.pile.core.ui.theme.PileTheme
import es.pile.core.ui.util.BiometricHelper
import es.pile.core.ui.util.UiText
import es.pile.features.backup.domain.BackupAuthHandler
import es.pile.features.settings.ui.composables.ItemPosition
import es.pile.features.settings.ui.composables.SettingsItem
import es.pile.features.settings.ui.composables.SettingsSection
import es.pile.features.settings.ui.composables.SettingsTopBar
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Duration.Companion.seconds


@Composable
fun BackupScreen(
    viewModel: BackupViewModel = koinViewModel(),
    onBack: () -> Unit,
    navigateToEncryptionSettings: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current
    val biometricHelper = remember { BiometricHelper(context) }
    val authTitle = stringResource(R.string.biometric_auth_title)
    val authSubtitle = stringResource(R.string.biometric_auth_subtitle)

    val runWithAuth = { action: () -> Unit ->
        if (biometricHelper.canAuthenticate()) {
            biometricHelper.authenticate(
                title = authTitle,
                subtitle = authSubtitle,
                onSuccess = action
            )
        } else {
            action()
        }
    }

    BackupContent(
        state = state,
        onEvent = { event ->
            when (event) {
                BackupEvent.OnBackClicked -> onBack()
                BackupEvent.OnNavigateToEncryption -> {
                    runWithAuth { navigateToEncryptionSettings() }
                }

                else -> viewModel.handleEvent(event)
            }
        }
    )
}

@Preview
@Composable
private fun BackupGoogleDrivePreview() {
    val mockProvider = BackupProviderInfo(
        name = "Google Drive",
        icon = R.drawable.google_drive,
        iconFill = R.drawable.google_drive_fill
    )

    PileTheme {
        BackupContent(
            state = BackupState(
                isLoading = false,
                syncState = SyncState.Error(UiText.DynamicString("mal")),
                selectedProvider = mockProvider,
                availableProviders = listOf(mockProvider)
            ),
            onEvent = {}
        )
    }
}

@Preview
@Composable
private fun BackupNoProviderPreview() {
    val mockProvider = BackupProviderInfo(
        name = "Google Drive",
        icon = R.drawable.google_drive,
        iconFill = R.drawable.google_drive_fill
    )

    PileTheme {
        BackupContent(
            state = BackupState(
                isLoading = false,
                selectedProvider = null,
                availableProviders = listOf(mockProvider)
            ),
            onEvent = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BackupContent(
    state: BackupState,
    onEvent: (BackupEvent) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            android.app.Activity.RESULT_OK -> {
                val data = result.data
                if (data != null) {
                    onEvent(BackupEvent.OnResolutionResult(Result.success(data)))
                } else {
                    onEvent(BackupEvent.OnResolutionResult(Result.failure(Exception("Result OK but data is null"))))
                }
            }

            android.app.Activity.RESULT_CANCELED -> {
                onEvent(BackupEvent.OnResolutionResult(Result.failure(UserCancelledException())))
            }

            else -> {
                onEvent(BackupEvent.OnResolutionResult(Result.failure(Exception("Result error: ${result.resultCode}"))))
            }
        }
    }

    LaunchedEffect(state.pendingResolution) {
        state.pendingResolution?.let { pendingIntent ->
            launcher.launch(IntentSenderRequest.Builder(pendingIntent).build())
        }
    }

    val context = LocalContext.current
    val googleDriveClientId = stringResource(R.string.google_drive_client_id)
    val authHandler = koinInject<BackupAuthHandler>()

    LaunchedEffect(state.isAccountPickerVisible) {
        if (state.isAccountPickerVisible) {
            val email = authHandler.launchAccountPicker(context, googleDriveClientId)
            onEvent(BackupEvent.OnAccountSelected(email))
        }
    }

    LaunchedEffect(state.navigateToUrl) {
        state.navigateToUrl?.let { uiText ->
            val url = uiText.asString(context)
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
            onEvent(BackupEvent.OnUrlNavigated)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SettingsTopBar(
                title = stringResource(R.string.backup_and_sync),
                popBackStack = { onEvent(BackupEvent.OnBackClicked) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            val isSyncing = state.syncState.isSyncing
            val isVerifyingKey = state.syncState is SyncState.VerifyingKey

            LoadingWrapper(state.isLoading) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ProviderSelector(
                        selectedProvider = state.selectedProvider,
                        availableProviders = state.availableProviders,
                        onProviderSelected = { onEvent(BackupEvent.OnProviderSelected(it)) }
                    )

                    if (state.isAuthErrorAlertVisible) {
                        AuthenticationErrorAlert(
                            onRetry = { onEvent(BackupEvent.OnRetryAuthentication) },
                            onCancel = { onEvent(BackupEvent.OnCancelAuthentication) }
                        )
                    }

                    AnimatedContent(
                        targetState = state.selectedProvider,
                        label = "SyncContentAnimation"
                    ) { provider ->
                        if (provider == null) {
                            SyncDisabledEmptyState()
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                                BackupStatusCard(
                                    lastSyncTimestamp = state.lastSyncTimestamp,
                                    syncState = state.syncState
                                )

                                // Sync button
                                val size = ButtonDefaults.MediumContainerHeight
                                Button(
                                    onClick = { onEvent(BackupEvent.OnSyncClicked) },
                                    modifier = Modifier.fillMaxWidth().heightIn(size),
                                    contentPadding = ButtonDefaults.contentPaddingFor(size, hasStartIcon = true),
                                    enabled = !isSyncing
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.sync_24px),
                                        contentDescription = null,
                                        modifier = Modifier.size(ButtonDefaults.MediumIconSize)
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.MediumIconSpacing))
                                    Text(
                                        text = stringResource(R.string.sync_now),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                // Backup Settings Sections
                                SettingsSection(title = provider.name) {
                                    state.accountEmail?.let { email ->
                                        SettingsItem(
                                            itemPosition = ItemPosition.TOP,
                                            title = stringResource(R.string.google_account),
                                            subtitle = email,
                                            onAction = { onEvent(BackupEvent.OnSwitchAccountClicked) }
                                        )
                                    }

                                    state.storageUsage?.let { usage ->
                                        SettingsItem(
                                            itemPosition = ItemPosition.MIDDLE,
                                            title = stringResource(R.string.manage_google_storage),
                                            subtitle = usage.asString(),
                                            trailingIcon = {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                    contentDescription = null
                                                )
                                            },
                                            onAction = { onEvent(BackupEvent.OnManageStorageClicked) }
                                        )
                                    }

                                    SettingsItem(
                                        itemPosition = ItemPosition.BOTTOM,
                                        title = stringResource(R.string.sync_using_cellular),
                                        checked = state.backupUsingCellular,
                                        onAction = { onEvent(BackupEvent.OnCellularBackupToggled) }
                                    )
                                }

                                // Encryption Section
                                SettingsSection(title = stringResource(R.string.encryption)) {
                                    SettingsItem(
                                        itemPosition = ItemPosition.SINGLE,
                                        title = stringResource(R.string.encryption_settings),
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
                                            onEvent(BackupEvent.OnNavigateToEncryption)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (state.isEnterKeyDialogVisible) {
                        val dialogError = if (state.syncState is SyncState.InvalidKey) {
                            stringResource(R.string.invalid_recovery_key)
                        } else null

                        EnterRecoveryKeyDialog(
                            error = dialogError?.let { UiText.DynamicString(it) },
                            isCheckingKey = isVerifyingKey,
                            onConfirm = { onEvent(BackupEvent.OnEnterKeySubmitted(it)) },
                            onDismiss = { onEvent(BackupEvent.OnDismissEnterKeyDialog) }
                        )
                    }

                    val errorMessage = state.syncState.errorMessage
                    if (errorMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = errorMessage.asString(),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            if (isSyncing && !isVerifyingKey) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
private fun SyncDisabledEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(R.drawable.sync_disabled_24px),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.sync_disabled_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSelector(
    selectedProvider: BackupProviderInfo?,
    availableProviders: List<BackupProviderInfo>,
    onProviderSelected: (BackupProviderInfo?) -> Unit
) {
    var showDisableBackupAlert by remember { mutableStateOf(false) }
    var splitButtonExpanded by remember { mutableStateOf(false) }
    val selectedProviderName = selectedProvider?.name ?: stringResource(R.string.no_backup)

    if (showDisableBackupAlert) {
        DisableBackupAlert(
            onConfirm = {
                showDisableBackupAlert = false
                splitButtonExpanded = false
                onProviderSelected(null)
            },
            onDismiss = { showDisableBackupAlert = false }
        )
    }

    SplitButtonLayout(
        leadingButton = {
            SplitButtonDefaults.TonalLeadingButton(
                onClick = {
                    splitButtonExpanded = !splitButtonExpanded
                },
                modifier = Modifier.height(SplitButtonDefaults.MediumContainerHeight),
                shapes = SplitButtonDefaults.leadingButtonShapesFor(SplitButtonDefaults.MediumContainerHeight),
                contentPadding = SplitButtonDefaults.leadingButtonContentPaddingFor(
                    SplitButtonDefaults.MediumContainerHeight
                )
            ) {
                Icon(
                    painter = painterResource(
                        selectedProvider?.iconFill ?: R.drawable.warning_fill_24px
                    ),
                    modifier = Modifier.size(
                        SplitButtonDefaults.leadingButtonIconSizeFor(
                            SplitButtonDefaults.MediumContainerHeight
                        )
                    ),
                    contentDescription = null
                )

                Spacer(Modifier.size(8.dp))

                Text(selectedProviderName, modifier = Modifier.animateContentSize())
            }
        },
        trailingButton = {
            Box {
                val expandedString = stringResource(R.string.expanded)
                val collapsedString = stringResource(R.string.collapsed)
                val trailingButtonLabel =
                    stringResource(R.string.trailing_button_content_description)
                SplitButtonDefaults.TonalTrailingButton(
                    checked = splitButtonExpanded,
                    onCheckedChange = { splitButtonExpanded = it },
                    modifier =
                        Modifier
                            .height(SplitButtonDefaults.MediumContainerHeight)
                            .semantics {
                                stateDescription =
                                    if (splitButtonExpanded) expandedString else collapsedString
                                contentDescription = trailingButtonLabel
                            },
                    shapes = SplitButtonDefaults.trailingButtonShapesFor(SplitButtonDefaults.MediumContainerHeight),
                    contentPadding = SplitButtonDefaults.trailingButtonContentPaddingFor(
                        SplitButtonDefaults.MediumContainerHeight
                    ),
                ) {
                    val rotation: Float by
                    animateFloatAsState(
                        targetValue = if (splitButtonExpanded) 180f else 0f,
                        label = "Trailing Icon Rotation",
                    )
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        modifier =
                            Modifier
                                .size(
                                    SplitButtonDefaults.trailingButtonIconSizeFor(
                                        SplitButtonDefaults.MediumContainerHeight
                                    )
                                )
                                .graphicsLayer {
                                    this.rotationZ = rotation
                                },
                        contentDescription = null,
                    )
                }

                DropdownMenuPopup(
                    expanded = splitButtonExpanded,
                    onDismissRequest = { splitButtonExpanded = false }
                ) {
                    DropdownMenuGroup(
                        shapes = MenuDefaults.groupShape(0, 1),
                    ) {
                        val groupItemCount = availableProviders.size + 1

                        availableProviders.fastForEachIndexed { index, backupProvider ->
                            CheckableDropdownMenuItem(
                                text = { Text(text = backupProvider.name) },
                                shapes = MenuDefaults.itemShape(index, groupItemCount),
                                leadingIcon = {
                                    Icon(
                                        painterResource(backupProvider.icon),
                                        modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                        contentDescription = null,
                                    )
                                },
                                checkedLeadingIcon = {
                                    Icon(
                                        painterResource(backupProvider.iconFill),
                                        modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                        contentDescription = null,
                                    )
                                },
                                checked = selectedProviderName == backupProvider.name,
                                onCheckedChange = {
                                    onProviderSelected(backupProvider)
                                    splitButtonExpanded = false

                                },
                            )
                        }

                        HorizontalDivider(Modifier.padding(MenuDefaults.HorizontalDividerPadding))

                        CheckableDropdownMenuItem(
                            text = { Text(text = stringResource(R.string.no_backup)) },
                            shapes = MenuDefaults.itemShape(
                                availableProviders.size,
                                groupItemCount
                            ),
                            leadingIcon = {
                                Icon(
                                    painterResource(R.drawable.warning_24px),
                                    modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                    contentDescription = null,
                                )
                            },
                            checkedLeadingIcon = {
                                Icon(
                                    painterResource(R.drawable.warning_fill_24px),
                                    modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                    contentDescription = null,
                                )
                            },
                            checked = selectedProvider == null,
                            onCheckedChange = {
                                splitButtonExpanded = false
                                if (selectedProvider != null) showDisableBackupAlert = true
                            },
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun AuthenticationErrorAlert(
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.authentication_failed)) },
        text = { Text(stringResource(R.string.authentication_failed_message)) },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.disable_backup))
            }
        }
    )
}

@Composable
private fun EnterRecoveryKeyDialog(
    error: UiText? = null,
    isCheckingKey: Boolean = false,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var key by remember { mutableStateOf("") }
    val isKeyValid = key.length == 64

    AlertDialog(
        onDismissRequest = { if (!isCheckingKey) onDismiss() },
        title = { Text(stringResource(R.string.enter_recovery_key_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.enter_recovery_key_message))
                OutlinedTextField(
                    value = key,
                    onValueChange = {
                        key = it.lowercase().filter { c -> c in "0123456789abcdef" }
                    },
                    label = { Text(stringResource(R.string.recovery_key_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCheckingKey,
                    isError = (key.isNotEmpty() && !isKeyValid) || error != null,
                    supportingText = {
                        if (key.isNotEmpty() && !isKeyValid) {
                            Text(stringResource(R.string.invalid_key_length))
                        } else if (error != null) {
                            Text(text = error.asString(), color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                if (isCheckingKey) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(key) },
                enabled = isKeyValid && !isCheckingKey
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isCheckingKey
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun DisableBackupAlert(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.disable_backup_confirmation_title)) },
        text = { Text(stringResource(R.string.disable_backup_confirmation_message)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.disable))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun BackupStatusCard(
    lastSyncTimestamp: Long?,
    syncState: SyncState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Recalculate recentness every minute to allow the UI to transition automatically
    var isRecentSuccess by remember(lastSyncTimestamp) {
        mutableStateOf(lastSyncTimestamp != null && (System.currentTimeMillis() - lastSyncTimestamp) < 5 * 60 * 1000)
    }
    
    LaunchedEffect(lastSyncTimestamp) {
        if (lastSyncTimestamp != null) {
            while (true) {
                val elapsed = System.currentTimeMillis() - lastSyncTimestamp
                isRecentSuccess = elapsed < 5 * 60 * 1000
                if (!isRecentSuccess) break
                delay(30.seconds) // Check every 30 seconds
            }
        }
    }

    val lastSyncText = remember(lastSyncTimestamp) {
        formatLastSync(lastSyncTimestamp, context)
    }
    
    val isWaitingForWifi = syncState is SyncState.WaitingForWifi

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isWaitingForWifi -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedContent(
                targetState = when {
                    isWaitingForWifi -> "waiting"
                    isRecentSuccess -> "recent"
                    else -> "idle"
                },
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                label = "BackupIconAnimation"
            ) { status ->
                val iconColor = when (status) {
                    "waiting" -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = iconColor.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            when (status) {
                                "waiting" -> R.drawable.sync_disabled_24px
                                "recent" -> R.drawable.check_24px
                                else -> R.drawable.sync_24px
                            }
                        ),
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                AnimatedContent(
                    targetState = when {
                        isWaitingForWifi -> "waiting"
                        isRecentSuccess -> "recent"
                        else -> "idle"
                    },
                    transitionSpec = {
                        (fadeIn(tween(400)) + slideInVertically { it / 2 })
                            .togetherWith(fadeOut(tween(400)) + slideOutVertically { -it / 2 })
                    },
                    label = "BackupTextAnimation"
                ) { status ->
                    Text(
                        text = when (status) {
                            "waiting" -> stringResource(R.string.sync_status_waiting_wifi)
                            "recent" -> stringResource(R.string.sync_successful)
                            else -> lastSyncText
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = when (status) {
                            "waiting" -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                
                AnimatedVisibility(
                    visible = isRecentSuccess || isWaitingForWifi,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = lastSyncText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatLastSync(timestamp: Long?, context: android.content.Context): String {
    if (timestamp == null) return context.getString(R.string.never)

    val lastSync = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val today = LocalDate.now()
    val lastSyncDate = lastSync.toLocalDate()

    val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    val timeStr = lastSync.format(timeFormatter)

    val dateStr = when {
        lastSyncDate == today -> context.getString(R.string.today)
        lastSyncDate == today.minusDays(1) -> context.getString(R.string.yesterday)
        else -> {
            val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            lastSync.format(dateFormatter)
        }
    }

    return context.getString(R.string.last_sync_format, "$dateStr, $timeStr")
}
