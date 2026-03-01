package com.keepingstock.ui.components.screen

import android.R.attr.label
import android.R.attr.onClick
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.ContainerId
import com.keepingstock.core.contracts.Item
import com.keepingstock.core.contracts.ItemId
import com.keepingstock.data.entities.ItemStatus
import com.keepingstock.ui.components.thumbnail.ContainerThumbnail
import com.keepingstock.ui.components.thumbnail.ItemThumbnail
import java.util.Date

/**
 * Generic LoadingState UI. Just uses a basic CircularProgressIndicator
 *
 * :param modifier: Optional modifier applied to the full-screen container.
 */
@Composable
fun LoadingContent(modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/**
 * Generic ErrorState UI. Currently just displays an error message.
 *
 * TODO: Decide whether the UiState's throwable cause should be:
 *  - logged only (ViewModel/repository responsibility), or
 *  - shown in UI for debugging, or
 *  - Other?
 *
 * :param modifier: Optional modifier applied to the full-screen container.
 * :param message: Error message shown to user.
 * :param cause: Optional Throwable (not currently displayed).
 */
@Composable
fun ErrorContent(
    modifier: Modifier,
    message: String,
    cause: Throwable? = null
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.CenterVertically)
        )
    }
}

/**
 * Generic Detail Row composable for displaying a key:value pair
 *
 * :param modifier: Optional modifier applied to the row container.
 * :param label: The text of the label shown to the user.
 * :param value: The text of the value shown to the user.
 */
@Composable
fun DetailRow(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Row UI for a subcontainer entry in the Browser list. Uses a thumbnail (image when
 * available, icon fallback otherwise) and basic text fields.
 *
 * TODO(FUTURE): Consider adding a overflow menu for actions like rename, move, delete
 *
 * :param modifier: Modifier applied to the card container.
 * :param container: The container to display.
 * :param onClick: Invoked when user selects this container.
 */
@Composable
fun ContainerSummaryRow(
    modifier: Modifier,
    container: Container,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ContainerThumbnail handles image vs fallback icon
            ContainerThumbnail(imagePath = container.imageUri)

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = container.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                container.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Row UI for an item entry in the Browser list.
 *
 * Displays a thumbnail, name, and a subtitle built from item status and description.
 * If tags exist, they're supposed to be displayed in a hashtag-like format. Not tested.
 *
 * :param modifier: Modifier applied to the card container.
 * :param item: The item to display.
 * :param onClick: Called when user selects this item.
 */
@Composable
fun ItemSummaryRow(
    modifier: Modifier,
    item: Item,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ItemThumbnail handles image vs fallback icon internally.
            ItemThumbnail(imagePath = item.imageUri)

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val subtitle = buildString {
                    append(item.status.name)
                    if (!item.description.isNullOrBlank()) {
                        if (isNotEmpty()) append(" • ")
                        append(item.description)
                    }
                }

                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (item.tags.isNotEmpty()) {
                    Text(
                        text = item.tags.joinToString(
                            prefix = "#",
                            separator = " #",
                            transform = { it.name }
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Tile-style UI representation of a container for use in grid layouts.
 *
 * @param container The container model to render.
 * @param onClick Invoked when the tile is selected.
 */
@Composable
fun ContainerTile(
    container: Container,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Thumbnail at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.8f),
                contentAlignment = Alignment.Center
            ) {
                // Reuse thumbnail
                ContainerThumbnail(
                    imagePath = container.imageUri,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = container.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            container.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Tile-style UI representation of an item for use in grid layouts.
 *
 * @param item The item model to render.
 * @param onClick Invoked when the tile is selected.
 */
@Composable
fun ItemTile(
    item: Item,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.8f),
                contentAlignment = Alignment.Center
            ) {
                ItemThumbnail(
                    imagePath = item.imageUri,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Subtitle for grid
            val subtitle = buildString {
                append(item.status.name)
                if (!item.description.isNullOrBlank()) {
                    append(" • ")
                    append(item.description)
                }
            }

            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Compact row representation of a container.
 *
 * @param container The container model to render.
 * @param onClick Invoked when the row is selected.
 */
@Composable
fun ContainerCompactRow(
    container: Container,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContainerThumbnail(
                imagePath = container.imageUri,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(10.dp))

            Text(
                text = container.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Compact row representation of an item.
 *
 * @param item The item model to render.
 * @param onClick Invoked when the row is selected.
 */
@Composable
fun ItemCompactRow(
    item: Item,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemThumbnail(
                imagePath = item.imageUri,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(10.dp))

            Row (
                Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Vertical divider
                Spacer(Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = item.status.name,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Preview for the individual container tiles used in grid layouts
 */
@Preview
@Composable
private fun Preview_ContainerTile() {
    ContainerTile(
        container = Container(
            id = ContainerId(1L),
            name = "Garage",
            description = "The location we store our tools and garden supplies. When there's " +
                    "hail, we also store our cars here",
            imageUri = "demo",
            parentContainerId = null,
            createdDate = Date()
        ),
        onClick = { }
    )
}

/**
 * Preview for the individual item tiles used in grid layouts
 */
@Preview
@Composable
private fun Preview_ItemTile() {
    ItemTile(
        item = Item(
            id = ItemId(101L),
            name = "Impact Driver",
            description = "18V brushless",
            containerId = ContainerId(1L),
            status = ItemStatus.STORED
        ),
        onClick = { }
    )
}

/**
 * Preview for the individual container tiles used in grid layouts
 */
@Preview
@Composable
private fun Preview_ContainerCompactRow() {
    ContainerCompactRow (
        container = Container(
            id = ContainerId(1L),
            name = "Garage",
            description = "The location we store our tools and garden supplies. When there's " +
                    "hail, we also store our cars here",
            imageUri = "demo",
            parentContainerId = null,
            createdDate = Date()
        ),
        onClick = { }
    )
}

/**
 * Preview for the individual item tiles used in grid layouts
 */
@Preview
@Composable
private fun Preview_ItemCompactRow() {
    ItemCompactRow (
        item = Item(
            id = ItemId(101L),
            name = "Impact Driver",
            description = "18V brushless",
            containerId = ContainerId(1L),
            status = ItemStatus.STORED
        ),
        onClick = { }
    )
}