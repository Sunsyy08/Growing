package com.project.growing.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.input.pointer.pointerInput
import com.project.growing.ui.component.BottomNavBar
import com.project.growing.ui.component.BottomNavTab
import com.project.growing.ui.theme.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerInputChange

// ── 샘플 데이터 ──────────────────────────────────────────────

data class AiAnalysisUiModel(
    val statusTitle      : String,
    val statusSub        : String,
    val statusDesc       : String,
    val actions          : List<ActionItem>,
    val futurePrediction : String,
    val healthScores     : List<Float>, // 오늘~+6일
)

data class ActionItem(
    val icon  : String,
    val title : String,
    val desc  : String,
)

val sampleAiAnalysis = AiAnalysisUiModel(
    statusTitle = "건강한 상태",
    statusSub   = "식물이 잘 자라고 있어요",
    statusDesc  = "몬스테라는 현재 최적의 환경에서 성장하고 있습니다. 잎의 색상이 진한 녹색을 띠고 있으며, 새로운 잎이 규칙적으로 자라고 있습니다. 토양의 수분 상태도 적절하며, 햇빛 노출량이 이상적입니다.",
    actions     = listOf(
        ActionItem("💧", "물 주기",      "2일 후 물을 주세요"),
        ActionItem("☀️", "햇빛 위치 변경", "창가에서 50cm 더 멀리 배치하세요"),
        ActionItem("🌀", "공기 순환",    "통풍이 잘 되는 곳으로 이동하세요"),
    ),
    futurePrediction = "현재 관리 방식을 유지하면 일주일 후 건강 점수가 70점으로 낮아질 수 있어요. 추천 행동들을 따라주세요!",
    // 오늘=95, +1=92, +2=88, +3=84, +4=80, +5=76, +6=70 (0~100)
    healthScores = listOf(95f, 92f, 88f, 84f, 80f, 76f, 70f),
)

// ── AiAnalysisScreen ─────────────────────────────────────────

@Composable
fun AiAnalysisScreen(
    analysis      : AiAnalysisUiModel = sampleAiAnalysis,
    onBack        : () -> Unit        = {},
) {
    GrowingTheme {
        var selectedTab by remember { mutableStateOf(BottomNavTab.HOME) }

        Scaffold(
            containerColor = Color(0xFFF5F7F5),
            bottomBar = {
                BottomNavBar(
                    selectedTab   = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                // ── 상단 헤더 ──────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF2E7D32),
                                    Color(0xFF43A967),
                                )
                            )
                        )
                        .padding(
                            start  = 20.dp,
                            end    = 20.dp,
                            top    = 52.dp,
                            bottom = 24.dp,
                        )
                ) {
                    // 뒤로가기
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF)),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(
                            onClick  = onBack,
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.ArrowBackIosNew,
                                contentDescription = "뒤로가기",
                                tint               = White,
                                modifier           = Modifier.size(15.dp),
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(top = 44.dp)) {
                        Text(
                            text          = "AI 분석 결과",
                            fontSize      = 22.sp,
                            fontWeight    = FontWeight.Bold,
                            color         = White,
                            letterSpacing = (-0.3).sp,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text     = "식물의 상태를 분석했어요",
                            fontSize = 13.sp,
                            color    = White.copy(alpha = 0.85f),
                        )
                    }
                }

                // ── 식물 이미지 ────────────────────────────
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp)
                        .fillMaxWidth()
                        .height(180.dp)
                        .shadow(8.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF2E5C38), Color(0xFF5D9467))
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    // TODO: 실제 이미지로 교체
                    Text(text = "🌿", fontSize = 64.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── 상태 카드 ──────────────────────────────
                DetailCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {

                        // 건강한 상태 행
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector        = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint               = Color(0xFF43A967),
                                    modifier           = Modifier.size(22.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text       = analysis.statusTitle,
                                    fontSize   = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = TextPrimary,
                                )
                                Text(
                                    text     = analysis.statusSub,
                                    fontSize = 12.sp,
                                    color    = TextSecondary,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(14.dp))

                        // 상태 설명
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector        = Icons.Rounded.Info,
                                contentDescription = null,
                                tint               = Color(0xFFFFB300),
                                modifier           = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text       = "상태 설명",
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = TextPrimary,
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAF8))
                                .padding(12.dp)
                        ) {
                            Text(
                                text       = analysis.statusDesc,
                                fontSize   = 13.sp,
                                color      = TextSecondary,
                                lineHeight = 20.sp,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── 추천 행동 카드 ─────────────────────────
                DetailCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text       = "추천 행동",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary,
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        analysis.actions.forEachIndexed { idx, action ->
                            ActionRow(action = action)
                            if (idx < analysis.actions.lastIndex) {
                                HorizontalDivider(
                                    modifier  = Modifier.padding(vertical = 12.dp),
                                    color     = Color(0xFFF0F0F0),
                                    thickness = 0.8.dp,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── 미래 건강 예측 카드 ────────────────────
                DetailCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📈", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text       = "미래 건강 예측",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary,
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 예측 설명 (70점 강조)
                        val desc = analysis.futurePrediction
                        val highlightWord = "70점"
                        val startIdx = desc.indexOf(highlightWord)
                        if (startIdx >= 0) {
                            Text(
                                text = buildAnnotatedString {
                                    append(desc.substring(0, startIdx))
                                    withStyle(SpanStyle(
                                        color      = Color(0xFF43A967),
                                        fontWeight = FontWeight.Bold,
                                    )) {
                                        append(highlightWord)
                                    }
                                    append(desc.substring(startIdx + highlightWord.length))
                                },
                                fontSize   = 13.sp,
                                color      = TextSecondary,
                                lineHeight = 20.sp,
                            )
                        } else {
                            Text(
                                text       = desc,
                                fontSize   = 13.sp,
                                color      = TextSecondary,
                                lineHeight = 20.sp,
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 꺾은선 그래프
                        HealthLineChart(
                            scores = analysis.healthScores,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 범례
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF43A967))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text     = "건강 점수 추이",
                                fontSize = 12.sp,
                                color    = TextSecondary,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

// ── 추천 행동 행 ──────────────────────────────────────────────

@Composable
fun ActionRow(action: ActionItem) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFFF0FAF4)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = action.icon, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text       = action.title,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text       = action.desc,
                fontSize   = 12.sp,
                color      = TextSecondary,
                lineHeight = 18.sp,
            )
        }
    }
}

// ── 꺾은선 그래프 (터치 + 애니메이션) ───────────────────────

@Composable
fun HealthLineChart(
    scores   : List<Float>,
    modifier : Modifier = Modifier,
) {
    val labels    = listOf("오늘", "+1일", "+2일", "+3일", "+4일", "+5일", "+6일")
    val lineColor = Color(0xFF43A967)
    val dotColor  = Color(0xFF43A967)
    val gridColor = Color(0xFFEEEEEE)

    // ── 진입 애니메이션 ──────────────────────────────────
    var animStarted by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue   = if (animStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label         = "chartAnim",
    )
    LaunchedEffect(Unit) { animStarted = true }

    // ── 터치 상태 ────────────────────────────────────────
    var touchX by remember { mutableStateOf<Float?>(null) }

    BoxWithConstraints(modifier = modifier) {
        val density      = androidx.compose.ui.platform.LocalDensity.current
        val canvasWidth  = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()

        val paddingLeft   = with(density) { 40.dp.toPx() }
        val paddingRight  = with(density) { 16.dp.toPx() }
        val paddingTop    = with(density) { 20.dp.toPx() }
        val paddingBottom = with(density) { 32.dp.toPx() }
        val chartW = canvasWidth  - paddingLeft - paddingRight
        val chartH = canvasHeight - paddingTop  - paddingBottom

        // 점 좌표
        val points = scores.mapIndexed { i, score ->
            val x = paddingLeft + i * (chartW / (scores.size - 1))
            val y = paddingTop  + (1f - score / 100f) * chartH
            androidx.compose.ui.geometry.Offset(x, y)
        }

        // 가장 가까운 인덱스
        val selectedIdx = touchX?.let { tx ->
            points.indices.minByOrNull { i -> kotlin.math.abs(points[i].x - tx) }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart  = { offset: Offset -> touchX = offset.x },
                        onDrag       = { _: PointerInputChange, drag: Offset -> touchX = (touchX ?: 0f) + drag.x },
                        onDragEnd    = { touchX = null },
                        onDragCancel = { touchX = null },
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset: Offset ->
                            touchX = offset.x
                            tryAwaitRelease()
                            touchX = null
                        }
                    )
                }
        ) {
            val ySteps = listOf(0, 25, 50, 75, 100)

            // Y축 그리드
            ySteps.forEach { yVal ->
                val yFraction = 1f - yVal / 100f
                val y = paddingTop + yFraction * chartH
                drawLine(
                    color       = gridColor,
                    start       = androidx.compose.ui.geometry.Offset(paddingLeft, y),
                    end         = androidx.compose.ui.geometry.Offset(size.width - paddingRight, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            // 애니메이션 적용 포인트 (왼쪽→오른쪽으로 쌱)
            val visibleCount = (animProgress * (scores.size - 1)).toInt() + 1
            val partialFraction = (animProgress * (scores.size - 1)) - (visibleCount - 1)
            val animPoints = if (visibleCount < scores.size) {
                val partial = points[visibleCount - 1].let { a ->
                    val b = points[visibleCount]
                    androidx.compose.ui.geometry.Offset(
                        x = a.x + (b.x - a.x) * partialFraction,
                        y = a.y + (b.y - a.y) * partialFraction,
                    )
                }
                points.take(visibleCount) + listOf(partial)
            } else {
                points
            }

            // 라인
            val path = Path().apply {
                animPoints.forEachIndexed { i, pt ->
                    if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                }
            }
            drawPath(
                path  = path,
                color = lineColor,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap   = StrokeCap.Round,
                    join  = StrokeJoin.Round,
                ),
            )

            // 점 + 라벨 (애니메이션 완료된 것만)
            animPoints.forEachIndexed { i, pt ->
                if (i >= visibleCount) return@forEachIndexed

                val isSelected = selectedIdx == i
                val dotRadius  = if (isSelected) 6.dp.toPx() else 4.dp.toPx()

                // 선택 시 수직 가이드선
                if (isSelected) {
                    drawLine(
                        color       = lineColor.copy(alpha = 0.25f),
                        start       = androidx.compose.ui.geometry.Offset(pt.x, paddingTop),
                        end         = androidx.compose.ui.geometry.Offset(pt.x, paddingTop + chartH),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect  = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(6f, 4f), 0f
                        ),
                    )
                }

                drawCircle(color = Color.White, radius = dotRadius + 1.5.dp.toPx(), center = pt)
                drawCircle(color = dotColor,    radius = dotRadius,                 center = pt)
                if (isSelected) {
                    drawCircle(color = dotColor.copy(alpha = 0.2f), radius = dotRadius + 5.dp.toPx(), center = pt)
                }
            }

            // 선택 툴팁 (점 위에 작은 텍스트)
            if (selectedIdx != null && selectedIdx < animPoints.size) {
                val pt    = animPoints[selectedIdx]
                val label = "${labels[selectedIdx]}\n${scores[selectedIdx].toInt()}점"
                val paint = android.graphics.Paint().apply {
                    color       = android.graphics.Color.WHITE
                    textSize    = 11.dp.toPx()
                    isAntiAlias = true
                    textAlign   = android.graphics.Paint.Align.CENTER
                    typeface    = android.graphics.Typeface.DEFAULT_BOLD
                }

                val boxW  = 52.dp.toPx()
                val boxH  = 34.dp.toPx()
                val boxX  = pt.x.coerceIn(paddingLeft + boxW / 2, size.width - paddingRight - boxW / 2)
                val boxY  = pt.y - 46.dp.toPx()

                // 말풍선 배경
                drawRoundRect(
                    color       = lineColor,
                    topLeft     = androidx.compose.ui.geometry.Offset(boxX - boxW / 2, boxY),
                    size        = androidx.compose.ui.geometry.Size(boxW, boxH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                )
                // 말풍선 꼬리
                val tailPath = Path().apply {
                    moveTo(boxX - 5.dp.toPx(), boxY + boxH)
                    lineTo(boxX + 5.dp.toPx(), boxY + boxH)
                    lineTo(boxX, boxY + boxH + 6.dp.toPx())
                    close()
                }
                drawPath(path = tailPath, color = lineColor)

                // 텍스트 (drawContext.canvas 로 네이티브 그리기)
                drawContext.canvas.nativeCanvas.apply {
                    // 날짜
                    paint.textSize  = 10.dp.toPx()
                    paint.color     = android.graphics.Color.argb(200, 255, 255, 255)
                    drawText(labels[selectedIdx], boxX, boxY + 13.dp.toPx(), paint)
                    // 점수
                    paint.textSize  = 12.dp.toPx()
                    paint.color     = android.graphics.Color.WHITE
                    drawText("${scores[selectedIdx].toInt()}점", boxX, boxY + 27.dp.toPx(), paint)
                }
            }

            // X축 라벨 (항상 표시)
            points.forEachIndexed { i, pt ->
                val paint = android.graphics.Paint().apply {
                    color     = android.graphics.Color.argb(160, 100, 100, 100)
                    textSize  = 9.5.dp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText(
                    labels[i],
                    pt.x,
                    paddingTop + chartH + 20.dp.toPx(),
                    paint,
                )
            }

            // Y축 라벨
            ySteps.forEach { yVal ->
                val yFraction = 1f - yVal / 100f
                val y = paddingTop + yFraction * chartH
                val paint = android.graphics.Paint().apply {
                    color     = android.graphics.Color.argb(140, 100, 100, 100)
                    textSize  = 9.dp.toPx()
                    textAlign = android.graphics.Paint.Align.RIGHT
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "$yVal",
                    paddingLeft - 6.dp.toPx(),
                    y + 4.dp.toPx(),
                    paint,
                )
            }
        }
    }
}

// ── PlantDetailScreen에 BottomNavBar 추가 ────────────────────
// PlantDetailScreen.kt 의 Preview 및 Scaffold 감싸는 부분을 아래로 교체

@Composable
fun PlantDetailScreenWithNav(
    plant    : PlantDetailUiModel = samplePlantDetail,
    onBack   : () -> Unit        = {},
    onAiAnalysis : () -> Unit    = {},
    onAskExpert  : () -> Unit    = {},
) {
    GrowingTheme {
        var selectedTab by remember { mutableStateOf(BottomNavTab.HOME) }
        Scaffold(
            containerColor = Color(0xFFF5F7F5),
            bottomBar = {
                BottomNavBar(
                    selectedTab   = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                PlantDetailScreen(
                    plant        = plant,
                    onBack       = onBack,
                    onAiAnalysis = onAiAnalysis,
                    onAskExpert  = onAskExpert,
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "AI Analysis Screen")
@Composable
fun AiAnalysisScreenPreview() {
    AiAnalysisScreen()
}