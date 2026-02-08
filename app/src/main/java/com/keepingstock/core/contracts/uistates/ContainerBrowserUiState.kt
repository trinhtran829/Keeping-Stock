package com.keepingstock.core.contracts.uistates

import com.keepingstock.ui.models.ContainerSummaryUi
import com.keepingstock.ui.models.ItemSummaryUi


sealed interface ContainerBrowserUiState {
    data object Loading : ContainerBrowserUiState

    data class Ready(
        val containerId: Long?,
        val containerName: String,
        val subcontainers: List<ContainerSummaryUi>,
        val items: List<ItemSummaryUi>
    ) : ContainerBrowserUiState

    data class Error(val message: String) : ContainerBrowserUiState
}