package com.project.growing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.project.growing.ui.navigation.AppNavHost
import com.project.growing.ui.theme.GrowingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GrowingTheme {
                AppNavHost()
            }
        }
    }
}

