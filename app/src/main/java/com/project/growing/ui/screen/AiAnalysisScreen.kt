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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import com.project.growing.ui.theme.*
import kotlin.math.abs

// ── 샘플 데이터 ──────────────────────────────────────────────

data class AiAnalysisUiModel(
    val statusTitle      : String,
    val statusSub        : String,
    val statusDesc       : String,
    val actions          : List<ActionItem>,
    val futurePrediction : String,
    val healthScores     : List<Float>,
)

data class ActionItem(
    val icon  : String,
    val title : String,
    val desc  : String,
)

val sampleAiAnalysis = AiAnalysisUiModel(
    statusTitle      = "건강한 상태",
    statusSub        = "식물이 잘 자라고 있어요",
    statusDesc       = "몬스테라는 현재 최적의 환경에서 성장하고 있습니다. 잎의 색상이 진한 녹색을 띠고 있으며, 새로운 잎이 규칙적으로 자라고 있습니다.",
    actions          = listOf(
        ActionItem("💧", "물 주기",       "2일 후 물을 주세요"),
        ActionItem("☀️", "햇빛 위치 변경", "창가에서 50cm 더 멀리 배치하세요"),
        ActionItem("🌀", "공기 순환",     "통풍이 잘 되는 곳으로 이동하세요"),
    ),
    futurePrediction = "현재 관리 방식을 유지하면 일주일 후 건강 점수가 70점으로 낮아질 수 있어요. 추천 행동들을 따라주세요!",
    healthScores     = listOf(95f, 92f, 88f, 84f, 80f, 76f, 70f),
)

// ── AiAnalysisScreen ─────────────────────────────────────────

@Composable
fun AiAnalysisScreen(
    analysis : AiAnalysisUiModel = sampleAiAnalysis,
    onBack   : () -> Unit        = {},
) {
    GrowingTheme {
        val listState = rememberLazyListState()

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

        // ── 화면 진입 애니메이션 ──────────────────────────────
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

        LazyColumn(
            state          = listState,
            modifier       = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F5)),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {

            // ── 헤더 ──────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha        = headerAlpha
                            translationY = headerTranslationY
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1B5E20),
                                    Color(0xFF2E7D32),
                                    Color(0xFF43A967),
                                    Color(0xFF66BB7A),
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp)
                ) {
                    Column {
                        // 뒤로가기
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    imageVector        = Icons.Rounded.ArrowBackIosNew,
                                    contentDescription = "뒤로가기",
                                    tint               = White,
                                    modifier           = Modifier.size(16.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // AI 뱃지
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint               = White,
                                modifier           = Modifier.size(13.dp),
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(text = "AI 분석", fontSize = 11.sp, color = White, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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
            }

            // ── 식물 이미지 카드 ───────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .height(160.dp)
                        .graphicsLayer { alpha = contentAlpha; translationY = contentSlide }
                        .shadow(8.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFF2E5C38), Color(0xFF5D9467)))
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "🌿", fontSize = 64.sp)
                    // TODO: 실제 이미지로 교체
                }
            }

            // ── 상태 요약 카드 (한눈에 보이는 디자인) ────────────
            item {
                Spacer(modifier = Modifier.height(14.dp))
                AiCard(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .graphicsLayer { alpha = contentAlpha; translationY = contentSlide }
                ) {
                    // 상단 상태 배지
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 큰 상태 아이콘
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint               = Color(0xFF43A967),
                                modifier           = Modifier.size(28.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = analysis.statusTitle,
                                fontSize   = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary,
                            )
                            Text(
                                text     = analysis.statusSub,
                                fontSize = 13.sp,
                                color    = TextSecondary,
                            )
                        }
                        // 점수 배지
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF43A967))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text       = "95점",
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color      = White,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // 상태 설명 (배경 강조)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0FAF4))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector        = Icons.Rounded.Info,
                                contentDescription = null,
                                tint               = Color(0xFF43A967),
                                modifier           = Modifier.size(16.dp).padding(top = 1.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text       = analysis.statusDesc,
                                fontSize   = 13.sp,
                                color      = Color(0xFF2E7D32),
                                lineHeight = 20.sp,
                            )
                        }
                    }
                }
            }

            // ── 추천 행동 카드 ────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(14.dp))
                AiCard(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .graphicsLayer { alpha = contentAlpha; translationY = contentSlide }
                ) {
                    // 타이틀
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF43A967)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.Lightbulb,
                                contentDescription = null,
                                tint               = White,
                                modifier           = Modifier.size(15.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text       = "추천 행동",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text       = "${analysis.actions.size}가지",
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color(0xFF43A967),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    analysis.actions.forEachIndexed { idx, action ->
                        // 각 액션을 번호 + 카드 형태로
                        Row(
                            modifier          = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                        ) {
                            // 번호
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF43A967)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text       = "${idx + 1}",
                                    fontSize   = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = White,
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            // 액션 내용
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF8FAF8))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = action.icon, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text       = action.title,
                                            fontSize   = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color      = TextPrimary,
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text       = action.desc,
                                            fontSize   = 12.sp,
                                            color      = TextSecondary,
                                            lineHeight = 17.sp,
                                        )
                                    }
                                }
                            }
                        }
                        if (idx < analysis.actions.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // ── 미래 예측 + 그래프 카드 ───────────────────────
            item {
                Spacer(modifier = Modifier.height(14.dp))
                AiCard(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .graphicsLayer { alpha = contentAlpha; translationY = contentSlide }
                ) {
                    // 타이틀
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFB300)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.TrendingDown,
                                contentDescription = null,
                                tint               = White,
                                modifier           = Modifier.size(15.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text       = "미래 건강 예측",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 예측 설명 강조
                    val desc          = analysis.futurePrediction
                    val highlightWord = "70점"
                    val startIdx      = desc.indexOf(highlightWord)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFF8E1))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector        = Icons.Rounded.Warning,
                                contentDescription = null,
                                tint               = Color(0xFFFFB300),
                                modifier           = Modifier.size(15.dp).padding(top = 1.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (startIdx >= 0) {
                                Text(
                                    text = buildAnnotatedString {
                                        append(desc.substring(0, startIdx))
                                        withStyle(SpanStyle(color = Color(0xFFE65100), fontWeight = FontWeight.Bold)) {
                                            append(highlightWord)
                                        }
                                        append(desc.substring(startIdx + highlightWord.length))
                                    },
                                    fontSize   = 13.sp,
                                    color      = Color(0xFF795548),
                                    lineHeight = 20.sp,
                                )
                            } else {
                                Text(text = desc, fontSize = 13.sp, color = Color(0xFF795548), lineHeight = 20.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 그래프
                    HealthLineChart(
                        scores   = analysis.healthScores,
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 범례
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF43A967))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "예측 건강 점수 추이", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
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
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x10000000), spotColor = Color(0x18000000))
            .clip(RoundedCornerShape(20.dp))
            .background(White)
    ) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}

// ── 꺾은선 그래프 ─────────────────────────────────────────────

@Composable
fun HealthLineChart(
    scores   : List<Float>,
    modifier : Modifier = Modifier,
) {
    val labels    = listOf("오늘", "+1일", "+2일", "+3일", "+4일", "+5일", "+6일")
    val lineColor = Color(0xFF43A967)
    val gridColor = Color(0xFFEEEEEE)

    // ── 그래프 드로잉 애니메이션 ──────────────────────────────
    var animStarted by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue   = if (animStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 1400, easing = EaseInOutCubic),
        label         = "chartAnim",
    )
    LaunchedEffect(Unit) { animStarted = true }

    // ── 터치 상태 ─────────────────────────────────────────────
    var touchX by remember { mutableStateOf<Float?>(null) }

    BoxWithConstraints(modifier = modifier) {
        val density      = LocalDensity.current
        val canvasWidth  = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()

        val paddingLeft   = with(density) { 40.dp.toPx() }
        val paddingRight  = with(density) { 16.dp.toPx() }
        val paddingTop    = with(density) { 24.dp.toPx() }
        val paddingBottom = with(density) { 36.dp.toPx() }
        val chartW = canvasWidth  - paddingLeft - paddingRight
        val chartH = canvasHeight - paddingTop  - paddingBottom

        val points = scores.mapIndexed { i, score ->
            Offset(
                x = paddingLeft + i * (chartW / (scores.size - 1)),
                y = paddingTop  + (1f - score / 100f) * chartH,
            )
        }

        val selectedIdx = touchX?.let { tx ->
            points.indices.minByOrNull { i -> abs(points[i].x - tx) }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart  = { o: Offset -> touchX = o.x },
                        onDrag       = { _: PointerInputChange, d: Offset -> touchX = (touchX ?: 0f) + d.x },
                        onDragEnd    = { touchX = null },
                        onDragCancel = { touchX = null },
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { o: Offset -> touchX = o.x; tryAwaitRelease(); touchX = null }
                    )
                }
        ) {
            // Y 그리드
            listOf(0, 25, 50, 75, 100).forEach { yVal ->
                val y = paddingTop + (1f - yVal / 100f) * chartH
                drawLine(gridColor, Offset(paddingLeft, y), Offset(size.width - paddingRight, y), 1.dp.toPx())
                drawContext.canvas.nativeCanvas.drawText(
                    "$yVal", paddingLeft - 6.dp.toPx(), y + 4.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(140, 100, 100, 100)
                        textSize = 9.dp.toPx(); textAlign = android.graphics.Paint.Align.RIGHT; isAntiAlias = true
                    }
                )
            }

            // 애니메이션 포인트 계산
            val visibleCount    = (animProgress * (scores.size - 1)).toInt() + 1
            val partialFraction = (animProgress * (scores.size - 1)) - (visibleCount - 1)
            val animPoints = if (visibleCount < scores.size) {
                val a = points[visibleCount - 1]; val b = points[visibleCount]
                points.take(visibleCount) + listOf(Offset(a.x + (b.x - a.x) * partialFraction, a.y + (b.y - a.y) * partialFraction))
            } else points

            // 그라디언트 fill
            val fillPath = Path().apply {
                animPoints.forEachIndexed { i, pt -> if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y) }
                lineTo(animPoints.last().x, paddingTop + chartH)
                lineTo(animPoints.first().x, paddingTop + chartH)
                close()
            }
            drawPath(
                path  = fillPath,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF43A967).copy(alpha = 0.2f), Color(0xFF43A967).copy(alpha = 0f)),
                    startY = paddingTop, endY = paddingTop + chartH,
                ),
            )

            // 라인
            val linePath = Path().apply {
                animPoints.forEachIndexed { i, pt -> if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y) }
            }
            drawPath(linePath, lineColor, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

            // 포인트 + 선택 효과
            animPoints.forEachIndexed { i, pt ->
                if (i >= visibleCount) return@forEachIndexed
                val sel = selectedIdx == i
                val r   = if (sel) 6.dp.toPx() else 4.dp.toPx()

                if (sel) {
                    // 수직 점선
                    drawLine(
                        color       = lineColor.copy(alpha = 0.3f),
                        start       = Offset(pt.x, paddingTop),
                        end         = Offset(pt.x, paddingTop + chartH),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect  = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f),
                    )
                    drawCircle(lineColor.copy(alpha = 0.15f), r + 6.dp.toPx(), pt)
                }
                drawCircle(Color.White, r + 2.dp.toPx(), pt)
                drawCircle(lineColor, r, pt)
            }

            // ── 툴팁 (기록 화면 스타일: 흰 박스) ─────────────────
            if (selectedIdx != null && selectedIdx < animPoints.size) {
                val pt    = animPoints[selectedIdx]
                val line1 = labels[selectedIdx]
                val line2 = "score : ${scores[selectedIdx].toInt()}"

                val titleP = android.graphics.Paint().apply {
                    textSize = 30f; isFakeBoldText = true; isAntiAlias = true
                    color = android.graphics.Color.argb(255, 30, 130, 60)
                    textAlign = android.graphics.Paint.Align.LEFT
                }
                val bodyP = android.graphics.Paint().apply {
                    textSize = 26f; isAntiAlias = true
                    color = android.graphics.Color.argb(200, 80, 80, 80)
                    textAlign = android.graphics.Paint.Align.LEFT
                }

                val boxW   = maxOf(titleP.measureText(line1), bodyP.measureText(line2)) + 52f
                val boxH   = 80f
                val radius = 16f
                var boxL   = pt.x - boxW / 2f
                var boxT   = pt.y - boxH - 18f
                if (boxL < paddingLeft) boxL = paddingLeft
                if (boxL + boxW > size.width - paddingRight) boxL = size.width - paddingRight - boxW
                if (boxT < 4f) boxT = pt.y + 18f

                // 그림자
                drawContext.canvas.nativeCanvas.drawRoundRect(
                    boxL + 2f, boxT + 3f, boxL + boxW + 2f, boxT + boxH + 3f, radius, radius,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(25, 0, 0, 0)
                        maskFilter = android.graphics.BlurMaskFilter(10f, android.graphics.BlurMaskFilter.Blur.NORMAL)
                    }
                )
                // 흰 박스
                drawContext.canvas.nativeCanvas.drawRoundRect(
                    boxL, boxT, boxL + boxW, boxT + boxH, radius, radius,
                    android.graphics.Paint().apply { color = android.graphics.Color.argb(252, 255, 255, 255) }
                )
                // 테두리
                drawContext.canvas.nativeCanvas.drawRoundRect(
                    boxL, boxT, boxL + boxW, boxT + boxH, radius, radius,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(35, 0, 0, 0)
                        style = android.graphics.Paint.Style.STROKE; strokeWidth = 1.5f
                    }
                )
                // 텍스트
                drawContext.canvas.nativeCanvas.drawText(line1, boxL + 20f, boxT + 28f, titleP)
                drawContext.canvas.nativeCanvas.drawText(line2, boxL + 20f, boxT + 58f, bodyP)
            }

            // X축 날짜
            points.forEachIndexed { i, pt ->
                drawContext.canvas.nativeCanvas.drawText(
                    labels[i], pt.x, paddingTop + chartH + 22.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(160, 100, 100, 100)
                        textSize = 9.5.dp.toPx(); textAlign = android.graphics.Paint.Align.CENTER; isAntiAlias = true
                    }
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