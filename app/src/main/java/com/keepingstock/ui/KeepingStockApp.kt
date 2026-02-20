package com.keepingstock.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.keepingstock.ui.navigation.AppNavGraph
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.scaffold.TopBarConfig
import kotlinx.coroutines.launch

/**
 * Root composable for the Keeping Stock app
 *
 * Defines the global UI shell for all screens. Includes a single shared NavController and
 * a global scaffold for the top bar, bottom bar, and snackbars.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = false, showBackground = true)
@Composable
fun KeepingStockApp() {
    // Single NavController instance for the entire app. Owned here so global UI can trigger
    // navigation (i.e. top bar and bottom bar)
    val navController = rememberNavController()

    // Holds snackbar queue and state across recompositions
    val snackbarHostState = remember { SnackbarHostState() }

    // Coroutine tied to this composable so I can launch suspend functions from UI callbacks
    val scope = rememberCoroutineScope()

    // Helper function for displaying snackbars from anywhere in the UI layer.
    val showSnackbar: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    // Current configuration of the top app bar. Updated by AppNavGraph when destination changes
    var topBarConfig by remember {
        mutableStateOf(TopBarConfig(title = "Keeping Stock"))
    }

    // For bottom navigation - observes current navigation destination
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Global UI
    Scaffold(
        topBar = {
            KeepingStockTopBar(
                config = topBarConfig,
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            KeepingStockBottomBar(
                currentDestination = currentDestination,
                onGoContainers = {
                    navController.navigate(NavRoute.ContainerBrowser.createRoute(null)) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onGoItems = {
                    navController.navigate(NavRoute.ItemBrowser.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onGoScan = {
                    navController.navigate(NavRoute.QRScan.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState)}
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier,
            contentPadding = innerPadding,
            onTopBarChange = { topBarConfig = it },
            showSnackbar = showSnackbar
        )
    }
}

/**
 * The App's Top Bar composable.
 *
 * Displays
 * - Current screen's title
 * - Optional back button controlled by TopBarConfig
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeepingStockTopBar(
    config: TopBarConfig,
    onBack: () -> Unit
) {
    TopAppBar(
        title = { Text(config.title) },
        navigationIcon = {
            if (config.showBack) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        }
    )
}

/**
 * Persistent bottom navigation bar for all screens
 *
 * TODO: Must decide what to do if navigating away from unfinished forms!
 *
 * Provides direct navigation to:
 * - Container Browser
 * - Item Browser
 * - QR Scanner (or camera)
 *
 * Select is derived from the current NavDestination hierarchy
 */
@Composable
private fun KeepingStockBottomBar(
    currentDestination: NavDestination?,
    onGoContainers: () -> Unit,
    onGoItems: () -> Unit,
    onGoScan: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentDestination?.hierarchy?.any { it.route == NavRoute.ContainerBrowser.route } == true,
            onClick = onGoContainers,
            icon = { Icon(Icons.Default.Inventory, contentDescription = "Containers") },
            label = { Text("Containers") }
        )
        NavigationBarItem(
            selected = currentDestination?.hierarchy?.any { it.route == NavRoute.ItemBrowser.route } == true,
            onClick = onGoItems,
            icon = { Icon(Icons.Default.Category, contentDescription = "Items") },
            label = { Text("Items") }
        )
        NavigationBarItem(
            selected = currentDestination?.hierarchy?.any { it.route == NavRoute.QRScan.route } == true,
            onClick = onGoScan,
            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan") },
            label = { Text("Scan") }
        )
    }
}