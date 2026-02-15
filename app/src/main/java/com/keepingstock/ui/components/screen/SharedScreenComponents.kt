package com.keepingstock.ui.components.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.Container
import com.keepingstock.core.contracts.Item
import com.keepingstock.ui.components.thumbnail.ContainerThumbnail
import com.keepingstock.ui.components.thumbnail.ItemThumbnail

/**
 * Generic LoadingState UI. Just uses a basic CircularProgressIndicator
 *
 * :param modifier: Optional modifier applied to the full-screen container.
 */
@Composable
fun LoadingContent(modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Companion.Center) {
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
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Companion.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
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
    modifier: Modifier = Modifier.Companion,
    label: String,
    value: String
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.Companion.weight(1f)
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
                        text = item.tags.joinToString(prefix = "#", separator = " #"),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}