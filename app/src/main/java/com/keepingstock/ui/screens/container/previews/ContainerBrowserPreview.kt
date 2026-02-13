package com.keepingstock.ui.screens.container.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.uistates.container.ContainerBrowserUiState
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.ui.screens.container.ContainerBrowserScreen

/**
 * Provides previews for each UI State of the ContainerBrowserScreen
 */

@Preview(showBackground = true)
@Composable
private fun Preview_ContainerBrowser_Loading() {
    ContainerBrowserScreen(
        uiState = ContainerBrowserUiState.Loading,
        onOpenSubcontainer = {},
        onOpenItem = {},
        onOpenContainerInfo = {},
        onAddContainer = {},
        onAddItem = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview_ContainerBrowser_Error() {
    ContainerBrowserScreen(
        uiState = ContainerBrowserUiState.Error("Failed to load container."),
        onOpenSubcontainer = {},
        onOpenItem = {},
        onOpenContainerInfo = {},
        onAddContainer = {},
        onAddItem = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview_ContainerBrowser_EmptyReady() {
    ContainerBrowserScreen(
        uiState = ContainerBrowserUiState.Ready(
            containerId = ContainerId(1L),
                containerName = "Garage",
                subcontainers = emptyList(),
                items = emptyList()
            ),
        onOpenSubcontainer = {},
        onOpenItem = {},
        onOpenContainerInfo = {},
        onAddContainer = {},
        onAddItem = {},
    )
}

/**
 *
 * ---
 * GenAI usage citation:
 * Sample Items and Containers auto-generated with the help of ChatGPT.
 */
@Preview(showBackground = true)
@Composable
private fun Preview_ContainerBrowser_PopulatedReady() {
    val subcontainers = listOf(
        Container(ContainerId(10L), "Tool Chest", ContainerId(1L)),
        Container(ContainerId(11L), "Garage Box 1", ContainerId(1L))
    )
    val items = listOf(
        Item(
            id = ItemId(100L),
                name = "Impact Driver",
                description = "DeWalt Brand 18V brushless",
                imagePath = null,
                status = ItemStatus.STORED,
                containerId = ContainerId(1L)
            ),
        Item(
            id = ItemId(101L),
            name = "Reciprocating Saw",
            description = "Ryobi Brand",
            imagePath = null,
            status = ItemStatus.STORED,
            containerId = ContainerId(1L)
        )
    )

    ContainerBrowserScreen(
        uiState = ContainerBrowserUiState.Ready(
            containerId = ContainerId(1L),
            containerName = "Garage",
            subcontainers = subcontainers,
            items = items
        ),
        onOpenSubcontainer = {},
        onOpenItem = {},
        onOpenContainerInfo = {},
        onAddContainer = {},
        onAddItem = {},
    )
}