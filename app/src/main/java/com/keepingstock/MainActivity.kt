package com.keepingstock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.keepingstock.ui.theme.KeepingStockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KeepingStockTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "camera") {
                    composable("camera") { CameraScreen(navController) }
                    composable("gallery") { GalleryScreen(navController) }
                    composable(
                        "photo/{uri}",
                        arguments = listOf(navArgument("uri") { type = NavType.StringType })
                    ) { backStackEntry ->
                        backStackEntry.arguments?.getString("uri")?.let {
                            PhotoScreen(it, navController)
                        }
                    }
                }
            }
        }
    }
}


