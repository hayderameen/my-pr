package com.hayderameen.mypr.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.hayderameen.mypr.ui.theme.LmText
import com.hayderameen.mypr.ui.theme.Motion

data class ChartPoint(val label: String, val value: Double)

/** Weight-progress line chart from the PR reveal screen (dashed grid, area fill, last point solid). */
@Composable
fun ProgressChart(points: List<ChartPoint>, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val measurer = rememberTextMeasurer()
    val labelStyle = LmText.monoSm.copy(color = cs.onSurfaceVariant, fontSize = LmText.monoSm.fontSize * 0.85f)
    val reveal = remember(points) { Animatable(0f) }
    LaunchedEffect(points) { reveal.animateTo(1f, tween(600, easing = Motion.enterEasing)) }

    Canvas(modifier.fillMaxWidth().height(170.dp)) {
        if (points.isEmpty()) return@Canvas
        val padL = 8.dp.toPx()
        val padR = 12.dp.toPx()
        val padT = 14.dp.toPx()
        val h = size.height - padT - 34.dp.toPx()
        val w = size.width - padL - padR
        val min = points.minOf { it.value }
        val max = points.maxOf { it.value }
        val pad = ((max - min) * 0.15).coerceAtLeast(2.5)
        val lo = min - pad
        val range = (max + pad * 0.5) - lo

        for (g in 0..2) {
            val y = padT + h / 2 * g
            drawLine(cs.outlineVariant, Offset(padL, y), Offset(padL + w, y), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
        }

        val coords = points.mapIndexed { i, p ->
            val x = if (points.size == 1) padL + w / 2 else padL + w / (points.size - 1) * i
            Offset(x, (padT + h - ((p.value - lo) / range) * h).toFloat())
        }
        val line = Path().apply {
            moveTo(coords[0].x, coords[0].y)
            coords.drop(1).forEach { lineTo(it.x, it.y) }
        }
        val area = Path().apply {
            addPath(line)
            lineTo(coords.last().x, padT + h)
            lineTo(coords.first().x, padT + h)
            close()
        }
        clipRect(right = padL + (w + padR) * reveal.value) {
            drawPath(area, Brush.verticalGradient(listOf(cs.primary.copy(alpha = 0.22f), Color.Transparent), startY = padT, endY = padT + h))
            drawPath(line, cs.primary, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            val dense = points.size > 12
            coords.forEachIndexed { i, c ->
                val last = i == coords.lastIndex
                if (dense && !last) return@forEachIndexed
                drawCircle(if (last) cs.primary else cs.surfaceContainerLowest, (if (last) 5 else 4).dp.toPx(), c)
                drawCircle(cs.primary, (if (last) 5 else 4).dp.toPx(), c, style = Stroke(2.5.dp.toPx()))
            }
        }

        // X labels: all of them when few, otherwise first / middle / last.
        val labelIdx = if (points.size <= 5) points.indices.toList() else listOf(0, points.size / 2, points.lastIndex)
        labelIdx.forEach { i ->
            val layout = measurer.measure(points[i].label, labelStyle)
            val x = (coords[i].x - layout.size.width / 2f).coerceIn(0f, size.width - layout.size.width)
            drawText(layout, topLeft = Offset(x, padT + h + 10.dp.toPx()))
        }
    }
}
