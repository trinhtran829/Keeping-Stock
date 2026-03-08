package com.keepingstock.core.contracts.uistates.utility

import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Routes

/**
 * UI state contract for the Select Container flow.
 *
 * This sealed interface represents all renderable states for the destination picker:
 * - Loading: destination data is being prepared.
 * - Ready: browsing and selection UI is available.
 * - Error: unrecoverable failure while loading required data.
 */
sealed interface SelectContainerUiState {

    data object Loading : SelectContainerUiState

    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : SelectContainerUiState

    /**
     * Container destinations successfully loaded and ready for browsing and selection.
     *
     * Selection data:
     * - [currentAssignedContainer] represents the subject's current container assignment before
     *   any new selection is confirmed.
     * - [selectedDestinationContainer] represents the destination currently selected by the user.
     * - [browsingContainer] represents the container whose direct children are currently shown in
     *   [rows], or null when browsing the root level.
     *
     * Hierarchy data:
     * - [breadcrumbs] represents the full path from Root to the current browsing location.
     * - [rows] represents the direct child containers visible at the current browsing location.
     *
     * Invariants:
     * - [breadcrumbs] must always describe the current [browsingContainer] path.
     * - [rows] must always represent the direct children of [browsingContainer], or root
     *   containers when [browsingContainer] is null.
     * - [selectedDestinationContainer] may be null to indicate Root.
     *
     * The ViewModel is responsible for maintaining these invariants whenever browsing or selection
     * changes.
     *
     * @param subjectType: Indicates whether the subject being moved is a [Routes.SubjectType.Container]
     *                     or [Routes.SubjectType.Item].
     * @param subjectId: Identifier of the subject being moved.
     * @param subjectName: Display name of the subject being moved.
     * @param currentAssignedContainer: The subject's currently assigned container, or null when
     *                                  currently assigned to Root / no container.
     * @param selectedDestinationContainer: The container currently selected as the destination, or
     *                                      null to indicate Root.
     * @param browsingContainer: The container currently being browsed, or null when browsing Root.
     * @param breadcrumbs: The breadcrumb path from Root to [browsingContainer].
     * @param rows: The visible child container rows for the current browsing location.
     */
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
         * Represents a single clickable segment in the current breadcrumb path.
         *
         * Each breadcrumb identifies a container location the user can jump back to while browsing
         * the hierarchy.
         *
         * @param id: Identifier of the breadcrumb container, or null to represent Root.
         * @param label: Display label shown for the breadcrumb segment.
         */
        data class Breadcrumb(
            val id: ContainerId?,
            val label: String
        )

        /**
         * Represents a single visible container row in the current destination list.
         *
         * This row contains the container to display as well as derived UI flags describing whether
         * it is the current assignment, the currently selected destination, or disallowed.
         *
         * @param container: The container represented by this row.
         * @param isSelected: Whether this row is the currently selected destination.
         * @param isCurrent: Whether this row matches the subject's current assigned container.
         * @param isDisabled: Whether this row is unavailable for selection or browsing.
         */
        data class ContainerSelectRow(
            val container: Container,
            val isSelected: Boolean,
            val isCurrent: Boolean,
            val isDisabled: Boolean
        )
    }
}