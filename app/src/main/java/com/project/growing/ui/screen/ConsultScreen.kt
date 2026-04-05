package com.project.growing.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.growing.ui.component.BottomNavBar
import com.project.growing.ui.component.BottomNavTab
import com.project.growing.ui.theme.*
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.scale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import com.project.growing.data.consult.ConsultRecord
import com.project.growing.viewmodel.ConsultViewModel

// ── 샘플 데이터 ──────────────────────────────────────────────

data class ExpertUiModel(
    val rank     : Int,
    val name     : String,
    val specialty: String,
    val rating   : Float,
    val answers  : Int,
    val emoji    : String,
)

data class QuestionUiModel(
    val id          : String,
    val plantName   : String,
    val plantColor  : Color,
    val timeAgo     : String,
    val title       : String,
    val aiAnswer    : String,
    val answerCount : Int,
    val likeCount   : Int,
    val isAnswered  : Boolean,
    val emoji       : String,
)

val sampleExperts = listOf(
    ExpertUiModel(1, "김정원", "관엽식물 전문", 4.9f, 142, "🌿"),
    ExpertUiModel(2, "이현수", "다육식물 전문", 4.8f, 98,  "🌵"),
    ExpertUiModel(3, "박민지", "병충해 전문",   4.9f, 156, "🍃"),
)

val sampleQuestions = listOf(
    QuestionUiModel(
        id          = "1",
        plantName   = "몬스테라",
        plantColor  = Color(0xFF43A967),
        timeAgo     = "2시간 전",
        title       = "잎 끝이 갈색으로 변하는데 어떻게 해야 하...",
        aiAnswer    = "과습 또는 건조로 인한 증상일 수 있습니다.",
        answerCount = 3,
        likeCount   = 12,
        isAnswered  = true,
        emoji       = "🌿",
    ),
    QuestionUiModel(
        id          = "2",
        plantName   = "다육이",
        plantColor  = Color(0xFFFFB300),
        timeAgo     = "5시간 전",
        title       = "잎이 물러지고 있어요. 살릴 수 있을까요?",
        aiAnswer    = "과습이 의심됩니다. 물 주기를 줄여 보세요.",
        answerCount = 1,
        likeCount   = 5,
        isAnswered  = false,
        emoji       = "🌵",
    ),
)

// ── ConsultScreen ────────────────────────────────────────────

@Composable
fun ConsultScreen(
    consultViewModel : ConsultViewModel    = viewModel(),
    onWriteQuestion : () -> Unit       = {},
    onQuestionClick : (String) -> Unit = {},
) {
    GrowingTheme {
        // ── ViewModel 연결 ─────────────────────────────────────
        val records by consultViewModel.records.collectAsStateWithLifecycle()

        val listState = rememberLazyListState()

        // 기존 애니메이션 코드 유지...
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

        val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
        val fabGlowAlpha by infiniteTransition.animateFloat(
            initialValue  = 0.35f, targetValue = 0.65f,
            animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "fab_glow",
        )
        val fabScale by infiniteTransition.animateFloat(
            initialValue  = 1f, targetValue = 1.04f,
            animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "fab_scale",
        )

        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7F5))
        ) {
            LazyColumn(
                state          = listState,
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
            ) {

                // ── 헤더 ──────────────────────────────────────
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
                            .padding(start = 22.dp, end = 22.dp, top = 52.dp, bottom = 28.dp)
                    ) {
                        Column {
                            Text("전문가 상담", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = White, letterSpacing = (-0.5).sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("AI 상담사가 식물 고민을 해결해드려요 🌱", fontSize = 13.sp, color = White.copy(alpha = 0.88f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                HeaderStatBadge(icon = "🤖", label = "AI 상담사 3명")
                                HeaderStatBadge(icon = "💬", label = "상담 ${records.size}건")
                            }
                        }
                    }
                }

                // ── 상담 기록이 없을 때 ────────────────────────
                if (records.isEmpty()) {
                    item {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🌿", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text       = "아직 상담 기록이 없어요\n+ 버튼을 눌러 질문해보세요",
                                    fontSize   = 14.sp,
                                    color      = TextSecondary,
                                    textAlign  = TextAlign.Center,
                                    lineHeight = 20.sp,
                                )
                            }
                        }
                    }
                }

                // ── 상담 기록 목록 ─────────────────────────────
                if (records.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text("상담 기록", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.2).sp)
                            Text("${records.size}건", fontSize = 13.sp, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    items(
                        items = records,
                        key   = { it.id },
                    ) { record ->
                        ConsultRecordCard(
                            record   = record,
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 5.dp)
                                .clickable(                              // ← 추가
                                    indication        = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { onQuestionClick(record.id) },
                        )
                    }
                }
            }

            // ── FAB ───────────────────────────────────────────
            FloatingActionButton(
                onClick        = onWriteQuestion,
                containerColor = Color(0xFF43A967),
                contentColor   = White,
                shape          = RoundedCornerShape(28.dp),
                modifier       = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .scale(fabScale)
                    .shadow(14.dp, RoundedCornerShape(28.dp),
                        ambientColor = Color(0xFF43A967).copy(alpha = fabGlowAlpha),
                        spotColor    = Color(0xFF43A967).copy(alpha = fabGlowAlpha)),
            ) {
                Row(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("질문 작성하기", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── 상담 기록 카드 ────────────────────────────────────────────

@Composable
fun ConsultRecordCard(
    record   : ConsultRecord,
    modifier : Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(5.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x0D000000), spotColor = Color(0x0D000000))
            .clip(RoundedCornerShape(18.dp))
            .background(White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // ── 상담사 + 이미지 ────────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                // 이미지 (있을 때만)
                if (record.imageUri != null) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model              = android.net.Uri.parse(record.imageUri),
                            contentDescription = "식물 사진",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize(),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    // 상담사 뱃지
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF43A967).copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text       = record.expert,
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color(0xFF43A967),
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text     = formatTime(record.createdAt),
                            fontSize = 11.sp,
                            color    = TextSecondary,
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 질문
                    Text(
                        text       = record.message,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary,
                        maxLines   = 2,
                        overflow   = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── AI 답변 ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF0FAF4))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF43A967)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("AI", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text       = record.answer,
                        fontSize   = 12.sp,
                        color      = TextSecondary,
                        maxLines   = 3,
                        overflow   = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        lineHeight = 18.sp,
                        modifier   = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

// ── 시간 포맷 ─────────────────────────────────────────────────
fun formatTime(timestamp: Long): String {
    val now  = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000         -> "방금 전"
        diff < 3_600_000      -> "${diff / 60_000}분 전"
        diff < 86_400_000     -> "${diff / 3_600_000}시간 전"
        else                  -> "${diff / 86_400_000}일 전"
    }
}

// ── 헤더 통계 뱃지 ───────────────────────────────────────────

@Composable
private fun HeaderStatBadge(icon: String, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(White.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = icon, fontSize = 11.sp)
        Text(
            text       = label,
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color      = White,
        )
    }
}

// ── 전문가 카드: 입장 애니메이션 ─────────────────────────────

@Composable
private fun AnimatedExpertCard() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(500)) + slideInVertically(
            initialOffsetY = { 30 },
            animationSpec  = tween(500, easing = EaseOutCubic),
        ),
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .shadow(
                    elevation    = 6.dp,
                    shape        = RoundedCornerShape(20.dp),
                    ambientColor = Color(0x0F000000),
                    spotColor    = Color(0x0F000000),
                )
                .clip(RoundedCornerShape(20.dp))
                .background(White)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏆", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text       = "전문가 랭킹",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                sampleExperts.forEachIndexed { idx, expert ->
                    ExpertRankRow(expert = expert)
                    if (idx < sampleExperts.lastIndex) {
                        HorizontalDivider(
                            modifier  = Modifier.padding(vertical = 10.dp),
                            color     = Color(0xFFF2F2F2),
                            thickness = 0.8.dp,
                        )
                    }
                }
            }
        }
    }
}

// ── 질문 카드: 입장 애니메이션 래퍼 ──────────────────────────

@Composable
private fun AnimatedQuestionCard(
    question : QuestionUiModel,
    onClick  : () -> Unit,
    modifier : Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(question.id) { visible = true }

    AnimatedVisibility(
        visible  = visible,
        modifier = modifier,
        enter    = fadeIn(tween(450)) + slideInVertically(
            initialOffsetY = { 40 },
            animationSpec  = tween(450, easing = EaseOutCubic),
        ),
    ) {
        QuestionCard(
            question = question,
            onClick  = onClick,
        )
    }
}

// ── 전문가 랭킹 행 ────────────────────────────────────────────

@Composable
fun ExpertRankRow(expert: ExpertUiModel) {
    val animatedRating by animateFloatAsState(
        targetValue   = expert.rating,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label         = "rating_${expert.rank}",
    )

    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when (expert.rank) {
                        1    -> Color(0xFFFFF8E1)
                        2    -> Color(0xFFF5F5F5)
                        3    -> Color(0xFFFBE9E7)
                        else -> Color(0xFFF5F5F5)
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = "${expert.rank}",
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                color      = when (expert.rank) {
                    1    -> Color(0xFFFFB300)
                    2    -> Color(0xFF9E9E9E)
                    3    -> Color(0xFFFF7043)
                    else -> TextSecondary
                },
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = expert.emoji, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = expert.name,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
            )
            Text(
                text     = expert.specialty,
                fontSize = 12.sp,
                color    = TextSecondary,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Rounded.Star,
                    contentDescription = null,
                    tint               = Color(0xFFFFB300),
                    modifier           = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text       = "%.1f".format(animatedRating),
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary,
                )
            }
            Text(
                text     = "${expert.answers}개 답변",
                fontSize = 11.sp,
                color    = TextSecondary,
            )
        }
    }
}

// ── 질문 카드 ─────────────────────────────────────────────────

@Composable
fun QuestionCard(
    question : QuestionUiModel,
    onClick  : () -> Unit,
    modifier : Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "card_scale",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
            .shadow(
                elevation    = if (isPressed) 2.dp else 5.dp,
                shape        = RoundedCornerShape(18.dp),
                ambientColor = Color(0x0D000000),
                spotColor    = Color(0x0D000000),
            )
            .clip(RoundedCornerShape(18.dp))
            .background(White)
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick,
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8F5EF)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = question.emoji, fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(question.plantColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text       = question.plantName,
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = question.plantColor,
                            )
                        }
                        Text(
                            text     = question.timeAgo,
                            fontSize = 11.sp,
                            color    = TextSecondary,
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text       = question.title,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF43A967)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text       = "AI",
                        fontSize   = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color      = White,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text       = question.aiAnswer,
                    fontSize   = 12.sp,
                    color      = TextSecondary,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    modifier   = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Rounded.ChatBubbleOutline,
                            contentDescription = null,
                            tint               = TextSecondary,
                            modifier           = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text     = "${question.answerCount}개 답변",
                            fontSize = 12.sp,
                            color    = TextSecondary,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Rounded.ThumbUp,
                            contentDescription = null,
                            tint               = TextSecondary,
                            modifier           = Modifier.size(13.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text     = "${question.likeCount}",
                            fontSize = 12.sp,
                            color    = TextSecondary,
                        )
                    }
                }

                if (question.isAnswered) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text       = "답변완료",
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color(0xFF43A967),
                        )
                    }
                }
            }
        }
    }
}

// ── Preview ──────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Consult Screen")
@Composable
fun ConsultScreenPreview() {
    ConsultScreen()
}