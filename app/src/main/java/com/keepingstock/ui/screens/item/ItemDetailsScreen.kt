package com.keepingstock.ui.screens.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ItemDetailsScreen(
    itemId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("Item Details Screen (placeholder)")
        Text("itemId = $itemId")

        Button(onClick = onBack, modifier = Modifier.padding(top = 12.dp)) {
            Text("Back")
        }
    }
}