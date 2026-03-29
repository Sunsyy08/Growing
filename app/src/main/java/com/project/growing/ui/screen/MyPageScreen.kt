package com.project.growing.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.growing.ui.component.BottomNavBar
import com.project.growing.ui.component.BottomNavTab
import com.project.growing.ui.theme.*

// ── 샘플 데이터 ──────────────────────────────────────────────

private data class RecentActivity(
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val title: String,
    val timeAgo: String,
    val hasAlert: Boolean = false,
)

private val sampleActivities = listOf(
    RecentActivity(
        icon = Icons.Rounded.Yard,
        iconBg = Color(0xFFE8F5E9),
        iconTint = Color(0xFF43A967),
        title = "몬스테라 물 주기",
        timeAgo = "오늘 오전 10:30",
    ),
    RecentActivity(
        icon = Icons.Rounded.ChatBubbleOutline,
        iconBg = Color(0xFFE3F2FD),
        iconTint = Color(0xFF1E88E5),
        title = "전문가 답변 도착",
        timeAgo = "2시간 전",
        hasAlert = true,
    ),
    RecentActivity(
        icon = Icons.Rounded.EmojiEvents,
        iconBg = Color(0xFFFFF8E1),
        iconTint = Color(0xFFFFB300),
        title = "식물 마스터 뱃지 획득",
        timeAgo = "1일 전",
    ),
)

// ── MyPageScreen ──────────────────────────────────────────────

@Composable
fun MyPageScreen(
    onEditProfile: () -> Unit = {},
    onNotificationSetting: () -> Unit = {},
    onExpertAnswers: () -> Unit = {},
    onPlantHistory: () -> Unit = {},
    onAppSetting: () -> Unit = {},
    onHelp: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    GrowingTheme {
        val GreenPrimary = Color(0xFF43A967)
        val userName = "김민수"
        val userEmail = "minsu@example.com"
        val plantCount = 4
        val questionCount = 12
        val expertAnswerBadge = 3

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F5)),
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
                            start = 20.dp,
                            end = 20.dp,
                            top = 52.dp,
                            bottom = 24.dp,
                        )
                ) {
                    Column {
                        Text(
                            text = "마이페이지",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.3).sp,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "계정 및 활동 정보",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }
            }

            // ── 프로필 카드 ────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color(0x14000000),
                            spotColor = Color(0x20000000),
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(GreenPrimary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = userName.first().toString(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = userEmail,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = onEditProfile,
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = "편집",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GreenPrimary,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            MiniStatCard(
                                modifier = Modifier.weight(1f),
                                emoji = "🌱",
                                bgColor = Color(0xFFEDF7F1),
                                count = plantCount,
                                label = "관리 중\n인 식물",
                            )
                            MiniStatCard(
                                modifier = Modifier.weight(1f),
                                emoji = "💬",
                                bgColor = Color(0xFFE8F3FD),
                                count = questionCount,
                                label = "작성한\n질문",
                            )
                        }
                    }
                }
            }

            // ── 최근 활동 카드 ─────────────────────────────
            item {
                Spacer(modifier = Modifier.height(14.dp))
                PageCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "최근 활동",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 각 활동을 개별 미니 카드로
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        sampleActivities.forEach { activity ->
                            ActivityCard(activity = activity)
                        }
                    }
                }
            }

            // ── 알림 카드 ──────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(14.dp))
                PageCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "알림",
                        fontSize = 12.sp,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    MenuRow(
                        icon = Icons.Rounded.NotificationsNone,
                        iconBg = Color(0xFFF5F5F5),
                        iconTint = TextSecondary,
                        label = "알림 설정",
                        onClick = onNotificationSetting,
                    )
                }
            }

            // ── 활동 카드 ──────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(14.dp))
                PageCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "활동",
                        fontSize = 12.sp,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    MenuRow(
                        icon = Icons.Rounded.ChatBubbleOutline,
                        iconBg = Color(0xFFE3F2FD),
                        iconTint = Color(0xFF1E88E5),
                        label = "전문가 답변 내역",
                        badge = expertAnswerBadge,
                        onClick = onExpertAnswers,
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Color(0xFFF2F2F2),
                        thickness = 0.8.dp,
                    )
                    MenuRow(
                        icon = Icons.Rounded.Yard,
                        iconBg = Color(0xFFE8F5E9),
                        iconTint = GreenPrimary,
                        label = "식물 관리 기록",
                        onClick = onPlantHistory,
                    )
                }
            }

            // ── 설정 카드 ──────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(14.dp))
                PageCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "설정",
                        fontSize = 12.sp,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    MenuRow(
                        icon = Icons.Rounded.Settings,
                        iconBg = Color(0xFFF5F5F5),
                        iconTint = TextSecondary,
                        label = "앱 설정",
                        onClick = onAppSetting,
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Color(0xFFF2F2F2),
                        thickness = 0.8.dp,
                    )
                    MenuRow(
                        icon = Icons.Rounded.HelpOutline,
                        iconBg = Color(0xFFF5F5F5),
                        iconTint = TextSecondary,
                        label = "도움말",
                        onClick = onHelp,
                    )
                }
            }

            // ── 로그아웃 ───────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(14.dp),
                            ambientColor = Color(0x0A000000),
                            spotColor = Color(0x0A000000),
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onLogout,
                        )
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Logout,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "로그아웃",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                        )
                    }
                }
            }

            // ── 버전 정보 ──────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "PlantCare AI v1.0.0",
                        fontSize = 11.sp,
                        color = TextSecondary.copy(alpha = 0.6f),
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "© 2026 PlantCare. All rights reserved.",
                        fontSize = 10.sp,
                        color = TextSecondary.copy(alpha = 0.45f),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ── 활동 개별 카드 ─────────────────────────────────────────────

@Composable
private fun ActivityCard(activity: RecentActivity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color(0x0A000000),
                spotColor = Color(0x0A000000),
            )
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8F9F8))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 아이콘
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(activity.iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = activity.icon,
                    contentDescription = null,
                    tint = activity.iconTint,
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = activity.timeAgo,
                    fontSize = 11.sp,
                    color = TextSecondary,
                )
            }

            // 알림 닷
            if (activity.hasAlert) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF43A967))
                )
            }
        }
    }
}

// ── 통계 미니 카드 ─────────────────────────────────────────────

@Composable
private fun MiniStatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    bgColor: Color,
    count: Int,
    label: String,
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color(0x0E000000),
                spotColor = Color(0x0E000000),
            )
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .padding(vertical = 14.dp, horizontal = 12.dp),
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${count}개",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp,
            )
        }
    }
}

// ── 메뉴 행 ───────────────────────────────────────────────────

@Composable
private fun MenuRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    badge: Int? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(17.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
        if (badge != null && badge > 0) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF43A967))
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$badge",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFBBBBBB),
            modifier = Modifier.size(18.dp),
        )
    }
}

// ── 카드 래퍼 ─────────────────────────────────────────────────

@Composable
private fun PageCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0x14000000),
                spotColor = Color(0x20000000),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF8F8F8))
                )
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content,
        )
    }
}

// ── Preview ────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "My Page Screen")
@Composable
fun MyPageScreenPreview() {
    MyPageScreen()
}