package com.keepingstock.ui.components.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// *************************************************************
// Screen demo components
// *************************************************************

/**
 * Temporary demo modes for the Container Browser destination.
 *
 * These exist to allow manual toggling between UI states (Ready populated/empty, Loading, Error)
 * before the real ContainerBrowserViewModel is implemented.
 */
enum class DemoMode {
    POPULATED,
    EMPTY,
    READY,
    LOADING,
    ERROR
}

/**
 * Demo-only toggle UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DemoModeToggleRow(
    title: String,
    selected: T,
    options: List<ChipOption<T>>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                DemoChip(option.label, selected == option.value) {
                    onSelect(option.value)
                }
            }
        }
    }
}

/**
 * Simple wrapper class for chip demo options and their respective label
 */
@Immutable
data class ChipOption<T>(
    val value: T,
    val label: String,
)

/**
 * Demo-only chip used by DemoModeToggleRow.
 *
 * :param label: Visible label for the chip.
 * :param selected: Whether this chip is currently selected.
 * :param onClick: Click handler that selects this mode.
 *
 * TODO: Might be useful for selected tag filters?
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}