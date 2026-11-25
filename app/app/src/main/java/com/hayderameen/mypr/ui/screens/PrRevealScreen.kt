package com.hayderameen.mypr.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.hayderameen.mypr.data.LiftRepository
import com.hayderameen.mypr.data.SettingsStore
import com.hayderameen.mypr.data.db.ExerciseEntity
import com.hayderameen.mypr.data.db.SetEntity
import com.hayderameen.mypr.domain.Equipment
import com.hayderameen.mypr.domain.WeightUnit
import com.hayderameen.mypr.domain.displayWeight
import com.hayderameen.mypr.domain.formatSignedWeight
import com.hayderameen.mypr.domain.formatWeight
import com.hayderameen.mypr.ui.appViewModel
import com.hayderameen.mypr.ui.components.BackHeader
import com.hayderameen.mypr.ui.components.BottomNavBar
import com.hayderameen.mypr.ui.components.ChartPoint
import com.hayderameen.mypr.ui.components.EquipmentVisual
import com.hayderameen.mypr.ui.components.FilterPill
import com.hayderameen.mypr.ui.components.IconCircleButton
import com.hayderameen.mypr.ui.components.LmCard
import com.hayderameen.mypr.ui.components.MainTab
import com.hayderameen.mypr.ui.components.PrimaryButton
import com.hayderameen.mypr.ui.components.ProgressChart
import com.hayderameen.mypr.ui.components.SectionLabel
import com.hayderameen.mypr.ui.components.equipmentScene
import com.hayderameen.mypr.ui.formatShortDate
import com.hayderameen.mypr.ui.theme.LmText
import com.hayderameen.mypr.ui.theme.Radius
import com.hayderameen.mypr.ui.theme.Space
import com.hayderameen.mypr.ui.toLocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class RevealUi(
    val exercise: ExerciseEntity? = null,
    val unit: WeightUnit = WeightUnit.KG,
    val sets: List<SetEntity> = emptyList(),
)

class PrRevealViewModel(repo: LiftRepository, settings: SettingsStore, exerciseId: String) : ViewModel() {
    val state = combine(repo.observeExercise(exerciseId), settings.unit, repo.observeExerciseSets(exerciseId)) { e, unit, sets ->
        RevealUi(e, unit, sets)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RevealUi())
}

enum class Period(val label: String, val months: Long?) { M1("1M", 1), M3("3M", 3), M6("6M", 6), Y1("1Y", 12), ALL("All", null) }

private val dayLabel = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
private val monthLabel = DateTimeFormatter.ofPattern("MMM ''yy", Locale.getDefault())

/** Best weight per training day inside [period], oldest first, in [unit]. */
fun progressPoints(sets: List<SetEntity>, unit: WeightUnit, period: Period, today: LocalDate = LocalDate.now()): List<ChartPoint> {
    val cutoff = period.months?.let { today.minusMonths(it) }
    val fmt = if (period == Period.Y1 || period == Period.ALL) monthLabel else dayLabel
    return sets
        .groupBy { it.performedAt.toLocalDate() }
        .filterKeys { cutoff == null || !it.isBefore(cutoff) }
        .toSortedMap()
        .map { (date, daySets) ->
            val best = daySets.maxBy { it.weightKg }
            ChartPoint(date.format(fmt), displayWeight(best.weightKg, best.enteredWeight, best.enteredUnit, unit))
        }
}

@Composable
fun PrRevealScreen(exerciseId: String, onBack: () -> Unit, onTab: (MainTab) -> Unit) {
    val vm = appViewModel(key = "reveal-$exerciseId") { PrRevealViewModel(it.repository, it.settings, exerciseId) }
    val ui by vm.state.collectAsStateWithLifecycle()
    val cs = MaterialTheme.colorScheme
    var replay by remember { mutableIntStateOf(0) }
    var showGraph by rememberSaveable { mutableStateOf(true) }
    var period by rememberSaveable { mutableStateOf(Period.M3) }

    val exercise = ui.exercise
    val best = ui.sets.maxWithOrNull(compareBy<SetEntity>({ it.weightKg }, { it.reps }).thenByDescending { it.performedAt })

    Column(Modifier.fillMaxSize().background(cs.surface)) {
        BackHeader(onBack = onBack, label = "PR Reveal") {
            IconCircleButton(Icons.Filled.Replay, "Replay plate load", { replay++ })
        }
        if (exercise != null && best != null) {
            val unit = ui.unit
            val bestWeight = displayWeight(best.weightKg, best.enteredWeight, best.enteredUnit, unit)
            val isBodyweight = exercise.equipment == Equipment.BODYWEIGHT
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(Space.page),
                verticalArrangement = Arrangement.spacedBy(Space.lg),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    Row(
                        Modifier.clip(Radius.full).background(cs.primary.copy(alpha = 0.1f)).padding(horizontal = Space.md, vertical = Space.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.EmojiEvents, null, tint = cs.primary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(Space.xs))
                        Text("Verified PR • ${formatShortDate(best.performedAt)}", style = LmText.labelSm, color = cs.primary)
                    }
                    Text(exercise.name, style = LmText.headlineLgMobile)
                }

                RevealStage(exercise.equipment, bestWeight, unit, best.reps, isBodyweight, replay)

                PrimaryButton(
                    if (showGraph) "Hide Progress Graph" else "Show Progress Graph",
                    onClick = { showGraph = !showGraph },
                    leadingIcon = Icons.AutoMirrored.Filled.ShowChart,
                    container = cs.inverseSurface,
                    content = cs.inverseOnSurface,
                )

                AnimatedVisibility(showGraph, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    val points = remember(ui.sets, unit, period) { progressPoints(ui.sets, unit, period) }
                    LmCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Weight Progress", style = LmText.titleLg, modifier = Modifier.weight(1f))
                            val delta = if (points.size >= 2) points.last().value - points.first().value else 0.0
                            Text(
                                when {
                                    points.size < 2 -> "${points.size} session${if (points.size == 1) "" else "s"}"
                                    period == Period.ALL -> "${formatSignedWeight(delta)} ${unit.label} all-time"
                                    else -> "${formatSignedWeight(delta)} ${unit.label} over ${period.label}"
                                },
                                style = LmText.monoSm,
                                color = if (delta > 0) cs.primary else cs.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(Space.md))
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                            Period.entries.forEach { p -> FilterPill(p.label, p == period, { period = p }, Modifier.weight(1f)) }
                        }
                        Spacer(Modifier.height(Space.md))
                        if (points.isEmpty()) {
                            Text(
                                "No sets in the last ${period.label}.",
                                style = LmText.bodyMd,
                                color = cs.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = Space.xl),
                            )
                        } else {
                            ProgressChart(points)
                        }
                    }
                }
            }
        } else {
            Spacer(Modifier.weight(1f))
        }
        BottomNavBar(MainTab.HISTORY, onTab)
    }
}

/** The plate-loading reveal: plates go on one by one while the readout counts up to the PR. */
@Composable
private fun RevealStage(equipment: Equipment, weight: Double, unit: WeightUnit, reps: Int, isBodyweight: Boolean, replay: Int) {
    val cs = MaterialTheme.colorScheme
    val scene = remember(equipment, weight, unit) { equipmentScene(equipment, weight, unit) }
    val steps = scene.steps.size
    val t = remember(scene) { Animatable(0f) }
    val stepMs = when (equipment) {
        Equipment.BARBELL -> 220
        Equipment.DUMBBELL -> 160
        Equipment.BODYWEIGHT -> 200
        Equipment.CABLE, Equipment.MACHINE -> 420
    }
    LaunchedEffect(scene, replay) {
        t.snapTo(0f)
        delay(180)
        t.animateTo(steps.toFloat(), tween(stepMs * steps, easing = LinearEasing))
    }
    val progress = { i: Int -> (t.value - i).coerceIn(0f, 1f) }
    val totalShare = scene.steps.sum()
    val fraction: Double =
        if (totalShare > 0) scene.steps.indices.sumOf { progress(it).toDouble() * scene.steps[it] } / totalShare
        else (t.value / steps).toDouble()
    val readout = scene.baseWeight + (weight - scene.baseWeight) * fraction

    LmCard(Modifier.fillMaxWidth(), padding = 0.dp) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(cs.surfaceContainerLow)
                .padding(horizontal = Space.lg, vertical = Space.xl),
        ) {
            val shadow = cs.onSurface.copy(alpha = 0.16f)
            Canvas(Modifier.matchParentSize()) {
                val w = size.width * 0.8f
                val h = 16.dp.toPx()
                drawOval(
                    Brush.radialGradient(listOf(shadow, Color.Transparent), center = Offset(size.width / 2, size.height - 14.dp.toPx()), radius = w / 2),
                    topLeft = Offset((size.width - w) / 2, size.height - 14.dp.toPx() - h / 2),
                    size = Size(w, h),
                )
            }
            EquipmentVisual(scene, progress = progress, depth = true)
        }
        Row(Modifier.fillMaxWidth().padding(Space.lg), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                SectionLabel(if (isBodyweight) "All-Time Best (Added)" else "All-Time Best")
                Row(verticalAlignment = Alignment.Bottom) {
                    Text((if (isBodyweight) "+" else "") + formatWeight(readout), style = LmText.monoMetricMobile)
                    Spacer(Modifier.width(Space.xs))
                    Text(unit.label, style = LmText.titleMd, color = cs.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
            Text(
                "$reps reps",
                style = LmText.monoSm,
                color = cs.onPrimary,
                modifier = Modifier.clip(Radius.full).background(cs.primary).padding(horizontal = Space.md, vertical = Space.xs),
            )
        }
    }
}
