package com.hayderameen.mypr.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hayderameen.mypr.domain.PlateColor
import com.hayderameen.mypr.domain.formatWeight
import com.hayderameen.mypr.ui.theme.LmText
import com.hayderameen.mypr.ui.theme.Motion
import com.hayderameen.mypr.ui.theme.Radius
import com.hayderameen.mypr.ui.theme.Space
import kotlinx.coroutines.launch
import kotlin.random.Random

private class Confetti(
    val x: Float, val vx: Float, val vy: Float,
    val color: Color, val w: Float, val h: Float, val spin: Float,
)

/**
 * Shown the moment a set beats the all-time best. One confetti burst (no loop), the trophy,
 * and the new weight counting up from the old best.
 */
@Composable
fun PrCelebration(
    exerciseName: String,
    newWeight: Double,
    previousWeight: Double,
    unitLabel: String,
    reps: Int,
    onDone: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    BackHandler(onBack = onDone)

    val colors = remember {
        listOf(PlateColor.RED, PlateColor.BLUE, PlateColor.YELLOW, PlateColor.GREEN, PlateColor.WHITE)
            .map { PlatePaint.stops(it)[1] } + Color(0xFFB22200)
    }
    val pieces = remember {
        val r = Random(System.nanoTime())
        List(90) {
            Confetti(
                x = r.nextFloat(),
                vx = (r.nextFloat() - 0.5f) * 0.5f,
                vy = -(0.55f + r.nextFloat() * 0.6f),
                color = colors[r.nextInt(colors.size)],
                w = 6f + r.nextFloat() * 6f,
                h = 10f + r.nextFloat() * 8f,
                spin = (r.nextFloat() - 0.5f) * 900f,
            )
        }
    }
    val t = remember { Animatable(0f) }
    val pop = remember { Animatable(0.6f) }
    val count = remember { Animatable(previousWeight.toFloat()) }
    LaunchedEffect(Unit) {
        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        launch { t.animateTo(1f, tween(2400, easing = LinearEasing)) }
        launch { pop.animateTo(1f, tween(Motion.ENTER_MS * 2, easing = Motion.enterEasing)) }
        count.animateTo(newWeight.toFloat(), tween(900, delayMillis = 150, easing = Motion.enterEasing))
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(cs.inverseSurface.copy(alpha = 0.97f))
            .clickable(remember { MutableInteractionSource() }, indication = null) {},
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val time = t.value
            val g = 1.6f
            pieces.forEach { p ->
                val x = (p.x + p.vx * time) * size.width
                val y = size.height * (0.55f + p.vy * time + 0.5f * g * time * time)
                if (y > size.height + 20f) return@forEach
                rotate(p.spin * time, Offset(x, y)) {
                    drawRect(p.color, Offset(x - p.w / 2, y - p.h / 2), Size(p.w, p.h), alpha = (1.2f - time).coerceIn(0f, 1f))
                }
            }
        }

        Column(
            Modifier
                .align(Alignment.Center)
                .padding(horizontal = Space.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .graphicsLayer { scaleX = pop.value; scaleY = pop.value }
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(cs.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.EmojiEvents, null, tint = Color.White, modifier = Modifier.size(52.dp))
            }
            Spacer(Modifier.height(Space.xl))
            Text(
                "NEW PR",
                style = LmText.labelSm,
                color = cs.inverseOnSurface,
                modifier = Modifier
                    .clip(Radius.full)
                    .background(cs.primaryContainer)
                    .padding(horizontal = Space.md, vertical = Space.xs),
            )
            Spacer(Modifier.height(Space.md))
            Text(exerciseName, style = LmText.headlineLgMobile, color = cs.inverseOnSurface, textAlign = TextAlign.Center)
            Spacer(Modifier.height(Space.lg))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(formatWeight(count.value.toDouble()), style = LmText.monoMetric, color = Color.White)
                Spacer(Modifier.width(Space.sm))
                Text(unitLabel, style = LmText.titleMd, color = cs.inverseOnSurface, modifier = Modifier.padding(bottom = 8.dp))
            }
            Text("× $reps reps", style = LmText.monoSm, color = cs.inverseOnSurface.copy(alpha = 0.8f))
            Spacer(Modifier.height(Space.md))
            Text(
                "+${formatWeight(newWeight - previousWeight)} $unitLabel over your previous best of ${formatWeight(previousWeight)} $unitLabel",
                style = LmText.bodyMd,
                color = cs.inverseOnSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(Space.page),
            verticalArrangement = Arrangement.Bottom,
        ) {
            PrimaryButton("Keep Lifting", onDone)
        }
    }
}
