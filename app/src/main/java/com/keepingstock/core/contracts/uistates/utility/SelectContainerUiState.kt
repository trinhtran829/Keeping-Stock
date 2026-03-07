package com.keepingstock.core.contracts.uistates.utility

import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Routes


sealed interface SelectContainerUiState {

    data object Loading : SelectContainerUiState

    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : SelectContainerUiState

    data class Ready(
        val subjectType: Routes.SubjectType,
        val subjectId: Long,
        val subjectName: String,

        val currentAssignedContainer: Container?,
        val selectedDestinationContainer: Container?,
        val browsingContainer: Container?,

        val breadcrumbs: List<Breadcrumb>,
        val rows: List<ContainerSelectRow>,
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

        /**
         * Simple, lightweight data class for showing container information and selection status
         */
        data class ContainerSelectRow(
            val container: Container,
            val isSelected: Boolean,
            val isCurrent: Boolean,
            val isDisabled: Boolean
        )
    }
}