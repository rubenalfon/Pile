package com.ganadoro.pile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ganadoro.pile.ui.screens.pileDetail.PileDetailScreen
import com.ganadoro.pile.ui.screens.home.HomeScreen
import java.util.UUID

@Composable
fun NavGraph(modifier: Modifier = Modifier, navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoute.Home.path,
        modifier = modifier
    ) {
        addHomeScreen(navController = navController, navGraphBuilder = this)
        addPileDetailScreen(navController = navController, navGraphBuilder = this)
    }
}

private fun addHomeScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.composable(route = NavRoute.Home.path) {
        HomeScreen(
            navigateToEditPiles = { id ->
                navController.navigate(
                    NavRoute.PileDetail.withArgs(
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
        route = NavRoute.PileDetail.withArgsFormat(NavRoute.PileDetail.id),
        arguments = listOf(navArgument(NavRoute.PileDetail.id) {
            type = NavType.StringType
        })
    ) { navBackStackEntry ->
        val args = navBackStackEntry.arguments
        PileDetailScreen(
            id = UUID.fromString(args?.getString(NavRoute.PileDetail.id)),
            popBackStack = {
                navController.popBackStack()
            }
        )
    }
}