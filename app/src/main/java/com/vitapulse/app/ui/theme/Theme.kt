package com.vitapulse.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Performance-dark palette: electric lime + cyan on near-black, neon colour per data type. */
object VP {
    val Lime = Color(0xFFC8FF3E)
    val Cyan = Color(0xFF2EE6D6)
    val OnLime = Color(0xFF0A0F00)

    val Steps = Color(0xFF2EE6D6)
    val Fuel = Color(0xFFFF8A3D)
    val Water = Color(0xFF3DA5FF)
    val Active = Color(0xFFC8FF3E)
    val Sleep = Color(0xFF9D8CFF)
    val Heart = Color(0xFFFF4D6D)
    val Weight = Color(0xFFD16BFF)
    val Protein = Color(0xFFFF6B8A)
    val Carbs = Color(0xFFFFC93D)
    val Fat = Color(0xFF5AC8FA)

    val Bg = Color(0xFF07090C)
    val Card = Color(0xFF0F1318)
    val CardHi = Color(0xFF161C24)

    val brand: Brush get() = Brush.linearGradient(listOf(Lime, Cyan))
}

private val Scheme = darkColorScheme(
    primary = VP.Lime, onPrimary = VP.OnLime,
    primaryContainer = Color(0xFF223000), onPrimaryContainer = VP.Lime,
    secondary = VP.Cyan, onSecondary = Color(0xFF00201D),
    tertiary = VP.Fuel,
    background = VP.Bg, onBackground = Color(0xFFF2F5F8),
    surface = VP.Bg, onSurface = Color(0xFFF2F5F8),
    surfaceVariant = VP.CardHi, onSurfaceVariant = Color(0xFF8C97A4),
    surfaceContainerLowest = Color(0xFF050608), surfaceContainerLow = Color(0xFF0B0E12),
    surfaceContainer = VP.Card, surfaceContainerHigh = VP.CardHi, surfaceContainerHighest = Color(0xFF1C232D),
    outline = Color(0xFF2A3340), outlineVariant = Color(0xFF1E252E),
    error = Color(0xFFFF5A6E),
)

private val base = Typography()
private val VTypography = base.copy(
    displayMedium = base.displayMedium.copy(fontWeight = FontWeight.Black, letterSpacing = (-1.5).sp),
    displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Black, letterSpacing = (-1).sp),
    headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.8).sp),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelSmall = base.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
)

/** Dark-first by design — the neon accents need a dark canvas. */
@Composable
fun VitaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = VTypography, content = content)
}
