package com.hayderameen.mypr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.hayderameen.mypr.domain.Equipment
import com.hayderameen.mypr.domain.Plate
import com.hayderameen.mypr.domain.PlateColor
import com.hayderameen.mypr.domain.WeightUnit
import com.hayderameen.mypr.domain.barWeight
import com.hayderameen.mypr.domain.breakdown
import com.hayderameen.mypr.domain.platesPerSide
import com.hayderameen.mypr.domain.toKg
import com.hayderameen.mypr.ui.theme.LmText
import com.hayderameen.mypr.ui.theme.Metal
import kotlin.math.floor
import kotlin.math.roundToInt

// ------------------------------------------------------------------------------------------
// Plate paint
// ------------------------------------------------------------------------------------------

object PlatePaint {
    fun stops(c: PlateColor): List<Color> = when (c) {
        PlateColor.RED -> listOf(Color(0xFF991B1B), Color(0xFFDC2626), Color(0xFFB91C1C), Color(0xFF7F1D1D))
        PlateColor.BLUE -> listOf(Color(0xFF1E40AF), Color(0xFF2563EB), Color(0xFF1D4ED8), Color(0xFF1E3A8A))
        PlateColor.YELLOW -> listOf(Color(0xFFA16207), Color(0xFFEAB308), Color(0xFFCA8A04), Color(0xFF854D0E))
        PlateColor.GREEN -> listOf(Color(0xFF166534), Color(0xFF16A34A), Color(0xFF15803D), Color(0xFF14532D))
        PlateColor.WHITE -> listOf(Color(0xFF94A3B8), Color(0xFFF8FAFC), Color(0xFFE2E8F0), Color(0xFF64748B))
        PlateColor.BLACK -> Metal.ironStops
        PlateColor.CHROME -> listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1), Color(0xFF94A3B8), Color(0xFF64748B))
    }

    fun rim(c: PlateColor): Color = when (c) {
        PlateColor.RED -> Color(0xFFFCA5A5)
        PlateColor.BLUE -> Color(0xFF93C5FD)
        PlateColor.YELLOW -> Color(0xFFFDE047)
        PlateColor.GREEN -> Color(0xFF86EFAC)
        PlateColor.WHITE -> Color(0xFF94A3B8)
        PlateColor.BLACK -> Color(0xFF64748B)
        PlateColor.CHROME -> Color(0xFFCBD5E1)
    }

    /** Text colour on the face of a plate (plates reference screen). */
    fun ink(c: PlateColor): Color = when (c) {
        PlateColor.YELLOW, PlateColor.WHITE, PlateColor.CHROME -> Color(0xFF0F172A)
        else -> Color.White
    }
}

private val STOP_POS = floatArrayOf(0f, 0.4f, 0.8f, 1f)

private fun hGrad(colors: List<Color>, x: Float, w: Float): Brush {
    val pos = if (colors.size == 4) STOP_POS else FloatArray(colors.size) { it / (colors.size - 1f) }
    return Brush.horizontalGradient(*Array(colors.size) { pos[it] to colors[it] }, startX = x, endX = x + w)
}

private fun vGrad(colors: List<Color>, y: Float, h: Float): Brush =
    Brush.verticalGradient(*Array(colors.size) { (it / (colors.size - 1f)) to colors[it] }, startY = y, endY = y + h)

private val steel = listOf(Metal.steelLight, Metal.steelMid, Metal.steelDark)
private val collar = listOf(Metal.collarLight, Metal.collarMid, Metal.collarDark)

private fun DrawScope.rr(brush: Brush, x: Float, y: Float, w: Float, h: Float, r: Float, alpha: Float = 1f) =
    drawRoundRect(brush, Offset(x, y), Size(w, h), CornerRadius(r, r), alpha = alpha)

private fun DrawScope.rr(color: Color, x: Float, y: Float, w: Float, h: Float, r: Float, alpha: Float = 1f) =
    drawRoundRect(color, Offset(x, y), Size(w, h), CornerRadius(r, r), alpha = alpha)

private fun DrawScope.rrStroke(color: Color, x: Float, y: Float, w: Float, h: Float, r: Float, width: Float, alpha: Float = 1f) =
    drawRoundRect(color, Offset(x, y), Size(w, h), CornerRadius(r, r), alpha = alpha, style = Stroke(width))

/** Runs [block] with alpha [p] and a scale-in from [fromScale] around ([cx], [cy]). */
private inline fun DrawScope.appear(p: Float, cx: Float, cy: Float, fromScale: Float = 1.12f, dy: Float = 0f, block: DrawScope.(alpha: Float) -> Unit) {
    if (p <= 0f) return
    val s = fromScale + (1f - fromScale) * p
    withTransform({
        translate(0f, dy * (1f - p))
        scale(s, s, Offset(cx, cy))
    }) { block(p.coerceIn(0f, 1f)) }
}

// ------------------------------------------------------------------------------------------
// Scene model (shared by the live log-set visual and the animated PR reveal)
// ------------------------------------------------------------------------------------------

private const val MAX_BAR_PLATES = 8
private const val MAX_PIN_PLATES = 6

/**
 * What to draw for a given equipment + weight. [steps] are the animatable pieces in loading
 * order with the share of the weight each contributes (used by the PR reveal count-up).
 */
data class EquipmentScene(
    val equipment: Equipment,
    val weight: Double,
    val unit: WeightUnit,
    val kg: Double,
    val plates: List<Plate>,
    val overflow: Int,
    val steps: List<Double>,
    /** Where the reveal count-up starts (empty bar for barbells). */
    val baseWeight: Double,
)

fun equipmentScene(equipment: Equipment, weight: Double, unit: WeightUnit): EquipmentScene {
    val kg = toKg(weight, unit)
    return when (equipment) {
        Equipment.BARBELL -> {
            val all = platesPerSide(weight, unit)
            val shown = all.take(MAX_BAR_PLATES)
            EquipmentScene(equipment, weight, unit, kg, shown, all.size - shown.size, shown.map { it.weight } + 0.0, barWeight(unit))
        }
        Equipment.BODYWEIGHT -> {
            val all = breakdown(weight, unit)
            val shown = all.take(MAX_PIN_PLATES)
            EquipmentScene(equipment, weight, unit, kg, shown, all.size - shown.size, shown.map { it.weight }.ifEmpty { listOf(0.0) }, 0.0)
        }
        Equipment.DUMBBELL -> {
            val count = dumbbellDiscs(kg)
            EquipmentScene(equipment, weight, unit, kg, emptyList(), 0, List(count) { 1.0 }, 0.0)
        }
        Equipment.CABLE, Equipment.MACHINE -> EquipmentScene(equipment, weight, unit, kg, emptyList(), 0, listOf(1.0), 0.0)
    }
}

private fun dumbbellDiscs(kg: Double) = (kg / 6.0).roundToInt().coerceIn(2, 6)

private fun stackIndex(equipment: Equipment, kg: Double): Int {
    val perPlate = if (equipment == Equipment.MACHINE) 30.0 else 14.0
    return floor((kg - 10.0) / perPlate).toInt().coerceIn(0, 7)
}

// ------------------------------------------------------------------------------------------
// Composable
// ------------------------------------------------------------------------------------------

/**
 * Live equipment drawing in the Stitch 300x84 drawing space.
 * [progress] gives each loading step's appearance (0..1) — the log-set screen leaves it at 1,
 * the PR reveal animates it. [depth] adds the 3D rims used on the reveal stage.
 */
@Composable
fun EquipmentVisual(
    scene: EquipmentScene,
    modifier: Modifier = Modifier,
    progress: (Int) -> Float = { 1f },
    depth: Boolean = false,
) {
    Box(modifier.fillMaxWidth().aspectRatio(300f / 84f)) {
        Canvas(Modifier.matchParentSize()) {
            val s = size.width / 300f
            withTransform({ scale(s, s, Offset.Zero) }) {
                when (scene.equipment) {
                    Equipment.BARBELL -> drawBarbell(scene, progress, depth)
                    Equipment.DUMBBELL -> drawDumbbell(scene, progress)
                    Equipment.CABLE, Equipment.MACHINE -> drawStack(scene, progress(0))
                    Equipment.BODYWEIGHT -> drawBeltAndPin(scene, progress)
                }
            }
        }
        val note = when {
            scene.overflow > 0 && scene.equipment == Equipment.BARBELL -> "+${scene.overflow} plates / side"
            scene.overflow > 0 -> "+${scene.overflow} plates"
            scene.equipment == Equipment.BODYWEIGHT && scene.weight <= 0.0 -> "Bodyweight only"
            else -> null
        }
        if (note != null) {
            Text(
                note,
                style = LmText.monoSm,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 4.dp),
            )
        }
    }
}

private fun DrawScope.drawBarbell(scene: EquipmentScene, progress: (Int) -> Float, depth: Boolean) {
    val flex = if (scene.kg >= 140) ((scene.kg - 130) * 0.08).toFloat().coerceAtMost(5f) else 0f
    // Sleeves
    rr(vGrad(collar, 37 + flex * 0.6f, 10f), 14f, 37 + flex * 0.6f, 58f, 10f, 3f)
    rr(vGrad(collar, 37 + flex * 0.6f, 10f), 228f, 37 + flex * 0.6f, 58f, 10f, 3f)
    // Shaft (bends under heavy loads)
    val shaft = Path().apply {
        moveTo(72f, 42 + flex * 0.4f)
        quadraticTo(150f, 42 - flex, 228f, 42 + flex * 0.4f)
    }
    drawPath(shaft, vGrad(steel, 39f, 6f), style = Stroke(5.5f, cap = StrokeCap.Round))
    // Inner collars
    rr(vGrad(collar, 31f, 22f), 70f, 31 + flex * 0.3f, 6f, 22f, 2f)
    rr(vGrad(collar, 31f, 22f), 224f, 31 + flex * 0.3f, 6f, 22f, 2f)

    val plates = scene.plates
    val natural = plates.sumOf { (it.width + 2f).toDouble() }.toFloat()
    val fit = if (natural > 56f) 56f / natural else 1f
    var off = 0f
    plates.forEachIndexed { i, p ->
        val w = p.width * fit
        val lx = 68 - off - w
        val rx = 232 + off
        val py = 42 + flex * 0.5f - p.height / 2
        val stops = PlatePaint.stops(p.color)
        val rim = PlatePaint.rim(p.color)
        appear(progress(i), 150f, 42f) { a ->
            if (depth) {
                rr(Metal.depth, lx - 2, py + 1, w + 2, p.height - 2, 3f, alpha = 0.35f * a)
                rr(Metal.depth, rx, py + 1, w + 2, p.height - 2, 3f, alpha = 0.35f * a)
            }
            rr(hGrad(stops, lx, w), lx, py, w, p.height, 3f, alpha = a)
            rrStroke(rim, lx, py, w, p.height, 3f, 0.5f, alpha = a)
            rr(hGrad(stops, rx, w), rx, py, w, p.height, 3f, alpha = a)
            rrStroke(rim, rx, py, w, p.height, 3f, 0.5f, alpha = a)
        }
        off += w + 2f * fit
    }
    if (plates.isNotEmpty()) {
        appear(progress(plates.size), 150f, 42f, fromScale = 1f) { a ->
            rr(vGrad(collar, 34f, 16f), 68 - off - 4, 34 + flex * 0.5f, 4f, 16f, 1.5f, alpha = a)
            rr(vGrad(collar, 34f, 16f), 232 + off, 34 + flex * 0.5f, 4f, 16f, 1.5f, alpha = a)
        }
    }
}

private fun DrawScope.drawDumbbell(scene: EquipmentScene, progress: (Int) -> Float) {
    val count = scene.steps.size
    val barY = 42f
    rr(vGrad(steel, barY - 4, 8f), 112f, barY - 4, 76f, 8f, 2f)
    rr(Metal.steelDark, 124f, barY - 4, 52f, 8f, 0f, alpha = 0.45f)
    for (i in 0 until count) {
        val h = 48f - if (i == count - 1) 6f else 0f
        val gap = i * 10f
        appear(progress(i), 150f, barY, fromScale = 0.92f) { a ->
            for (x in listOf(102f - gap, 189f + gap)) {
                rr(vGrad(Metal.oldIronStops, barY - h / 2, h), x, barY - h / 2, 9f, h, 3f, alpha = a)
                rrStroke(Metal.collarMid, x, barY - h / 2, 9f, h, 3f, 0.6f, alpha = a)
            }
        }
    }
    val outer = count * 10f
    rr(vGrad(collar, barY - 6, 12f), 106 - outer, barY - 6, 5f, 12f, 1.5f)
    rr(vGrad(collar, barY - 6, 12f), 189 + outer, barY - 6, 5f, 12f, 1.5f)
}

private fun DrawScope.drawStack(scene: EquipmentScene, pinProgress: Float) {
    val active = stackIndex(scene.equipment, scene.kg)
    // Guide rods and cable
    drawLine(Metal.steelMid, Offset(110f, 2f), Offset(110f, 82f), 1.5f)
    drawLine(Metal.steelMid, Offset(190f, 2f), Offset(190f, 82f), 1.5f)
    drawLine(Metal.steelDark, Offset(150f, 0f), Offset(150f, 8f), 2f)
    for (i in 0 until 8) {
        val y = 8f + i * 9f
        when {
            i == active -> rr(hGrad(PlatePaint.stops(PlateColor.RED), 116f, 68f), 116f, y, 68f, 7f, 2f)
            i < active -> rr(hGrad(Metal.ironStops, 116f, 68f), 116f, y, 68f, 7f, 2f)
            else -> rr(Metal.stackIdle, 116f, y, 68f, 7f, 2f)
        }
    }
    val pinY = 8f + active * 9f + 3.5f
    appear(pinProgress, 150f, pinY, fromScale = 1f, dy = -16f) { a ->
        drawLine(Metal.steelLight, Offset(150f, pinY), Offset(196f, pinY), 2f, alpha = a)
        drawCircle(Color.White, 2.5f, Offset(150f, pinY), alpha = a)
        drawCircle(Metal.steelMid, 3f, Offset(198f, pinY), alpha = a)
    }
}

/** New design (no Stitch screen): dip belt + chain + loading pin with plates stacked flat. */
private fun DrawScope.drawBeltAndPin(scene: EquipmentScene, progress: (Int) -> Float) {
    // Belt
    val belt = Path().apply {
        moveTo(92f, 9f)
        quadraticTo(150f, 34f, 208f, 9f)
    }
    drawPath(belt, vGrad(Metal.leather, 6f, 22f), style = Stroke(8f, cap = StrokeCap.Round))
    drawPath(belt, Color.White.copy(alpha = 0.22f), style = Stroke(0.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 2.5f))))
    // D-rings
    drawCircle(Metal.steelMid, 4f, Offset(96f, 13f), style = Stroke(1.8f))
    drawCircle(Metal.steelMid, 4f, Offset(204f, 13f), style = Stroke(1.8f))
    // Chain
    val chain = PathEffect.dashPathEffect(floatArrayOf(4f, 2.5f))
    drawLine(Metal.steelMid, Offset(99f, 16f), Offset(148f, 40f), 2.6f, StrokeCap.Round, chain)
    drawLine(Metal.steelMid, Offset(201f, 16f), Offset(152f, 40f), 2.6f, StrokeCap.Round, chain)
    // Carabiner + loading pin
    rrStroke(Metal.steelDark, 146f, 38f, 8f, 9f, 3f, 1.6f)
    rr(hGrad(steel, 147.5f, 5f), 147.5f, 46f, 5f, 36f, 1.5f)

    // Plates lying flat on the pin, heaviest at the bottom.
    var y = 81f
    scene.plates.forEachIndexed { i, p ->
        val w = p.height * 1.15f
        val t = (p.width * 0.4f).coerceAtLeast(2.4f)
        y -= t + 0.8f
        val x = 150f - w / 2
        val top = y
        appear(progress(i), 150f, top + t / 2, fromScale = 1f, dy = -18f) { a ->
            rr(hGrad(PlatePaint.stops(p.color), x, w), x, top, w, t, 1.5f, alpha = a)
            rrStroke(PlatePaint.rim(p.color), x, top, w, t, 1.5f, 0.4f, alpha = a)
        }
    }
}
