package com.keepingstock.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.keepingstock.ui.screens.item.ItemBrowserScreen

@Preview(showSystemUi = false, showBackground = true)
@Composable
fun KeepingStockApp() {
    ItemBrowserScreen()
}