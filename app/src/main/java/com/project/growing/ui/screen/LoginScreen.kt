package com.project.growing.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.*
import com.project.growing.R
import com.project.growing.ui.theme.*
import com.project.growing.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel          : AuthViewModel = viewModel(),
    onLoginSuccess     : () -> Unit    = {},
    onNavigateToSignUp : () -> Unit    = {},
) {
    val state by viewModel.loginState.collectAsStateWithLifecycle()

    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) onLoginSuccess()
    }

    // ── Lottie 설정 ───────────────────────────────────────────
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current

    val composition by if (isPreview) {
        remember { mutableStateOf(null) }
    } else {
        rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.login_animation)
        )
    }

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations  = LottieConstants.IterateForever,
    )

    GrowingTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFD4EDDE),
                            Color(0xFFEAF6EE),
                            Color(0xFFF5FCF7),
                        )
                    )
                )
        ) {
            // ── 배경 블러 장식 ──────────────────────────────
            Box(
                modifier = Modifier
                    .size(380.dp)
                    .offset(x = (-100).dp, y = (-100).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF34C77B).copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                    .blur(40.dp)
            )
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 80.dp, y = 60.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF30D158).copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                    .blur(50.dp)
            )
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-60).dp, y = 60.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1A7A4A).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                    .blur(40.dp)
            )

            // ── 본문 ───────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                // ── 기존 아이콘 박스 → Lottie 애니메이션 ────────
                if (!isPreview) {
                    LottieAnimation(
                        composition = composition,
                        progress    = { progress },
                        modifier    = Modifier.size(160.dp),
                    )
                } else {
                    Spacer(modifier = Modifier.size(160.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text       = "안녕하세요 👋",
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary,
                    letterSpacing = (-0.5).sp,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text     = "식물들이 기다리고 있어요",
                    fontSize = 15.sp,
                    color    = TextSecondary,
                )

                Spacer(modifier = Modifier.height(36.dp))

                // ── 유리 카드 ──────────────────────────────
                GlassCard {
                    Column(modifier = Modifier.padding(24.dp)) {

                        GlassTextField(
                            value         = state.email,
                            onValueChange = viewModel::onLoginEmailChange,
                            label         = "이메일",
                            placeholder   = "hello@example.com",
                            icon          = Icons.Rounded.Email,
                            isError       = state.emailError != null,
                            errorMessage  = state.emailError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction    = ImeAction.Next,
                            ),
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        GlassTextField(
                            value         = state.password,
                            onValueChange = viewModel::onLoginPasswordChange,
                            label         = "비밀번호",
                            placeholder   = "비밀번호 입력",
                            icon          = Icons.Rounded.Lock,
                            isError       = state.passwordError != null,
                            errorMessage  = state.passwordError,
                            visualTransformation = if (state.isPasswordVisible)
                                VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon  = {
                                IconButton(
                                    onClick  = viewModel::onLoginPasswordVisibilityToggle,
                                    modifier = Modifier.size(34.dp),
                                ) {
                                    Icon(
                                        imageVector = if (state.isPasswordVisible)
                                            Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint     = TextSecondary,
                                        modifier = Modifier.size(17.dp),
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction    = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { viewModel.onLoginSubmit() }
                            ),
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        AppleButton(
                            text      = "로그인",
                            onClick   = viewModel::onLoginSubmit,
                            isLoading = state.isLoading,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text     = "처음이신가요?",
                        fontSize = 14.sp,
                        color    = TextSecondary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(
                        onClick        = onNavigateToSignUp,
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(
                            text       = "계정 만들기 →",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = GreenPrimary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

// ── 공용 컴포넌트 ─────────────────────────────────────────────

@Composable
fun GlassCard(
    modifier : Modifier      = Modifier,
    content  : @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation    = 24.dp,
                shape        = RoundedCornerShape(28.dp),
                ambientColor = Color(0xFF1A7A4A).copy(alpha = 0.08f),
                spotColor    = Color(0xFF1A7A4A).copy(alpha = 0.12f),
            )
            .clip(RoundedCornerShape(28.dp))
            .background(GlassWhite)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xBBFFFFFF),
                        Color(0x44FFFFFF),
                    )
                ),
                shape = RoundedCornerShape(28.dp),
            )
    ) {
        content()
    }
}

@Composable
fun GlassTextField(
    value                : String,
    onValueChange        : (String) -> Unit,
    label                : String,
    placeholder          : String                    = "",
    icon                 : ImageVector,
    trailingIcon         : @Composable (() -> Unit)? = null,
    isError              : Boolean                   = false,
    errorMessage         : String?                   = null,
    visualTransformation : VisualTransformation      = VisualTransformation.None,
    keyboardOptions      : KeyboardOptions           = KeyboardOptions.Default,
    keyboardActions      : KeyboardActions           = KeyboardActions.Default,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            isError   -> ErrorColor.copy(alpha = 0.7f)
            isFocused -> GreenMid.copy(alpha = 0.8f)
            else      -> Color(0x22000000)
        },
        animationSpec = tween(250),
        label = "border",
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isError   -> Color(0xFFFFF4F5)
            isFocused -> Color(0xFFF2FBF6)
            else      -> Color(0xFFF7F9F8)
        },
        animationSpec = tween(250),
        label = "bg",
    )

    Column {
        Text(
            text          = label,
            fontSize      = 12.sp,
            fontWeight    = FontWeight.SemiBold,
            color         = if (isError) ErrorColor
            else if (isFocused) GreenPrimary
            else TextSecondary,
            letterSpacing = 0.3.sp,
            modifier      = Modifier.padding(start = 2.dp, bottom = 6.dp),
        )

        BasicTextField(
            value                = value,
            onValueChange        = onValueChange,
            singleLine           = true,
            visualTransformation = visualTransformation,
            keyboardOptions      = keyboardOptions,
            keyboardActions      = keyboardActions,
            interactionSource    = interactionSource,
            textStyle = TextStyle(
                fontSize      = 15.sp,
                color         = TextPrimary,
                fontWeight    = FontWeight.Normal,
                letterSpacing = (-0.1).sp,
            ),
            cursorBrush = SolidColor(GreenMid),
            modifier    = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        tint               = if (isFocused) GreenMid else TextHint,
                        modifier           = Modifier.size(17.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text     = placeholder,
                                fontSize = 15.sp,
                                color    = TextHint,
                            )
                        }
                        innerTextField()
                    }
                    if (trailingIcon != null) trailingIcon()
                }
            }
        )

        if (isError && errorMessage != null) {
            Text(
                text     = "· $errorMessage",
                color    = ErrorColor,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 5.dp),
            )
        }
    }
}

@Composable
fun AppleButton(
    text      : String,
    onClick   : () -> Unit,
    isLoading : Boolean  = false,
    modifier  : Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
                elevation    = 12.dp,
                shape        = RoundedCornerShape(14.dp),
                ambientColor = GreenPrimary.copy(alpha = 0.3f),
                spotColor    = GreenPrimary.copy(alpha = 0.4f),
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF34C77B),
                        Color(0xFF1A7A4A),
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x22FFFFFF),
                            Color(0x00FFFFFF),
                        )
                    )
                )
        )
        Button(
            onClick   = { if (!isLoading) onClick() },
            modifier  = Modifier.fillMaxSize(),
            shape     = RoundedCornerShape(14.dp),
            colors    = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            elevation = null,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color       = White,
                    strokeWidth = 2.dp,
                    modifier    = Modifier.size(20.dp),
                )
            } else {
                Text(
                    text          = text,
                    fontSize      = 16.sp,
                    fontWeight    = FontWeight.SemiBold,
                    color         = White,
                    letterSpacing = 0.2.sp,
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Login Screen")
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}