package com.keepingstock.ui.screens.utility

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.keepingstock.core.contracts.intents.container.AddEditContainerIntent
import com.keepingstock.core.contracts.intents.utility.SelectContainerIntent
import com.keepingstock.core.contracts.uistates.container.AddEditContainerUiState
import com.keepingstock.core.contracts.uistates.container.AddEditContainerUiState.Error
import com.keepingstock.core.contracts.uistates.container.AddEditContainerUiState.Loading
import com.keepingstock.core.contracts.uistates.container.AddEditContainerUiState.Ready
import com.keepingstock.core.contracts.uistates.utility.SelectContainerUiState
import com.keepingstock.ui.components.screen.ErrorContent
import com.keepingstock.ui.components.screen.LoadingContent

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

        is SelectContainerUiState.Ready -> TODO()
    }
}

@Composable
fun ReadyContents() {

}