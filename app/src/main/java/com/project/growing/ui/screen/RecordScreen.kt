package com.project.growing.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.growing.ui.component.BottomNavBar
import com.project.growing.ui.component.BottomNavTab
import com.project.growing.ui.theme.*
import kotlin.math.abs
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.growing.data.plant.GraphPointDto
import com.project.growing.viewmodel.PlantViewModel

// ── 데이터 모델 ───────────────────────────────────────────────

data class ChartPoint(val date: String, val score: Int)

data class ActivityRecord(
    val id: String,
    val emoji: String,
    val plantName: String,
    val dateTime: String,
    val actionIcon: String,
    val action: String,
    val memo: String,
    val scoreDelta: Int?,
)

// ── 샘플 데이터 ───────────────────────────────────────────────

val chartPoints: List<ChartPoint> = listOf(
    ChartPoint("3/10", 85),
    ChartPoint("3/11", 87),
    ChartPoint("3/12", 88),
    ChartPoint("3/13", 90),
    ChartPoint("3/14", 92),
    ChartPoint("3/15", 91),
    ChartPoint("3/16", 94),
    ChartPoint("3/17", 96),
    ChartPoint("3/18", 95),
)

val activityRecords: List<ActivityRecord> = listOf(
    ActivityRecord(
        id = "1",
        emoji = "🌿",
        plantName = "몬스테라",
        dateTime = "2026년 3월 18일 · 오전 10:30",
        actionIcon = "water",
        action = "물주기",
        memo = "건강 상태가 좋아졌어요",
        scoreDelta = 2,
    ),
    ActivityRecord(
        id = "2",
        emoji = "🌵",
        plantName = "다육이",
        dateTime = "2026년 3월 17일 · 오후 3:15",
        actionIcon = "camera",
        action = "사진 촬영",
        memo = "AI 분석 완료",
        scoreDelta = null,
    ),
    ActivityRecord(
        id = "3",
        emoji = "🍃",
        plantName = "떡갈고무나무",
        dateTime = "2026년 3월 16일 · 오전 9:00",
        actionIcon = "water",
        action = "물주기",
        memo = "새 잎이 자라고 있어요",
        scoreDelta = 3,
    ),
    ActivityRecord(
        id = "4",
        emoji = "🪴",
        plantName = "스투키",
        dateTime = "2026년 3월 15일 · 오후 2:20",
        actionIcon = "water",
        action = "물주기",
        memo = "과습 주의가 필요해요",
        scoreDelta = -5,
    ),
)

// ── RecordScreen ──────────────────────────────────────────────

@Composable
fun RecordScreen(
    plantViewModel: PlantViewModel = viewModel(),
) {
    val homeState   by plantViewModel.homeState.collectAsStateWithLifecycle()
    val recordState by plantViewModel.recordState.collectAsStateWithLifecycle()

    // ── 선택된 식물 ───────────────────────────────────────────
    var selectedPlantId by remember { mutableStateOf<Int?>(null) }

    // ── 홈 화면 식물 목록 로드 ────────────────────────────────
    LaunchedEffect(Unit) {
        plantViewModel.loadHomePlants()
    }

    // ── 식물 목록 로드되면 첫 번째 식물 자동 선택 ────────────
    LaunchedEffect(homeState.plants) {
        if (homeState.plants.isNotEmpty() && selectedPlantId == null) {
            selectedPlantId = homeState.plants.first().plantId
        }
    }

    // ── 식물 선택 시 그래프 로드 ──────────────────────────────
    LaunchedEffect(selectedPlantId) {
        selectedPlantId?.let { plantViewModel.loadGraph(it) }
    }

    // ── 기존 코드에서 chartPoints 대신 실제 데이터 사용 ──────
    val chartPoints = recordState.graphPoints.mapIndexed { idx, point ->
        ChartPoint(
            date  = point.date?.take(5) ?: "${idx + 1}일",  // 날짜 짧게
            score = point.score?.toInt() ?: 0,
        )
    }

    // ── 기존 GrowingTheme 코드 유지 ───────────────────────────
    GrowingTheme {
        val GreenPrimary = Color(0xFF43A967)

        val drawProgress = remember { Animatable(0f) }

        // 그래프 데이터 바뀔 때마다 애니메이션 재실행
        LaunchedEffect(chartPoints) {
            drawProgress.snapTo(0f)
            drawProgress.animateTo(
                targetValue   = 1f,
                animationSpec = tween(durationMillis = 1600, easing = EaseInOutCubic),
            )
        }

        var selectedPointIdx by remember { mutableStateOf<Int?>(null) }

        val avgScore  = if (chartPoints.isNotEmpty()) chartPoints.map { it.score }.average().toInt() else 0
        val maxScore  = if (chartPoints.isNotEmpty()) chartPoints.maxOf { it.score } else 0
        val careCount = chartPoints.size

        LazyColumn(
            modifier       = Modifier.fillMaxSize().background(Color(0xFFF5F7F5)),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {

            // ── 헤더 ──────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(
                                Color(0xFF1B5E20), Color(0xFF2E7D32),
                                Color(0xFF43A967), Color(0xFF66BB7A),
                            ))
                        )
                        .padding(start = 20.dp, end = 20.dp, top = 52.dp, bottom = 24.dp)
                ) {
                    Column {
                        Text("기록", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = (-0.3).sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("식물의 성장 과정을 확인하세요", fontSize = 13.sp, color = Color.White.copy(alpha = 0.88f))
                    }
                }
            }

            // ── 식물 선택 탭 ───────────────────────────────────
            if (homeState.plants.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        contentPadding        = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(homeState.plants) { plant ->
                            val isSelected = selectedPlantId == plant.plantId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isSelected) GreenPrimary
                                        else Color.White
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) GreenPrimary else Color(0xFFDDDDDD),
                                        RoundedCornerShape(50)
                                    )
                                    .clickable(
                                        indication        = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) { selectedPlantId = plant.plantId }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text       = plant.plantName ?: "식물 ${plant.plantId}",
                                    fontSize   = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color      = if (isSelected) Color.White else TextSecondary,
                                )
                            }
                        }
                    }
                }
            }

            // ── 로딩 중 ───────────────────────────────────────
            if (recordState.isLoading) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
            }

            // ── 데이터 없을 때 ────────────────────────────────
            if (!recordState.isLoading && chartPoints.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📊", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text       = "아직 기록이 없어요\n식물을 업데이트하면 그래프가 생겨요",
                                fontSize   = 14.sp,
                                color      = TextSecondary,
                                textAlign  = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 20.sp,
                            )
                        }
                    }
                }
            }

            // ── 건강 점수 차트 카드 ────────────────────────────
            if (chartPoints.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    RecordElevatedCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("건강 점수 변화", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            if (chartPoints.size >= 2) {
                                val diff = chartPoints.last().score - chartPoints.first().score
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(if (diff >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector        = if (diff >= 0) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                                        contentDescription = null,
                                        tint               = if (diff >= 0) GreenPrimary else Color(0xFFE53935),
                                        modifier           = Modifier.size(14.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text       = "${if (diff >= 0) "+" else ""}${diff}점",
                                        fontSize   = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = if (diff >= 0) GreenPrimary else Color(0xFFE53935),
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LineChart(
                            points        = chartPoints,
                            drawProgress  = drawProgress.value,
                            selectedIdx   = selectedPointIdx,
                            onPointTapped = { idx ->
                                selectedPointIdx = if (selectedPointIdx == idx) null else idx
                            },
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFF2F2F2), thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            SummaryChip(Modifier.weight(1f), "평균 점수", "${avgScore}점", TextPrimary, Color(0xFFF5F5F5))
                            SummaryChip(Modifier.weight(1f), "최고 점수", "${maxScore}점", GreenPrimary, Color(0xFFE8F5E9))
                            SummaryChip(Modifier.weight(1f), "기록 횟수", "${careCount}회", Color(0xFF1E88E5), Color(0xFFE3F2FD))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ── 타임라인 행 ───────────────────────────────────────────────

@Composable
fun TimelineActivityRow(
    record: ActivityRecord,
    isLast: Boolean,
    greenPrimary: Color,
) {
    // 아이콘 원 크기
    val iconCircleSize = 32.dp

    Row(modifier = Modifier.fillMaxWidth()) {

        // ── 좌측: 아이콘 원 + 수직선 ─────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(iconCircleSize),
        ) {
            // 아이콘 원
            Box(
                modifier = Modifier
                    .size(iconCircleSize)
                    .clip(CircleShape)
                    .background(
                        if (record.actionIcon == "water") Color(0xFFE3F2FD)
                        else Color(0xFFF0F0F0)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (record.actionIcon == "water")
                        Icons.Rounded.WaterDrop else Icons.Rounded.CameraAlt,
                    contentDescription = null,
                    tint = if (record.actionIcon == "water")
                        Color(0xFF5BB8F5) else Color(0xFF9E9E9E),
                    modifier = Modifier.size(14.dp),
                )
            }

            // 연결선 (마지막 제외)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .weight(1f)
                        .background(Color(0xFFDDDDDD))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // ── 우측: 썸네일 + 텍스트 ────────────────────────────
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // 식물 썸네일
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFDCEFE3)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = record.emoji, fontSize = 28.sp)
                // TODO: 실제 이미지로 교체
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 텍스트 정보
            Column(modifier = Modifier.weight(1f)) {
                // 식물명 + 점수 뱃지
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = record.plantName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    if (record.scoreDelta != null) {
                        val isPos = record.scoreDelta > 0
                        val txColor = if (isPos) greenPrimary else Color(0xFFE53935)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isPos) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                )
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = if (isPos) Icons.Rounded.TrendingUp
                                else Icons.Rounded.TrendingDown,
                                contentDescription = null,
                                tint = txColor,
                                modifier = Modifier.size(11.dp),
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${if (isPos) "+" else ""}${record.scoreDelta}점",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = txColor,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // 날짜
                Text(
                    text = record.dateTime,
                    fontSize = 11.sp,
                    color = TextSecondary,
                )

                Spacer(modifier = Modifier.height(5.dp))

                // 액션
                Text(
                    text = record.action,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )

                Spacer(modifier = Modifier.height(2.dp))

                // 메모
                Text(
                    text = record.memo,
                    fontSize = 12.sp,
                    color = TextSecondary,
                )
            }
        }
    }
}

// ── 라인 차트 ─────────────────────────────────────────────────

@Composable
fun LineChart(
    points        : List<ChartPoint>,
    drawProgress  : Float,
    selectedIdx   : Int?,
    onPointTapped : (Int) -> Unit,
    modifier      : Modifier = Modifier,
) {
    // ── 데이터 없거나 1개면 표시 안 함 ───────────────────────
    if (points.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("데이터가 부족해요", fontSize = 13.sp, color = TextSecondary)
        }
        return
    }

    val paddingLeft   = 38f
    val paddingRight  = 16f
    val paddingTop    = 72f
    val paddingBottom = 28f

    // ── Y축 범위 동적 계산 ────────────────────────────────────
    val minScore = (points.minOf { it.score } - 5).coerceAtLeast(0).toFloat()
    val maxScore = (points.maxOf { it.score } + 5).coerceAtMost(100).toFloat()

    val tooltipAlpha by animateFloatAsState(
        targetValue   = if (selectedIdx != null) 1f else 0f,
        animationSpec = tween(200),
        label         = "tooltip_alpha",
    )

    var canvasSize by remember { mutableStateOf(Size.Zero) }

    fun getOffset(idx: Int, size: Size): Offset {
        val chartW = size.width - paddingLeft - paddingRight
        val chartH = size.height - paddingTop - paddingBottom
        val x = paddingLeft + idx * (chartW / (points.size - 1))
        val y = paddingTop + chartH * (1f - (points[idx].score - minScore) / (maxScore - minScore))
        return Offset(x, y)
    }

    Canvas(
        modifier = modifier.pointerInput(points) {
            detectTapGestures { tap: Offset ->
                if (canvasSize == Size.Zero) return@detectTapGestures
                var bestIdx  = 0
                var bestDist = Float.MAX_VALUE
                for (i in points.indices) {
                    val d = abs(tap.x - getOffset(i, canvasSize).x)
                    if (d < bestDist) { bestDist = d; bestIdx = i }
                }
                if (bestDist < 60f) onPointTapped(bestIdx)
            }
        }
    ) {
        canvasSize = size
        val chartH = size.height - paddingTop - paddingBottom

        // ── Y 가이드라인 동적 생성 ─────────────────────────────
        val range     = (maxScore - minScore).toInt()
        val step      = when {
            range <= 20  -> 5
            range <= 50  -> 10
            range <= 100 -> 20
            else         -> 25
        }
        val startVal  = (minScore.toInt() / step) * step

        var yVal = startVal
        while (yVal <= maxScore.toInt()) {
            val y = paddingTop + chartH * (1f - (yVal - minScore) / (maxScore - minScore))
            if (y in paddingTop..(paddingTop + chartH)) {
                drawLine(
                    color       = Color(0xFFEEEEEE),
                    start       = Offset(paddingLeft, y),
                    end         = Offset(size.width - paddingRight, y),
                    strokeWidth = 1f,
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "$yVal", paddingLeft - 8f, y + 4f,
                    android.graphics.Paint().apply {
                        textSize  = 22f
                        color     = android.graphics.Color.argb(140, 0, 0, 0)
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )
            }
            yVal += step
        }

        val total   = points.size - 1
        val prog    = drawProgress * total
        val full    = prog.toInt().coerceIn(0, total)
        val partial = prog - full

        val fillPath = Path()
        val linePath = Path()

        val p0 = getOffset(0, size)
        fillPath.moveTo(p0.x, p0.y)
        linePath.moveTo(p0.x, p0.y)

        for (i in 1..full) {
            val p = getOffset(i, size)
            fillPath.lineTo(p.x, p.y)
            linePath.lineTo(p.x, p.y)
        }

        val lastPt: Offset = if (full < total && partial > 0f) {
            val from = getOffset(full, size)
            val to   = getOffset(full + 1, size)
            val lx   = from.x + (to.x - from.x) * partial
            val ly   = from.y + (to.y - from.y) * partial
            fillPath.lineTo(lx, ly)
            linePath.lineTo(lx, ly)
            Offset(lx, ly)
        } else {
            getOffset(full.coerceAtMost(points.size - 1), size)
        }

        fillPath.lineTo(lastPt.x, paddingTop + chartH)
        fillPath.lineTo(p0.x, paddingTop + chartH)
        fillPath.close()

        drawPath(
            path  = fillPath,
            brush = Brush.verticalGradient(
                listOf(Color(0xFF43A967).copy(alpha = 0.22f), Color(0xFF43A967).copy(alpha = 0f)),
                startY = paddingTop, endY = paddingTop + chartH,
            ),
        )
        drawPath(
            path  = linePath,
            color = Color(0xFF43A967),
            style = Stroke(3f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        // 포인트 점
        val visCount = full + 1 + if (full < total && partial > 0.8f) 1 else 0
        for (i in 0 until visCount.coerceAtMost(points.size)) {
            val pt  = getOffset(i, size)
            val sel = selectedIdx == i
            if (sel) drawCircle(Color(0xFF43A967).copy(alpha = 0.15f), 18f, pt)
            drawCircle(Color.White,       if (sel) 10f else 5.5f, pt)
            drawCircle(Color(0xFF43A967), if (sel) 6f  else 3f,   pt)
        }

        // 툴팁
        if (selectedIdx != null && tooltipAlpha > 0f && selectedIdx < points.size) {
            val pt    = getOffset(selectedIdx, size)
            val date  = points[selectedIdx].date
            val score = points[selectedIdx].score

            val datePaint = android.graphics.Paint().apply {
                textSize       = 38f
                color          = android.graphics.Color.argb((tooltipAlpha * 255).toInt(), 30, 130, 60)
                isFakeBoldText = true
                textAlign      = android.graphics.Paint.Align.LEFT
            }
            val scorePaint = android.graphics.Paint().apply {
                textSize  = 32f
                color     = android.graphics.Color.argb((tooltipAlpha * 210).toInt(), 80, 80, 80)
                textAlign = android.graphics.Paint.Align.LEFT
            }

            val line1    = date
            val line2    = "score : $score"
            val contentW = maxOf(datePaint.measureText(line1), scorePaint.measureText(line2))
            val boxW     = contentW + 60f
            val boxH     = 96f
            val radius   = 18f

            var boxL = pt.x - boxW / 2f
            var boxT = pt.y - boxH - 22f
            if (boxL < paddingLeft) boxL = paddingLeft
            if (boxL + boxW > size.width - paddingRight) boxL = size.width - paddingRight - boxW
            if (boxT < 2f) boxT = pt.y + 22f

            drawContext.canvas.nativeCanvas.drawRoundRect(
                boxL + 2f, boxT + 4f, boxL + boxW + 2f, boxT + boxH + 4f, radius, radius,
                android.graphics.Paint().apply {
                    color       = android.graphics.Color.argb((tooltipAlpha * 30).toInt(), 0, 0, 0)
                    maskFilter  = android.graphics.BlurMaskFilter(12f, android.graphics.BlurMaskFilter.Blur.NORMAL)
                }
            )
            drawContext.canvas.nativeCanvas.drawRoundRect(
                boxL, boxT, boxL + boxW, boxT + boxH, radius, radius,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.argb((tooltipAlpha * 252).toInt(), 255, 255, 255)
                }
            )
            drawContext.canvas.nativeCanvas.drawRoundRect(
                boxL, boxT, boxL + boxW, boxT + boxH, radius, radius,
                android.graphics.Paint().apply {
                    color       = android.graphics.Color.argb((tooltipAlpha * 35).toInt(), 0, 0, 0)
                    style       = android.graphics.Paint.Style.STROKE
                    strokeWidth = 1.5f
                }
            )
            drawContext.canvas.nativeCanvas.drawText(line1, boxL + 24f, boxT + 36f, datePaint)
            drawContext.canvas.nativeCanvas.drawText(line2, boxL + 24f, boxT + 72f, scorePaint)
        }

        // X축 날짜 - 전체 점 표시
        val step2 = when {
            points.size <= 5  -> 1
            points.size <= 10 -> 2
            else              -> points.size / 5
        }
        for (i in points.indices step step2) {
            val pt = getOffset(i, size)
            drawContext.canvas.nativeCanvas.drawText(
                points[i].date, pt.x, size.height - 2f,
                android.graphics.Paint().apply {
                    textSize  = 22f
                    color     = android.graphics.Color.argb(140, 0, 0, 0)
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

// ── 요약 칩 ───────────────────────────────────────────────────

@Composable
fun SummaryChip(
    modifier: Modifier,
    label: String,
    value: String,
    color: Color,
    bgColor: Color,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

// ── 카드 래퍼 ─────────────────────────────────────────────────

@Composable
fun RecordElevatedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0x12000000),
                spotColor = Color(0x18000000),
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF9F9F9))
                )
            )
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

// ── Preview ────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Record Screen")
@Composable
fun RecordScreenPreview() {
    RecordScreen()
}