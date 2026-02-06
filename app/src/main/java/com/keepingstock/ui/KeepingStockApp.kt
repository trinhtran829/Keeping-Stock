package com.keepingstock.ui

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.keepingstock.ui.navigation.AppNavGraph

@Preview(showSystemUi = false, showBackground = true)
@Composable
fun KeepingStockApp() {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState)},
        topBar = {} // TODO
    ) { innerPadding ->
        AppNavGraph(
            modifier = Modifier,
            contentPadding = innerPadding
        )
    }

}