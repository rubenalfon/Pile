package es.pile.features.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pile.R
import es.pile.core.ui.theme.ExtendedTheme
import es.pile.core.ui.theme.PileTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    OnboardingContent(
        state = state,
        onEvent = { viewModel.handleEvent(it) }
    )
}

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    PileTheme {
        Surface(Modifier.fillMaxSize()) {
            OnboardingContent(
                state = OnboardingState(currentPage = 0),
                onEvent = {}
            )
        }
    }
}

@Composable
fun OnboardingContent(
    state: OnboardingState,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val onboardingPages = listOf(
        OnboardingPage(
            title = stringResource(R.string.onboarding_welcome_title),
            description = stringResource(R.string.onboarding_welcome_description),
            image = {
                FirstPage()
            }
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_piles_title),
            description = stringResource(R.string.onboarding_piles_description),
            image = {
                SecondPage()
            }
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_pdf_title),
            description = stringResource(R.string.onboarding_pdf_description),
            image = {
                ThirdPage()
            }
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_privacy_title),
            description = stringResource(R.string.onboarding_privacy_description),
            image = {
                FourthPage()
            }
        )
    )

    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })

    // Sync ViewModel state to PagerState (for button clicks)
    LaunchedEffect(state.currentPage) {
        if (state.currentPage != pagerState.currentPage) {
            pagerState.animateScrollToPage(state.currentPage)
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            OnboardingBottomBar(
                pagerState = pagerState,
                pageCount = onboardingPages.size,
                onNext = {
                    if (pagerState.currentPage < onboardingPages.size - 1) {
                        onEvent(OnboardingEvent.OnNextClicked)
                    } else {
                        onEvent(OnboardingEvent.OnFinished)
                    }
                },
                onSkip = {
                    onEvent(OnboardingEvent.OnFinished)
                }
            )
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { pageIndex ->
            OnboardingPageContent(onboardingPages[pageIndex])
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            page.image()
        }
        Spacer(modifier = Modifier.height(48.dp))
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 2 },
            exit = fadeOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OnboardingBottomBar(
    pagerState: PagerState,
    pageCount: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        PagerIndicator(
            pageCount = pageCount,
            currentPage = pagerState.currentPage,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onSkip) {
                Text(text = stringResource(R.string.onboarding_skip))
            }

            Button(onClick = onNext) {
                AnimatedContent(
                    targetState = pagerState.currentPage == pageCount - 1,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                scaleIn(
                                    initialScale = 0.92f,
                                    animationSpec = tween(220, delayMillis = 90)
                                ))
                            .togetherWith(fadeOut(animationSpec = tween(90)))
                    },
                    label = "ButtonTextTransition"
                ) { isLastPage ->
                    Text(
                        text = if (isLastPage) {
                            stringResource(R.string.onboarding_get_started)
                        } else {
                            stringResource(R.string.onboarding_next)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(pageCount) { index ->
            val color = if (currentPage == index) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun FirstPage() {
    var animate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animate = true
    }
    val transition = updateTransition(targetState = animate, label = "LogoTransition")

    val alpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 500) },
        label = "alpha"
    ) { isVisible ->
        if (isVisible) 1f else 0f
    }

    val scale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 500) },
        label = "scale"
    ) { isVisible ->
        if (isVisible) 1f else 0f
    }

    Image(
        painter = painterResource(R.drawable.ic_launcher_foreground),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            },
        contentScale = ContentScale.FillBounds
    )
}

@Composable
private fun SecondPage() {
    var animate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animate = true
    }
    val transition =
        updateTransition(targetState = animate, label = "PilesTransition")

    val invoiceXOffset by transition.animateDp(
        transitionSpec = {
            spring(
                dampingRatio = 0.5f,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "invoiceXOffset"
    ) { isVisible ->
        if (isVisible) (-50).dp else (-150).dp
    }

    val mailXOffset by transition.animateDp(
        transitionSpec = {
            spring(
                dampingRatio = 0.5f,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "mailXOffset"
    ) { isVisible ->
        if (isVisible) 43.dp else 150.dp
    }

    val alpha by transition.animateFloat(label = "alpha") { isVisible ->
        if (isVisible) 1f else 0f
    }

    val scale by transition.animateFloat(label = "scale") { isVisible ->
        if (isVisible) 1f else 0.8f
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val customTheme = ExtendedTheme.colors.customColorList

        // Invoices card
        val invoiceColor = customTheme[3]
        Surface(
            modifier = Modifier
                .width(155.dp)
                .offset {
                    IntOffset(
                        x = invoiceXOffset.roundToPx(),
                        y = (-40).dp.roundToPx()
                    )
                }
                .graphicsLayer {
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                },
            shape = RoundedCornerShape(12.dp),
            color = invoiceColor.colorContainer
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 31.dp, end = 12.dp)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.user_ic_receipt_24px),
                    contentDescription = null,
                    tint = invoiceColor.onColorContainer,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.onboarding_invoices),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = invoiceColor.onColorContainer
                )
            }
        }

        // Mail card
        val mailColor = customTheme[8]
        Surface(
            modifier = Modifier
                .width(180.dp)
                .offset {
                    IntOffset(
                        x = mailXOffset.roundToPx(),
                        y = 40.dp.roundToPx()
                    )
                }
                .graphicsLayer {
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                },
            shape = RoundedCornerShape(12.dp),
            color = mailColor.colorContainer
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 12.dp
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.mail_24px),
                    contentDescription = null,
                    tint = mailColor.onColorContainer,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.onboarding_mail),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = mailColor.onColorContainer
                )
            }
        }
    }
}

@Composable
private fun ThirdPage() {
    var animate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animate = true
    }
    val transition = updateTransition(targetState = animate, label = "PDFIconTransition")

    val xOffset by transition.animateDp(
        transitionSpec = {
            spring(
                dampingRatio = 0.5f,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "invoiceXOffset"
    ) { isVisible ->
        if (isVisible) (0).dp else (-60).dp
    }

    val alpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 500) },
        label = "alpha"
    ) { isVisible ->
        if (isVisible) 1f else 0f
    }

    val scale by transition.animateFloat(label = "scale") { isVisible ->
        if (isVisible) 1f else 0.8f
    }

    val contrast = 1.5f
    val brightness = 15f
    val colorFilter = if (isSystemInDarkTheme()) {
        val colorMatrix = floatArrayOf(
            contrast, 0f, 0f, 0f, brightness,
            0f, contrast, 0f, 0f, brightness,
            0f, 0f, contrast, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        )
        ColorFilter.colorMatrix(ColorMatrix(colorMatrix))
    } else null

    Image(
        painter = painterResource(R.drawable.pdf_icon),
        contentDescription = null,
        modifier = Modifier
            .size(135.dp)
            .offset {
                IntOffset(
                    x = xOffset.roundToPx(),
                    y = 0.dp.roundToPx()
                )
            }
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            },
        colorFilter = colorFilter,
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun FourthPage() {
    var animate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animate = true
    }
    val transition = updateTransition(targetState = animate, label = "SecurityTransition")

    val shieldAlpha by transition.animateFloat(
        transitionSpec = { tween(600) },
        label = "shieldAlpha"
    ) { if (it) 1f else 0f }

    val checksAlpha by transition.animateFloat(
        transitionSpec = { tween(400, delayMillis = 800) },
        label = "checksAlpha"
    ) { if (it) 1f else 0f }

    val checksScale by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow) },
        label = "checksScale"
    ) { if (it) 1f else 0f }

    // Animate the cut radius along with the checks
    val cutRadius by transition.animateDp(
        transitionSpec = { spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow) },
        label = "cutRadius"
    ) { if (it) 29.dp else 0.dp }

    val lockAlpha by transition.animateFloat(
        transitionSpec = { tween(600, delayMillis = 1400) },
        label = "lockAlpha"
    ) { if (it) 1f else 0f }

    val lockScale by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow) },
        label = "lockScale"
    ) { if (it) 1f else 0.5f }

    val shieldPainter = painterResource(R.drawable.ic_privacy_shield)
    val tickPainter = painterResource(R.drawable.ic_privacy_tick)
    val lockPainter = painterResource(R.drawable.ic_privacy_lock)

    val tickVOffset = 0

    val contrast = 1.5f
    val brightness = 15f
    val colorFilter = if (isSystemInDarkTheme()) {
        val colorMatrix = floatArrayOf(
            contrast, 0f, 0f, 0f, brightness,
            0f, contrast, 0f, 0f, brightness,
            0f, 0f, contrast, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        )
        ColorFilter.colorMatrix(ColorMatrix(colorMatrix))
    } else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(y = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        // Shield with gaps (using BlendMode.Clear to cut holes)
        Canvas(
            modifier = Modifier
                .size(142.dp)
                .aspectRatio(0.8264463f)
                .graphicsLayer {
                    alpha = shieldAlpha
                    // We need a separate layer for BlendMode.Clear to work as expected
                    compositingStrategy = CompositingStrategy.Offscreen
                }
        ) {
            // Draw the shield XML
            with(shieldPainter) {
                draw(size)
            }

            // Subtract circles where the ticks will be
            // The radius animates so the "cut" appears when the ticks do
            val radiusPx = cutRadius.toPx()
            if (radiusPx > 0f) {
                val leftCenter =
                    Offset(
                        size.width * 0.5f - 65.dp.toPx(),
                        size.height * 0.5f + tickVOffset.dp.toPx()
                    )
                val rightCenter =
                    Offset(
                        size.width * 0.5f + 65.dp.toPx(),
                        size.height * 0.5f + tickVOffset.dp.toPx()
                    )

                drawCircle(
                    color = Color.Transparent,
                    radius = radiusPx,
                    center = leftCenter,
                    blendMode = BlendMode.Clear
                )
                drawCircle(
                    color = Color.Transparent,
                    radius = radiusPx,
                    center = rightCenter,
                    blendMode = BlendMode.Clear
                )
            }
        }

        // Left Tick
        Image(
            painter = tickPainter,
            contentDescription = null,
            modifier = Modifier
                .offset(x = (-65).dp, y = tickVOffset.dp)
                .graphicsLayer {
                    alpha = checksAlpha
                    scaleX = checksScale
                    scaleY = checksScale
                }
                .size(45.dp),
            colorFilter = colorFilter
        )

        // Right Tick
        Image(
            painter = tickPainter,
            contentDescription = null,
            modifier = Modifier
                .offset(x = 65.dp, y = tickVOffset.dp)
                .graphicsLayer {
                    alpha = checksAlpha
                    scaleX = checksScale
                    scaleY = checksScale
                }
                .size(45.dp),
            colorFilter = colorFilter
        )

        // Central Lock
        Image(
            painter = lockPainter,
            contentDescription = null,
            modifier = Modifier
                .graphicsLayer {
                    alpha = lockAlpha
                    scaleX = lockScale
                    scaleY = lockScale
                }
                .size(85.dp),
            colorFilter = colorFilter
        )
    }
}

private data class OnboardingPage(
    val title: String,
    val description: String,
    val image: @Composable (() -> Unit)
)