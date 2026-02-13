package com.keepingstock.ui.screens.container

import android.R.attr.top
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.uistates.container.ContainerBrowserUiState

/**
 *
 */
@Composable
fun ContainerBrowserScreen(
    modifier: Modifier = Modifier,
    uiState: ContainerBrowserUiState,
    onOpenSubcontainer: (containerId: Long) -> Unit = {},
    onOpenItem: (itemId: Long) -> Unit = {},
    onOpenContainerInfo: (containerId: Long) -> Unit = {},
    onAddContainer: (parentContainerId: Long?) -> Unit = {},
    onAddItem: (containerId: String?) -> Unit = {}
) {
    /*
    Column(modifier = modifier.padding(16.dp)) {
        Text("Container Browser Screen (placeholder)")
        Text("containerId = ${containerId ?: "ROOT"}")

        Button(
            onClick = { onOpenSubcontainer("02") },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Open example subcontainer 02")
        }

        Button(
            onClick = { onOpenItem("01") },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Open example item 01")
        }

        Button(
            onClick = { if (containerId != null) onOpenContainerInfo(containerId) },
            enabled = containerId != null,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Open container info")
        }

        Button(
            onClick = { onAddContainer(containerId) },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(
                if (containerId == null) {
                    "Add container"
                } else {
                    "Add subcontainer"
                }
            )
        }

        Button(
            onClick = { onAddItem(containerId) },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(
                if (containerId == null) {
                    "Add item (choose container later)"
                } else {
                    "Add item to this container"
                }
            )
        }

        Button(
            onClick = onGoToItemBrowser,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Go to Item Browser")
        }

        Button(
            onClick = onScanQr,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Scan QR")
        }

    }
    */
}

/**
 *
 */
@Composable
private fun LoadingContent(modifier: Modifier) {

}

/**
 *
 */
@Composable
private fun ErrorContent(modifier: Modifier) {

}

/**
 *
 */
@Composable
private fun ReadyContent(
    modifier: Modifier,
    containerId: Long?,
    containerName: String,
    subcontainers: List<Container>,
    items: List<Item>,
    onOpenSubcontainer: (containerId: Long) -> Unit = {},
    onOpenItem: (itemId: Long) -> Unit = {},
    onOpenContainerInfo: (containerId: Long) -> Unit = {},
    onAddContainer: (parentContainerId: Long?) -> Unit = {},
    onAddItem: (containerId: String?) -> Unit = {}
) {

}

/**
 *
 */
@Composable
private fun EmptyState(
    modifier: Modifier,
    onAddContaienr: () -> Unit,
    onAddItem: () -> Unit
) {

}

/**
 *
 */
@Composable
private fun ContainerSummaryRow(
    modifier: Modifier,
    container: Container,
    onClick: () -> Unit
) {

}

/**
 * 
 */
@Composable
private fun ItemSummaryRow(
    modifier: Modifier,
    item: Item,
    onClick: () -> Unit
) {

}
