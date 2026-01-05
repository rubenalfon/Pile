package com.ganadoro.pile.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.ganadoro.pile.ui.navigation.Pane
import com.ganadoro.pile.ui.navigation.PileNavigation
import com.ganadoro.pile.ui.theme.PileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PileTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
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
                    Pane.EditNewPDF(
                        documentId = tempFile
                    )
                )

                intent.removeExtra("NEW_PDF_ID")
            }
        }
    }
}