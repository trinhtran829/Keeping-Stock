package com.keepingstock.ui.components.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

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