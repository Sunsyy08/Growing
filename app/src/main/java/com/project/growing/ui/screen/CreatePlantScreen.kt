package com.project.growing.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import com.project.growing.viewmodel.PlantViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// ── 데이터 ────────────────────────────────────────────────────

private val plantTypes = listOf(
    "몬스테라", "다육이", "떡갈고무\n나무",
    "스투키", "산세베리\n아", "알로카시\n아",
    "필로덴드\n론", "스킨답서\n스", "기타",
)

private val locations = listOf(
    "거실", "침실", "주방",
    "화장실", "베란다", "사무실",
    "기타",
)

private data class PotSize(val label: String, val sub: String)
private val potSizes = listOf(
    PotSize("소형", "15cm 이하"),
    PotSize("중형", "15-25cm"),
    PotSize("대형", "25cm 이상"),
)

private data class CareTip(val label: String, val value: String)
private val sampleCareTips = listOf(
    CareTip("물 주기", "일주일에 1-2회"),
    CareTip("햇빛", "간접광 선호"),
    CareTip("온도", "18-24°C 유지"),
)

// ── 물 주기 카드 ───────────────────────────────────────────────
private val waterCycleOptions = listOf(
    "1일 전", "2일 전", "3일 전",
    "4일 전", "5일 전", "6일 전",
    "1주일 전", "2주일 전", "한달 전",
)

// ── AddPlantScreen ─────────────────────────────────────────────

@Composable
fun AddPlantScreen(
    onBack   : () -> Unit = {},
    onSubmit : () -> Unit = {},
) {
    GrowingTheme {
        val context = LocalContext.current

        // ── 카메라 관련 상태 ───────────────────────────────────
        var capturedUri  by remember { mutableStateOf<Uri?>(null) }
        var tempUri      by remember { mutableStateOf<Uri?>(null) }

        val plantViewModel : PlantViewModel = viewModel()
        val uiState by plantViewModel.uiState.collectAsStateWithLifecycle()

        // 등록 성공 시 화면 이동
        LaunchedEffect(uiState.isSuccess) {
            if (uiState.isSuccess) {
                plantViewModel.resetState()
                onSubmit()
            }
        }

        // 기존 상태 변수들 아래에 추가
        var selectedWaterCycle by remember { mutableStateOf<String?>(null) }

        fun createTempImageUri(): Uri {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file      = File(context.cacheDir, "plant_$timestamp.jpg")
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
        }

        val cameraLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && tempUri != null) capturedUri = tempUri
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                val uri = createTempImageUri()
                tempUri = uri
                cameraLauncher.launch(uri)
            }
        }

        fun launchCamera() {
            val permission = Manifest.permission.CAMERA
            if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val uri = createTempImageUri()
                tempUri = uri
                cameraLauncher.launch(uri)
            } else {
                permissionLauncher.launch(permission)
            }
        }

        // ── 폼 상태 ───────────────────────────────────────────
        var plantName    by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf<String?>(null) }
        var selectedLoc  by remember { mutableStateOf<String?>(null) }
        var selectedSize by remember { mutableStateOf<String?>(null) }

        val GreenPrimary = Color(0xFF43A967)
        val isFormValid  = plantName.isNotBlank()
                && selectedType != null
                && selectedLoc  != null
                && selectedSize != null
                && selectedWaterCycle != null

        // ── 스크롤 상태 ───────────────────────────────────────
        val listState = rememberLazyListState()

        val headerAlpha by remember {
            derivedStateOf {
                when {
                    listState.firstVisibleItemIndex > 0 -> 0f
                    else -> (1f - listState.firstVisibleItemScrollOffset / 300f).coerceIn(0f, 1f)
                }
            }
        }
        val headerTranslationY by remember {
            derivedStateOf {
                if (listState.firstVisibleItemIndex > 0) -40f
                else -(1f - headerAlpha) * 40f
            }
        }

        // ── 진입 애니메이션 ────────────────────────────────────
        var entered by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { entered = true }

        val contentAlpha by animateFloatAsState(
            targetValue   = if (entered) 1f else 0f,
            animationSpec = tween(durationMillis = 400, delayMillis = 100, easing = EaseOutCubic),
            label         = "content_alpha",
        )
        val contentSlide by animateFloatAsState(
            targetValue   = if (entered) 0f else 30f,
            animationSpec = tween(durationMillis = 400, delayMillis = 100, easing = EaseOutCubic),
            label         = "content_slide",
        )

        LazyColumn(
            state          = listState,
            modifier       = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F5)),
            contentPadding = PaddingValues(bottom = 24.dp),
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
                                )
                            )
                        )
                        // 상단 상태바까지 색상 채우기
                        .statusBarsPadding()
                        .padding(
                            start  = 16.dp,
                            end    = 16.dp,
                            top    = 8.dp,     // statusBarsPadding이 자동으로 상단 처리
                            bottom = 24.dp,
                        )
                ) {
                    Column {
                        // 뒤로가기
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
                            text          = "식물 등록하기",
                            fontSize      = 22.sp,
                            fontWeight    = FontWeight.Bold,
                            color         = Color.White,
                            letterSpacing = (-0.3).sp,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text     = "새로운 식물을 추가해보세요 🌱",
                            fontSize = 13.sp,
                            color    = Color.White.copy(alpha = 0.88f),
                        )
                    }
                }
            }

            // ── 이하 콘텐츠 전체에 진입 애니메이션 적용 ────────

            // ── 식물 사진 카드 ─────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ElevatedSectionCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha        = contentAlpha
                            translationY = contentSlide
                        }
                ) {
                    Text(
                        text       = "식물 사진",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 사진 찍혔으면 미리보기, 아니면 업로드 UI
                    if (capturedUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model             = capturedUri,
                                contentDescription = "촬영된 식물 사진",
                                contentScale      = ContentScale.Crop,
                                modifier          = Modifier.fillMaxSize(),
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
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFFE0E0E0), Color(0xFFF5F5F5))
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                )
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFFFAFAFA), Color(0xFFF2F2F2))
                                    )
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(GreenPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector        = Icons.Rounded.Upload,
                                        contentDescription = null,
                                        tint               = GreenPrimary,
                                        modifier           = Modifier.size(24.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text       = "사진 촬영 또는 업로드",
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = TextPrimary,
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text      = "식물의 전체 모습이 잘 보이는 사진",
                                    fontSize  = 11.sp,
                                    color     = TextSecondary,
                                    textAlign = TextAlign.Center,
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
                            modifier           = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text       = if (capturedUri != null) "재촬영" else "사진 촬영",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // ── 식물 이름 카드 ─────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedSectionCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha        = contentAlpha
                            translationY = contentSlide
                        }
                ) {
                    Text(
                        text       = "식물 이름",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value         = plantName,
                        onValueChange = { plantName = it },
                        modifier      = Modifier.fillMaxWidth(),
                        placeholder   = {
                            Text(
                                text     = "우리 집 몬스테라",
                                fontSize = 14.sp,
                                color    = TextSecondary.copy(alpha = 0.5f),
                            )
                        },
                        singleLine = true,
                        shape      = RoundedCornerShape(10.dp),
                        colors     = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = GreenPrimary,
                            unfocusedBorderColor = Color(0xFFDDDDDD),
                            cursorColor          = GreenPrimary,
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            color    = TextPrimary,
                        ),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text     = "식물에게 애칭을 지어주세요",
                        fontSize = 11.sp,
                        color    = TextSecondary,
                    )
                }
            }

            // ── 식물 종류 카드 ─────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedSectionCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha        = contentAlpha
                            translationY = contentSlide
                        }
                ) {
                    Text(
                        text       = "식물 종류",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    val rows = plantTypes.chunked(3)
                    rows.forEachIndexed { rowIdx, rowItems ->
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowItems.forEach { type ->
                                SelectChip(
                                    label        = type,
                                    isSelected   = selectedType == type,
                                    greenPrimary = GreenPrimary,
                                    modifier     = Modifier.weight(1f),
                                    onClick      = { selectedType = type },
                                )
                            }
                            repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                        if (rowIdx < rows.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // ── 식물 위치 카드 ─────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedSectionCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha        = contentAlpha
                            translationY = contentSlide
                        }
                ) {
                    Text(
                        text       = "식물 위치",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    val rows = locations.chunked(3)
                    rows.forEachIndexed { rowIdx, rowItems ->
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowItems.forEach { loc ->
                                SelectChip(
                                    label        = loc,
                                    isSelected   = selectedLoc == loc,
                                    greenPrimary = GreenPrimary,
                                    modifier     = Modifier.weight(1f),
                                    onClick      = { selectedLoc = loc },
                                )
                            }
                            repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                        if (rowIdx < rows.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // ── 화분 크기 카드 ─────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedSectionCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha        = contentAlpha
                            translationY = contentSlide
                        }
                ) {
                    Text(
                        text       = "화분 크기",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    potSizes.forEachIndexed { idx, pot ->
                        val isSelected  = selectedSize == pot.label
                        val borderColor by animateColorAsState(
                            targetValue   = if (isSelected) GreenPrimary else Color(0xFFE2E2E2),
                            animationSpec = tween(200),
                            label         = "pot_border_${pot.label}",
                        )
                        val bgColor by animateColorAsState(
                            targetValue   = if (isSelected) GreenPrimary.copy(alpha = 0.06f) else Color(0xFFFCFCFC),
                            animationSpec = tween(200),
                            label         = "pot_bg_${pot.label}",
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(if (isSelected) 0.dp else 2.dp, RoundedCornerShape(10.dp),
                                    ambientColor = Color(0x18000000), spotColor = Color(0x18000000))
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                                .background(bgColor)
                                .clickable(
                                    indication        = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { selectedSize = pot.label }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                        ) {
                            Text(
                                text       = "${pot.label} (${pot.sub})",
                                fontSize   = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color      = if (isSelected) GreenPrimary else TextPrimary,
                            )
                        }
                        if (idx < potSizes.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // ── 물 주기 카드 ───────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedSectionCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha        = contentAlpha
                            translationY = contentSlide
                        }
                ) {
                    Text(
                        text       = "마지막 물 주기",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text     = "언제 마지막으로 물을 주셨나요?",
                        fontSize = 12.sp,
                        color    = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val rows = waterCycleOptions.chunked(3)
                    rows.forEachIndexed { rowIdx, rowItems ->
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowItems.forEach { cycle ->
                                SelectChip(
                                    label        = cycle,
                                    isSelected   = selectedWaterCycle == cycle,
                                    greenPrimary = GreenPrimary,
                                    modifier     = Modifier.weight(1f),
                                    onClick      = { selectedWaterCycle = cycle },
                                )
                            }
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        if (rowIdx < rows.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // ── AI 추천 관리법 카드 ────────────────────────────
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha        = contentAlpha
                            translationY = contentSlide
                        }
                        .shadow(6.dp, RoundedCornerShape(16.dp),
                            ambientColor = GreenPrimary.copy(alpha = 0.15f),
                            spotColor    = GreenPrimary.copy(alpha = 0.15f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFFEDF7F1), Color(0xFFF5FAF7))))
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(listOf(GreenPrimary.copy(alpha = 0.35f), Color(0xFFD0EDD8))),
                            shape = RoundedCornerShape(16.dp),
                        )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .shadow(4.dp, RoundedCornerShape(8.dp),
                                        ambientColor = GreenPrimary.copy(alpha = 0.4f),
                                        spotColor    = GreenPrimary.copy(alpha = 0.4f))
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GreenPrimary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Rounded.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(17.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI 추천 관리법", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text       = "선택하신 식물에 맞는 기본 관리법을\n알려드려요!",
                            fontSize   = 13.sp,
                            color      = TextSecondary,
                            lineHeight = 19.sp,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        sampleCareTips.forEach { tip ->
                            Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("•", fontSize = 13.sp, color = GreenPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${tip.label}: ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text(tip.value, fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // ── 등록 버튼 ────────────────────────────
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .graphicsLayer { alpha = contentAlpha; translationY = contentSlide },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(
                        onClick  = {
                            if (isFormValid) {
                                plantViewModel.registerPlant(
                                    plantKind     = selectedType       ?: "",
                                    plantLocation = selectedLoc        ?: "",
                                    potSize       = selectedSize       ?: "",
                                    waterCycle    = selectedWaterCycle ?: "",
                                    imageUri      = capturedUri,
                                )
                            }
                        },
                        enabled  = isFormValid && !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .then(
                                if (isFormValid) Modifier.shadow(
                                    elevation    = 10.dp,
                                    shape        = RoundedCornerShape(16.dp),
                                    ambientColor = GreenPrimary.copy(alpha = 0.4f),
                                    spotColor    = GreenPrimary.copy(alpha = 0.4f),
                                ) else Modifier
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor         = GreenPrimary,
                            disabledContainerColor = Color(0xFFCCCCCC),
                        ),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color       = Color.White,
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text       = "등록 중...",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White,
                            )
                        } else {
                            Text(text = "🌿", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text       = "식물 등록하기",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White,
                            )
                        }
                    }

                    if (!isFormValid) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text     = "모든 항목을 입력해주세요",
                            fontSize = 12.sp,
                            color    = TextSecondary,
                        )
                    }

                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFEBEE))
                                .padding(12.dp),
                        ) {
                            Text(
                                text     = uiState.errorMessage!!,
                                fontSize = 13.sp,
                                color    = Color(0xFFE53935),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ── 섹션 카드 ─────────────────────────────────────────────────

@Composable
private fun ElevatedSectionCard(
    modifier : Modifier = Modifier,
    content  : @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x14000000), spotColor = Color(0x20000000))
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF8F8F8))))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE0E0E0))),
                shape = RoundedCornerShape(16.dp),
            )
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

// ── 선택 칩 ───────────────────────────────────────────────────

@Composable
private fun SelectChip(
    label        : String,
    isSelected   : Boolean,
    greenPrimary : Color,
    modifier     : Modifier = Modifier,
    onClick      : () -> Unit,
) {
    val borderColor by animateColorAsState(if (isSelected) greenPrimary else Color(0xFFDDDDDD), tween(200), label = "cb_$label")
    val bgColor     by animateColorAsState(if (isSelected) greenPrimary.copy(alpha = 0.08f) else Color(0xFFFCFCFC), tween(200), label = "cbg_$label")
    val textColor   by animateColorAsState(if (isSelected) greenPrimary else TextSecondary, tween(200), label = "ct_$label")

    Box(
        modifier = modifier
            .shadow(if (isSelected) 0.dp else 2.dp, RoundedCornerShape(10.dp), ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text       = label,
            fontSize   = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color      = textColor,
            textAlign  = TextAlign.Center,
            lineHeight = 17.sp,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Add Plant Screen")
@Composable
fun AddPlantScreenPreview() {
    AddPlantScreen()
}