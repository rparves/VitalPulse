package com.vitapulse.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Category colours: Fitbit-style teal hero + Apple Health-style colour per data type. */
object VP {
    val Steps = Color(0xFF00B5B8)
    val Fuel = Color(0xFFFF8A3D)
    val Water = Color(0xFF3D9BFF)
    val Active = Color(0xFF34C759)
    val Sleep = Color(0xFF8E7CFF)
    val Heart = Color(0xFFFF375F)
    val Weight = Color(0xFFBF5AF2)
    val Protein = Color(0xFFFF6B8A)
    val Carbs = Color(0xFFFFC23D)
    val Fat = Color(0xFF5AC8FA)
}

private val Dark = darkColorScheme(
    primary = Color(0xFF00C7C9), onPrimary = Color(0xFF00201F),
    primaryContainer = Color(0xFF0E3A3B), onPrimaryContainer = Color(0xFFB8F5F5),
    secondary = Color(0xFFFF8A3D),
    background = Color(0xFF0B0F14), onBackground = Color(0xFFE8EDF2),
    surface = Color(0xFF0B0F14), onSurface = Color(0xFFE8EDF2),
    surfaceContainer = Color(0xFF161B22), surfaceContainerHigh = Color(0xFF1D232B),
    surfaceContainerLow = Color(0xFF12161C),
    onSurfaceVariant = Color(0xFF9AA5B1), outline = Color(0xFF3A434E),
)

private val Light = lightColorScheme(
    primary = Color(0xFF00898B), onPrimary = Color.White,
    primaryContainer = Color(0xFFCCF2F2), onPrimaryContainer = Color(0xFF00393A),
    secondary = Color(0xFFE5641B),
    background = Color(0xFFF4F6F8), onBackground = Color(0xFF12171D),
    surface = Color(0xFFF4F6F8), onSurface = Color(0xFF12171D),
    surfaceContainer = Color.White, surfaceContainerHigh = Color(0xFFEDEFF2),
    surfaceContainerLow = Color(0xFFF9FAFB),
    onSurfaceVariant = Color(0xFF5B6670), outline = Color(0xFFC9D0D6),
)

@Composable
fun VitaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}
