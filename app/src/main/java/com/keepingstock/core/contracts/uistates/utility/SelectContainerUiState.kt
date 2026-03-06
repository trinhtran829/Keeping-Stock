package com.keepingstock.core.contracts.uistates.utility

import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId


sealed interface SelectContainerUiState {

    data object Loading : SelectContainerUiState

    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : SelectContainerUiState

    data class Ready(
        val currentContainer: Container?,
        val selectedContainer: Container?,

        val breadcrumbs: List<Breadcrumb>,
    ) : SelectContainerUiState {

        /**
         * Simple, lighweight data class to connect container to name
         *
         * TODO: Do I already have a class that can be reused here?
         */
        data class Breadcrumb(
            val id: ContainerId?,
            val label: String
        )
    }
}