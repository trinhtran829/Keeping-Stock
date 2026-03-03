package com.keepingstock.ui.navigation.destinations.debug

import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.debug.DebugGalleryScreen
import kotlinx.coroutines.launch

internal fun NavGraphBuilder.addDebugGalleryDestination(
    deps: NavDeps
) {
    composable(route = NavRoute.DebugGallery.route) {
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            deps.onTopBarChange(
                TopBarConfig(
                    title = "Debug Gallery",
                    showBack = false
                )
            )
        }

        DebugGalleryScreen(
            onLoadDemoData = {
                scope.launch {
                    runCatching { deps.demoDataManager.loadDemoData() }
                        .onSuccess { deps.showSnackbar("Demo data added.") }
                        .onFailure { deps.showSnackbar("Load failed: ${it.message}") }
                }
            },
            onResetToDemoData = {
                scope.launch {
                    runCatching { deps.demoDataManager.resetToDemoData() }
                        .onSuccess { deps.showSnackbar("Demo data reset.") }
                        .onFailure { deps.showSnackbar("Reset failed: ${it.message}") }
                }
            },
            onClearAllData = {
                scope.launch {
                    runCatching { deps.demoDataManager.clearAllData() }
                        .onSuccess { deps.showSnackbar("All data cleared.") }
                        .onFailure { deps.showSnackbar("Clear failed: ${it.message}") }
                }
            },
            onOpenContainerBrowser = {
                deps.navController.navigate(NavRoute.ContainerBrowser.createRoute(null))
            },
            onOpenItemBrowser = {
                deps.navController.navigate(NavRoute.ItemBrowser.route)
            },
            onOpenQrScan = { deps.navController.navigate(NavRoute.QRScan.route) },
            onOpenCamera = { deps.navController.navigate(NavRoute.Camera.route) },
            onOpenGallery = { deps.navController.navigate(NavRoute.Gallery.route) },

            // TODO: placeholder URI. Swap with real demo photo Uri later
            onOpenPhotoDemo = {
                val demo = Uri.parse("content://media/external/images/media/1")
                deps.navController.navigate(NavRoute.Photo.createRoute(demo))
            },
            onShowSnackbarDemo = {
                deps.showSnackbar("This is a demo snackbar message!")
            }
        )
    }
}
