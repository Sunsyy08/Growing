package com.project.growing.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.growing.ui.theme.*

enum class BottomNavTab(
    val label        : String,
    val icon         : ImageVector,
    val selectedIcon : ImageVector,
) {
    HOME   ("홈",   Icons.Outlined.Home,      Icons.Rounded.Home),
    RECORD ("기록", Icons.Outlined.BarChart,   Icons.Rounded.BarChart),
    CHAT   ("상담", Icons.Outlined.ChatBubble, Icons.Rounded.ChatBubble),
    MY     ("마이", Icons.Outlined.Person,      Icons.Rounded.Person),
}

@Composable
fun BottomNavBar(
    selectedTab   : BottomNavTab           = BottomNavTab.HOME,
    onTabSelected : (BottomNavTab) -> Unit = {},
    modifier      : Modifier               = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(White)
    ) {
        // 상단 구분선
        HorizontalDivider(
            color     = Color(0xFFEAEAEA),
            thickness = 0.8.dp,
        )
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(58.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            BottomNavTab.entries.forEach { tab ->
                BottomNavItem(
                    tab        = tab,
                    isSelected = tab == selectedTab,
                    onClick    = { onTabSelected(tab) },
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    tab        : BottomNavTab,
    isSelected : Boolean,
    onClick    : () -> Unit,
) {
    val activeColor   = Color(0xFF43A967)
    val inactiveColor = Color(0xFFAAAAAA)

    IconButton(
        onClick  = onClick,
        modifier = Modifier.size(60.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier            = Modifier.fillMaxSize(),
        ) {
            Icon(
                imageVector        = if (isSelected) tab.selectedIcon else tab.icon,
                contentDescription = tab.label,
                tint               = if (isSelected) activeColor else inactiveColor,
                modifier           = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text       = tab.label,
                fontSize   = 10.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color      = if (isSelected) activeColor else inactiveColor,
            )
        }
    }
}

@Preview(showBackground = true, name = "Bottom Nav Bar")
@Composable
fun BottomNavBarPreview() {
    GrowingTheme {
        BottomNavBar()
    }
}