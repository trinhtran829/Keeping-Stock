package com.keepingstock.ui.screens.container.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.keepingstock.core.contracts.uistates.container.ContainerBrowserUiState
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

}

@Preview(showBackground = true)
@Composable
private fun Preview_ContainerBrowser_PopulatedReady() {

}