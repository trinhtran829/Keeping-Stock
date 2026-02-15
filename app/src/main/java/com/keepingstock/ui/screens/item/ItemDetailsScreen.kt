package com.keepingstock.ui.screens.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.core.contracts.uistates.item.ItemDetailUiState
import com.keepingstock.ui.components.screen.ErrorContent
import com.keepingstock.ui.components.screen.LoadingContent

@Composable
fun ItemDetailsScreen(
    modifier: Modifier = Modifier,
    uiState: ItemDetailUiState,
    onBack: () -> Unit,
    onEdit: (ItemId) -> Unit = {},
    onMove: (ItemId) -> Unit = {},
    onDelete: (ItemId) -> Unit = {}
) {
    Column(modifier = modifier.padding(16.dp)) {
        when (uiState) {
            ItemDetailUiState.Loading -> LoadingContent(modifier)

            is ItemDetailUiState.Error -> ErrorContent(
                modifier = modifier,
                message = uiState.message
                // TODO: uiState.cause not displayed yet
            )

            is ItemDetailUiState.Ready -> ReadyContent(
                modifier = modifier,
                uiState = uiState,
                onBack = onBack,
                onEdit = onEdit,
                onMove = onMove,
                onDelete = onDelete
            )
        }
    }
}

/**
 * Ready-state UI for item detail
 *
 * Uses a LazyColumn to ensure content is scrollable if needed.
 *
 * Layout (separate cards):
 * - header (type + name + image + full description)
 * - metadata (details)
 * - actions (edit/move/delete/back)
 *
 * :param modifier: Modifier applied to the scroll container.
 * :param uiState: Ready state containing all container details required for display.
 * :param onBack: User intent to navigate back.
 * :param onEdit: User intent to edit this container.
 * :param onMove: User intent to move/re-parent this container.
 * :param onDelete: User intent to delete this container.
 */
@Composable
private fun ReadyContent(
    modifier: Modifier,
    uiState: ItemDetailUiState.Ready,
    onBack: () -> Unit = {},
    onEdit: (ItemId) -> Unit = {},
    onMove: (ItemId) -> Unit = {},
    onDelete: (ItemId) -> Unit = {}
) {

}