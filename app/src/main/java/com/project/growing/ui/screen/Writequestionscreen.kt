package com.project.growing.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.growing.ui.component.BottomNavBar
import com.project.growing.ui.component.BottomNavTab
import com.project.growing.ui.theme.*

// ── 전문가 목록 ────────────────────────────────────────────────

private data class ExpertOption(
    val name      : String,
    val specialty : String,
    val avgTime   : String,
    val rating    : Float,
)

private val expertOptions = listOf(
    ExpertOption("김정원", "관엽식물 전문", "평균 2시간", 4.9f),
    ExpertOption("이현수", "다육식물 전문", "평균 3시간", 4.8f),
    ExpertOption("박민지", "병충해 전문",   "평균 1시간", 4.9f),
)

// ── WriteQuestionScreen ────────────────────────────────────────

@Composable
fun WriteQuestionScreen(
    onBack   : () -> Unit = {},
    onSubmit : () -> Unit = {},
) {
    GrowingTheme {
        var selectedTab      by remember { mutableStateOf(BottomNavTab.CHAT) }
        var descriptionText  by remember { mutableStateOf("") }
        var selectedExpert   by remember { mutableStateOf<String?>(null) }

        val GreenPrimary = Color(0xFF43A967)

        Scaffold(
            containerColor = Color(0xFFF5F7F5),
            bottomBar = {
                BottomNavBar(
                    selectedTab   = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            },
        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {

                // ── 스크롤 콘텐츠 ──────────────────────────────
                LazyColumn(
                    modifier       = Modifier.fillMaxSize(),
                    // 하단 버튼 높이(52) + 버티컬 패딩(14*2) + 여유 = 100dp
                    contentPadding = PaddingValues(bottom = 100.dp),
                ) {

                    // ── 헤더 ──────────────────────────────────
                    item {
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
                                    start  = 16.dp,
                                    end    = 16.dp,
                                    top    = 48.dp,
                                    bottom = 24.dp,
                                )
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .clickable(
                                            indication        = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                            onClick           = onBack,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector        = Icons.Rounded.ArrowBackIosNew,
                                        contentDescription = "뒤로가기",
                                        tint               = Color.White,
                                        modifier           = Modifier.size(18.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text          = "질문 작성하기",
                                    fontSize      = 22.sp,
                                    fontWeight    = FontWeight.Bold,
                                    color         = Color.White,
                                    letterSpacing = (-0.3).sp,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text     = "식물의 문제를 자세히 알려주세요",
                                    fontSize = 13.sp,
                                    color    = Color.White.copy(alpha = 0.88f),
                                )
                            }
                        }
                    }

                    // ── 식물 사진 카드 ─────────────────────────
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text       = "식물 사진",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary,
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = 1.5.dp,
                                        color = Color(0xFFDDDDDD),
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                    .background(Color(0xFFF9F9F9)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(GreenPrimary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector        = Icons.Rounded.Upload,
                                            contentDescription = null,
                                            tint               = GreenPrimary,
                                            modifier           = Modifier.size(26.dp),
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text       = "사진 업로드",
                                        fontSize   = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color      = TextPrimary,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text       = "식물의 문제가 잘 보이는 사진을\n올려주세요",
                                        fontSize   = 12.sp,
                                        color      = TextSecondary,
                                        textAlign  = TextAlign.Center,
                                        lineHeight = 17.sp,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick  = { /* TODO: 카메라/갤러리 연동 */ },
                                colors   = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape    = RoundedCornerShape(50),
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                            ) {
                                Icon(
                                    imageVector        = Icons.Rounded.CameraAlt,
                                    contentDescription = null,
                                    modifier           = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text       = "사진 선택",
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }

                    // ── 상세 설명 카드 ─────────────────────────
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text       = "상세 설명",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary,
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value         = descriptionText,
                                onValueChange = { descriptionText = it },
                                modifier      = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                placeholder   = {
                                    Text(
                                        text       = "식물의 상태, 관리 방법, 환경 등을 자세히 적어주세요.\n\n예시:\n- 언제부터 증상이 시작되었나요?",
                                        fontSize   = 13.sp,
                                        color      = TextSecondary.copy(alpha = 0.6f),
                                        lineHeight = 19.sp,
                                    )
                                },
                                shape  = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor   = GreenPrimary,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    cursorColor          = GreenPrimary,
                                ),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize   = 13.sp,
                                    lineHeight = 19.sp,
                                    color      = TextPrimary,
                                ),
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text     = "${descriptionText.length}자",
                                fontSize = 11.sp,
                                color    = TextSecondary,
                                modifier = Modifier.align(Alignment.End),
                            )
                        }
                    }

                    // ── 전문가 선택 카드 ───────────────────────
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text       = "전문가 선택",
                                    fontSize   = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = TextPrimary,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text     = "(선택사항)",
                                    fontSize = 12.sp,
                                    color    = TextSecondary,
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text     = "특정 전문가를 지정할 수 있어요",
                                fontSize = 12.sp,
                                color    = TextSecondary,
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            ExpertSelectRow(
                                name         = "자동 배정",
                                subtitle     = "가장 적합한 전문가가 답변해요",
                                rating       = null,
                                isSelected   = selectedExpert == null,
                                greenPrimary = GreenPrimary,
                                onClick      = { selectedExpert = null },
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            expertOptions.forEachIndexed { idx, expert ->
                                ExpertSelectRow(
                                    name         = expert.name,
                                    subtitle     = "${expert.specialty}  ·  ${expert.avgTime}",
                                    rating       = expert.rating,
                                    isSelected   = selectedExpert == expert.name,
                                    greenPrimary = GreenPrimary,
                                    onClick      = { selectedExpert = expert.name },
                                )
                                if (idx < expertOptions.lastIndex) {
                                    HorizontalDivider(
                                        modifier  = Modifier.padding(vertical = 10.dp),
                                        color     = Color(0xFFF2F2F2),
                                        thickness = 0.8.dp,
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // ── 하단 고정 버튼 (BottomNavBar 바로 위) ───────
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFF5F7F5).copy(alpha = 0f),
                                    Color(0xFFF5F7F5),
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Button(
                        onClick  = onSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(
                                elevation    = 10.dp,
                                shape        = RoundedCornerShape(16.dp),
                                ambientColor = GreenPrimary.copy(alpha = 0.35f),
                                spotColor    = GreenPrimary.copy(alpha = 0.35f),
                            ),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape  = RoundedCornerShape(16.dp),
                    ) {
                        Icon(
                            imageVector        = Icons.Rounded.Send,
                            contentDescription = null,
                            modifier           = Modifier.size(17.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text       = "질문 등록하기",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

// ── 섹션 카드 래퍼 ─────────────────────────────────────────────

@Composable
private fun SectionCard(
    modifier : Modifier = Modifier,
    content  : @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation    = 4.dp,
                shape        = RoundedCornerShape(16.dp),
                ambientColor = Color(0x0A000000),
                spotColor    = Color(0x0A000000),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content  = content,
        )
    }
}

// ── 전문가 선택 행 ─────────────────────────────────────────────

@Composable
private fun ExpertSelectRow(
    name         : String,
    subtitle     : String,
    rating       : Float?,
    isSelected   : Boolean,
    greenPrimary : Color,
    onClick      : () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue   = if (isSelected) greenPrimary else Color.Transparent,
        animationSpec = tween(200),
        label         = "expert_border_$name",
    )
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) greenPrimary.copy(alpha = 0.05f) else Color.Transparent,
        animationSpec = tween(200),
        label         = "expert_bg_$name",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            )
            .background(bgColor)
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = name,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (isSelected) greenPrimary else TextPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text     = subtitle,
                    fontSize = 12.sp,
                    color    = TextSecondary,
                )
            }

            if (rating != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Rounded.Star,
                        contentDescription = null,
                        tint               = Color(0xFFFFB300),
                        modifier           = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text       = "$rating",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                    )
                }
            }
        }
    }
}

// ── Preview ────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Write Question Screen")
@Composable
fun WriteQuestionScreenPreview() {
    WriteQuestionScreen()
}