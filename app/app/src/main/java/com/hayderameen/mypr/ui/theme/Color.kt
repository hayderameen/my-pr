package com.hayderameen.mypr.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Colour tokens from stitch_lifting_pr_tracker_app/liftmark_design_system.md

val LightColors = lightColorScheme(
    primary = Color(0xFFB22200),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD93814),
    onPrimaryContainer = Color(0xFFFFFCFF),
    secondary = Color(0xFFA0402A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFE876C),
    onSecondaryContainer = Color(0xFF741F0D),
    tertiary = Color(0xFF005CAF),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF0074DB),
    onTertiaryContainer = Color(0xFFFEFCFF),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFF8F9FF),
    onBackground = Color(0xFF0B1C30),
    surface = Color(0xFFF8F9FF),
    surfaceDim = Color(0xFFCBDBF5),
    surfaceBright = Color(0xFFF8F9FF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEFF4FF),
    surfaceContainer = Color(0xFFE5EEFF),
    surfaceContainerHigh = Color(0xFFDCE9FF),
    surfaceContainerHighest = Color(0xFFD3E4FE),
    onSurface = Color(0xFF0B1C30),
    surfaceVariant = Color(0xFFFFDAD2),
    onSurfaceVariant = Color(0xFF5B403A),
    outline = Color(0xFF907068),
    outlineVariant = Color(0xFFE4BEB5),
    inverseSurface = Color(0xFF213145),
    inverseOnSurface = Color(0xFFEAF1FF),
    inversePrimary = Color(0xFFFFB4A3),
    scrim = Color(0xFF000000),
)

val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB4A3),
    onPrimary = Color(0xFF630F00),
    primaryContainer = Color(0xFFD93814),
    onPrimaryContainer = Color(0xFFFFFCFF),
    secondary = Color(0xFFFFB4A3),
    onSecondary = Color(0xFF611202),
    secondaryContainer = Color(0xFF842B17),
    onSecondaryContainer = Color(0xFFFFA08A),
    tertiary = Color(0xFFA8C8FF),
    onTertiary = Color(0xFF003061),
    tertiaryContainer = Color(0xFF0074DB),
    onTertiaryContainer = Color(0xFFFEFCFF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF031427),
    onBackground = Color(0xFFD3E4FE),
    surface = Color(0xFF031427),
    surfaceDim = Color(0xFF031427),
    surfaceBright = Color(0xFF2A3A4F),
    surfaceContainerLowest = Color(0xFF000F21),
    surfaceContainerLow = Color(0xFF0B1C30),
    surfaceContainer = Color(0xFF102034),
    surfaceContainerHigh = Color(0xFF1B2B3F),
    surfaceContainerHighest = Color(0xFF26364A),
    onSurface = Color(0xFFD3E4FE),
    surfaceVariant = Color(0xFF5B403A),
    onSurfaceVariant = Color(0xFFE4BEB5),
    outline = Color(0xFFAB8981),
    outlineVariant = Color(0xFF5B403A),
    inverseSurface = Color(0xFFD3E4FE),
    inverseOnSurface = Color(0xFF213145),
    inversePrimary = Color(0xFFB72300),
    scrim = Color(0xFF000000),
)

/** Metal / plate palette used by the equipment drawings (same in light and dark, like real iron). */
object Metal {
    val steelLight = Color(0xFFE2E8F0)
    val steelMid = Color(0xFF94A3B8)
    val steelDark = Color(0xFF475569)
    val collarLight = Color(0xFFCBD5E1)
    val collarMid = Color(0xFF64748B)
    val collarDark = Color(0xFF334155)
    val ironStops = listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF020617))
    val oldIronStops = listOf(Color(0xFF52525B), Color(0xFF27272A), Color(0xFF18181B), Color(0xFF09090B))
    val stackIdle = Color(0xFFCBDBF5)
    val depth = Color(0xFF0F172A)
    val leather = listOf(Color(0xFF3F2A22), Color(0xFF2A1C16), Color(0xFF1A110D))
}
