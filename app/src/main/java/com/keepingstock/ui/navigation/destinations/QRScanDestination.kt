package com.keepingstock.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.qr.QRScanScreen

internal fun NavGraphBuilder.addQRScanDestination(
    deps: NavDeps
) {
    composable(route = NavRoute.QRScan.route) {
        LaunchedEffect(Unit) {
            deps.onTopBarChange(
                TopBarConfig(
                    title = "Scan QR",
                    showBack = true
                )
            )
        }

        QRScanScreen(
            onScannedContainer = { scannedContainerId ->
                deps.navController.popBackStack()
                // Navigate to the container contents after a successful scan
                deps.navController.navigate(NavRoute.ContainerBrowser.createRoute(scannedContainerId))
            },
            onCancel = { deps.navController.popBackStack() }
        )
    }
}