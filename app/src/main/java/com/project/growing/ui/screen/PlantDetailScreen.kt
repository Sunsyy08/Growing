package com.project.growing.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.project.growing.network.RetrofitClient
import com.project.growing.ui.theme.*
import com.project.growing.viewmodel.PlantViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// ── 샘플 데이터 ───────────────────────────────────────────────

data class PlantDetailUiModel(
    val name         : String,
    val location     : String,
    val size         : String,
    val healthScore  : Int,
    val aiAnalysis   : String,
    val survivalRate : Int,
    val lastWatered  : String,
    val nextWater    : String,
    val sunlight     : String,
    val temperature  : String,
    val humidity     : String,
)

val samplePlantDetail = PlantDetailUiModel(
    name         = "몬스테라",
    location     = "거실 창가",
    size         = "중형 (25cm)",
    healthScore  = 95,
    aiAnalysis   = "현재 식물은 매우 건강한 상태입니다.",
    survivalRate = 98,
    lastWatered  = "2일 전",
    nextWater    = "2일 후",
    sunlight     = "적절함",
    temperature  = "23°C",
    humidity     = "65%",
)

// ── PlantDetailScreen ─────────────────────────────────────────

@Composable
fun PlantDetailScreen(
    plantId        : Int            = 0,              // ← 추가
    plantViewModel : PlantViewModel = viewModel(),    // ← 추가
    onBack         : () -> Unit     = {},
    onAiAnalysis   : () -> Unit     = {},
    onAskExpert    : () -> Unit     = {},
) {
    GrowingTheme {
        val context = LocalContext.current
        val detailState by plantViewModel.detailState.collectAsStateWithLifecycle()

        // ── 화면 진입 시 데이터 로드 ──────────────────────
        LaunchedEffect(plantId) {
            if (plantId > 0) plantViewModel.loadPlantDetail(plantId)
        }

        // ── 실제 데이터로 UiModel 구성 ────────────────────
        val plant = detailState.detail?.let { d ->
            PlantDetailUiModel(
                name         = d.plant_kind      ?: "내 식물",
                location     = d.plant_location  ?: "-",
                size         = d.pot_size        ?: "-",
                healthScore  = d.score?.toInt()  ?: 0,
                aiAnalysis   = d.analysis_ai     ?: "AI 분석 중입니다.",
                survivalRate = d.score?.toInt()  ?: 0,
                lastWatered  = d.water_cycle     ?: "-",
                nextWater    = d.water_cycle     ?: "-",
                sunlight     = d.sunlight        ?: "-",
                temperature  = "-",
                humidity     = "-",
            )
        } ?: samplePlantDetail

        // ── 이미지 URL ────────────────────────────────────
        val imageUrl = if (plantId > 0)
            "${RetrofitClient.BASE_URL}get_plant_image?plant_id=$plantId"
        else null

        // ── 카메라 관련 ───────────────────────────────────
        var capturedUri by remember { mutableStateOf<Uri?>(null) }
        var tempUri     by remember { mutableStateOf<Uri?>(null) }

        fun createTempImageUri(): Uri {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFile = File(context.cacheDir, "plant_update_$timestamp.jpg")
            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
        }

        val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && tempUri != null) capturedUri = tempUri
        }
        val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) { val uri = createTempImageUri(); tempUri = uri; cameraLauncher.launch(uri) }
        }
        fun launchCamera() {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val uri = createTempImageUri(); tempUri = uri; cameraLauncher.launch(uri)
            } else permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        // ── 로딩 중 ───────────────────────────────────────
        if (detailState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF43A967))
            }
            return@GrowingTheme
        }

        // ── 애니메이션 ────────────────────────────────────
        var entered by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { entered = true }

        val ringProgress by animateFloatAsState(
            targetValue   = if (entered) plant.healthScore / 100f else 0f,
            animationSpec = tween(1400, easing = EaseOutCubic),
            label         = "ring_progress",
        )
        val displayScore by animateIntAsState(
            targetValue   = if (entered) plant.healthScore else 0,
            animationSpec = tween(1400, easing = EaseOutCubic),
            label         = "score_count",
        )
        val waterCardAlpha by animateFloatAsState(
            targetValue   = if (entered) 1f else 0f,
            animationSpec = tween(600, delayMillis = 300, easing = EaseOutCubic),
            label         = "water_alpha",
        )
        val waterCardSlide by animateFloatAsState(
            targetValue   = if (entered) 0f else 40f,
            animationSpec = tween(600, delayMillis = 300, easing = EaseOutCubic),
            label         = "water_slide",
        )
        val sunCardAlpha by animateFloatAsState(
            targetValue   = if (entered) 1f else 0f,
            animationSpec = tween(600, delayMillis = 480, easing = EaseOutCubic),
            label         = "sun_alpha",
        )
        val sunCardSlide by animateFloatAsState(
            targetValue   = if (entered) 0f else 40f,
            animationSpec = tween(600, delayMillis = 480, easing = EaseOutCubic),
            label         = "sun_slide",
        )
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val waterIconScale by infiniteTransition.animateFloat(
            initialValue  = 1f, targetValue = 1.18f,
            animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label         = "water_icon_scale",
        )
        val sunIconRotate by infiniteTransition.animateFloat(
            initialValue  = -8f, targetValue = 8f,
            animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label         = "sun_icon_rotate",
        )

        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7F5))) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                // ── 상단 이미지 영역 ───────────────────────
                Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(listOf(Color(0xFF2E5C38), Color(0xFF4A7C57), Color(0xFF5D9467)))
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        // ── 실제 이미지 ──────────────────────
                        if (imageUrl != null) {
                            AsyncImage(
                                model              = imageUrl,
                                contentDescription = plant.name,
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier.fillMaxSize(),
                            )
                        } else {
                            Text(text = "🌿", fontSize = 80.sp)
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp).align(Alignment.BottomCenter)
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFFF5F7F5))))
                    )

                    Box(
                        modifier = Modifier.padding(top = 48.dp, start = 16.dp).size(36.dp)
                            .shadow(6.dp, CircleShape).clip(CircleShape).background(White),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Rounded.ArrowBackIosNew, "뒤로가기", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // ── 식물 정보 카드 ─────────────────────────
                Box(
                    modifier = Modifier.padding(horizontal = 20.dp).offset(y = (-16).dp)
                        .shadow(10.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x1443A967), spotColor = Color(0x1443A967))
                        .clip(RoundedCornerShape(20.dp)).background(White).fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                        Text(plant.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.3).sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📍", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(plant.location, fontSize = 13.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌱", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(plant.size, fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                // ── 건강 점수 카드 ─────────────────────────
                DetailCard(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("건강 점수", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(20.dp))
                        Box(modifier = Modifier.size(140.dp).align(Alignment.CenterHorizontally), contentAlignment = Alignment.Center) {
                            CircularHealthIndicator(ringProgress, 140.dp, 12.dp, Color(0xFF43A967), Color(0xFFE0EDE5))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$displayScore", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43A967), letterSpacing = (-1).sp)
                                Text("점", fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0FAF4)).padding(14.dp)) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🌿", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("AI 상태 분석", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(plant.aiAnalysis, fontSize = 13.sp, color = TextSecondary, lineHeight = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("생존 확률", fontSize = 14.sp, color = TextSecondary)
                            Text("${plant.survivalRate}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43A967))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── 물주기 / 햇빛 카드 ────────────────────
                Row(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.weight(1f)
                            .graphicsLayer { alpha = waterCardAlpha; translationY = waterCardSlide }
                            .shadow(8.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x2064B5F6), spotColor = Color(0x2064B5F6))
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFF4FC3F7), Color(0xFF0288D1))))
                            .padding(16.dp)
                    ) {
                        Column {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0x33FFFFFF)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.WaterDrop, null, tint = White, modifier = Modifier.size(18.dp).scale(waterIconScale))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("마지막 물주기", fontSize = 11.sp, color = White.copy(alpha = 0.85f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(plant.lastWatered, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = White, letterSpacing = (-0.3).sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("다음: ${plant.nextWater}", fontSize = 11.sp, color = White.copy(alpha = 0.85f))
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1f)
                            .graphicsLayer { alpha = sunCardAlpha; translationY = sunCardSlide }
                            .shadow(8.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x20FFB300), spotColor = Color(0x20FFB300))
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFFFFCA28), Color(0xFFFB8C00))))
                            .padding(16.dp)
                    ) {
                        Column {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0x33FFFFFF)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.WbSunny, null, tint = White, modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = sunIconRotate })
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("햇빛", fontSize = 11.sp, color = White.copy(alpha = 0.85f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(plant.sunlight, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = White, letterSpacing = (-0.3).sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("현재 상태", fontSize = 11.sp, color = White.copy(alpha = 0.85f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── 환경 정보 카드 ─────────────────────────
                DetailCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("환경 정보", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(14.dp))
                        EnvInfoRow("🌡", "온도", plant.temperature)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0), thickness = 0.8.dp)
                        EnvInfoRow("💧", "습도", plant.humidity)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onAiAnalysis,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A967)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                ) {
                    Text("🌿", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI 분석 보기", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onAskExpert,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD)),
                ) {
                    Icon(Icons.Rounded.ChatBubbleOutline, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("전문가에게 질문하기", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { launchCamera() },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(52.dp)
                        .shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = Color(0xFF1565C0).copy(alpha = 0.3f), spotColor = Color(0xFF1565C0).copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                ) {
                    Icon(Icons.Rounded.CameraAlt, null, tint = White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("식물 업데이트", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = White)
                }

                if (capturedUri != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(12.dp)).background(Color(0xFFE3F2FD)).padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF1976D2), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("사진이 저장되었어요! 서버 연결 후 업로드됩니다.", fontSize = 12.sp, color = Color(0xFF1565C0))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ── 공용 컴포저블 ─────────────────────────────────────────────

@Composable
fun DetailCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = modifier.fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x0F000000), spotColor = Color(0x0F000000))
            .clip(RoundedCornerShape(20.dp)).background(White)
    ) { Column(content = content) }
}

@Composable
fun EnvInfoRow(emoji: String, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 14.sp, color = TextSecondary)
        }
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
fun CircularHealthIndicator(
    progress   : Float,
    size       : Dp,
    stroke     : Dp,
    color      : Color,
    trackColor : Color,
) {
    Canvas(modifier = Modifier.size(size)) {
        val strokePx = stroke.toPx()
        val padding  = strokePx / 2f
        val diameter = this.size.minDimension - strokePx

        drawArc(
            color      = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter  = false,
            style      = Stroke(width = strokePx, cap = StrokeCap.Round),
            topLeft    = androidx.compose.ui.geometry.Offset(padding, padding),
            size       = androidx.compose.ui.geometry.Size(diameter, diameter),
        )
        drawArc(
            color      = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter  = false,
            style      = Stroke(width = strokePx, cap = StrokeCap.Round),
            topLeft    = androidx.compose.ui.geometry.Offset(padding, padding),
            size       = androidx.compose.ui.geometry.Size(diameter, diameter),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Plant Detail")
@Composable
fun PlantDetailScreenPreview() {
    PlantDetailScreen()
}