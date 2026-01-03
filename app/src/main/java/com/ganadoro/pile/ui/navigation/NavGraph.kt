package com.ganadoro.pile.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ganadoro.pile.ui.screens.addDocument.AddDocumentScreen
import com.ganadoro.pile.ui.screens.documentDetail.DocumentDetailScreen
import com.ganadoro.pile.ui.screens.editPDF.EditPDFScreen
import com.ganadoro.pile.ui.screens.home.HomeScreen
import com.ganadoro.pile.ui.screens.pileDetail.PileDetailScreen
import com.ganadoro.pile.ui.screens.search.SearchBarScreen
import comganadoro.pile.ui.screens.editDocumentPiles.EditDocumentPilesScreen
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun NavGraph(modifier: Modifier = Modifier, navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoute.HomeRoute.path,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it / 5 }) + fadeIn(spring(stiffness = Spring.StiffnessMediumLow))
        },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 4 }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 4 }) },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it / 5 }) + fadeOut(spring(stiffness = Spring.StiffnessMedium))
        }
    ) {
        addHomeScreen(navController = navController, navGraphBuilder = this)

        addPileDetailScreen(navController = navController, navGraphBuilder = this)

        addDocumentDetailScreen(navController = navController, navGraphBuilder = this)

        addEditDocumentPilesScreen(navController = navController, navGraphBuilder = this)

        addEditPDFScreen(navController = navController, navGraphBuilder = this)

        addAddDocumentScreen(navController = navController, navGraphBuilder = this)

        addSearchScreen(navController = navController, navGraphBuilder = this)
    }
}

private fun addHomeScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.composable(route = NavRoute.HomeRoute.path) {
        HomeScreen(
            navigateToPileDetail = { id ->
                navController.navigate(
                    NavRoute.PileDetailRoute.withArgs(
                        id
                    )
                )
            },
            navigateToDocumentDetail = { id ->
                navController.navigate(
                    NavRoute.DocumentDetailRoute.withArgs(
                        id
                    )
                )
            },
            navigateToEditPDF = { id ->
                val encodedDestination = NavRoute.AddDocumentRoute.withArgs(id)
                navController.navigate(
                    NavRoute.EditPDFRoute.withArgs(
                        id,
                        URLEncoder.encode(encodedDestination, StandardCharsets.UTF_8.toString()),
                        /*inclusive = */false.toString()
                    )
                )
            }
        )
    }
}

private fun addPileDetailScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.composable(
        route = NavRoute.PileDetailRoute.withArgsFormat(NavRoute.PileDetailRoute.PILE_ID_KEY),
        arguments = listOf(navArgument(NavRoute.PileDetailRoute.PILE_ID_KEY) {
            type = NavType.StringType
        })
    ) { navBackStackEntry ->
        val args = navBackStackEntry.arguments
        val pileID = args?.getString(NavRoute.PileDetailRoute.PILE_ID_KEY)

        if (pileID.isNullOrBlank()) {
            navController.popBackStack()
            return@composable
        }

        PileDetailScreen(
            pileID = pileID,
            navigateToDocumentDetail = {
                navController.navigate(
                    NavRoute.DocumentDetailRoute.withArgs(
                        it
                    )
                )
            },
            navigateToSearchScreen = {
                navController.navigate(
                    NavRoute.SearchRoute.path
                )
            },
            popBackStack = {
                navController.popBackStack()
            }
        )
    }
}

private fun addDocumentDetailScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.composable(
        route = NavRoute.DocumentDetailRoute.withArgsFormat(NavRoute.DocumentDetailRoute.DOCUMENT_ID_KEY),
        arguments = listOf(navArgument(NavRoute.DocumentDetailRoute.DOCUMENT_ID_KEY) {
            type = NavType.StringType
        })
    ) { navBackStackEntry ->
        val args = navBackStackEntry.arguments
        val documentId = args?.getString(NavRoute.DocumentDetailRoute.DOCUMENT_ID_KEY)

        if (documentId.isNullOrBlank()) {
            navController.popBackStack()
            return@composable
        }

        DocumentDetailScreen(
            documentId = documentId,
            navigateToPileDetail = { id ->
                navController.navigate(
                    NavRoute.PileDetailRoute.withArgs(
                        id
                    )
                )
            },
            navigateToEditDocument = { id ->
                val encodedDestination = NavRoute.DocumentDetailRoute.withArgs(id)

                navController.navigate(
                    NavRoute.EditPDFRoute.withArgs(
                        id,
                        URLEncoder.encode(encodedDestination, StandardCharsets.UTF_8.toString()),
                        /* inclusive = */true.toString()
                    )
                )
            },
            navigateToEditDocumentPiles = { id ->
                navController.navigate(
                    NavRoute.EditDocumentPilesRoute.withArgs(
                        id
                    )
                )
            },
            popBackStack = {
                navController.popBackStack()
            }
        )
    }
}

private fun addEditDocumentPilesScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.composable(
        route = NavRoute.EditDocumentPilesRoute.withArgsFormat(NavRoute.EditDocumentPilesRoute.DOCUMENT_ID_KEY),
        arguments = listOf(navArgument(NavRoute.EditDocumentPilesRoute.DOCUMENT_ID_KEY) {
            type = NavType.StringType
        })
    ) {
        val args = it.arguments
        val documentId = args?.getString(NavRoute.EditDocumentPilesRoute.DOCUMENT_ID_KEY)

        if (documentId.isNullOrBlank()) {
            navController.popBackStack()
            return@composable
        }

        EditDocumentPilesScreen(
            documentId = documentId,
            popBackStack = {
                navController.popBackStack()
            }
        )
    }
}

private fun addEditPDFScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.composable(
        route = NavRoute.EditPDFRoute.withArgsFormat(
            NavRoute.EditPDFRoute.DOCUMENT_ID_KEY,
            NavRoute.EditPDFRoute.DESTINATION_KEY,
            NavRoute.EditPDFRoute.INCLUSIVE_KEY
        ),
        arguments = listOf(
            navArgument(NavRoute.EditPDFRoute.DOCUMENT_ID_KEY) { type = NavType.StringType },
            navArgument(NavRoute.EditPDFRoute.DESTINATION_KEY) { type = NavType.StringType },
            navArgument(NavRoute.EditPDFRoute.INCLUSIVE_KEY) { type = NavType.BoolType }
        )
    ) { navBackStackEntry ->
        val args = navBackStackEntry.arguments
        val documentId = args?.getString(NavRoute.EditPDFRoute.DOCUMENT_ID_KEY)
        val destination = args?.getString(NavRoute.EditPDFRoute.DESTINATION_KEY)
        val destinationInclusive = args?.getBoolean(NavRoute.EditPDFRoute.INCLUSIVE_KEY)

        if (documentId.isNullOrBlank() || destination.isNullOrBlank()) {
            navController.popBackStack()
            return@composable
        }

        EditPDFScreen(
            documentId = documentId,
            popBackStack = {
                navController.popBackStack()
            },
            onNext = {
                navController.navigate(
                    destination
                ) {
                    if (destinationInclusive == true) {
                        popUpTo(route = NavRoute.HomeRoute.path) {
                            inclusive = false
                        }
                    }
                }
            }
        )
    }
}

private fun addAddDocumentScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.composable(
        route = NavRoute.AddDocumentRoute.withArgsFormat(NavRoute.AddDocumentRoute.DOCUMENT_ID_KEY),
        arguments = listOf(navArgument(NavRoute.AddDocumentRoute.DOCUMENT_ID_KEY) {
            type = NavType.StringType
        })
    ) { navBackStackEntry ->
        val args = navBackStackEntry.arguments
        val documentId = args?.getString(NavRoute.AddDocumentRoute.DOCUMENT_ID_KEY)

        if (documentId.isNullOrBlank()) {
            navController.popBackStack()
            return@composable
        }

        AddDocumentScreen(
            documentId = documentId,
            popBackStack = {
                navController.popBackStack()
            },
            navigateToDocumentDetail = { id ->
                navController.navigate(
                    NavRoute.DocumentDetailRoute.withArgs(
                        id
                    )
                ) {
                    popUpTo(route = NavRoute.HomeRoute.path) {
                        inclusive = false
                    }
                }
            }
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
private fun addSearchScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.composable(route = NavRoute.SearchRoute.path) {
        val showKeyboard = remember { mutableStateOf(true) }
        val focusRequester = remember { FocusRequester() }
        val keyboard = LocalSoftwareKeyboardController.current

        LaunchedEffect(focusRequester) {
            if (showKeyboard.value) {
                focusRequester.requestFocus()
                delay(100)
                keyboard?.show()
            }
        }

        Scaffold(
            contentWindowInsets = WindowInsets.displayCutout,
            topBar = {
                SearchBarScreen(
                    expanded = true,
                    onExpandedChange = {
                        if (!it) navController.popBackStack()
                    },
                    onSettingsClick = {}, // Do not
                    navigateToDocumentDetail = { id ->
                        navController.navigate(
                            NavRoute.DocumentDetailRoute.withArgs(
                                id
                            )
                        )
                    },
                    focusRequester = focusRequester
                )
            },
            content = {}
        )
    }
}

