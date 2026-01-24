package com.example.keepingstock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.keepingstock.ui.KeepingStockApp
import com.example.keepingstock.ui.theme.KeepingStockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KeepingStockTheme {
                KeepingStockApp()
            }
        }
    }
}