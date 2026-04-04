package com.project.growing.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.growing.ui.theme.*
import com.project.growing.viewmodel.PlantViewModel
import kotlin.math.abs

@Composable
fun AiAnalysisScreen(
    plantId        : Int            = 0,
    plantViewModel : PlantViewModel = viewModel(),
    onBack         : () -> Unit     = {},
) {
    GrowingTheme {
        val analysisState by plantViewModel.analysisState.collectAsStateWithLifecycle()
        val listState = rememberLazyListState()

        // ── 화면 진입 시 AI 분석 호출 ─────────────────────────
        LaunchedEffect(plantId) {
            if (plantId > 0) plantViewModel.loadAiAnalysis(plantId)
        }

        // ── 헤더 스크롤 애니메이션 ────────────────────────────
        val headerAlpha by remember {
            derivedStateOf {
                when {
                    listState.firstVisibleItemIndex > 0 -> 0f
                    else -> (1f - listState.firstVisibleItemScrollOffset / 320f).coerceIn(0f, 1f)
                }
            }
        }
        val headerTranslationY by remember {
            derivedStateOf {
                if (listState.firstVisibleItemIndex > 0) -40f
                else -(1f - headerAlpha) * 40f
            }
        }

        // ── 진입 애니메이션 ───────────────────────────────────
        var entered by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { entered = true }

        val contentAlpha by animateFloatAsState(
            targetValue   = if (entered) 1f else 0f,
            animationSpec = tween(500, delayMillis = 150, easing = EaseOutCubic),
            label         = "content_alpha",
        )
        val contentSlide by animateFloatAsState(
            targetValue   = if (entered) 0f else 40f,
            animationSpec = tween(500, delayMillis = 150, easing = EaseOutCubic),
            label         = "content_slide",
        )

        // ── 로딩 중 ───────────────────────────────────────────
        if (analysisState.isLoading) {
            Box(
                modifier         = Modifier.fillMaxSize().background(Color(0xFFF5F7F5)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF43A967))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AI가 식물을 분석 중이에요...", fontSize = 14.sp, color = TextSecondary)
                }
            }
            return@GrowingTheme
        }

        val analysis = analysisState.analysis

        LazyColumn(
            state          = listState,
            modifier       = Modifier.fillMaxSize().background(Color(0xFFF5F7F5)),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {

            // ── 헤더 ──────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { alpha = headerAlpha; translationY = headerTranslationY }
                        .background(
                            Brush.verticalGradient(listOf(
                                Color(0xFF1B5E20), Color(0xFF2E7D32),
                                Color(0xFF43A967), Color(0xFF66BB7A),
                            ))
                        )
                        .statusBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(36.dp).clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Rounded.ArrowBackIosNew, "뒤로가기", tint = White, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, null, tint = White, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("AI 분석", fontSize = 11.sp, color = White, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("AI 분석 결과", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = White, letterSpacing = (-0.3).sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("식물의 상태를 분석했어요", fontSize = 13.sp, color = White.copy(alpha = 0.85f))
                    }
                }
            }

            // ── 에러 상태 ─────────────────────────────────────
            if (analysisState.errorMessage != null) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("😢", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text     = analysisState.errorMessage!!,
                                fontSize = 14.sp,
                                color    = TextSecondary,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { plantViewModel.loadAiAnalysis(plantId) },
                                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A967)),
                                shape   = RoundedCornerShape(50),
                            ) {
                                Text("다시 시도", fontSize = 14.sp, color = White)
                            }
                        }
                    }
                }
            }

            // ── 분석 결과 ─────────────────────────────────────
            if (analysis != null) {

                // ── 상태 요약 카드 ─────────────────────────────
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    AiCard(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .graphicsLayer { alpha = contentAlpha; translationY = contentSlide }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF43A967), modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text       = when {
                                        (analysis.score ?: 0) >= 70 -> "건강한 상태"
                                        (analysis.score ?: 0) >= 40 -> "보통 상태"
                                        else                         -> "관리 필요"
                                    },
                                    fontSize   = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = TextPrimary,
                                )
                                Text("AI가 분석한 결과예요", fontSize = 13.sp, color = TextSecondary)
                            }
                            // 점수 뱃지
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF43A967))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text("${analysis.score}점", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = White)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(14.dp))

                        // 상태 설명
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF0FAF4)).padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Rounded.Info, null, tint = Color(0xFF43A967), modifier = Modifier.size(16.dp).padding(top = 1.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(analysis.state_description ?: "분석 중...", fontSize = 13.sp, color = Color(0xFF2E7D32), lineHeight = 20.sp)
                            }
                        }
                    }
                }

                // ── 환경 분석 카드 ─────────────────────────────
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    AiCard(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .graphicsLayer { alpha = contentAlpha; translationY = contentSlide }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFB300)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Rounded.WbSunny, null, tint = White, modifier = Modifier.size(15.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("환경 분석", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // 햇빛 상태
                        EnvAnalysisRow(
                            icon  = "☀️",
                            label = "햇빛 상태",
                            value = analysis.sunlight_status ?: "-",
                            color = Color(0xFFFFB300),
                            bg    = Color(0xFFFFF8E1),
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF0F0F0), thickness = 0.8.dp)

                        // 공기 순환
                        EnvAnalysisRow(
                            icon  = "🌬️",
                            label = "공기 순환",
                            value = analysis.air_circulation ?: "-",
                            color = Color(0xFF1E88E5),
                            bg    = Color(0xFFE3F2FD),
                        )
                    }
                }

                // ── 미래 예측 카드 ─────────────────────────────
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    AiCard(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .graphicsLayer { alpha = contentAlpha; translationY = contentSlide }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF43A967)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Rounded.TrendingUp, null, tint = White, modifier = Modifier.size(15.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("건강 예측", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF0FAF4)).padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text("🌿", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text       = analysis.health_prediction ?: "예측 중...",
                                    fontSize   = 13.sp,
                                    color      = TextSecondary,
                                    lineHeight = 20.sp,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// ── 환경 분석 행 ──────────────────────────────────────────────

@Composable
private fun EnvAnalysisRow(
    icon  : String,
    label : String,
    value : String,
    color : Color,
    bg    : Color,
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 14.sp, color = TextSecondary)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(bg)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

// ── AI 카드 래퍼 ──────────────────────────────────────────────

@Composable
private fun AiCard(
    modifier : Modifier = Modifier,
    content  : @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier.fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x10000000), spotColor = Color(0x18000000))
            .clip(RoundedCornerShape(20.dp)).background(White)
    ) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "AI Analysis Screen")
@Composable
fun AiAnalysisScreenPreview() {
    AiAnalysisScreen()
}