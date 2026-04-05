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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.project.growing.data.consult.ConsultRecord
import com.project.growing.ui.theme.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
@Composable
fun ConsultDetailScreen(
    record : ConsultRecord,
    onBack : () -> Unit = {},
) {
    GrowingTheme {

        // ── 진입 애니메이션 ───────────────────────────────────
        var entered by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { entered = true }

        val contentAlpha by animateFloatAsState(
            targetValue   = if (entered) 1f else 0f,
            animationSpec = tween(500, easing = EaseOutCubic),
            label         = "alpha",
        )
        val contentSlide by animateFloatAsState(
            targetValue   = if (entered) 0f else 30f,
            animationSpec = tween(500, easing = EaseOutCubic),
            label         = "slide",
        )

        // ── 상담사 정보 ───────────────────────────────────────
        val expertEmoji = when (record.expert) {
            "츤데레 선인장"    -> "🌵"
            "숲의 요정"        -> "🧚"
            "아빠 친구 식물학자" -> "👨‍🔬"
            else               -> "🌿"
        }
        val expertColor = when (record.expert) {
            "츤데레 선인장"    -> Color(0xFF43A967)
            "숲의 요정"        -> Color(0xFF9C27B0)
            "아빠 친구 식물학자" -> Color(0xFF1E88E5)
            else               -> Color(0xFF43A967)
        }
        val expertBg = when (record.expert) {
            "츤데레 선인장"    -> listOf(Color(0xFF2E7D32), Color(0xFF43A967), Color(0xFF66BB7A))
            "숲의 요정"        -> listOf(Color(0xFF6A1B9A), Color(0xFF9C27B0), Color(0xFFCE93D8))
            "아빠 친구 식물학자" -> listOf(Color(0xFF1565C0), Color(0xFF1E88E5), Color(0xFF64B5F6))
            else               -> listOf(Color(0xFF2E7D32), Color(0xFF43A967), Color(0xFF66BB7A))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F5))
                .verticalScroll(rememberScrollState())
        ) {

            // ── 상단 헤더 영역 ─────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                // 배경 그라디언트
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(expertBg)),
                )

                // 배경 페이드
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xFFF5F7F5))
                            )
                        )
                )

                // 뒤로가기 버튼
                Box(
                    modifier = Modifier
                        .padding(top = 48.dp, start = 16.dp)
                        .size(36.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Rounded.ArrowBackIosNew,
                            "뒤로가기",
                            tint     = TextPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                // ── 상담사 프로필 영역 ─────────────────────────
                Column(
                    modifier            = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer { alpha = contentAlpha; translationY = contentSlide },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // 상담사 이모지 원
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .shadow(12.dp, CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.2f),
                                spotColor    = Color.Black.copy(alpha = 0.2f))
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(expertEmoji, fontSize = 44.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 상담사 이름
                    Text(
                        text       = record.expert,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 시간
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Rounded.AccessTime,
                            null,
                            tint     = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text     = formatTime(record.createdAt),
                            fontSize = 12.sp,
                            color    = Color.White.copy(alpha = 0.9f),
                        )
                    }
                }
            }

            // ── 이미지 (있을 때만) ─────────────────────────────
            if (record.imageUri != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .height(200.dp)
                        .graphicsLayer { alpha = contentAlpha; translationY = contentSlide }
                        .shadow(8.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    AsyncImage(
                        model              = android.net.Uri.parse(record.imageUri),
                        contentDescription = "식물 사진",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize(),
                    )
                    // 이미지 위 뱃지
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text("첨부 사진", fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 질문 카드 ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .graphicsLayer { alpha = contentAlpha; translationY = contentSlide }
                    .shadow(5.dp, RoundedCornerShape(18.dp),
                        ambientColor = Color(0x0E000000), spotColor = Color(0x0E000000))
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF0F0F0)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Rounded.Person,
                                null,
                                tint     = TextSecondary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text       = "내 질문",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextSecondary,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text       = record.message,
                        fontSize   = 14.sp,
                        color      = TextPrimary,
                        lineHeight = 22.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── AI 답변 카드 ──────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .graphicsLayer { alpha = contentAlpha; translationY = contentSlide }
                    .shadow(5.dp, RoundedCornerShape(18.dp),
                        ambientColor = expertColor.copy(alpha = 0.1f),
                        spotColor    = expertColor.copy(alpha = 0.1f))
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // 답변 헤더
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(expertColor),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("AI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text       = record.expert,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = expertColor,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(expertEmoji, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.8.dp)

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── 답변 내용 ────────────────────────────────
                    // 줄바꿈 기준으로 단락 분리해서 표시
                    // ── 답변 내용 (마크다운 파싱) ────────────────────────────────
                    MarkdownText(
                        text     = record.answer,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MarkdownText(
    text     : String,
    modifier : Modifier = Modifier,
) {
    val GreenPrimary = Color(0xFF43A967)

    Column(modifier = modifier) {
        text.split("\n").forEach { line ->
            when {
                line.startsWith("### ") -> {
                    Text(
                        text       = line.removePrefix("### "),
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary,
                        modifier   = Modifier.padding(vertical = 3.dp),
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        text       = line.removePrefix("## "),
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                        modifier   = Modifier.padding(vertical = 4.dp),
                    )
                }
                line.startsWith("# ") -> {
                    Text(
                        text       = line.removePrefix("# "),
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                        modifier   = Modifier.padding(vertical = 4.dp),
                    )
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    Row(
                        modifier          = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 9.dp, end = 8.dp, start = 4.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(GreenPrimary)
                        )
                        Text(
                            text       = parseBold(line.removePrefix("- ").removePrefix("* ")),
                            fontSize   = 14.sp,
                            lineHeight = 23.sp,
                            color      = TextPrimary,
                            modifier   = Modifier.weight(1f),
                        )
                    }
                }
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }
                else -> {
                    Text(
                        text       = parseBold(line),
                        fontSize   = 14.sp,
                        lineHeight = 23.sp,
                        color      = TextPrimary,
                        modifier   = Modifier.padding(vertical = 2.dp),
                    )
                }
            }
        }
    }
}

// ── **굵게** 파싱 ─────────────────────────────────────────────
fun parseBold(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("**")
        parts.forEachIndexed { idx, part ->
            if (idx % 2 == 1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
    }
}