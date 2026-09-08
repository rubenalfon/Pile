package es.pile.features.backup.ui.compostables

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.pile.R
import es.pile.core.domain.models.SyncState
import es.pile.core.ui.theme.PileTheme
import es.pile.core.ui.util.UiText
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private enum class CardStyleCategory {
    SUCCESS,
    SYNCING,
    WAITING_WIFI,
    NO_INTERNET,
    ERROR
}

@Preview
@Composable
private fun SyncStatusIndicatorAllStatesPreview() {
    val sampleTimestamp = System.currentTimeMillis()

    PileTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SyncStatusIndicator(
                syncState = SyncState.Success(sampleTimestamp),
                lastSyncTimestamp = sampleTimestamp
            )

            SyncStatusIndicator(
                syncState = SyncState.Syncing,
                lastSyncTimestamp = sampleTimestamp
            )

            SyncStatusIndicator(
                syncState = SyncState.WaitingForWifi,
                lastSyncTimestamp = sampleTimestamp
            )

            SyncStatusIndicator(
                syncState = SyncState.Error(UiText.DynamicString("No internet connection")),
                lastSyncTimestamp = sampleTimestamp
            )

            SyncStatusIndicator(
                syncState = SyncState.Error(UiText.DynamicString("Error")),
                lastSyncTimestamp = sampleTimestamp
            )
        }
    }
}

@Preview
@Composable
private fun SyncStatusIndicatorInteractivePreview() {
    var currentState by remember { mutableStateOf<SyncState>(SyncState.Syncing) }
    var lastTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000.milliseconds)
            currentState = SyncState.Success(System.currentTimeMillis())
            lastTimestamp = System.currentTimeMillis()

            delay(3000.milliseconds)
            currentState = SyncState.Syncing

            delay(3000.milliseconds)
            currentState = SyncState.WaitingForWifi

            delay(3000.milliseconds)
            currentState = SyncState.Error(UiText.DynamicString("No internet connection"))

            delay(3000.milliseconds)
            currentState = SyncState.Error(UiText.DynamicString("Server Timeout"))
        }
    }

    PileTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Animation Test (Changes every 3s):", style = MaterialTheme.typography.labelSmall)

            SyncStatusIndicator(
                syncState = currentState,
                lastSyncTimestamp = lastTimestamp
            )
        }
    }
}

@Composable
fun SyncStatusIndicator(
    syncState: SyncState,
    modifier: Modifier = Modifier,
    lastSyncTimestamp: Long? = null,
) {
    val context = LocalContext.current

    var isRecentSuccess by remember(lastSyncTimestamp) {
        mutableStateOf(lastSyncTimestamp != null && (System.currentTimeMillis() - lastSyncTimestamp) < 5 * 60 * 1000)
    }

    LaunchedEffect(lastSyncTimestamp) {
        if (lastSyncTimestamp != null) {
            while (true) {
                val elapsed = System.currentTimeMillis() - lastSyncTimestamp
                isRecentSuccess = elapsed < 5 * 60 * 1000
                if (!isRecentSuccess) break
                delay(30.seconds)
            }
        }
    }

    val lastSyncText = remember(lastSyncTimestamp) {
        formatLastSync(lastSyncTimestamp, context)
    }

    val styleCategory = when {
        syncState.isSyncing -> CardStyleCategory.SYNCING
        syncState is SyncState.WaitingForWifi -> CardStyleCategory.WAITING_WIFI
        syncState is SyncState.Error && syncState.message.asString(context)
            .contains("internet", ignoreCase = true) -> CardStyleCategory.NO_INTERNET

        syncState is SyncState.Error -> CardStyleCategory.ERROR
        else -> CardStyleCategory.SUCCESS
    }

    val titleText: String = when (styleCategory) {
        CardStyleCategory.SYNCING -> stringResource(R.string.syncing)
        CardStyleCategory.WAITING_WIFI -> stringResource(R.string.sync_status_waiting_wifi)
        CardStyleCategory.NO_INTERNET -> stringResource(R.string.no_internet_connection)
        CardStyleCategory.ERROR -> {
            val err = syncState.errorMessage?.asString(context)
            if (!err.isNullOrBlank()) err else stringResource(R.string.sync_failed)
        }

        CardStyleCategory.SUCCESS -> stringResource(R.string.sync_successful)
    }

    val showSubtitle =
        styleCategory != CardStyleCategory.SYNCING && styleCategory != CardStyleCategory.ERROR && lastSyncText != null

    val targetContainerColor = when (styleCategory) {
        CardStyleCategory.SYNCING, CardStyleCategory.SUCCESS -> MaterialTheme.colorScheme.surfaceContainerHigh
        CardStyleCategory.WAITING_WIFI -> MaterialTheme.colorScheme.tertiaryContainer
        CardStyleCategory.NO_INTERNET, CardStyleCategory.ERROR -> MaterialTheme.colorScheme.errorContainer
    }
    val animatedContainerColor by animateColorAsState(
        targetValue = targetContainerColor,
        animationSpec = tween(500),
        label = "ContainerColorAnimation"
    )

    val targetIconBackground = when (styleCategory) {
        CardStyleCategory.SYNCING, CardStyleCategory.SUCCESS -> MaterialTheme.colorScheme.secondary
        CardStyleCategory.WAITING_WIFI -> MaterialTheme.colorScheme.tertiary
        CardStyleCategory.NO_INTERNET, CardStyleCategory.ERROR -> MaterialTheme.colorScheme.error
    }
    val animatedIconBackground by animateColorAsState(
        targetValue = targetIconBackground,
        animationSpec = tween(500),
        label = "IconCircleColorAnimation"
    )

    val targetIconForeground = when (styleCategory) {
        CardStyleCategory.SYNCING, CardStyleCategory.SUCCESS -> MaterialTheme.colorScheme.onSecondary
        CardStyleCategory.WAITING_WIFI -> MaterialTheme.colorScheme.onTertiary
        CardStyleCategory.NO_INTERNET, CardStyleCategory.ERROR -> MaterialTheme.colorScheme.onError
    }
    val animatedIconForeground by animateColorAsState(
        targetValue = targetIconForeground,
        animationSpec = tween(500),
        label = "IconTintColorAnimation"
    )

    val targetTextColor = when (styleCategory) {
        CardStyleCategory.SYNCING, CardStyleCategory.SUCCESS -> MaterialTheme.colorScheme.onSurface
        CardStyleCategory.WAITING_WIFI -> MaterialTheme.colorScheme.onTertiaryContainer
        CardStyleCategory.NO_INTERNET, CardStyleCategory.ERROR -> MaterialTheme.colorScheme.error
    }
    val animatedTextColor by animateColorAsState(
        targetValue = targetTextColor,
        animationSpec = tween(500),
        label = "TitleColorAnimation"
    )

    // Rotating animation when syncing
    val infiniteTransition = rememberInfiniteTransition(label = "SyncRotationInfinite")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SyncRotationAngle"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(400)),
        colors = CardDefaults.cardColors(containerColor = animatedContainerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(color = animatedIconBackground, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = styleCategory,
                    transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                    label = "IconTransition"
                ) { cat ->
                    val iconRes = when (cat) {
                        CardStyleCategory.SUCCESS -> R.drawable.check_24px
                        CardStyleCategory.SYNCING -> R.drawable.sync_24px
                        CardStyleCategory.WAITING_WIFI, CardStyleCategory.NO_INTERNET -> R.drawable.no_connection
                        CardStyleCategory.ERROR -> R.drawable.warning_24px
                    }
                    val rotation = if (cat == CardStyleCategory.SYNCING) rotationAngle else 0f

                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = animatedIconForeground,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(rotationZ = rotation)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedContent(
                    targetState = titleText,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                    label = "TitleTextTransition"
                ) { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = animatedTextColor
                    )
                }

                AnimatedVisibility(
                    visible = showSubtitle,
                    enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                    exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
                ) {
                    Text(
                        text = lastSyncText ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = animatedTextColor
                    )
                }
            }
        }
    }
}

private fun formatLastSync(timestamp: Long?, context: Context): String? {
    if (timestamp == null) return null

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
