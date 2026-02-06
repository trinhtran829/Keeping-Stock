package com.keepingstock.ui

import android.R.id.message
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.keepingstock.ui.navigation.AppNavGraph
import com.keepingstock.ui.scaffold.TopBarConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = false, showBackground = true)
@Composable
fun KeepingStockApp() {
    // The navigation manager that tracks current screen and back stack
    val navController = rememberNavController()

    // Holds snackbar state across recompositions
    val snackbarHostState = remember { SnackbarHostState() }

    // For calling suspend functions safely
    val scope = rememberCoroutineScope()

    // Simple snackbar hook for future use
    val showSnackbar: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    // Default top bar configuration
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
                        IconButton (
                            onClick = { navController.popBackStack() }
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier,
            contentPadding = innerPadding,
            onTopBarChange = { topBarConfig = it }
            // TODO: pass showSnackbar hook if needed later
        )
    }
}