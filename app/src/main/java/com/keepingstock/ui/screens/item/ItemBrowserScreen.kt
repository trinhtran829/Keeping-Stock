package com.keepingstock.ui.screens.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ItemBrowserScreen(
    modifier: Modifier = Modifier,
    onOpenItem: (itemId: String) -> Unit = {},
    onOpenContainerBrowser: () -> Unit = {}
) {
    Column (modifier = modifier.padding(16.dp)) {
        Text("Item Browser Screen (placeholder)")

        Button(onClick = { onOpenItem("01") }, modifier = Modifier.padding(top = 12.dp)) {
            Text("Open Example Item 01")
        }

        Button(onClick = onOpenContainerBrowser, modifier = Modifier.padding(top = 12.dp)) {
            Text("Go to Container Browser (root)")
        }
    }
}