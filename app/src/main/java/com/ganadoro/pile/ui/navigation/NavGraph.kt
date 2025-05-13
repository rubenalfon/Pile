package com.ganadoro.pile.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ganadoro.pile.ui.screens.home.HomeScreen

@Composable
fun NavGraph(modifier: Modifier = Modifier, navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoute.Home.path,
        modifier = modifier
    ) {
        addHomeScreen(navController = navController, navGraphBuilder = this)
    }
}

private fun addHomeScreen(
    navController: NavHostController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.composable(route = NavRoute.Home.path) {
        HomeScreen(
            navigateToEditPiles = {
                navController.navigate(NavRoute.EditPiles.path)
            },
//            navigateToCalendarEntry = { id ->
//                navController.navigate(
//                    NavRoute.CalendarEntry.withArgs(
//                        id.toString()
//                    )
//                )
//            }
        )

    }
}