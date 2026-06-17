package es.pile.core.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import es.pile.core.domain.models.AppTheme
import es.pile.core.domain.repositories.BitmapCacheRepository
import es.pile.core.ui.navigation.Pane
import es.pile.core.ui.navigation.PileNavigation
import es.pile.core.ui.theme.PileTheme
import es.pile.features.onboarding.ui.OnboardingScreen
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.java.KoinJavaComponent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mainViewModel: MainViewModel by viewModel()

        setContent {
            val state by mainViewModel.state.collectAsStateWithLifecycle()
            val settings = state.settings
            val appPreferences = state.appPreferences

            val useDarkTheme = when (settings.theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            PileTheme(useDarkTheme, settings.isMaterialColor) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    AnimatedContent(
                        targetState = appPreferences.isOnboardingCompleted,
                        transitionSpec = {
                            if (targetState) {
                                (fadeIn(tween(300)) + scaleIn(
                                    initialScale = 0.95f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )).togetherWith(fadeOut(tween(200)))
                            } else {
                                fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                            }
                        },
                        label = "OnboardingToHomeTransition"
                    ) { onboardingCompleted ->
                        if (onboardingCompleted) {
                            val backStack = rememberNavBackStack(Pane.Home)

                            LaunchedEffect(Unit) {
                                handleIntent(intent, backStack)
                            }

                            PileNavigation(
                                backStack = backStack
                            )
                        } else {
                            OnboardingScreen()
                        }
                    }
                }
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        val bitmapCacheRepository = KoinJavaComponent.getKoin().get<BitmapCacheRepository>()

        if (level == TRIM_MEMORY_UI_HIDDEN || level == TRIM_MEMORY_BACKGROUND) {
            bitmapCacheRepository.clearCache()
        }
    }

    companion object {
        const val EXTRA_NEW_DOCUMENT_ID = "NEW_DOCUMENT_ID"
        const val EXTRA_IS_PDF = "IS_PDF"
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        recreate()
    }

    /**
     * A function that helps to process the intent.
     */
    private fun handleIntent(intent: Intent?, backStack: NavBackStack<NavKey>) {
        if (intent?.hasExtra(EXTRA_NEW_DOCUMENT_ID) == true) {
            val documentId = intent.getStringExtra(EXTRA_NEW_DOCUMENT_ID)
            val isPdf = intent.getBooleanExtra(EXTRA_IS_PDF, false)

            if (!documentId.isNullOrBlank()) {
                if (isPdf) {
                    backStack.add(Pane.AddDocument(documentId = documentId))
                } else {
                    backStack.add(Pane.EditNewDocument(documentId = documentId))
                }
                intent.removeExtra(EXTRA_NEW_DOCUMENT_ID)
                intent.removeExtra(EXTRA_IS_PDF)
            }
        }
    }
}