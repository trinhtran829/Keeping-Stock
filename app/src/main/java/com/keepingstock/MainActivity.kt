package com.keepingstock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.keepingstock.ui.KeepingStockApp
import com.keepingstock.ui.theme.KeepingStockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _root_ide_package_.com.keepingstock.ui.theme.KeepingStockTheme {
                KeepingStockApp()
            }
        }
    }
}