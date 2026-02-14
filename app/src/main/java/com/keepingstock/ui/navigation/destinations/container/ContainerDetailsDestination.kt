package com.keepingstock.ui.navigation.destinations.container

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Routes
import com.keepingstock.core.contracts.uistates.container.ContainerDetailUiState
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.containerIdOrNull
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.container.ContainerDetailScreen


/**
 * Stupid name because Android Studio kept constantly forgetting that this exists and kept
 * trying to use the ContainerBrowserDestination's DemoMode enum. Renaming was easier than
 * diagnosing the underlying issue, especially since it is meant to be temporary.
 *
 * TODO(REMOVE): Demo-only state toggles. Delete when ContainerDetailViewModel is implemented.
 */
private enum class DemoMode_ConDeets {
    READY,
    LOADING,
    ERROR
}

internal fun NavGraphBuilder.addContainerDetailsDestination(
    deps: NavDeps
) {
    // Register the ContainerDetails destination: when route == "container_details" with
    // containerId, show ContainerDetailScreen
    composable(
        route = NavRoute.ContainerDetail.route,
        arguments = listOf(
            navArgument(Routes.Args.CONTAINER_ID) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val containerId =
            backStackEntry.arguments?.containerIdOrNull(Routes.Args.CONTAINER_ID)
            ?: error("Missing containerId")

        // TODO(REMOVE): Demo-only mode selector
        var demoMode by rememberSaveable(containerId.value) {
            mutableStateOf(DemoMode_ConDeets.READY)
        }

        // TODO(REMOVE): Replace this with ViewModel uiState.
        val uiState = remember(containerId, demoMode) {
            when (demoMode) {
                DemoMode_ConDeets.LOADING -> ContainerDetailUiState.Loading
                DemoMode_ConDeets.ERROR -> ContainerDetailUiState.Error("Demo error loading container details.")
                DemoMode_ConDeets.READY -> demoContainerDetailReadyState(containerId)
            }
        }

        LaunchedEffect(containerId) {
            deps.onTopBarChange(
                TopBarConfig(
                    title = "Container $containerId Details",
                    showBack = true
                )
            )
        }

        ContainerDetailScreen(
            containerId = containerId,
            onBack = { deps.navController.popBackStack() },
            onEdit = { id ->
                deps.navController.navigate(NavRoute.AddEditContainer.createRoute(containerId = id))
            }
        )
    }
}

/**
 * Builds top bar title/back behavior from ContainerDetailUiState.
 */
private fun containerDetailTopBarConfig(uiState: ContainerDetailUiState): TopBarConfig {

}

/**
 * TODO(REMOVE): Demo-only Ready state builder.
 */
private fun demoContainerDetailReadyState(containerId: ContainerId): ContainerDetailUiState.Ready {

}

/**
 * TODO(REMOVE): Demo-only toggle UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DemoModeToggleRow(
    selected: DemoMode_ConDeets,
    onSelect: (DemoMode_ConDeets) -> Unit,
    modifier: Modifier = Modifier,
) {

}

/**
 * TODO(REMOVE): Demo-only chip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DemoChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    
}