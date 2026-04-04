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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.project.growing.ui.theme.*
import com.project.growing.viewmodel.ConsultViewModel
import com.project.growing.viewmodel.ExpertType
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WriteQuestionScreen(
    consultViewModel : ConsultViewModel = viewModel(),
    onBack           : () -> Unit       = {},
    onSubmit         : () -> Unit       = {},
) {
    GrowingTheme {
        val context    = LocalContext.current
        val writeState by consultViewModel.writeState.collectAsStateWithLifecycle()

        // ── 제출 성공 시 화면 이동 ────────────────────────────
        LaunchedEffect(writeState.isSuccess) {
            if (writeState.isSuccess) {
                consultViewModel.resetWriteState()
                onSubmit()
            }
        }

        // ── 카메라 관련 ───────────────────────────────────────
        var capturedUri by remember { mutableStateOf<Uri?>(null) }
        var tempUri     by remember { mutableStateOf<Uri?>(null) }

        fun createTempUri(): Uri {
            val ts   = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(context.cacheDir, "consult_$ts.jpg")
            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }

        val cameraLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && tempUri != null) {
                capturedUri = tempUri
                // ── 이미지 URI 로컬 저장 ───────────────────
                consultViewModel.onImageCaptured(tempUri.toString())
            }
        }

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
                            .statusBarsPadding()
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(36.dp).clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .clickable(
                                        indication        = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick           = onBack,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Rounded.ArrowBackIosNew, "뒤로가기", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("질문 작성하기", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = (-0.3).sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("AI 상담사가 답변해드려요 🌱", fontSize = 13.sp, color = Color.White.copy(alpha = 0.88f))
                        }
                    }
                }

                // ── 식물 사진 카드 ─────────────────────────────
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    WqSectionCard(
                        modifier = Modifier.padding(horizontal = 16.dp)
                            .graphicsLayer { alpha = contentAlpha; translationY = contentSlide }
                    ) {
                        Text("식물 사진", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("사진은 상담 목록에 표시됩니다", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (capturedUri != null) {
                            Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))) {
                                AsyncImage(
                                    model              = capturedUri,
                                    contentDescription = "식물 사진",
                                    contentScale       = ContentScale.Crop,
                                    modifier           = Modifier.fillMaxSize(),
                                )
                                Box(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                                        .clip(CircleShape).background(Color.Black.copy(alpha = 0.5f))
                                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { launchCamera() }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Rounded.CameraAlt, "재촬영", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp))
                                    .border(1.5.dp, Color(0xFFDDDDDD), RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF9F9F9)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF43A967).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(Icons.Rounded.Upload, null, tint = Color(0xFF43A967), modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("선택사항", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("식물 사진을 첨부할 수 있어요", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick  = { launchCamera() },
                            colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A967)),
                            shape    = RoundedCornerShape(50),
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        ) {
                            Icon(Icons.Rounded.CameraAlt, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (capturedUri != null) "재촬영" else "사진 촬영", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // ── 질문 내용 카드 ─────────────────────────────
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    WqSectionCard(
                        modifier = Modifier.padding(horizontal = 16.dp)
                            .graphicsLayer { alpha = contentAlpha; translationY = contentSlide }
                    ) {
                        Text("질문 내용", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value         = writeState.message,
                            onValueChange = { consultViewModel.onMessageChange(it) },
                            modifier      = Modifier.fillMaxWidth().height(160.dp),
                            placeholder   = {
                                Text(
                                    text       = "식물의 상태, 관리 방법, 환경 등을\n자세히 적어주세요.",
                                    fontSize   = 13.sp,
                                    color      = TextSecondary.copy(alpha = 0.6f),
                                    lineHeight = 19.sp,
                                )
                            },
                            shape  = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = Color(0xFF43A967),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                cursorColor          = Color(0xFF43A967),
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, lineHeight = 19.sp, color = TextPrimary),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${writeState.message.length}자", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.align(Alignment.End))
                    }
                }

                // ── AI 상담사 선택 카드 ────────────────────────
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    WqSectionCard(
                        modifier = Modifier.padding(horizontal = 16.dp)
                            .graphicsLayer { alpha = contentAlpha; translationY = contentSlide }
                    ) {
                        Text("AI 상담사 선택", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("상담사마다 다른 스타일로 답변해드려요", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        ExpertType.entries.forEach { expert ->
                            ExpertCard(
                                expert       = expert,
                                isSelected   = writeState.selectedExpert == expert,
                                onClick      = { consultViewModel.onExpertSelected(expert) },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // ── 하단 버튼 ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .graphicsLayer { alpha = contentAlpha }
                    .background(Brush.verticalGradient(listOf(Color(0xFFF5F7F5).copy(alpha = 0f), Color(0xFFF5F7F5))))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Button(
                    onClick  = { consultViewModel.submitQuestion() },
                    enabled  = !writeState.isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                        .shadow(10.dp, RoundedCornerShape(16.dp),
                            ambientColor = Color(0xFF43A967).copy(alpha = 0.35f),
                            spotColor    = Color(0xFF43A967).copy(alpha = 0.35f)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A967)),
                    shape  = RoundedCornerShape(16.dp),
                ) {
                    if (writeState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("답변 생성 중...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Rounded.Send, null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("질문 등록하기", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // ── 에러 메시지 ───────────────────────────────
                if (writeState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFEBEE)).padding(12.dp)
                    ) {
                        Text(writeState.errorMessage!!, fontSize = 13.sp, color = Color(0xFFE53935))
                    }
                }
            }
        }
    }
}

// ── AI 상담사 카드 ─────────────────────────────────────────────

@Composable
private fun ExpertCard(
    expert     : ExpertType,
    isSelected : Boolean,
    onClick    : () -> Unit,
) {
    val GreenPrimary = Color(0xFF43A967)
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) GreenPrimary.copy(alpha = 0.06f) else Color.White,
        animationSpec = tween(200), label = "expert_bg",
    )
    val borderColor by animateColorAsState(
        targetValue   = if (isSelected) GreenPrimary else Color(0xFFEEEEEE),
        animationSpec = tween(200), label = "expert_border",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isSelected) 0.dp else 2.dp, RoundedCornerShape(14.dp),
                ambientColor = Color(0x0E000000), spotColor = Color(0x0E000000))
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick)
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // 이모지 프로필
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(if (isSelected) GreenPrimary.copy(alpha = 0.12f) else Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center,
            ) {
                Text(expert.emoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = expert.displayName,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (isSelected) GreenPrimary else TextPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text     = when (expert) {
                        ExpertType.TSUNDERE  -> "팩트 위주, 츤데레 말투"
                        ExpertType.FAIRY     -> "귀엽고 상냥한 말투"
                        ExpertType.SCIENTIST -> "과학적, 친근한 말투"
                    },
                    fontSize = 12.sp,
                    color    = TextSecondary,
                )
            }

            // 라디오 버튼
            Box(
                modifier = Modifier.size(22.dp).clip(CircleShape)
                    .border(2.dp, if (isSelected) GreenPrimary else Color(0xFFDDDDDD), CircleShape)
                    .background(if (isSelected) GreenPrimary else Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(13.dp))
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
        modifier = modifier.fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x0A000000), spotColor = Color(0x0A000000))
            .clip(RoundedCornerShape(16.dp)).background(Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WriteQuestionScreenPreview() {
    WriteQuestionScreen()
}