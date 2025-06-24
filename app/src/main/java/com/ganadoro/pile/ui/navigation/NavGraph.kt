package com.ganadoro.pile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

@Composable
fun NavGraph(modifier: Modifier = Modifier, navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoute.Home.path,
        modifier = modifier
    ) {
        addHomeScreen(navController = navController, navGraphBuilder = this)

        addPileDetailScreen(navController = navController, navGraphBuilder = this)

        addDocumentDetailScreen(navController = navController, navGraphBuilder = this)

        addEditPDFScreen(navController = navController, navGraphBuilder = this)

        addAddDocumentScreen(navController = navController, navGraphBuilder = this)
    }
}

private fun addHomeScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.composable(route = NavRoute.Home.path) {
        HomeScreen(
            navigateToPileDetail = { id ->
                navController.navigate(
                    NavRoute.PileDetail.withArgs(
                        id
                    )
                )
            },
            navigateToDocumentDetail = { id ->
                navController.navigate(
                    NavRoute.DocumentDetail.withArgs(
                        id
                    )
                )
            },
            navigateToEditPDF = { id ->
                navController.navigate(
                    NavRoute.EditPDF.withArgs(
                        id
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
        route = NavRoute.PileDetail.withArgsFormat(NavRoute.PileDetail.PILE_ID_KEY),
        arguments = listOf(navArgument(NavRoute.PileDetail.PILE_ID_KEY) {
            type = NavType.StringType
        })
    ) { navBackStackEntry ->
        val args = navBackStackEntry.arguments
        val pileID = args?.getString(NavRoute.PileDetail.PILE_ID_KEY)

        if (pileID.isNullOrBlank()) {
            navController.popBackStack()
            return@composable
        }

        PileDetailScreen(
            pileID = pileID,
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
        route = NavRoute.DocumentDetail.withArgsFormat(NavRoute.DocumentDetail.DOCUMENT_ID_KEY),
        arguments = listOf(navArgument(NavRoute.DocumentDetail.DOCUMENT_ID_KEY) {
            type = NavType.StringType
        })
    ) { navBackStackEntry ->
        val args = navBackStackEntry.arguments
        val documentId = args?.getString(NavRoute.DocumentDetail.DOCUMENT_ID_KEY)

        if (documentId.isNullOrBlank()) {
            navController.popBackStack()
            return@composable
        }

        DocumentDetailScreen(
            documentId = documentId,
            navigateToPileDetail = { id ->
                navController.navigate(
                    NavRoute.PileDetail.withArgs(
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

private fun addEditPDFScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.composable(
        route = NavRoute.EditPDF.withArgsFormat(NavRoute.EditPDF.DOCUMENT_ID_KEY),
        arguments = listOf(navArgument(NavRoute.EditPDF.DOCUMENT_ID_KEY) {
            type = NavType.StringType
        })
    ) { navBackStackEntry ->
        val args = navBackStackEntry.arguments
        val documentId = args?.getString(NavRoute.EditPDF.DOCUMENT_ID_KEY)

        if (documentId.isNullOrBlank()) {
            navController.popBackStack()
            return@composable
        }

        EditPDFScreen(
            documentId = documentId,
            popBackStack = {
                navController.popBackStack()
            },
            navigateToAddDocument = {
                navController.navigate(
                    NavRoute.AddDocument.withArgs(
                        documentId
                    )
                )
            }
        )
    }
}

private fun addAddDocumentScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.composable(
        route = NavRoute.AddDocument.withArgsFormat(NavRoute.AddDocument.DOCUMENT_ID_KEY),
        arguments = listOf(navArgument(NavRoute.AddDocument.DOCUMENT_ID_KEY) {
            type = NavType.StringType
        })
    ) { navBackStackEntry ->
        val args = navBackStackEntry.arguments
        val documentId = args?.getString(NavRoute.AddDocument.DOCUMENT_ID_KEY)

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
                    NavRoute.DocumentDetail.withArgs(
                        id
                    )
                ) {
                    popUpTo(route = NavRoute.Home.path) {
                        inclusive = false
                    }
                }
            }
        )
    }
}