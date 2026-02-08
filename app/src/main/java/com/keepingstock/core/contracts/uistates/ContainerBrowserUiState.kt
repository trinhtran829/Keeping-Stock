package com.keepingstock.core.contracts.uistates

sealed interface ContainerBrowserUiState {
    data object Loading : ContainerBrowserUiState

    data class Ready(
        val containerId: String?,
        val containerName: String,
        val subcontainers: List<ContainerRowUi>,
        val items: List<ItemRowUi>
    ) : ContainerBrowserUiState

    data class Error(
        val message: String
    ) : ContainerBrowserUiState
}

data class ContainerRowUi(
    val id: String,
    val name: String
)

data class ItemRowUi(
    val id: String,
    val name: String
)