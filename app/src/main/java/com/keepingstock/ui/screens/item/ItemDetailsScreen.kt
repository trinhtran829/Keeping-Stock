package com.keepingstock.ui.screens.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keepingstock.core.contracts.ItemId

@Composable
fun ItemDetailsScreen(
    itemId: ItemId,
    onBack: () -> Unit,
    onEdit: (itemId: ItemId) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("Item Details Screen (placeholder)")
        Text("itemId = $itemId")

        Button(onClick = { onEdit(itemId) }, modifier = Modifier.padding(top = 12.dp)) {
            Text("Edit")
        }

        Button(onClick = onBack, modifier = Modifier.padding(top = 12.dp)) {
            Text("Back")
        }
    }
}