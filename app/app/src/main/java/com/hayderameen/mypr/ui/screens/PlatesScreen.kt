package com.hayderameen.mypr.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayderameen.mypr.domain.Plate
import com.hayderameen.mypr.domain.WeightUnit
import com.hayderameen.mypr.domain.platesFor
import com.hayderameen.mypr.ui.components.BackHeader
import com.hayderameen.mypr.ui.components.LmCard
import com.hayderameen.mypr.ui.components.PlatePaint
import com.hayderameen.mypr.ui.rememberApp
import com.hayderameen.mypr.ui.theme.LmText
import com.hayderameen.mypr.ui.theme.Metal
import com.hayderameen.mypr.ui.theme.SplineMono
import com.hayderameen.mypr.ui.theme.Space
import androidx.compose.ui.text.font.FontWeight
import java.util.Locale

@Composable
fun PlatesScreen(onBack: () -> Unit) {
    val app = rememberApp()
    val unit by app.settings.unit.collectAsStateWithLifecycle(WeightUnit.KG)
    val plates = platesFor(unit)
    val isKg = unit == WeightUnit.KG

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        BackHeader(onBack = onBack, title = if (isKg) "Olympic Plates" else "Pound Plates") {
            Text(if (isKg) "IPF SPEC" else "LB SET", style = LmText.monoSm, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = Space.md))
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(Space.page),
            horizontalArrangement = Arrangement.spacedBy(Space.md),
            verticalArrangement = Arrangement.spacedBy(Space.md),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(Space.sm), modifier = Modifier.padding(bottom = Space.sm)) {
                    Text(if (isKg) "Calibrated Steel Plates" else "Standard Pound Plates", style = LmText.headlineLgMobile)
                    Text(
                        if (isKg) "Colour-coded discs used to draw your barbell lifts. Sizes follow competition specs."
                        else "The plates My PR loads when pounds is your unit, colour-coded like lb bumper sets.",
                        style = LmText.bodyMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(plates) { plate -> PlateCard(plate, plates.first().height) }
        }
    }
}

@Composable
private fun PlateCard(plate: Plate, largest: Float) {
    val measurer = rememberTextMeasurer()
    val weightText = if (plate.weight % 1.0 == 0.0) plate.weight.toInt().toString() else String.format(Locale.US, "%s", plate.weight)
    LmCard(Modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().aspectRatio(1f)) {
            val c = Offset(size.width / 2, size.height / 2)
            val r = size.minDimension / 2 * 0.95f * (0.45f + 0.55f * plate.height / largest)
            val stops = PlatePaint.stops(plate.color)
            drawCircle(Brush.radialGradient(listOf(stops[1], stops[2], stops[3]), center = Offset(c.x - r * 0.3f, c.y - r * 0.4f), radius = r * 1.4f), r, c)
            drawCircle(stops.last(), r, c, style = Stroke(2.dp.toPx()))
            drawCircle(PlatePaint.rim(plate.color).copy(alpha = 0.5f), r * 0.85f, c, style = Stroke(1.dp.toPx()))
            val hub = r * 0.28f
            drawCircle(Brush.radialGradient(listOf(Metal.steelLight, Metal.steelMid, Metal.steelDark), center = c, radius = hub), hub, c)
            drawCircle(Metal.collarDark, hub, c, style = Stroke(1.dp.toPx()))
            drawCircle(Metal.depth.copy(alpha = 0.85f), hub * 0.55f, c)
            val ink = PlatePaint.ink(plate.color)
            val big = measurer.measure(weightText, TextStyle(fontFamily = SplineMono, fontWeight = FontWeight.Bold, fontSize = (r / 4.2f).toSp(), color = ink))
            drawText(big, topLeft = Offset(c.x - big.size.width / 2, c.y - r * 0.62f - big.size.height / 2))
            val small = measurer.measure(plate.unit.short, TextStyle(fontFamily = SplineMono, fontWeight = FontWeight.Bold, fontSize = (r / 6f).toSp(), color = ink.copy(alpha = 0.85f)))
            drawText(small, topLeft = Offset(c.x - small.size.width / 2, c.y + r * 0.6f - small.size.height / 2))
        }
        Spacer(Modifier.height(Space.sm))
        Text("$weightText ${plate.unit.label}", style = LmText.titleMd)
        Text(
            if (plate.diameterNote.isBlank()) plate.color.label else "${plate.color.label} • ${plate.diameterNote}",
            style = LmText.bodyMd,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
