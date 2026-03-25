package com.project.growing.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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

// ── 데이터 모델 ───────────────────────────────────────────────

data class ChartPoint(val date: String, val score: Int)

data class ActivityRecord(
    val id         : String,
    val emoji      : String,
    val plantName  : String,
    val dateTime   : String,
    val actionIcon : String,   // "water" | "camera"
    val action     : String,
    val memo       : String,
    val scoreDelta : Int?,
)

// ── 샘플 데이터 ───────────────────────────────────────────────

val chartPoints: List<ChartPoint> = listOf(
    ChartPoint("3/10", 85),
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
        id         = "1",
        emoji      = "🌿",
        plantName  = "몬스테라",
        dateTime   = "2026년 3월 18일 · 오전 10:30",
        actionIcon = "water",
        action     = "물주기",
        memo       = "건강 상태가 좋아졌어요",
        scoreDelta = 2,
    ),
    ActivityRecord(
        id         = "2",
        emoji      = "🌵",
        plantName  = "다육이",
        dateTime   = "2026년 3월 17일 · 오후 3:15",
        actionIcon = "camera",
        action     = "사진 촬영",
        memo       = "AI 분석 완료",
        scoreDelta = null,
    ),
    ActivityRecord(
        id         = "3",
        emoji      = "🍃",
        plantName  = "떡갈고무나무",
        dateTime   = "2026년 3월 16일 · 오전 9:00",
        actionIcon = "water",
        action     = "물주기",
        memo       = "새 잎이 자라고 있어요",
        scoreDelta = 3,
    ),
    ActivityRecord(
        id         = "4",
        emoji      = "🪴",
        plantName  = "스투키",
        dateTime   = "2026년 3월 15일 · 오후 2:20",
        actionIcon = "water",
        action     = "물주기",
        memo       = "과습 주의가 필요해요",
        scoreDelta = -5,
    ),
)

// ── RecordScreen ──────────────────────────────────────────────

@Composable
fun RecordScreen() {
    GrowingTheme {
        var selectedTab by remember { mutableStateOf(BottomNavTab.RECORD) }

        val GreenPrimary = Color(0xFF43A967)

        val drawProgress = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            drawProgress.animateTo(
                targetValue   = 1f,
                animationSpec = tween(durationMillis = 1600, easing = EaseInOutCubic),
            )
        }

        var selectedPointIdx by remember { mutableStateOf<Int?>(null) }

        val avgScore  = chartPoints.map { it.score }.average().toInt()
        val maxScore  = chartPoints.maxOf { it.score }
        val careCount = activityRecords.size

        Scaffold(
            containerColor = Color(0xFFF5F7F5),
            bottomBar = {
                BottomNavBar(
                    selectedTab   = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            },
        ) { innerPadding ->

            LazyColumn(
                modifier       = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {

                // ── 헤더 ──────────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
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
                            .padding(
                                start  = 20.dp,
                                end    = 20.dp,
                                top    = 52.dp,
                                bottom = 24.dp,
                            )
                    ) {
                        Column {
                            Text(
                                text          = "기록",
                                fontSize      = 22.sp,
                                fontWeight    = FontWeight.Bold,
                                color         = Color.White,
                                letterSpacing = (-0.3).sp,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text     = "식물의 성장 과정을 확인하세요",
                                fontSize = 13.sp,
                                color    = Color.White.copy(alpha = 0.88f),
                            )
                        }
                    }
                }

                // ── 건강 점수 차트 카드 ────────────────────────
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    RecordElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text       = "건강 점수 변화",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary,
                            )
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector        = Icons.Rounded.TrendingUp,
                                    contentDescription = null,
                                    tint               = GreenPrimary,
                                    modifier           = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text       = "+10점",
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = GreenPrimary,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LineChart(
                            points        = chartPoints,
                            drawProgress  = drawProgress.value,
                            selectedIdx   = selectedPointIdx,
                            onPointTapped = { idx: Int ->
                                selectedPointIdx = if (selectedPointIdx == idx) null else idx
                            },
                            modifier      = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFF2F2F2), thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            SummaryChip(
                                modifier = Modifier.weight(1f),
                                label    = "평균 점수",
                                value    = "${avgScore}점",
                                color    = TextPrimary,
                                bgColor  = Color(0xFFF5F5F5),
                            )
                            SummaryChip(
                                modifier = Modifier.weight(1f),
                                label    = "최고 점수",
                                value    = "${maxScore}점",
                                color    = GreenPrimary,
                                bgColor  = Color(0xFFE8F5E9),
                            )
                            SummaryChip(
                                modifier = Modifier.weight(1f),
                                label    = "관리 횟수",
                                value    = "${careCount}회",
                                color    = Color(0xFF1E88E5),
                                bgColor  = Color(0xFFE3F2FD),
                            )
                        }
                    }
                }

                // ── 활동 기록 (하나의 카드 안에 리스트) ──────────
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    RecordElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text       = "활동 기록",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary,
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        activityRecords.forEachIndexed { idx, record: ActivityRecord ->
                            ActivityRecordRow(
                                record       = record,
                                greenPrimary = GreenPrimary,
                            )
                            if (idx < activityRecords.lastIndex) {
                                HorizontalDivider(
                                    modifier  = Modifier.padding(
                                        vertical = 12.dp,
                                        horizontal = 0.dp,
                                    ),
                                    color     = Color(0xFFF2F2F2),
                                    thickness = 0.8.dp,
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

// ── 활동 기록 행 (카드 내부 아이템) ──────────────────────────

@Composable
fun ActivityRecordRow(
    record       : ActivityRecord,
    greenPrimary : Color,
    modifier     : Modifier = Modifier,
) {
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 좌측 액션 아이콘 (작은 원)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (record.actionIcon == "water") Color(0xFFE3F2FD)
                    else Color(0xFFF0F0F0)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = if (record.actionIcon == "water")
                    Icons.Rounded.WaterDrop
                else
                    Icons.Rounded.CameraAlt,
                contentDescription = null,
                tint               = if (record.actionIcon == "water")
                    Color(0xFF5BB8F5)
                else
                    Color(0xFF9E9E9E),
                modifier           = Modifier.size(15.dp),
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // 식물 썸네일 (정사각형)
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFE8F5EF)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = record.emoji, fontSize = 28.sp)
            // TODO: 실제 이미지로 교체 (AsyncImage 등)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 텍스트 정보
        Column(modifier = Modifier.weight(1f)) {
            // 식물 이름 + 점수 변화
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text       = record.plantName,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary,
                )
                if (record.scoreDelta != null) {
                    val isPos   = record.scoreDelta > 0
                    val txColor = if (isPos) greenPrimary else Color(0xFFE53935)
                    val prefix  = if (isPos) "+" else ""
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
                            imageVector        = if (isPos) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                            contentDescription = null,
                            tint               = txColor,
                            modifier           = Modifier.size(11.dp),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text       = "$prefix${record.scoreDelta}점",
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color      = txColor,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // 날짜
            Text(
                text     = record.dateTime,
                fontSize = 11.sp,
                color    = TextSecondary,
            )

            Spacer(modifier = Modifier.height(5.dp))

            // 액션
            Text(
                text       = record.action,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Medium,
                color      = TextPrimary,
            )

            Spacer(modifier = Modifier.height(2.dp))

            // 메모
            Text(
                text     = record.memo,
                fontSize = 12.sp,
                color    = TextSecondary,
            )
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
    val paddingLeft   = 36f
    val paddingRight  = 16f
    val paddingTop    = 20f
    val paddingBottom = 28f

    val minScore = (points.minOf { it.score } - 5).toFloat()
    val maxScore = (points.maxOf { it.score } + 3).toFloat()

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
        val chartW = size.width - paddingLeft - paddingRight
        val chartH = size.height - paddingTop - paddingBottom

        for (step in listOf(80, 85, 90, 95, 100)) {
            if (step >= minScore && step <= maxScore) {
                val y = paddingTop + chartH * (1f - (step - minScore) / (maxScore - minScore))
                drawLine(Color(0xFFEEEEEE), Offset(paddingLeft, y), Offset(size.width - paddingRight, y), 1f)
                drawContext.canvas.nativeCanvas.drawText(
                    "$step", paddingLeft - 8f, y + 4f,
                    android.graphics.Paint().apply {
                        textSize  = 22f
                        color     = android.graphics.Color.argb(140, 0, 0, 0)
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )
            }
        }

        val total   = points.size - 1
        val prog    = drawProgress * total
        val full    = prog.toInt().coerceIn(0, total)
        val partial = prog - full

        val fillPath = Path()
        val linePath = Path()

        if (points.isNotEmpty()) {
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
                    listOf(
                        Color(0xFF43A967).copy(alpha = 0.25f),
                        Color(0xFF43A967).copy(alpha = 0f),
                    ),
                    startY = paddingTop,
                    endY   = paddingTop + chartH,
                ),
            )
            drawPath(
                path  = linePath,
                color = Color(0xFF43A967),
                style = Stroke(3f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )

            val visCount = full + 1 + if (full < total && partial > 0.8f) 1 else 0
            for (i in 0 until visCount.coerceAtMost(points.size)) {
                val pt  = getOffset(i, size)
                val sel = selectedIdx == i
                drawCircle(Color.White,       if (sel) 9f else 6f,   pt)
                drawCircle(Color(0xFF43A967), if (sel) 5f else 3.5f, pt)
            }

            if (selectedIdx != null && tooltipAlpha > 0f && selectedIdx < points.size) {
                val pt    = getOffset(selectedIdx, size)
                val label = "${points[selectedIdx].date}  ${points[selectedIdx].score}점"
                val tp    = android.graphics.Paint().apply {
                    textSize       = 28f
                    color          = android.graphics.Color.WHITE
                    textAlign      = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
                val tw = tp.measureText(label)
                val bw = tw + 24f; val bh = 40f
                var bl = pt.x - bw / 2f; var bt = pt.y - bh - 14f
                if (bl < paddingLeft) bl = paddingLeft
                if (bl + bw > size.width - paddingRight) bl = size.width - paddingRight - bw
                if (bt < 0f) bt = pt.y + 14f
                drawContext.canvas.nativeCanvas.drawRoundRect(
                    bl, bt, bl + bw, bt + bh, 12f, 12f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(
                            (tooltipAlpha * 220).toInt(), 34, 85, 50
                        )
                    }
                )
                drawContext.canvas.nativeCanvas.drawText(
                    label, bl + bw / 2f, bt + bh / 2f + 9f,
                    tp.apply { alpha = (tooltipAlpha * 255).toInt() }
                )
            }

            for (i in listOf(0, 2, 4, 6, 7)) {
                if (i < points.size) {
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
    }
}

// ── 요약 칩 ───────────────────────────────────────────────────

@Composable
fun SummaryChip(
    modifier : Modifier,
    label    : String,
    value    : String,
    color    : Color,
    bgColor  : Color,
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
    modifier : Modifier = Modifier,
    content  : @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation    = 8.dp,
                shape        = RoundedCornerShape(20.dp),
                ambientColor = Color(0x14000000),
                spotColor    = Color(0x20000000),
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