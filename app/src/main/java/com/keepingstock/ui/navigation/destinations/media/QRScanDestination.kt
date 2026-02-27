package com.keepingstock.ui.navigation.destinations.media

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.keepingstock.data.integration.DemoContainerRepository
import com.keepingstock.platform.services.MlKitQrService
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.media.QRScanScreen
import com.keepingstock.ui.viewmodel.media.QrScanViewModel

internal fun NavGraphBuilder.addQRScanDestination(
    deps: NavDeps
) {
    composable(route = NavRoute.QRScan.route) {
        val context = LocalContext.current

        // Destination owns ViewModel creation so screen stays presentation-focused.
        val viewModel: QrScanViewModel = viewModel(
            factory = viewModelFactory {
                initializer {
                    QrScanViewModel(
                        qrService = MlKitQrService(context),
                        containerRepository = DemoContainerRepository()
                    )
                }
            }
        )

        // Collect state using lifecycle-aware API for Compose.
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        // Side-effect for top bar config when destination enters composition.
        LaunchedEffect(Unit) {
            deps.onTopBarChange(
                TopBarConfig(
                    title = "Scan QR",
                    showBack = true
                )
            )
        }

        QRScanScreen(
            viewModel = viewModel,
            uiState = uiState,
            onOpenScannedContainer = { scannedContainerId ->
                deps.navController.popBackStack()
                // Navigate to the container contents after a successful scan
                deps.navController.navigate(NavRoute.ContainerBrowser.createRoute(scannedContainerId))
            },
            onCancel = {
                viewModel.reset()
                deps.navController.popBackStack()
            }
        )
    }
}
