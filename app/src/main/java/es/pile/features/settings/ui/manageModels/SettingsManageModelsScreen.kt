package es.pile.features.settings.ui.manageModels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pile.R
import es.pile.core.domain.models.ocr.OcrModelGroup
import es.pile.core.domain.models.ocr.OcrModelGroupState
import es.pile.core.ui.theme.PileTheme
import es.pile.features.settings.ui.composables.ItemPosition
import es.pile.features.settings.ui.composables.SettingsItem
import es.pile.features.settings.ui.composables.SettingsSection
import es.pile.features.settings.ui.composables.SettingsTopBar
import es.pile.features.settings.ui.localAI.SettingsLocalAIEvent
import es.pile.features.settings.ui.localAI.SettingsLocalAIState
import es.pile.features.settings.ui.localAI.SettingsLocalAIViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsManageModelsScreen(
    viewModel: SettingsLocalAIViewModel = koinViewModel(),
    popBackStack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsManageModelsContent(
        state = state,
        onEvent = { event ->
            when (event) {
                is SettingsLocalAIEvent.OnBackClicked -> popBackStack()
                else -> viewModel.handleEvent(event)
            }
        }
    )
}

@Preview
@Composable
private fun SettingsManageModelsPrev() {
    PileTheme {
        SettingsManageModelsContent(
            state = SettingsLocalAIState(
                isAIEnabled = true,
                ocrModelGroupsState = listOf(
                    OcrModelGroupState(
                        group = OcrModelGroup(
                            id = "latin",
                            languageCodes = listOf("es"),
                            sizeInMb = 100f
                        ),
                        isDownloaded = true,
                        isSelected = true
                    ),
                    OcrModelGroupState(
                        group = OcrModelGroup(
                            id = "english",
                            languageCodes = listOf("en"),
                            sizeInMb = 180f
                        ),
                        isDownloaded = true,
                        isSelected = false
                    ),
                    OcrModelGroupState(
                        group = OcrModelGroup(
                            id = "chinese",
                            languageCodes = listOf("zh"),
                            sizeInMb = 340f
                        ),
                        isDownloaded = false,
                        isSelected = false
                    ),
                )
            ),
            onEvent = {}
        )
    }
}

@Composable
private fun SettingsManageModelsContent(
    modifier: Modifier = Modifier,
    state: SettingsLocalAIState,
    onEvent: (SettingsLocalAIEvent) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.displayCutout,
        topBar = {
            SettingsTopBar(
                title = stringResource(R.string.downloaded_language_models),
                popBackStack = { onEvent(SettingsLocalAIEvent.OnBackClicked) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            SettingsSection(title = stringResource(R.string.ocr_languages)) { 
                val downloadedGroups = state.ocrModelGroupsState.filter { it.isDownloaded }
                if (downloadedGroups.isEmpty()) {
                    ManageModelsEmptyState(
                        icon = Icons.Outlined.Translate,
                        text = stringResource(R.string.no_downloaded_ocr_models)
                    )
                } else {
                    downloadedGroups.forEachIndexed { index, modelGroupState ->
                        val position = if (downloadedGroups.count() == 1) ItemPosition.SINGLE
                        else when (index) {
                            0 -> ItemPosition.TOP
                            downloadedGroups.lastIndex -> ItemPosition.BOTTOM
                            else -> ItemPosition.MIDDLE
                        }
    
                        OcrLanguageItem(
                            itemPosition = position,
                            modelGroup = modelGroupState.group,
                            deviceLanguageCodes = state.recommendedLanguageCodes,
                            isInUse = modelGroupState.isSelected,
                            onLanguageClicked = {
                                onEvent(
                                    SettingsLocalAIEvent.OnDeleteOcrLanguage(
                                        modelGroupState.group.id
                                    )
                                )
                            }
                        )
                    }
                }
            }

//            SettingsSection(title = stringResource(R.string.llm_models)) {
//                state.llmModels.forEachIndexed { index, model ->
//                    val position = when (index) {
//                        0 -> ItemPosition.TOP
//                        state.ocrLanguages.lastIndex -> ItemPosition.BOTTOM
//                        else -> ItemPosition.MIDDLE
//                    }
//
//                    LlmModelItem(
//                        itemPosition = position,
//                        model = model,
//                        onLanguageClicked = { onEvent(SettingsManageModelsEvent.OnOcrLanguageClicked(model.languageCode)) },
//                        selected = state.selectedOcrLanguages.contains(model.languageCode)
//                    )
//                }
//            }
        }
    }
}

@Composable
private fun OcrLanguageItem(
    itemPosition: ItemPosition,
    modelGroup: OcrModelGroup,
    deviceLanguageCodes: List<String>,
    isInUse: Boolean,
    onLanguageClicked: () -> Unit
) {
    val sizeText =
        stringResource(
            R.string.model_size,
            modelGroup.sizeInMb.toInt().toString(),
            stringResource(R.string.mb)
        )

    val subtitle = if (isInUse) {
        stringResource(
            R.string.model_subtitle_with_tag, sizeText, stringResource(R.string.in_use)
        )
    } else {
        sizeText
    }

    SettingsItem(
        itemPosition = itemPosition,
        title = modelGroup.getDisplayName(deviceLanguageCodes),
        subtitle = subtitle,
        trailingIcon = {
            IconButton(onClick = onLanguageClicked) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
            }
        },
        onAction = {}
    )
}

@Composable
private fun ManageModelsEmptyState(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Icon(
                painter = rememberVectorPainter(icon),
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
