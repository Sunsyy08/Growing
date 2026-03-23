package com.project.growing.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
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
import com.project.growing.ui.theme.*
import com.project.growing.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    viewModel       : AuthViewModel = viewModel(),
    onSignUpSuccess : () -> Unit    = {},
    onNavigateBack  : () -> Unit    = {},
) {
    val state by viewModel.signUpState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.signUpSuccess) {
        if (state.signUpSuccess) onSignUpSuccess()
    }

    GrowingTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFCFEADA),
                            Color(0xFFE8F6EE),
                            Color(0xFFF5FCF7),
                        )
                    )
                )
        ) {
            // ── 배경 블러 장식 ──────────────────────────────
            Box(
                modifier = Modifier
                    .size(350.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 100.dp, y = (-80).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF34C77B).copy(alpha = 0.30f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                    .blur(50.dp)
            )
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 60.dp, y = 60.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1A7A4A).copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                    .blur(40.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(56.dp))

                // 뒤로가기
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(8.dp, CircleShape, ambientColor = Color(0x14000000))
                            .clip(CircleShape)
                            .background(GlassWhite),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(
                            onClick  = onNavigateBack,
                            modifier = Modifier.size(38.dp),
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.ArrowBack,
                                contentDescription = "뒤로가기",
                                tint               = TextPrimary,
                                modifier           = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 아이콘
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(
                            elevation    = 20.dp,
                            shape        = RoundedCornerShape(22.dp),
                            ambientColor = GreenMid.copy(alpha = 0.5f),
                            spotColor    = GreenPrimary.copy(alpha = 0.5f),
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF4CD988), Color(0xFF1A7A4A))
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    // 유리 하이라이트
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x33FFFFFF),
                                        Color(0x00FFFFFF),
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0x88FFFFFF),
                                        Color(0x11FFFFFF),
                                    )
                                ),
                                shape = RoundedCornerShape(22.dp),
                            )
                    )
                    Icon(
                        imageVector        = Icons.Rounded.Spa,
                        contentDescription = null,
                        tint               = White,
                        modifier           = Modifier.size(30.dp),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text          = "계정 만들기",
                    fontSize      = 28.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = TextPrimary,
                    letterSpacing = (-0.5).sp,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text     = "함께 식물을 키워봐요 🪴",
                    fontSize = 15.sp,
                    color    = TextSecondary,
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 유리 카드
                GlassCard {
                    Column(modifier = Modifier.padding(24.dp)) {

                        GlassTextField(
                            value         = state.name,
                            onValueChange = viewModel::onSignUpNameChange,
                            label         = "이름",
                            placeholder   = "홍길동",
                            icon          = Icons.Rounded.Person,
                            isError       = state.nameError != null,
                            errorMessage  = state.nameError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction    = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        GlassTextField(
                            value         = state.email,
                            onValueChange = viewModel::onSignUpEmailChange,
                            label         = "이메일",
                            placeholder   = "hello@example.com",
                            icon          = Icons.Rounded.Email,
                            isError       = state.emailError != null,
                            errorMessage  = state.emailError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction    = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        GlassTextField(
                            value         = state.password,
                            onValueChange = viewModel::onSignUpPasswordChange,
                            label         = "비밀번호",
                            placeholder   = "6자 이상 입력",
                            icon          = Icons.Rounded.Lock,
                            isError       = state.passwordError != null,
                            errorMessage  = state.passwordError,
                            visualTransformation = if (state.isPasswordVisible)
                                VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon  = {
                                IconButton(
                                    onClick  = viewModel::onSignUpPasswordVisibilityToggle,
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
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.onSignUpSubmit()
                                }
                            ),
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        AppleButton(
                            text      = "가입하기",
                            onClick   = {
                                focusManager.clearFocus()
                                viewModel.onSignUpSubmit()
                            },
                            isLoading = state.isLoading,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text     = "이미 계정이 있으신가요?",
                        fontSize = 14.sp,
                        color    = TextSecondary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(
                        onClick        = onNavigateBack,
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(
                            text       = "← 로그인",
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

@Preview(showBackground = true, showSystemUi = true, name = "SignUp Screen")
@Composable
fun SignUpScreenPreview() {
    SignUpScreen()
}