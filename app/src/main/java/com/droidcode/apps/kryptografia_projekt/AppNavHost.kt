package com.droidcode.apps.kryptografia_projekt

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavHost(modifier: Modifier, navController: NavHostController) {
    val viewModel = MainScreenViewModel()

    NavHost(
        navController = navController,
        startDestination = Menu.route,
        modifier = modifier.padding()
    ) {
        composable(Menu.route) {
            MenuScreen(
                Modifier,
                { navController.navigateSingleTopTo(Encrypt.route) },
                { navController.navigateSingleTopTo(Decipher.route) }
            )
        }

        composable(Encrypt.route) {
            EncryptScreen(Modifier, viewModel) { navController.navigateUp() }
        }

        composable(Decipher.route) {
            DecipherScreen(Modifier, viewModel) { navController.navigateUp() }
        }
    }
}

fun NavController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
