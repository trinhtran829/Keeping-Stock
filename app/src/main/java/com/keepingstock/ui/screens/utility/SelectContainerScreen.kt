package com.keepingstock.ui.screens.utility

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.intents.utility.SelectContainerIntent
import com.keepingstock.core.contracts.uistates.utility.SelectContainerUiState
import com.keepingstock.ui.components.screen.ErrorContent
import com.keepingstock.ui.components.screen.LoadingContent
import java.util.Date

/**
 * Add/Edit Container screen that renders based on uiState.
 *
 * State handling:
 * - [SelectContainerUiState.Loading] shows a loading indicator.
 * - [SelectContainerUiState.Error] shows an error message.
 * - [SelectContainerUiState.Ready] shows the editable form and emits [SelectContainerIntent]
 *   events via [onIntent].
 *
 * Navigation:
 * - [onNavigateBack] is called when the user confirms leaving (e.g. discard changes) or taps
 *   Cancel when the form is not dirty.
 *
 * TODO: Extract back handling to VM/effects
 *
 * @param modifier: Modifier applied to the screen container.
 * @param uiState: Current UI state for the Select Container flow.
 * @param onIntent: Callback for user intents (field edits, save, image changes, etc.).
 * @param onNavigateBack: Callback to navigate up/back out of this screen.
 */
@Composable
fun SelectContainerScreen(
    modifier: Modifier = Modifier,
    uiState: SelectContainerUiState,
    onIntent: (SelectContainerIntent) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    when (uiState) {
        SelectContainerUiState.Loading ->
            LoadingContent(modifier = modifier.fillMaxSize())

        is SelectContainerUiState.Error ->
            ErrorContent(
                modifier = modifier.fillMaxSize(),
                message = "Unable to load select new container screen."
            )

        is SelectContainerUiState.Ready ->
            ReadyContents(
                modifier = modifier.fillMaxSize(),
                uiState = uiState,
                onIntent = onIntent,
                onNavigateBack = onNavigateBack
            )
    }
}

/**
 * Renders the editable Add/Edit Container form for the [SelectContainerUiState.Ready] state.
 *
 * @param modifier: Modifier applied to the scrolling content container.
 * @param uiState: Ready state containing current field values, validation, and flags.
 * @param onIntent: Callback for emitting user intents to the state owner (demo controller / ViewModel).
 * @param onNavigateBack: Callback invoked when navigation away from the screen is confirmed.
 */
@Composable
fun ReadyContents(
    modifier: Modifier,
    uiState: SelectContainerUiState.Ready,
    onIntent: (SelectContainerIntent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding((12.dp)),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header Card
        // Context Card
        // Controls Card

        // Breadcrumb
        BreadcrumbRow(uiState, onIntent)

        // Current tree/results
    }
}

/**
 * Renders the breadcrumb path row(s)
 *
 * TODO: vertical spacing of breadcrumb is excessive - use different component?
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BreadcrumbRow(
    uiState: SelectContainerUiState.Ready,
    onIntent: (SelectContainerIntent) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        uiState.breadcrumbs.forEachIndexed { index, crumb ->
            /*
            TextButton(
                onClick = { onIntent(SelectContainerIntent.ClickBreadcrumb(crumb.id)) },
            ) {
                Text(crumb.label)
            }
             */

            AssistChip(
                onClick = { onIntent(SelectContainerIntent.ClickBreadcrumb(crumb.id)) },
                label = { Text(crumb.label) }
            )

            if (index != uiState.breadcrumbs.lastIndex) {
                Text(">", modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}

/**
 * Previews the current overall select container screen format
 */
@Preview(showBackground = true)
@Composable
fun Preview_SelectContainerScreen_Ready() {
    val currentContainer = Container(
        id = ContainerId(1L),
        name = "Garage",
        imageUri = "demo",
        createdDate = Date()
    )

    SelectContainerScreen(
        uiState = SelectContainerUiState.Ready(
            currentContainer = currentContainer,
            selectedContainer = currentContainer,
            breadcrumbs = listOf(
                SelectContainerUiState.Ready.Breadcrumb(null, "Root"),
                SelectContainerUiState.Ready.Breadcrumb(ContainerId(2L), "Example"),
                SelectContainerUiState.Ready.Breadcrumb(ContainerId(3L), "Example2"),
                SelectContainerUiState.Ready.Breadcrumb(ContainerId(4L), "Example3"),
                SelectContainerUiState.Ready.Breadcrumb(ContainerId(3L), "Example4"),
                SelectContainerUiState.Ready.Breadcrumb(ContainerId(3L), "Example5"),
                SelectContainerUiState.Ready.Breadcrumb(ContainerId(3L), "Example6"),
                SelectContainerUiState.Ready.Breadcrumb(ContainerId(3L), "Example7"),
                SelectContainerUiState.Ready.Breadcrumb(ContainerId(3L), "Example8"),
            )
        )
    )
}