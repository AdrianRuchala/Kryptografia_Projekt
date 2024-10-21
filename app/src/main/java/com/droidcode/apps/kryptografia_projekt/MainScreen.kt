package com.droidcode.apps.kryptografia_projekt

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun MainScreen(modifier: Modifier, navController: NavHostController) {
    Scaffold { padding ->
        AppNavHost(modifier.padding(padding), navController)
    }
}
