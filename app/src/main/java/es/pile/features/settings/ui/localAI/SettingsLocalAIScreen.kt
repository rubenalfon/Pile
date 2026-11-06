package es.pile.features.settings.ui.localAI

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pile.R
import es.pile.core.domain.models.DownloadState
import es.pile.core.domain.models.ocr.OcrModelGroup
import es.pile.core.domain.models.ocr.OcrModelGroupState
import es.pile.core.ui.theme.PileTheme
import es.pile.features.settings.ui.composables.ItemPosition
import es.pile.features.settings.ui.composables.SettingsCheckboxItem
import es.pile.features.settings.ui.composables.SettingsItem
import es.pile.features.settings.ui.composables.SettingsSection
import es.pile.features.settings.ui.composables.SettingsTopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsLocalAIScreen(
    viewModel: SettingsLocalAIViewModel = koinViewModel(),
    navigateToManageModels: () -> Unit,
    popBackStack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsLocalAIContent(
        state = state,
        onEvent = { event ->
            when (event) {
                is SettingsLocalAIEvent.OnBackClicked -> popBackStack()
                is SettingsLocalAIEvent.OnNavigateToManageModels -> navigateToManageModels()
                else -> viewModel.handleEvent(event)
            }
        }
    )
}

@Preview
@Composable
private fun SettingsLocalAIPrev() {
    PileTheme {
        SettingsLocalAIContent(
            state = SettingsLocalAIState(
                isAIEnabled = true,
                ocrModelGroupsState = listOf(
                    OcrModelGroupState(
                        group = OcrModelGroup(
                            id = "latin",
                            languageCodes = listOf("es", "en", "ca"),
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
                        isSelected = false,
                        downloadState = DownloadState.Error("")
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
                    OcrModelGroupState(
                        group = OcrModelGroup(
                            id = "chinese",
                            languageCodes = listOf("zh"),
                            sizeInMb = 340f
                        ),
                        isDownloaded = false,
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
                ),
                recommendedLanguageCodes = listOf("ht", "zh")
            ),
            onEvent = {}
        )
    }
}

@Composable
private fun SettingsLocalAIContent(
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
                title = stringResource(R.string.local_ai_features),
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
            Text(
                text = stringResource(R.string.local_ai_features_body),
                style = MaterialTheme.typography.bodyMedium
            )

            val cardContainerColor by animateColorAsState(
                targetValue = if (state.isAIEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                label = "containerColor"
            )
            val cardContentColor by animateColorAsState(
                targetValue = if (state.isAIEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "containerColor"
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = CircleShape,
                onClick = { onEvent(SettingsLocalAIEvent.OnUseAIClicked) },
                colors = CardDefaults.cardColors(
                    containerColor = cardContainerColor,
                    contentColor = cardContentColor
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(
                            start = 32.dp,
                            end = 20.dp,
                            top = 16.dp,
                            bottom = 16.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.use_ai_features),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.weight(1f))

                    Switch(
                        checked = state.isAIEnabled,
                        onCheckedChange = { onEvent(SettingsLocalAIEvent.OnUseAIClicked) }
                    )
                }
            }

            SettingsSection(title = stringResource(R.string.ocr_languages)) {
                state.ocrModelGroupsState.forEachIndexed { index, modelGroupState ->
                    val position = when (index) {
                        0 -> ItemPosition.TOP
                        state.ocrModelGroupsState.lastIndex -> ItemPosition.BOTTOM
                        else -> ItemPosition.MIDDLE
                    }

                    val isRecommended = modelGroupState.group.languageCodes.any { it in state.recommendedLanguageCodes }
                    OcrLanguageItem(
                        itemPosition = position,
                        modelGroupState = modelGroupState,
                        enabled = state.isAIEnabled,
                        isRecommended = isRecommended,
                        deviceLanguageCodes = state.recommendedLanguageCodes,
                        onLanguageClicked = { onEvent(SettingsLocalAIEvent.OnOcrModelGroupClicked(modelGroupState.group.id)) },
                        checked = modelGroupState.isSelected
                    )
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
//                        onLanguageClicked = { onEvent(SettingsLocaAIEvent.OnOcrLanguageClicked(model.languageCode)) },
//                        selected = state.selectedOcrLanguages.contains(model.languageCode)
//                    )
//                }
//            }

            SettingsSection(title = stringResource(R.string.data)) {
                SettingsItem(
                    itemPosition = ItemPosition.TOP,
                    title = stringResource(R.string.manage_downloaded_language_models),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null
                        )
                    },
                    onAction = { onEvent(SettingsLocalAIEvent.OnNavigateToManageModels) }
                )
                SettingsItem(
                    itemPosition = ItemPosition.BOTTOM,
                    title = stringResource(R.string.hugging_face_token),
                    subtitle = stringResource(R.string.not_available), // TODO Update
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.token),
                            contentDescription = null
                        )
                    },
                    onAction = {}
                )
            }
        }
    }
}

@Composable
private fun OcrLanguageItem(
    itemPosition: ItemPosition,
    modelGroupState: OcrModelGroupState,
    enabled: Boolean,
    isRecommended: Boolean,
    deviceLanguageCodes: List<String>,
    checked: Boolean,
    onLanguageClicked: () -> Unit
) {
    val sizeText =
        stringResource(
            R.string.model_size,
            modelGroupState.group.sizeInMb.toInt().toString(),
            stringResource(R.string.mb)
        )

    val subtitle = if (isRecommended) {
        stringResource(
            R.string.model_subtitle_with_tag, sizeText, stringResource(R.string.recommended_for_you)
        )
    } else {
        sizeText
    }

    SettingsCheckboxItem(
        enabled = enabled,
        itemPosition = itemPosition,
        title = modelGroupState.group.getDisplayName(deviceLanguageCodes),
        subtitle = subtitle,
        checked = checked,
        onAction = onLanguageClicked,
        hasError = modelGroupState.downloadState is es.pile.core.domain.models.DownloadState.Error
    )
}

//@Composable
//private fun LlmModelItem(
//    itemPosition: ItemPosition,
//    model: ,
//    selected: Boolean,
//    onLanguageClicked: () -> Unit
//) {
//    val sizeText =
//        stringResource(
//            R.string.model_size,
//            model.sizeInMb.toInt().toString(),
//            stringResource(R.string.mb)
//        )
//
//    val subtitle = if (model.isRecommended) {
//        stringResource(
//            R.string.model_subtitle_with_tag, sizeText, stringResource(R.string.recommended_for_you)
//        )
//    } else {
//        sizeText
//    }
//
//    SettingsRadioItem(
//        itemPosition = itemPosition,
//        title = model.getDisplayName(),
//        subtitle = subtitle,
//        selected = selected,
//        onAction = onLanguageClicked,
//        hasError = model.hasError
//    )
//}
