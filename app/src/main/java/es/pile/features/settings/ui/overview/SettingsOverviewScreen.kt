package es.pile.features.settings.ui.overview

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.S
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pile.R
import es.pile.core.domain.models.AppTheme
import es.pile.core.domain.models.ImageResolution
import es.pile.core.domain.models.ImageResolution.LOW
import es.pile.core.domain.models.ImageResolution.ORIGINAL
import es.pile.core.ui.composables.LoadingWrapper
import es.pile.core.ui.theme.PileTheme
import es.pile.features.settings.ui.composables.ItemPosition
import es.pile.features.settings.ui.composables.SettingsItem
import es.pile.features.settings.ui.composables.SettingsRadioButton
import es.pile.features.settings.ui.composables.SettingsSection
import es.pile.features.settings.ui.composables.SettingsTopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsOverviewScreen(
    viewModel: SettingsOverviewViewModel = koinViewModel(),
    popBackStack: () -> Unit,
    navigateToSettingsResolution: () -> Unit,
    navigateToBackup: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsOverviewContent(
        state = state,
        onEvent = { event ->
            when (event) {
                is SettingsOverviewEvent.OnBackClicked -> popBackStack()
                is SettingsOverviewEvent.OnResolutionClicked -> navigateToSettingsResolution()
                is SettingsOverviewEvent.OnBackupClicked -> navigateToBackup()
                else -> viewModel.handleEvent(event)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsOverviewPreview() {
    PileTheme {
        SettingsOverviewContent(
            state = SettingsOverviewState(
                isLoading = false,
                isLocalAiEnabled = true,
                isBackupSupported = true
            ),
            onEvent = {}
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsOverviewContent(
    state: SettingsOverviewState,
    onEvent: (SettingsOverviewEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showAppThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showImportConfirmationDialog by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it.asString(context))
            onEvent(SettingsOverviewEvent.OnSnackbarMessageShown)
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let { onEvent(SettingsOverviewEvent.OnExportUriSelected(it)) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { onEvent(SettingsOverviewEvent.OnImportUriSelected(it)) }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.displayCutout,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SettingsTopBar(
                title = stringResource(R.string.settings),
                popBackStack = { onEvent(SettingsOverviewEvent.OnBackClicked) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LoadingWrapper(state.isLoading) {
            Column(
                modifier = modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppearanceSection(
                    theme = state.theme,
                    isMaterialColor = state.isMaterialColor,
                    onAppThemeChange = { showAppThemeDialog = true },
                    onMaterialColorToggle = { onEvent(SettingsOverviewEvent.OnMaterialColorToggled) }
                )
//                  todo implement AI
//                AISection(
//                    isLocalAiEnabled = state.isLocalAiEnabled,
//                    selectedModel = state.selectedModel,
//                    onToggleLocalAi = { onEvent(SettingsOverviewEvent.OnLocalAiToggled) },
//                    onSelectLanguageModel = { }
//                )

                ResolutionSection(
                    imageResolution = state.imageResolution,
                    onResolutionChange = { onEvent(SettingsOverviewEvent.OnResolutionClicked) }
                )

                BackupSection(
                    isBackupSupported = state.isBackupSupported,
                    onBackupClick = { onEvent(SettingsOverviewEvent.OnBackupClicked) },
                    onExportClick = { exportLauncher.launch("pile_backup.zip") },
                    onImportClick = { showImportConfirmationDialog = true }
                )

            }
        }
    }

    if (showImportConfirmationDialog) {
        ShowImportConfirmationDialog(
            onConfirm = {
                importLauncher.launch(arrayOf("application/zip"))
                showImportConfirmationDialog = false
            },
            onDismiss = { showImportConfirmationDialog = false }
        )
    }

    if (showAppThemeDialog) {
        AppThemeDialog(
            currentTheme = state.theme,
            onDismiss = {
                showAppThemeDialog = false
            },
            onConfirm = {
                onEvent(SettingsOverviewEvent.OnThemeChanged(it))
                showAppThemeDialog = false
            }
        )
    }
}

@Composable
private fun ShowImportConfirmationDialog(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.import_backup_dialog_title)) },
        text = { Text(stringResource(R.string.import_backup_dialog_message)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.continue_text))
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
private fun AppearanceSection(
    modifier: Modifier = Modifier,
    theme: AppTheme,
    isMaterialColor: Boolean,
    onAppThemeChange: () -> Unit,
    onMaterialColorToggle: () -> Unit
) {
    val isMaterialColorCompatible = SDK_INT >= S

    val subtitle = when (theme) {
        AppTheme.SYSTEM -> stringResource(R.string.system_default)
        AppTheme.DARK -> stringResource(R.string.dark)
        AppTheme.LIGHT -> stringResource(R.string.light)
    }

    SettingsSection(modifier = modifier, title = stringResource(R.string.appearance)) {
        SettingsItem(
            itemPosition = if (isMaterialColorCompatible) ItemPosition.TOP else ItemPosition.SINGLE,
            title = stringResource(R.string.theme),
            subtitle = subtitle,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.dark_mode_24px),
                    contentDescription = null
                )
            },
            onAction = onAppThemeChange
        )

        if (isMaterialColorCompatible) {
            SettingsItem(
                itemPosition = ItemPosition.BOTTOM,
                title = stringResource(R.string.use_system_theme_color),
                checked = isMaterialColor,
                onAction = onMaterialColorToggle
            )
        }
    }
}

@Composable
private fun AISection(
    modifier: Modifier = Modifier,
    isLocalAiEnabled: Boolean,
    selectedModel: String?,
    onToggleLocalAi: () -> Unit,
    onSelectLanguageModel: () -> Unit
) {
    SettingsSection(modifier = modifier, title = stringResource(R.string.artificial_intelligence)) {
        SettingsItem(
            itemPosition = ItemPosition.TOP,
            title = stringResource(R.string.activate_local_ai_tittle),
            subtitle = stringResource(R.string.activate_local_ai_body),
            checked = isLocalAiEnabled,
            onAction = onToggleLocalAi
        )

        SettingsItem(
            enabled = isLocalAiEnabled,
            itemPosition = ItemPosition.BOTTOM,
            title = stringResource(R.string.select_language_model),
            subtitle = selectedModel ?: stringResource(R.string.no_model_selected),
            onAction = onSelectLanguageModel
        )
    }
}

@Composable
private fun ResolutionSection(
    modifier: Modifier = Modifier,
    imageResolution: ImageResolution,
    onResolutionChange: () -> Unit,
) {
    val subtitle = when (imageResolution) {
        ORIGINAL -> stringResource(R.string.original_quality_tittle)
        LOW -> stringResource(R.string.storage_saver_tittle)
    }

    SettingsSection(modifier = modifier, title = stringResource(R.string.resolution)) {
        SettingsItem(
            itemPosition = ItemPosition.SINGLE,
            title = stringResource(R.string.document_resolution),
            subtitle = subtitle,
            onAction = onResolutionChange
        )
    }
}

@Composable
private fun BackupSection(
    modifier: Modifier = Modifier,
    isBackupSupported: Boolean,
    onBackupClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
) {
    SettingsSection(modifier = modifier, title = stringResource(R.string.sync_and_backup)) {
        if (isBackupSupported) {
            SettingsItem(
                itemPosition = ItemPosition.TOP,
                title = stringResource(R.string.cloud_sync),
                subtitle = stringResource(R.string.manage_your_cloud_sync),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.cloud_sync_24px),
                        contentDescription = null
                    )
                },
                onAction = onBackupClick
            )
        }
        SettingsItem(
            itemPosition = if (isBackupSupported) ItemPosition.MIDDLE else ItemPosition.TOP,
            title = stringResource(R.string.export_data),
            subtitle = stringResource(R.string.export_data_desc),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.upload_24px),
                    contentDescription = null
                )
            },
            onAction = onExportClick
        )
        SettingsItem(
            itemPosition = ItemPosition.BOTTOM,
            title = stringResource(R.string.import_data),
            subtitle = stringResource(R.string.import_data_desc),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.download_24px),
                    contentDescription = null
                )
            },
            onAction = onImportClick
        )
    }
}

@Composable
fun AppThemeDialog(
    modifier: Modifier = Modifier,
    currentTheme: AppTheme,
    onDismiss: () -> Unit,
    onConfirm: (AppTheme) -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_theme)) },
        text = {
            Column {
                SettingsRadioButton(
                    title = stringResource(R.string.system_default),
                    selected = currentTheme == AppTheme.SYSTEM,
                    onClick = { onConfirm(AppTheme.SYSTEM) }
                )

                SettingsRadioButton(
                    title = stringResource(R.string.light),
                    selected = currentTheme == AppTheme.LIGHT,
                    onClick = { onConfirm(AppTheme.LIGHT) }
                )

                SettingsRadioButton(
                    title = stringResource(R.string.dark),
                    selected = currentTheme == AppTheme.DARK,
                    onClick = { onConfirm(AppTheme.DARK) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}