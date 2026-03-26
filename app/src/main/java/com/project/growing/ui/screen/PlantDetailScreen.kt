package com.project.growing.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.graphicsLayer
import com.project.growing.ui.theme.*

// ── 샘플 데이터 ───────────────────────────────────────────────

data class PlantDetailUiModel(
    val name         : String,
    val location     : String,
    val size         : String,
    val healthScore  : Int,
    val aiAnalysis   : String,
    val survivalRate : Int,
    val lastWatered  : String,
    val nextWater    : String,
    val sunlight     : String,
    val temperature  : String,
    val humidity     : String,
)

val samplePlantDetail = PlantDetailUiModel(
    name         = "몬스테라",
    location     = "거실 창가",
    size         = "중형 (25cm)",
    healthScore  = 95,
    aiAnalysis   = "현재 식물은 매우 건강한 상태입니다. 적절한 물 공급과 햇빛을 받고 있으며, 잎의 색상과 크기가 이상적입니다.",
    survivalRate = 98,
    lastWatered  = "2일 전",
    nextWater    = "2일 후",
    sunlight     = "적절함",
    temperature  = "23°C",
    humidity     = "65%",
)

// ── PlantDetailScreen ─────────────────────────────────────────

@Composable
fun PlantDetailScreen(
    plant        : PlantDetailUiModel = samplePlantDetail,
    onBack       : () -> Unit         = {},
    onAiAnalysis : () -> Unit         = {},
    onAskExpert  : () -> Unit         = {},
) {
    GrowingTheme {

        // ── 화면 진입 트리거 ───────────────────────────────
        var entered by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { entered = true }

        // ── 건강 점수 링 애니메이션 ────────────────────────
        val ringProgress by animateFloatAsState(
            targetValue   = if (entered) plant.healthScore / 100f else 0f,
            animationSpec = tween(durationMillis = 1400, easing = EaseOutCubic),
            label         = "ring_progress",
        )

        // ── 점수 숫자 카운트업 애니메이션 ─────────────────
        val displayScore by animateIntAsState(
            targetValue   = if (entered) plant.healthScore else 0,
            animationSpec = tween(durationMillis = 1400, easing = EaseOutCubic),
            label         = "score_count",
        )

        // ── 물주기 카드 슬라이드 + 페이드 ─────────────────
        val waterCardAlpha by animateFloatAsState(
            targetValue   = if (entered) 1f else 0f,
            animationSpec = tween(durationMillis = 600, delayMillis = 300, easing = EaseOutCubic),
            label         = "water_alpha",
        )
        val waterCardSlide by animateFloatAsState(
            targetValue   = if (entered) 0f else 40f,
            animationSpec = tween(durationMillis = 600, delayMillis = 300, easing = EaseOutCubic),
            label         = "water_slide",
        )

        // ── 햇빛 카드 슬라이드 + 페이드 (약간 딜레이) ─────
        val sunCardAlpha by animateFloatAsState(
            targetValue   = if (entered) 1f else 0f,
            animationSpec = tween(durationMillis = 600, delayMillis = 480, easing = EaseOutCubic),
            label         = "sun_alpha",
        )
        val sunCardSlide by animateFloatAsState(
            targetValue   = if (entered) 0f else 40f,
            animationSpec = tween(durationMillis = 600, delayMillis = 480, easing = EaseOutCubic),
            label         = "sun_slide",
        )

        // ── 카드 맥동 (물방울 아이콘) ─────────────────────
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val waterIconScale by infiniteTransition.animateFloat(
            initialValue  = 1f,
            targetValue   = 1.18f,
            animationSpec = infiniteRepeatable(
                animation  = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "water_icon_scale",
        )
        val sunIconRotate by infiniteTransition.animateFloat(
            initialValue  = -8f,
            targetValue   = 8f,
            animationSpec = infiniteRepeatable(
                animation  = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "sun_icon_rotate",
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F5))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                // ── 상단 이미지 영역 ───────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF2E5C38),
                                        Color(0xFF4A7C57),
                                        Color(0xFF5D9467),
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "🌿", fontSize = 80.sp)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xFFF5F7F5))
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .padding(top = 48.dp, start = 16.dp)
                            .size(36.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(White),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(
                            onClick  = onBack,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.ArrowBackIosNew,
                                contentDescription = "뒤로가기",
                                tint               = TextPrimary,
                                modifier           = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                // ── 식물 정보 카드 ─────────────────────────
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .offset(y = (-16).dp)
                        .shadow(
                            elevation    = 10.dp,
                            shape        = RoundedCornerShape(20.dp),
                            ambientColor = Color(0x1443A967),
                            spotColor    = Color(0x1443A967),
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(White)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 20.dp,
                            vertical   = 18.dp,
                        )
                    ) {
                        Text(
                            text          = plant.name,
                            fontSize      = 22.sp,
                            fontWeight    = FontWeight.Bold,
                            color         = TextPrimary,
                            letterSpacing = (-0.3).sp,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "📍", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = plant.location, fontSize = 13.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🌱", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = plant.size, fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                // ── 건강 점수 카드 ─────────────────────────
                DetailCard(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text       = "건강 점수",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary,
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // ── 원형 프로그레스 (애니메이션) ─────
                        Box(
                            modifier         = Modifier
                                .size(140.dp)
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularHealthIndicator(
                                progress   = ringProgress,
                                size       = 140.dp,
                                stroke     = 12.dp,
                                color      = Color(0xFF43A967),
                                trackColor = Color(0xFFE0EDE5),
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text          = "$displayScore",
                                    fontSize      = 38.sp,
                                    fontWeight    = FontWeight.Bold,
                                    color         = Color(0xFF43A967),
                                    letterSpacing = (-1).sp,
                                )
                                Text(
                                    text     = "점",
                                    fontSize = 13.sp,
                                    color    = TextSecondary,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // AI 상태 분석 박스
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF0FAF4))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🌿", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text       = "AI 상태 분석",
                                        fontSize   = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = Color(0xFF2E7D32),
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text       = plant.aiAnalysis,
                                    fontSize   = 13.sp,
                                    color      = TextSecondary,
                                    lineHeight = 20.sp,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text(text = "생존 확률", fontSize = 14.sp, color = TextSecondary)
                            Text(
                                text       = "${plant.survivalRate}%",
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color(0xFF43A967),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── 물주기 / 햇빛 카드 (입장 + 맥동 애니메이션) ─
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // ── 물주기 카드 ───────────────────────────
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                alpha        = waterCardAlpha
                                translationY = waterCardSlide
                            }
                            .shadow(
                                elevation    = 8.dp,
                                shape        = RoundedCornerShape(18.dp),
                                ambientColor = Color(0x2064B5F6),
                                spotColor    = Color(0x2064B5F6),
                            )
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF4FC3F7),
                                        Color(0xFF0288D1),
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFFFFF)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector        = Icons.Rounded.WaterDrop,
                                    contentDescription = null,
                                    tint               = White,
                                    modifier           = Modifier
                                        .size(18.dp)
                                        .scale(waterIconScale), // 맥동
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text     = "마지막 물주기",
                                fontSize = 11.sp,
                                color    = White.copy(alpha = 0.85f),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text          = plant.lastWatered,
                                fontSize      = 20.sp,
                                fontWeight    = FontWeight.Bold,
                                color         = White,
                                letterSpacing = (-0.3).sp,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text     = "다음: ${plant.nextWater}",
                                fontSize = 11.sp,
                                color    = White.copy(alpha = 0.85f),
                            )
                        }
                    }

                    // ── 햇빛 카드 ─────────────────────────────
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                alpha        = sunCardAlpha
                                translationY = sunCardSlide
                            }
                            .shadow(
                                elevation    = 8.dp,
                                shape        = RoundedCornerShape(18.dp),
                                ambientColor = Color(0x20FFB300),
                                spotColor    = Color(0x20FFB300),
                            )
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFFCA28),
                                        Color(0xFFFB8C00),
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFFFFF)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector        = Icons.Rounded.WbSunny,
                                    contentDescription = null,
                                    tint               = White,
                                    modifier           = Modifier
                                        .size(18.dp)
                                        .graphicsLayer {
                                            rotationZ = sunIconRotate // 흔들림
                                        },
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text     = "햇빛",
                                fontSize = 11.sp,
                                color    = White.copy(alpha = 0.85f),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text          = plant.sunlight,
                                fontSize      = 20.sp,
                                fontWeight    = FontWeight.Bold,
                                color         = White,
                                letterSpacing = (-0.3).sp,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text     = "현재 상태",
                                fontSize = 11.sp,
                                color    = White.copy(alpha = 0.85f),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── 환경 정보 카드 ─────────────────────────
                DetailCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text       = "환경 정보",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary,
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        EnvInfoRow(emoji = "🌡", label = "온도", value = plant.temperature)
                        HorizontalDivider(
                            modifier  = Modifier.padding(vertical = 12.dp),
                            color     = Color(0xFFF0F0F0),
                            thickness = 0.8.dp,
                        )
                        EnvInfoRow(emoji = "💧", label = "습도", value = plant.humidity)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── AI 분석 보기 버튼 ──────────────────────
                Button(
                    onClick  = onAiAnalysis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(52.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A967)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                ) {
                    Text(text = "🌿", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text       = "AI 분석 보기",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = White,
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ── 전문가에게 질문하기 버튼 ───────────────
                OutlinedButton(
                    onClick  = onAskExpert,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(52.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = Color(0xFFDDDDDD),
                    ),
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.ChatBubbleOutline,
                        contentDescription = null,
                        tint               = TextSecondary,
                        modifier           = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text       = "전문가에게 질문하기",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color      = TextSecondary,
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ── 공용 카드 ─────────────────────────────────────────────────

@Composable
fun DetailCard(
    modifier : Modifier = Modifier,
    content  : @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation    = 6.dp,
                shape        = RoundedCornerShape(20.dp),
                ambientColor = Color(0x0F000000),
                spotColor    = Color(0x0F000000),
            )
            .clip(RoundedCornerShape(20.dp))
            .background(White)
    ) {
        Column(content = content)
    }
}

// ── 환경 정보 행 ──────────────────────────────────────────────

@Composable
fun EnvInfoRow(emoji: String, label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 14.sp, color = TextSecondary)
        }
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

// ── 원형 건강 지표 (progress 직접 받음) ──────────────────────

@Composable
fun CircularHealthIndicator(
    progress   : Float,   // 0f ~ 1f (외부에서 애니메이션된 값 주입)
    size       : Dp,
    stroke     : Dp,
    color      : Color,
    trackColor : Color,
) {
    Canvas(modifier = Modifier.size(size)) {
        val strokePx = stroke.toPx()
        val padding  = strokePx / 2f
        val diameter = this.size.minDimension - strokePx

        drawArc(
            color      = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter  = false,
            style      = Stroke(width = strokePx, cap = StrokeCap.Round),
            topLeft    = androidx.compose.ui.geometry.Offset(padding, padding),
            size       = androidx.compose.ui.geometry.Size(diameter, diameter),
        )
        drawArc(
            color      = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter  = false,
            style      = Stroke(width = strokePx, cap = StrokeCap.Round),
            topLeft    = androidx.compose.ui.geometry.Offset(padding, padding),
            size       = androidx.compose.ui.geometry.Size(diameter, diameter),
        )
    }
}

// ── Preview ────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Plant Detail")
@Composable
fun PlantDetailScreenPreview() {
    PlantDetailScreen()
}