package com.hayderameen.mypr.ui.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hayderameen.mypr.domain.RulerSpec
import com.hayderameen.mypr.domain.nearlyEqual
import com.hayderameen.mypr.ui.theme.LmText
import com.hayderameen.mypr.ui.theme.Motion
import com.hayderameen.mypr.ui.theme.Radius
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

private fun isMultiple(v: Double, interval: Double): Boolean {
    val q = v / interval
    return abs(q - q.roundToInt()) < 1e-6
}

private fun tickLabel(v: Double): String = if (isMultiple(v, 1.0)) v.roundToInt().toString() else String.format(java.util.Locale.US, "%.1f", v)

/**
 * The "precision machined ruling scale" from the Stitch log-set screen: drag the scale under a
 * fixed vermilion needle. Flings decelerate and settle on a tick; every tick crossed gives a
 * haptic click. [resistance] slows dragging toward heavier values.
 *
 * A typed-in value that isn't on a tick is kept exactly; the needle simply sits between ticks.
 */
@Composable
fun RulerSlider(
    value: Double,
    onValueChange: (Double) -> Unit,
    spec: RulerSpec,
    contentDescription: String,
    modifier: Modifier = Modifier,
    stepWidth: Dp = 14.dp,
    resistance: (Double) -> Float = { 1f },
) {
    val cs = MaterialTheme.colorScheme
    val stepPx = with(LocalDensity.current) { stepWidth.toPx() }
    val totalSteps = ((spec.max - spec.min) / spec.step).roundToInt()
    val maxOffset = totalSteps * stepPx

    fun offsetFor(v: Double) = (((v - spec.min) / spec.step) * stepPx).toFloat().coerceIn(0f, maxOffset)
    fun valueAt(offset: Float): Double {
        val idx = (offset / stepPx).roundToInt().coerceIn(0, totalSteps)
        return spec.min + idx * spec.step
    }

    var offset by remember(spec, stepPx) { mutableFloatStateOf(offsetFor(value)) }
    var interacting by remember { mutableStateOf(false) }
    val lastEmitted = remember { doubleArrayOf(value) }
    val currentValue by rememberUpdatedState(value)
    val onChange by rememberUpdatedState(onValueChange)
    val resistanceFn by rememberUpdatedState(resistance)
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val decay = rememberSplineBasedDecay<Float>()
    var animJob by remember { mutableStateOf<Job?>(null) }

    // External changes (typed value, unit switch, defaults loading) move the scale.
    LaunchedEffect(value, spec) {
        lastEmitted[0] = value
        if (!interacting && !nearlyEqual(valueAt(offset), value)) offset = offsetFor(value)
    }

    fun emit() {
        val v = valueAt(offset)
        if (!nearlyEqual(v, lastEmitted[0])) {
            lastEmitted[0] = v
            haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
            onChange(v)
        }
    }

    fun settle(velocity: Float) {
        animJob?.cancel()
        animJob = scope.launch {
            val projected = decay.calculateTargetValue(offset, velocity).coerceIn(0f, maxOffset)
            val target = ((projected / stepPx).roundToInt() * stepPx).coerceIn(0f, maxOffset)
            val distance = abs(target - offset)
            val duration = (Motion.ENTER_MS + distance / 6f).toInt().coerceIn(Motion.STATE_MS, 900)
            try {
                animate(offset, target, animationSpec = tween(duration, easing = Motion.enterEasing)) { v, _ ->
                    offset = v
                    emit()
                }
            } finally {
                interacting = false
            }
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = LmText.monoSm.copy(color = cs.onSurfaceVariant)
    val fade = cs.surfaceContainerLow

    Box(
        modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(Radius.md)
            .background(fade)
            .semantics {
                this.contentDescription = contentDescription
                stateDescription = tickLabel(currentValue)
                progressBarRangeInfo = ProgressBarRangeInfo(currentValue.toFloat(), spec.min.toFloat()..spec.max.toFloat(), totalSteps)
                setProgress { target ->
                    val v = spec.min + ((target - spec.min) / spec.step).roundToInt() * spec.step
                    onChange(v.coerceIn(spec.min, spec.max)); true
                }
            }
            .pointerInput(spec, stepPx) {
                val tracker = VelocityTracker()
                detectHorizontalDragGestures(
                    onDragStart = {
                        animJob?.cancel()
                        interacting = true
                        tracker.resetTracking()
                    },
                    onHorizontalDrag = { change, dx ->
                        tracker.addPosition(change.uptimeMillis, change.position)
                        change.consume()
                        // Dragging left (dx < 0) moves toward heavier values.
                        val r = if (dx < 0) resistanceFn(currentValue) else 1f
                        offset = (offset - dx * r).coerceIn(0f, maxOffset)
                        emit()
                    },
                    onDragEnd = {
                        val v = -tracker.calculateVelocity().x
                        settle(if (v > 0) v * resistanceFn(currentValue) else v)
                    },
                    onDragCancel = { settle(0f) },
                )
            },
    ) {
        Canvas(Modifier.matchParentSize()) {
            val center = size.width / 2f
            val top = 12.dp.toPx()
            val first = floor((offset - center) / stepPx).toInt().coerceAtLeast(0)
            val last = ceil((offset + center) / stepPx).toInt().coerceAtMost(totalSteps)
            for (i in first..last) {
                val x = center + i * stepPx - offset
                val v = spec.min + i * spec.step
                val major = isMultiple(v, spec.majorInterval)
                val mid = !major && spec.midInterval?.let { isMultiple(v, it) } == true
                val (h, w, c) = when {
                    major -> Triple(30.dp, 2.dp, cs.onSurface)
                    mid -> Triple(18.dp, 2.dp, cs.onSurfaceVariant)
                    else -> Triple(11.dp, 1.dp, cs.outline)
                }
                drawLine(c, Offset(x, top), Offset(x, top + h.toPx()), w.toPx())
                if (major) {
                    val layout = textMeasurer.measure(tickLabel(v), labelStyle)
                    drawText(layout, topLeft = Offset(x - layout.size.width / 2f, top + 34.dp.toPx()))
                }
            }
            // Hairline rails
            val rail = cs.outlineVariant
            drawLine(rail, Offset(0f, 4.dp.toPx()), Offset(size.width, 4.dp.toPx()), 1.dp.toPx())
            drawLine(rail, Offset(0f, size.height - 4.dp.toPx()), Offset(size.width, size.height - 4.dp.toPx()), 1.dp.toPx())
            // Edge fades
            val fw = 56.dp.toPx()
            drawRect(Brush.horizontalGradient(listOf(fade, Color.Transparent), 0f, fw), size = Size(fw, size.height))
            drawRect(
                Brush.horizontalGradient(listOf(Color.Transparent, fade), size.width - fw, size.width),
                topLeft = Offset(size.width - fw, 0f),
                size = Size(fw, size.height),
            )
            // Vernier lens + needle
            drawRoundRect(
                cs.primary.copy(alpha = 0.08f),
                topLeft = Offset(center - 14.dp.toPx(), 4.dp.toPx()),
                size = Size(28.dp.toPx(), size.height - 8.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
            )
            drawLine(cs.primary, Offset(center, top - 2.dp.toPx()), Offset(center, top + 40.dp.toPx()), 3.dp.toPx(), StrokeCap.Round)
            val tri = Path().apply {
                moveTo(center - 6.dp.toPx(), 4.dp.toPx())
                lineTo(center + 6.dp.toPx(), 4.dp.toPx())
                lineTo(center, top + 2.dp.toPx())
                close()
            }
            drawPath(tri, cs.primary)
        }
    }
}
