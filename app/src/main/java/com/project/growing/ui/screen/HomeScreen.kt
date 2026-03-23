package com.project.growing.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.growing.ui.component.BottomNavBar
import com.project.growing.ui.component.BottomNavTab
import com.project.growing.ui.theme.*

enum class PlantStatus(
    val label    : String,
    val color    : Color,
    val badgeBg  : Color,
    val icon     : ImageVector,
    val iconBg   : Color,
    val iconTint : Color,
) {
    GOOD   ("좋음", Color(0xFF2E7D32), Color(0xFFE8F5E9), Icons.Rounded.Eco,       Color(0xFF43A967), Color.White),
    CAUTION("주의", Color(0xFFF57F17), Color(0xFFFFF8E1), Icons.Rounded.Warning,    Color(0xFFFFB300), Color.White),
    NORMAL ("중간", Color(0xFF1565C0), Color(0xFFE3F2FD), Icons.Rounded.WaterDrop,  Color(0xFF42A5F5), Color.White),
    DANGER ("위험", Color(0xFFC62828), Color(0xFFFFEBEE), Icons.Rounded.Dangerous,  Color(0xFFEF5350), Color.White),
}

data class PlantUiModel(
    val id          : String,
    val name        : String,
    val healthScore : Int,
    val nextWater   : String,
    val status      : PlantStatus,
    val memo        : String,
    val imageRes    : Int? = null,
)

val samplePlants = listOf(
    PlantUiModel(
        id          = "1",
        name        = "몬스테라",
        healthScore = 95,
        nextWater   = "2일 후",
        status      = PlantStatus.GOOD,
        memo        = "건강한 상태입니다 🌟",
    ),
    // TODO: 백엔드 연결 후 실제 데이터로 교체
)

@Composable
fun HomeScreen(
    onAddPlant : () -> Unit = {},
) {
    GrowingTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEEF6F1))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── 상단 헤더 (높이 축소) ──────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation    = 10.dp,
                            shape        = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                            ambientColor = Color(0xFF43A967).copy(alpha = 0.25f),
                            spotColor    = Color(0xFF2E7D32).copy(alpha = 0.25f),
                        )
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF2E7D32),
                                    Color(0xFF43A967),
                                    Color(0xFF66BB7A),
                                )
                            )
                        )
                        .padding(
                            start  = 24.dp,
                            end    = 24.dp,
                            top    = 48.dp,       // 54 → 48
                            bottom = 20.dp,       // 28 → 20
                        )
                ) {
                    Column {
                        Text(
                            text          = "안녕하세요 👋",
                            fontSize      = 22.sp,
                            fontWeight    = FontWeight.Bold,
                            color         = White,
                            letterSpacing = (-0.3).sp,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text     = "오늘도 식물을 잘 돌보고 있어요 🌿",
                            fontSize = 13.sp,
                            color    = White.copy(alpha = 0.88f),
                        )
                    }
                }

                // ── 스크롤 영역 ────────────────────────────
                LazyColumn(
                    modifier       = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 22.dp, end = 22.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text(
                                text          = "내 식물들",
                                fontSize      = 17.sp,
                                fontWeight    = FontWeight.Bold,
                                color         = TextPrimary,
                                letterSpacing = (-0.2).sp,
                            )
                            Text(
                                text     = "${samplePlants.size}개",
                                fontSize = 13.sp,
                                color    = TextSecondary,
                            )
                        }
                    }

                    items(samplePlants) { plant ->
                        PlantCard(
                            plant    = plant,
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 7.dp),
                        )
                    }
                }
            }

            // ── FAB ────────────────────────────────────────
            FloatingActionButton(
                onClick        = onAddPlant,
                containerColor = Color(0xFF43A967),
                contentColor   = White,
                shape          = CircleShape,
                modifier       = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 22.dp, bottom = 80.dp)
                    .size(52.dp)
                    .shadow(
                        elevation    = 14.dp,
                        shape        = CircleShape,
                        ambientColor = Color(0xFF43A967).copy(alpha = 0.45f),
                        spotColor    = Color(0xFF2E7D32).copy(alpha = 0.45f),
                    ),
            ) {
                Icon(
                    imageVector        = Icons.Rounded.Add,
                    contentDescription = "식물 추가",
                    modifier           = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
fun PlantCard(
    plant    : PlantUiModel,
    modifier : Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation    = 8.dp,
                shape        = RoundedCornerShape(20.dp),
                ambientColor = Color(0x1243A967),
                spotColor    = Color(0x122E7D32),
            )
            .clip(RoundedCornerShape(20.dp))
            .background(White)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // ── 이미지 + 상태 아이콘 ───────────────────────
            Box(modifier = Modifier.size(84.dp)) {
                // 이미지 (그림자 최소화)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.BottomStart)
                        .shadow(
                            elevation    = 2.dp,        // 6 → 2
                            shape        = RoundedCornerShape(14.dp),
                            ambientColor = Color(0x0A000000),
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFDFF2E6)),
                    contentAlignment = Alignment.Center,
                ) {
                    // TODO: AsyncImage 로 교체
                    Text(text = "🪴", fontSize = 32.sp)
                }

                // 상태 아이콘 뱃지
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .shadow(elevation = 3.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(plant.status.iconBg)
                        .border(1.5.dp, White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = plant.status.icon,
                        contentDescription = plant.status.label,
                        tint               = plant.status.iconTint,
                        modifier           = Modifier.size(13.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        text          = plant.name,
                        fontSize      = 15.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = TextPrimary,
                        letterSpacing = (-0.2).sp,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(plant.status.badgeBg)
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text       = plant.status.label,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = plant.status.color,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = "건강 점수",  fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text       = "${plant.healthScore}%",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = plant.status.color,
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFDCEEE2))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(plant.healthScore / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        plant.status.color.copy(alpha = 0.6f),
                                        plant.status.color,
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Rounded.WaterDrop,
                        contentDescription = null,
                        tint               = Color(0xFF64B5F6),
                        modifier           = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = plant.nextWater, fontSize = 12.sp, color = TextSecondary)
                }

                // 메모
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(plant.status.badgeBg)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                ) {
                    Text(
                        text     = plant.memo,
                        fontSize = 12.sp,
                        color    = plant.status.color,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home with BottomBar")
@Composable
fun HomeWithBottomBarPreview() {
    var selectedTab by remember { mutableStateOf(BottomNavTab.HOME) }
    GrowingTheme {
        Scaffold(
            containerColor = Color(0xFFEEF6F1),
            bottomBar = {
                BottomNavBar(
                    selectedTab   = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                HomeScreen()
            }
        }
    }
}