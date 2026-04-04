package com.project.growing.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.project.growing.ui.component.BottomNavBar
import com.project.growing.ui.component.BottomNavTab
import com.project.growing.ui.theme.*
import com.project.growing.viewmodel.PlantCardData
import com.project.growing.viewmodel.PlantViewModel

enum class PlantStatus(
    val label    : String,
    val color    : Color,
    val badgeBg  : Color,
    val icon     : ImageVector,
    val iconBg   : Color,
    val iconTint : Color,
) {
    GOOD   ("좋음", Color(0xFF2E7D32), Color(0xFFE8F5E9), Icons.Rounded.Eco,      Color(0xFF43A967), Color.White),
    CAUTION("주의", Color(0xFFF57F17), Color(0xFFFFF8E1), Icons.Rounded.Warning,   Color(0xFFFFB300), Color.White),
    NORMAL ("중간", Color(0xFF1565C0), Color(0xFFE3F2FD), Icons.Rounded.WaterDrop, Color(0xFF42A5F5), Color.White),
    DANGER ("위험", Color(0xFFC62828), Color(0xFFFFEBEE), Icons.Rounded.Dangerous, Color(0xFFEF5350), Color.White),
}

data class PlantUiModel(
    val id          : String,
    val name        : String,
    val healthScore : Int,
    val nextWater   : String,
    val status      : PlantStatus,
    val memo        : String,
    val imageRes    : Int? = null,
    val imageUrl    : String? = null,
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
)

@Composable
fun HomeScreen(
    plantViewModel : PlantViewModel     = viewModel(),
    onAddPlant   : () -> Unit       = {},
    onPlantClick : (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val plantViewModel : PlantViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )
    val homeState by plantViewModel.homeState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        android.util.Log.d("PlantVM", "LaunchedEffect 호출")
        plantViewModel.loadHomePlants()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                android.util.Log.d("PlantVM", "ON_RESUME 호출")
                plantViewModel.loadHomePlants()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    GrowingTheme {
        var entered by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { entered = true }

        val headerTranslateY by animateFloatAsState(
            targetValue   = if (entered) 0f else -120f,
            animationSpec = tween(550, easing = EaseOutCubic),
            label         = "header_ty",
        )
        val headerAlpha by animateFloatAsState(
            targetValue   = if (entered) 1f else 0f,
            animationSpec = tween(450, easing = EaseOutCubic),
            label         = "header_alpha",
        )
        val contentAlpha by animateFloatAsState(
            targetValue   = if (entered) 1f else 0f,
            animationSpec = tween(500, delayMillis = 200, easing = EaseOutCubic),
            label         = "content_alpha",
        )
        val fabScale by animateFloatAsState(
            targetValue   = if (entered) 1f else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            label         = "fab_scale",
        )

        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFFEEF6F1))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── 헤더 ──────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { translationY = headerTranslateY; alpha = headerAlpha }
                        .shadow(10.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                            ambientColor = Color(0xFF43A967).copy(alpha = 0.25f),
                            spotColor    = Color(0xFF2E7D32).copy(alpha = 0.25f))
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFF2E7D32), Color(0xFF43A967), Color(0xFF66BB7A))))
                        .statusBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 20.dp)
                ) {
                    Column {
                        Text("안녕하세요 👋", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = White, letterSpacing = (-0.3).sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("오늘도 식물을 잘 돌보고 있어요 🌿", fontSize = 13.sp, color = White.copy(alpha = 0.88f))
                    }
                }

                // ── 스크롤 영역 ───────────────────────────────
                LazyColumn(
                    modifier       = Modifier.weight(1f).graphicsLayer { alpha = contentAlpha },
                    contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text("내 식물들", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.2).sp)
                            Text("${homeState.plants.size}개", fontSize = 13.sp, color = TextSecondary)
                        }
                    }

                    // ── 로딩 ────────────────────────────────
                    if (homeState.isLoading) {
                        item {
                            Box(
                                modifier         = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = Color(0xFF43A967))
                            }
                        }
                    }

                    // ── 식물 없을 때 ─────────────────────────
                    if (!homeState.isLoading && homeState.plants.isEmpty()) {
                        item {
                            Box(
                                modifier         = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "🌱", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text       = "등록된 식물이 없어요\n+ 버튼으로 추가해보세요",
                                        fontSize   = 14.sp,
                                        color      = TextSecondary,
                                        textAlign  = TextAlign.Center,
                                        lineHeight = 20.sp,
                                    )
                                }
                            }
                        }
                    }

                    // ── 식물 카드 목록 ───────────────────────
                    items(homeState.plants) { plant ->
                        PlantCard(
                            plant    = plant.toPlantUiModel(),
                            onClick  = { onPlantClick(plant.plantId.toString()) },
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 7.dp),
                        )
                    }
                }
            }

            // ── FAB ───────────────────────────────────────────
            FloatingActionButton(
                onClick        = onAddPlant,
                containerColor = Color(0xFF43A967),
                contentColor   = White,
                shape          = CircleShape,
                modifier       = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 22.dp, bottom = 16.dp)
                    .size(52.dp)
                    .graphicsLayer { scaleX = fabScale; scaleY = fabScale }
                    .shadow(14.dp, CircleShape,
                        ambientColor = Color(0xFF43A967).copy(alpha = 0.45f),
                        spotColor    = Color(0xFF2E7D32).copy(alpha = 0.45f)),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "식물 추가", modifier = Modifier.size(24.dp))
            }
        }
    }
}

// ── PlantCardData → PlantUiModel 변환 ────────────────────────
fun PlantCardData.toPlantUiModel(): PlantUiModel {
    val plantStatus = when (status) {
        "좋음" -> PlantStatus.GOOD
        "보통" -> PlantStatus.NORMAL
        "나쁨" -> PlantStatus.DANGER
        else   -> PlantStatus.NORMAL
    }
    return PlantUiModel(
        id          = plantId.toString(),
        name        = plantName ?: plantKind ?: "내 식물",  // 이름 우선, 없으면 종류
        healthScore = score ?: 0,
        nextWater   = "",
        status      = plantStatus,
        memo        = "",
        imageUrl    = imageUrl,
    )
}

// ── 식물 카드 ─────────────────────────────────────────────────

@Composable
fun PlantCard(
    plant    : PlantUiModel,
    onClick  : () -> Unit   = {},
    modifier : Modifier = Modifier,
) {
    // ── 건강 점수 바 애니메이션 ────────────────────────────
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(plant.id) { startAnim = true }

    val animatedProgress by animateFloatAsState(
        targetValue   = if (startAnim) plant.healthScore / 100f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis    = 300,
            easing         = EaseOutCubic,
        ),
        label = "health_bar_${plant.id}",
    )

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
            .clickable { onClick() }
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // ── 이미지 + 상태 아이콘 ───────────────────────
            Box(modifier = Modifier.size(84.dp)) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.BottomStart)
                        .shadow(2.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFDFF2E6)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (plant.imageUrl != null) {
                        AsyncImage(
                            model              = plant.imageUrl,
                            contentDescription = plant.name,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                        )
                    } else {
                        Text(text = "🪴", fontSize = 32.sp)
                    }
                }

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

                // 이름 + 상태 뱃지 Row
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,  // ← 양끝 정렬
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    // 왼쪽: 식물 종류
                    Text(
                        text          = plant.name.ifEmpty { "식물" },
                        fontSize      = 15.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = TextPrimary,
                        letterSpacing = (-0.2).sp,
                        modifier      = Modifier.weight(1f),  // ← 남은 공간 차지
                    )

                    // 오른쪽 끝: 상태 뱃지
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

                // 건강 점수
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = "건강 점수", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text       = "${plant.healthScore}%",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = plant.status.color,
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                // 건강 점수 바
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFDCEEE2))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
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

                // 물주기, 메모 — 비어있으면 숨김
                if (plant.nextWater.isNotEmpty()) {
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
                }

                if (plant.memo.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(plant.status.badgeBg)
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text       = plant.memo,
                            fontSize   = 12.sp,
                            color      = plant.status.color,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

// ── Preview ────────────────────────────────────────────────────

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