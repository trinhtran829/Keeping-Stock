package com.keepingstock.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.keepingstock.ui.navigation.AppNavGraph
import com.keepingstock.ui.scaffold.TopBarConfig

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = false, showBackground = true)
@Composable
fun KeepingStockApp() {
    val snackbarHostState = remember { SnackbarHostState() }

    var topBarConfig by remember {
        mutableStateOf(TopBarConfig(title = "Keeping Stock"))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState)},
        topBar = {
            TopAppBar(
                title = { Text(topBarConfig.title) },
                navigationIcon = {
                    if (topBarConfig.showBack) {
                        IconButton (onClick = { /* TODO */ }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    } else null
                }
            )
        }
    ) { innerPadding ->
        AppNavGraph(
            modifier = Modifier,
            contentPadding = innerPadding,
            onTopBarChange = { topBarConfig = it }
        )
    }

}