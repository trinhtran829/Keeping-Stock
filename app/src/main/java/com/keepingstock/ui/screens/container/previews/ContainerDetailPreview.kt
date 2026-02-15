package com.keepingstock.ui.screens.container.previews

import android.R.attr.name
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.uistates.container.ContainerDetailUiState
import com.keepingstock.ui.screens.container.ContainerDetailScreen

/**
 * Provides previews for each UI State of the ContainerDetailScreen.
 */

@Preview(showBackground = true)
@Composable
private fun Preview_ContainerDetail_Loading() {
    ContainerDetailScreen(
        uiState = ContainerDetailUiState.Loading,
        onBack = {},
        onEdit = {},
        onMove = {},
        onDelete = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview_ContainerDetail_Error() {
    ContainerDetailScreen(
        uiState = ContainerDetailUiState.Error("Failed to load container details."),
        onBack = {},
        onEdit = {},
        onMove = {},
        onDelete = {},
    )
}

/**
 * ---
 * GenAI usage citation:
 * Sample container detail data auto-generated with the help of ChatGPT.
 * Prompt: "Please generate data for a sample object with the following class signature:"
 */
@Preview(showBackground = true)
@Composable
private fun Preview_ContainerDetail_Ready_CanDelete() {
    val containerId = ContainerId(1L)
    ContainerDetailScreen(
        uiState = ContainerDetailUiState.Ready(
            containerId = containerId,
            container = Container(
                id = containerId,
                name = "Garage",
                description = "Tools, hardware, and project materials.",
                imageUri = "demo", //dummy flag - use demo img
                parentContainerId = null
            ),
            parentContainerName = null,
            subcontainerCount = 0,
            itemCount = 0,
            canDelete = true,
            deleteBlockedReason = null
        ),
        onBack = {},
        onEdit = {},
        onMove = {},
        onDelete = {},
    )
}

/**
 * ---
 * GenAI usage citation:
 * Sample container detail data auto-generated with the help of ChatGPT.
 * Prompt: "Please generate data for a sample object with the following class signature:"
 */
@Preview(showBackground = true)
@Composable
private fun Preview_ContainerDetail_Ready_DeleteBlocked() {
    val containerId = ContainerId(10L)
    ContainerDetailScreen(
        uiState = ContainerDetailUiState.Ready(
            containerId = ContainerId(10L),
            container = Container(
                id = containerId,
                name = "Tool Chest Main",
                description = "My favorite red one. Contains frequently used hand tools.",
                imageUri = null,
                parentContainerId = ContainerId(1L)
            ),
            parentContainerName = "Garage",
            subcontainerCount = 2,
            itemCount = 12,
            canDelete = false,
            deleteBlockedReason = "Container must be empty to delete."
        ),
        onBack = {},
        onEdit = {},
        onMove = {},
        onDelete = {},
    )
}
