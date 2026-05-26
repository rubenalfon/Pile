package com.ganadoro.pile.core.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.ganadoro.pile.core.domain.models.AppTheme
import com.ganadoro.pile.core.domain.repositories.BitmapCacheRepository
import com.ganadoro.pile.core.ui.navigation.Pane
import com.ganadoro.pile.core.ui.navigation.PileNavigation
import com.ganadoro.pile.core.ui.theme.PileTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.java.KoinJavaComponent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mainViewModel: MainViewModel by viewModel()

        setContent {
            val settings by mainViewModel.uiState.collectAsStateWithLifecycle()

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
                    val backStack = rememberNavBackStack(Pane.Home)

                    LaunchedEffect(Unit) {
                        handleIntent(intent, backStack)
                    }

                    PileNavigation(
                        backStack = backStack
                    )
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        recreate()
    }

    /**
     * Una función de ayuda para procesar el intent y evitar duplicar código.
     */
    private fun handleIntent(intent: Intent?, backStack: NavBackStack<NavKey>) {
        if (intent?.hasExtra("NEW_PDF_ID") == true) {
            val tempFile = intent.getStringExtra("NEW_PDF_ID")

            if (!tempFile.isNullOrBlank()) {
                backStack.add(
                    Pane.EditNewDocument(
                        documentId = tempFile
                    )
                )

                intent.removeExtra("NEW_PDF_ID")
            }
        }
    }
}