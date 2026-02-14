package com.keepingstock.ui.navigation.destinations.container

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

        val topBarConfig = remember(uiState) { containerDetailTopBarConfig(uiState) }

        LaunchedEffect(topBarConfig) {
            deps.onTopBarChange(topBarConfig)
        }

        Column(Modifier.fillMaxSize()) {
            // TODO(REMOVE): demo-only UI controls.
            DemoModeToggleRow(
                selected = demoMode,
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
        is ContainerDetailUiState.Ready -> uiState.name
        is ContainerDetailUiState.Loading -> "Loading…"
        is ContainerDetailUiState.Error -> "Container details"
    }
    return TopBarConfig(title = title, showBack = true)
}

/**
 * TODO(REMOVE): Demo-only Ready state builder.
 */
private fun demoContainerDetailReadyState(containerId: ContainerId): ContainerDetailUiState.Ready {
    return ContainerDetailUiState.Ready(
        containerId = containerId,
        name = "Container ${containerId.value}",
        description = "Example container detail description.",
        imageUri = null,
        parentContainerId = null,
        subcontainerCount = 2,
        itemCount = 5,
        canDelete = false,
        deleteBlockedReason = "Container must be empty to delete."
    )
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "Select demo mode:",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DemoChip("Ready", selected == DemoMode_ConDeets.READY) {
                onSelect(DemoMode_ConDeets.READY)
            }
            DemoChip("Loading", selected == DemoMode_ConDeets.LOADING) {
                onSelect(DemoMode_ConDeets.LOADING)
            }
            DemoChip("Error", selected == DemoMode_ConDeets.ERROR) {
                onSelect(DemoMode_ConDeets.ERROR)
            }
        }
    }
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
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}