package com.keepingstock.ui.screens.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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