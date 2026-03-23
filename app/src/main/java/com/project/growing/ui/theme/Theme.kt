package com.project.growing.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GreenPrimary   = Color(0xFF1A7A4A)
val GreenMid       = Color(0xFF25A865)
val GreenLight     = Color(0xFF4CD988)
val GreenPale      = Color(0xFFE8F8EF)
val GreenSurface   = Color(0xFFF2FBF6)
val AccentMint     = Color(0xFFAAF0C4)
val TextPrimary    = Color(0xFF0D1B12)
val TextSecondary  = Color(0xFF6B7A72)
val TextHint       = Color(0xFFB0BDB6)
val White          = Color(0xFFFFFFFF)
val GlassWhite     = Color(0xCCFFFFFF)   // 유리 카드 배경
val GlassBorder    = Color(0x55FFFFFF)   // 유리 테두리
val BackgroundGray = Color(0xFFF0F7F3)
val DividerColor   = Color(0xFFDDEDE4)
val ErrorColor     = Color(0xFFDC3545)
val ErrorBg        = Color(0xFFFFF0F2)

private val GrowingColorScheme = lightColorScheme(
    primary      = GreenPrimary,
    onPrimary    = White,
    secondary    = GreenMid,
    background   = BackgroundGray,
    surface      = White,
    onBackground = TextPrimary,
    onSurface    = TextPrimary,
    error        = ErrorColor,
)

@Composable
fun GrowingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GrowingColorScheme,
        content     = content,
    )
}