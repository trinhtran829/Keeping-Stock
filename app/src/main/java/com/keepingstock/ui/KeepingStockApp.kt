package com.keepingstock.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.keepingstock.ui.navigation.AppNavGraph

@Preview(showSystemUi = false, showBackground = true)
@Composable
fun KeepingStockApp() {
    AppNavGraph()
}