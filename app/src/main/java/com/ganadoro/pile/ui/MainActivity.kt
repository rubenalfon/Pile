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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ganadoro.pile.ui.navigation.NavGraph
import com.ganadoro.pile.ui.navigation.NavRoute
import com.ganadoro.pile.ui.theme.PileTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
                    val navController = rememberNavController()

                    LaunchedEffect(Unit) {
                        handleIntent(intent, navController)
                    }

                    NavGraph(navController = navController)
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
    private fun handleIntent(intent: Intent?, navController: NavHostController) {
        if (intent?.hasExtra("NEW_PDF_ID") == true) {
            val tempFile = intent.getStringExtra("NEW_PDF_ID")

            if (!tempFile.isNullOrBlank()) {
                val encodedDestination = NavRoute.AddDocumentRoute.withArgs(tempFile)
                navController.navigate(
                    NavRoute.EditPDFRoute.withArgs(
                        tempFile,
                        URLEncoder.encode(encodedDestination, StandardCharsets.UTF_8.toString()),
                        /*inclusive = */false.toString()
                    )
                )

                intent.removeExtra("NEW_PDF_ID")
            }
        }
    }
}