package com.project.growing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.project.growing.ui.component.BottomNavBar
import com.project.growing.ui.component.BottomNavTab
import com.project.growing.ui.screen.HomeScreen
import com.project.growing.ui.theme.GrowingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GrowingTheme {
                var selectedTab by remember { mutableStateOf(BottomNavTab.HOME) }

                Scaffold(
                    bottomBar = {
                        BottomNavBar(
                            selectedTab   = selectedTab,
                            onTabSelected = { selectedTab = it },
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (selectedTab) {
                            BottomNavTab.HOME   -> HomeScreen()
                            BottomNavTab.RECORD -> { /* TODO */ }
                            BottomNavTab.CHAT   -> { /* TODO */ }
                            BottomNavTab.MY     -> { /* TODO */ }
                        }
                    }
                }
            }
        }
    }
}

