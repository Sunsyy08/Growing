package com.project.growing.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.project.growing.R
import com.project.growing.ui.theme.*

enum class BottomNavTab(
    val label     : String,
    val lottieRes : Int,
) {
    HOME   ("홈",   R.raw.tab_home),
    RECORD ("기록", R.raw.tab_record),
    CHAT   ("상담", R.raw.tab_chat),
    MY     ("마이", R.raw.tab_my),
}

@Composable
fun BottomNavBar(
    selectedTab   : BottomNavTab           = BottomNavTab.HOME,
    onTabSelected : (BottomNavTab) -> Unit = {},
    modifier      : Modifier               = Modifier,
) {
    val GreenPrimary = Color(0xFF43A967)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(White)
    ) {
        HorizontalDivider(color = Color(0xFFEAEAEA), thickness = 0.8.dp)

        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(62.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            BottomNavTab.entries.forEach { tab ->
                LottieNavItem(
                    tab          = tab,
                    isSelected   = tab == selectedTab,
                    greenPrimary = GreenPrimary,
                    onClick      = { onTabSelected(tab) },
                )
            }
        }
    }
}

@Composable
private fun LottieNavItem(
    tab          : BottomNavTab,
    isSelected   : Boolean,
    greenPrimary : Color,
    onClick      : () -> Unit,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(tab.lottieRes)
    )

    val progress by animateLottieCompositionAsState(
        composition   = composition,
        isPlaying     = isSelected,
        iterations    = 1,
        restartOnPlay = true,
        speed         = 1.5f,
    )

    // ── 아이콘 스케일: 선택 시 크게, 비선택 시 작게 ──────────
    val iconScale by animateFloatAsState(
        targetValue   = if (isSelected) 1.3f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium,
        ),
        label = "icon_scale_${tab.name}",
    )

    // ── 아이콘 위로 살짝 튀어오르기 ──────────────────────────
    val iconTranslateY by animateFloatAsState(
        targetValue   = if (isSelected) -4f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium,
        ),
        label = "icon_ty_${tab.name}",
    )

    // ── 텍스트 스케일 ─────────────────────────────────────────
    val textScale by animateFloatAsState(
        targetValue   = if (isSelected) 1.1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium,
        ),
        label = "text_scale_${tab.name}",
    )

    // ── 텍스트 알파: 비선택 시 살짝 투명 ─────────────────────
    val textAlpha by animateFloatAsState(
        targetValue   = if (isSelected) 1f else 0.55f,
        animationSpec = tween(220),
        label         = "text_alpha_${tab.name}",
    )

    // ── 색상 전환 ─────────────────────────────────────────────
    val textColor by animateColorAsState(
        targetValue   = if (isSelected) greenPrimary else Color(0xFFAAAAAA),
        animationSpec = tween(200),
        label         = "text_color_${tab.name}",
    )

    IconButton(
        onClick  = onClick,
        modifier = Modifier.size(64.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier            = Modifier.fillMaxSize(),
        ) {
            // ── 아이콘 ───────────────────────────────────────
            LottieAnimation(
                composition = composition,
                progress    = { progress },
                modifier    = Modifier
                    .size(if (tab == BottomNavTab.HOME) 22.dp else 28.dp)
                    .graphicsLayer {
                        scaleX       = iconScale
                        scaleY       = iconScale
                        translationY = iconTranslateY
                    },
            )

            Spacer(modifier = Modifier.height(2.dp))

            // ── 텍스트 ───────────────────────────────────────
            Text(
                text       = tab.label,
                fontSize   = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color      = textColor.copy(alpha = textAlpha),
                modifier   = Modifier.scale(textScale),
            )
        }
    }
}

@Preview(showBackground = true, name = "Lottie Bottom Nav")
@Composable
fun LottieBottomNavPreview() {
    GrowingTheme {
        BottomNavBar()
    }
}