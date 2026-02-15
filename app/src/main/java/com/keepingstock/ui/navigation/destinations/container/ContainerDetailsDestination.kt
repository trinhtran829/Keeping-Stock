package com.keepingstock.ui.navigation.destinations.container

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Routes
import com.keepingstock.core.contracts.uistates.container.ContainerDetailUiState
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.containerIdOrNull
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.container.ContainerDetailScreen
import com.keepingstock.ui.components.navigation.ChipOption
import com.keepingstock.ui.components.navigation.DemoMode
import com.keepingstock.ui.components.navigation.DemoModeToggleRow

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
            mutableStateOf(DemoMode.READY)
        }

        // TODO(REMOVE): Replace this with ViewModel uiState.
        val uiState = remember(containerId, demoMode) {
            when (demoMode) {
                DemoMode.LOADING -> ContainerDetailUiState.Loading
                DemoMode.READY, DemoMode.POPULATED -> demoContainerDetailReadyState(containerId)
                DemoMode.ERROR, DemoMode.EMPTY ->
                    ContainerDetailUiState.Error("Demo error loading container details.")
            }
        }

        val topBarConfig = remember(uiState) { containerDetailTopBarConfig(uiState) }

        LaunchedEffect(topBarConfig) {
            deps.onTopBarChange(topBarConfig)
        }

        Column(Modifier.fillMaxSize()) {
            // TODO(REMOVE): demo-only UI controls.
            DemoModeToggleRow(
                title = "Select demo mode:",
                selected = demoMode,
                options = listOf (
                    ChipOption(DemoMode.READY, "Ready"),
                    ChipOption(DemoMode.LOADING, "Loading"),
                    ChipOption(DemoMode.ERROR, "Error")
                ),
                onSelect = { demoMode = it }
            )

            ContainerDetailScreen(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onBack = { deps.navController.popBackStack() },
                onEdit = { id ->
                    deps.navController.navigate(
                        NavRoute.AddEditContainer.createRoute(containerId = id)
                    )
                },
                onMove = { /* TODO: hook up when Move flow exists */ },
                onDelete = { /* TODO: hook up when Delete flow exists */ }
            )
        }
    }
}

/**
 * Builds top bar title/back behavior from ContainerDetailUiState.
 */
private fun containerDetailTopBarConfig(uiState: ContainerDetailUiState): TopBarConfig {
    val title = when (uiState) {
        is ContainerDetailUiState.Ready ->  "Container Details"//uiState.containerName + " Details"
        is ContainerDetailUiState.Loading -> "Loading…"
        is ContainerDetailUiState.Error -> "Container details"
    }
    return TopBarConfig(title = title, showBack = true)
}

/**
 * TODO(REMOVE): Demo-only Ready state builder.
 * ---
 * GenAI usage citation:
 * Sample container detail data auto-generated with the help of ChatGPT.
 * Prompt: "Please generate data for a sample object with the following class signature:"
 */
private fun demoContainerDetailReadyState(containerId: ContainerId): ContainerDetailUiState.Ready {
    return ContainerDetailUiState.Ready(
        containerId = containerId,
        container = Container(
            id = containerId,
            name = "Container ${containerId.value}",
            description = "Example container detail description.",
            imageUri = "demo",
            parentContainerId = null,
        ),
        parentContainerName = null,
        subcontainerCount = 2,
        itemCount = 5,
        canDelete = false,
        deleteBlockedReason = "Container must be empty to delete."
    )
}