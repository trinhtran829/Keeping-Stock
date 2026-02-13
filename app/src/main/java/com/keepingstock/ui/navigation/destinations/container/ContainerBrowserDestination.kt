package com.keepingstock.ui.navigation.destinations.container

import android.util.Log
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.Routes
import com.keepingstock.core.contracts.uistates.container.ContainerBrowserUiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.ui.navigation.NavDeps
import com.keepingstock.ui.navigation.NavRoute
import com.keepingstock.ui.navigation.containerIdOrNull
import com.keepingstock.ui.scaffold.TopBarConfig
import com.keepingstock.ui.screens.container.ContainerBrowserScreen

private enum class DemoMode {
    POPULATED,
    EMPTY,
    LOADING,
    ERROR
}

internal fun NavGraphBuilder.addContainerBrowserDestination(
    deps: NavDeps,
    lastContainerIdState: MutableState<ContainerId?>
) {
    // Register the ContainerBrowser destination: when route == "container_browser" with or
    // without the containerId, show ContainerBrowser of that container (or root)
    composable(
        route = NavRoute.ContainerBrowser.route,
        arguments = listOf(
            navArgument(Routes.Args.CONTAINER_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val containerId =
            backStackEntry.arguments?.containerIdOrNull(Routes.Args.CONTAINER_ID)

        // TODO: remove at launch?
        var demoMode by rememberSaveable(containerId?.value) { // key per container
            mutableStateOf(DemoMode.POPULATED)
        }

        val uiState = remember(containerId, demoMode) {
            when (demoMode) {
                DemoMode.LOADING -> ContainerBrowserUiState.Loading
                DemoMode.ERROR -> ContainerBrowserUiState.Error("Demo error loading container.")
                DemoMode.EMPTY -> demoContainerBrowserReadyState(containerId, empty = true)
                DemoMode.POPULATED -> demoContainerBrowserReadyState(containerId, empty = false)
            }
        }
        val topBarConfig = remember(uiState) { containerBrowserTopBarConfig(uiState) }

        LaunchedEffect(containerId) {
            deps.onTopBarChange(topBarConfig)
        }

        // Track the last visited container for "Return to Containers" behavior.
        lastContainerIdState.value = containerId

        // TODO: temporary column of rows for togglable demo of states. remove when ViewModel
        //      is implemented.
        Column (Modifier.fillMaxSize()) {
            DemoModeToggleRow(
                selected = demoMode,
                onSelect = { demoMode = it }
            )

            ContainerBrowserScreen(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onOpenSubcontainer = { subId ->
                    val route = NavRoute.ContainerBrowser.createRoute(subId)
                    Log.d("Nav", "Navigating to: $route")
                    deps.navController.navigate(NavRoute.ContainerBrowser.createRoute(subId))
                },
                onOpenItem = { itemId ->
                    deps.navController.navigate(NavRoute.ItemDetails.createRoute(itemId))
                },
                onOpenContainerInfo = { id ->
                    deps.navController.navigate(NavRoute.ContainerDetail.createRoute(id))
                },
                onAddContainer = { parentId ->
                    deps.navController.navigate(
                        NavRoute.AddEditContainer.createRoute(parentContainerId = parentId)
                    )
                },
                onAddItem = { cid ->
                    deps.navController.navigate(NavRoute.AddEditItem.createRoute(containerId = cid))
                }
            )
        }
    }
}

/**
 *
 */
private fun containerBrowserTopBarConfig(uiState: ContainerBrowserUiState): TopBarConfig {
    val title = when (uiState) {
        is ContainerBrowserUiState.Ready -> uiState.containerName
        is ContainerBrowserUiState.Loading -> "Loading…"
        is ContainerBrowserUiState.Error -> "Containers"
    }

    val showBack = (uiState is ContainerBrowserUiState.Ready && uiState.containerId != null)

    return TopBarConfig(title = title, showBack = showBack)
}

/**
 * ---
 * GenAI usage citation:
 * This example UiState was generated with the assistance of ChatGPT.
 */
private fun demoContainerBrowserReadyState(
    containerId: ContainerId?,
    empty: Boolean
): ContainerBrowserUiState.Ready {
    // Simple deterministic demo data based on whether we're at root or inside a container.
    return if (containerId == null) {
        ContainerBrowserUiState.Ready(
            containerId = null,
            containerName = "All Containers",
            subcontainers = if (empty) emptyList() else listOf(
                Container(
                    id = ContainerId(1L),
                    name = "Garage",
                    description = "Tools and hardware",
                    parentContainerId = null
                ),
                Container(
                    id = ContainerId(2L),
                    name = "Kitchen",
                    description = "Appliances and misc",
                    parentContainerId = null
                ),
            ),
            items = emptyList()
        )
    } else {
        ContainerBrowserUiState.Ready(
            containerId = containerId,
            containerName = "Container ${containerId.value}",
            subcontainers = if (empty) emptyList() else listOf(
                Container(
                    id = ContainerId(containerId.value * 10 + 1),
                    name = "Subcontainer A",
                    description = "Example subcontainer",
                    parentContainerId = containerId
                ),
                Container(
                    id = ContainerId(containerId.value * 10 + 2),
                    name = "Subcontainer B",
                    description = "Another subcontainer",
                    parentContainerId = containerId
                )
            ),
            items = if (empty) emptyList() else listOf(
                Item(
                    id = ItemId(containerId.value * 100 + 1),
                    name = "Impact Driver",
                    description = "18V brushless",
                    containerId = containerId,
                    status = ItemStatus.STORED
                ),
                Item(
                    id = ItemId(containerId.value * 100 + 2),
                    name = "Reciprocating Saw",
                    description = "Corded",
                    containerId = containerId,
                    status = ItemStatus.STORED
                ),
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DemoModeToggleRow(
    selected: DemoMode,
    onSelect: (DemoMode) -> Unit,
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
            DemoChip("Populated", selected == DemoMode.POPULATED) {
                onSelect(DemoMode.POPULATED)
            }
            DemoChip("Empty", selected == DemoMode.EMPTY) {
                onSelect(DemoMode.EMPTY)
            }
            DemoChip("Loading", selected == DemoMode.LOADING) {
                onSelect(DemoMode.LOADING)
            }
            DemoChip("Error", selected == DemoMode.ERROR) {
                onSelect(DemoMode.ERROR)
            }
        }
    }
}

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