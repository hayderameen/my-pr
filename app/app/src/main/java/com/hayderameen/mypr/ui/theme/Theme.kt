package com.hayderameen.mypr.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object Space {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val section = 32.dp
    val page = 16.dp
}

object Radius {
    val sm = RoundedCornerShape(8.dp)
    val default = RoundedCornerShape(12.dp)
    val md = RoundedCornerShape(16.dp)
    val lg = RoundedCornerShape(20.dp)
    val xl = RoundedCornerShape(24.dp)
    val full = RoundedCornerShape(percent = 50)
}

object Motion {
    const val TICK_MS = 80
    const val STATE_MS = 120
    const val ENTER_MS = 200
    val enterEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
}

@Composable
fun MyPRTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = Shapes(
            extraSmall = Radius.sm,
            small = Radius.default,
            medium = Radius.md,
            large = Radius.lg,
            extraLarge = Radius.xl,
        ),
        content = content,
    )
}
