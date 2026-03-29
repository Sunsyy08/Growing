package com.project.growing.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.project.growing.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// ── 전문가 목록 ───────────────────────────────────────────────

private data class ExpertOption(
    val name      : String,
    val specialty : String,
    val avgTime   : String,
    val rating    : Float,
    val emoji     : String,
)

private val expertOptions = listOf(
    ExpertOption("김정원", "관엽식물 전문", "평균 2시간", 4.9f, "🌿"),
    ExpertOption("이현수", "다육식물 전문", "평균 3시간", 4.8f, "🌵"),
    ExpertOption("박민지", "병충해 전문",   "평균 1시간", 4.9f, "🍃"),
)

// ── WriteQuestionScreen ───────────────────────────────────────

@Composable
fun WriteQuestionScreen(
    onBack   : () -> Unit = {},
    onSubmit : () -> Unit = {},
) {
    GrowingTheme {
        val context = LocalContext.current

        // ── 카메라 관련 ────────────────────────────────────────
        var capturedUri by remember { mutableStateOf<Uri?>(null) }
        var tempUri     by remember { mutableStateOf<Uri?>(null) }

        fun createTempUri(): Uri {
            val ts   = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(context.cacheDir, "question_$ts.jpg")
            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }

        val cameraLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success -> if (success && tempUri != null) capturedUri = tempUri }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                val uri = createTempUri(); tempUri = uri; cameraLauncher.launch(uri)
            }
        }

        fun launchCamera() {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val uri = createTempUri(); tempUri = uri; cameraLauncher.launch(uri)
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        // ── 폼 상태 ───────────────────────────────────────────
        var descriptionText by remember { mutableStateOf("") }
        var selectedExpert  by remember { mutableStateOf<String?>(null) }
        val GreenPrimary    = Color(0xFF43A967)

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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F5))
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
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)
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

                // ── 식물 사진 카드 ─────────────────────────────
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    WqSectionCard(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .graphicsLayer { alpha = contentAlpha; translationY = contentSlide }
                    ) {
                        Text("식물 사진", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        // 사진 찍혔으면 미리보기
                        if (capturedUri != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model              = capturedUri,
                                    contentDescription = "촬영된 사진",
                                    contentScale       = ContentScale.Crop,
                                    modifier           = Modifier.fillMaxSize(),
                                )
                                // 재촬영 버튼
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .clickable(
                                            indication        = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                        ) { launchCamera() }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector        = Icons.Rounded.CameraAlt,
                                        contentDescription = "재촬영",
                                        tint               = Color.White,
                                        modifier           = Modifier.size(18.dp),
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.5.dp, Color(0xFFDDDDDD), RoundedCornerShape(12.dp))
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
                                    Text("사진 업로드", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
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
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick  = { launchCamera() },
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
                                text       = if (capturedUri != null) "재촬영" else "사진 선택",
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                // ── 상세 설명 카드 ─────────────────────────────
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    WqSectionCard(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .graphicsLayer { alpha = contentAlpha; translationY = contentSlide }
                    ) {
                        Text("상세 설명", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value         = descriptionText,
                            onValueChange = { descriptionText = it },
                            modifier      = Modifier.fillMaxWidth().height(160.dp),
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
                                fontSize = 13.sp, lineHeight = 19.sp, color = TextPrimary,
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

                // ── 전문가 선택 카드 ───────────────────────────
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    WqSectionCard(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .graphicsLayer { alpha = contentAlpha; translationY = contentSlide }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text       = "전문가 선택",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFF0F0F0))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                            ) {
                                Text(text = "선택사항", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "특정 전문가를 지정할 수 있어요", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        // 자동 배정 옵션
                        AutoAssignRow(
                            isSelected   = selectedExpert == null,
                            greenPrimary = GreenPrimary,
                            onClick      = { selectedExpert = null },
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // 전문가 목록
                        expertOptions.forEachIndexed { idx, expert ->
                            ExpertOptionCard(
                                expert       = expert,
                                isSelected   = selectedExpert == expert.name,
                                greenPrimary = GreenPrimary,
                                onClick      = { selectedExpert = expert.name },
                            )
                            if (idx < expertOptions.lastIndex) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // ── 하단 고정 버튼 ────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .graphicsLayer { alpha = contentAlpha; translationY = contentSlide }
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFF5F7F5).copy(alpha = 0f), Color(0xFFF5F7F5))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Button(
                    onClick  = onSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(10.dp, RoundedCornerShape(16.dp),
                            ambientColor = GreenPrimary.copy(alpha = 0.35f),
                            spotColor    = GreenPrimary.copy(alpha = 0.35f)),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape  = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Rounded.Send, null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "질문 등록하기", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── 자동 배정 행 ──────────────────────────────────────────────

@Composable
private fun AutoAssignRow(
    isSelected   : Boolean,
    greenPrimary : Color,
    onClick      : () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) greenPrimary.copy(alpha = 0.08f) else Color(0xFFF8F8F8),
        animationSpec = tween(200),
        label         = "auto_bg",
    )
    val borderColor by animateColorAsState(
        targetValue   = if (isSelected) greenPrimary else Color(0xFFE8E8E8),
        animationSpec = tween(200),
        label         = "auto_border",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = onClick,
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 자동 배정 아이콘
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isSelected) greenPrimary.copy(alpha = 0.15f) else Color(0xFFEEEEEE)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint               = if (isSelected) greenPrimary else TextSecondary,
                modifier           = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = "자동 배정",
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = if (isSelected) greenPrimary else TextPrimary,
            )
            Text(
                text     = "가장 적합한 전문가가 답변해요",
                fontSize = 12.sp,
                color    = TextSecondary,
            )
        }

        // 선택 표시
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (isSelected) greenPrimary else Color(0xFFDDDDDD),
                    shape = CircleShape,
                )
                .background(if (isSelected) greenPrimary else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Icon(
                    imageVector        = Icons.Rounded.Check,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(13.dp),
                )
            }
        }
    }
}

// ── 전문가 옵션 카드 ──────────────────────────────────────────

@Composable
private fun ExpertOptionCard(
    expert       : ExpertOption,
    isSelected   : Boolean,
    greenPrimary : Color,
    onClick      : () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) greenPrimary.copy(alpha = 0.06f) else Color.White,
        animationSpec = tween(200),
        label         = "expert_bg_${expert.name}",
    )
    val borderColor by animateColorAsState(
        targetValue   = if (isSelected) greenPrimary else Color(0xFFEEEEEE),
        animationSpec = tween(200),
        label         = "expert_border_${expert.name}",
    )
    val elevationDp = if (isSelected) 0.dp else 2.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevationDp, RoundedCornerShape(14.dp),
                ambientColor = Color(0x0E000000), spotColor = Color(0x0E000000))
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = onClick,
            )
            .padding(14.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 프로필 이모지
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) greenPrimary.copy(alpha = 0.12f) else Color(0xFFF0F0F0)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = expert.emoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = expert.name,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (isSelected) greenPrimary else TextPrimary,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // 전문 분야 태그
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isSelected) greenPrimary.copy(alpha = 0.12f)
                                else Color(0xFFF0F0F0)
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text     = expert.specialty,
                            fontSize = 10.sp,
                            color    = if (isSelected) greenPrimary else TextSecondary,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // 별점
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Rounded.Star,
                            contentDescription = null,
                            tint               = Color(0xFFFFB300),
                            modifier           = Modifier.size(12.dp),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text       = "${expert.rating}",
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextPrimary,
                        )
                    }
                    Text(text = "·", fontSize = 10.sp, color = TextSecondary)
                    // 평균 응답
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Rounded.AccessTime,
                            contentDescription = null,
                            tint               = TextSecondary,
                            modifier           = Modifier.size(11.dp),
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text     = expert.avgTime,
                            fontSize = 11.sp,
                            color    = TextSecondary,
                        )
                    }
                }
            }

            // 라디오 버튼
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) greenPrimary else Color(0xFFDDDDDD),
                        shape = CircleShape,
                    )
                    .background(if (isSelected) greenPrimary else Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(
                        imageVector        = Icons.Rounded.Check,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(13.dp),
                    )
                }
            }
        }
    }
}

// ── 섹션 카드 ─────────────────────────────────────────────────

@Composable
private fun WqSectionCard(
    modifier : Modifier = Modifier,
    content  : @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp),
                ambientColor = Color(0x0A000000), spotColor = Color(0x0A000000))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Write Question Screen")
@Composable
fun WriteQuestionScreenPreview() {
    WriteQuestionScreen()
}