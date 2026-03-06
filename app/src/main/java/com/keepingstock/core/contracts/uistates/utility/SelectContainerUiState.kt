package com.keepingstock.core.contracts.uistates.utility

import com.keepingstock.core.contracts.Container


sealed interface SelectContainerUiState {

    data object Loading : SelectContainerUiState

    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : SelectContainerUiState

    data class Ready(
        val currentContainer: Container?,
        val selectedContainer: Container?
    ) : SelectContainerUiState 
}