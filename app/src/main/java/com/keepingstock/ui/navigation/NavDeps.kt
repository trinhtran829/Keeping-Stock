package com.keepingstock.ui.navigation

import androidx.navigation.NavHostController
import com.keepingstock.ui.scaffold.TopBarConfig

/**
 * Small deps holder to reduce parameters passed for each destination in NavHost
 */
internal data class NavDeps(
    val navController: NavHostController,
    val onTopBarChange: (TopBarConfig) -> Unit,
    val showSnackbar: (String) -> Unit
)