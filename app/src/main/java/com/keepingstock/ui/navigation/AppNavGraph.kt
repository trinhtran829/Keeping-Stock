package com.keepingstock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.keepingstock.ui.screens.item.ItemBrowserScreen

@Composable
fun AppNavGraph() {
    // The navigation manager that tracks current screen and back stack
    val navController = rememberNavController()

    // The place in UI where the active destination composable is displayed
    NavHost(
        navController = navController,
        startDestination = NavRoute.ItemBrowser.route
    ) {
        // Register the ItemBrowser destination: when route == "item_browser", show ItemBrowserScreen
        composable(route = NavRoute.ItemBrowser.route) {
            ItemBrowserScreen()
        }
    }
}