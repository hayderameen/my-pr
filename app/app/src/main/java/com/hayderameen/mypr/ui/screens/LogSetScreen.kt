package com.hayderameen.mypr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.hayderameen.mypr.data.LiftRepository
import com.hayderameen.mypr.data.SettingsStore
import com.hayderameen.mypr.data.db.ExerciseEntity
import com.hayderameen.mypr.domain.Equipment
import com.hayderameen.mypr.domain.RepsRuler
import com.hayderameen.mypr.domain.RulerSpec
import com.hayderameen.mypr.domain.WeightUnit
import com.hayderameen.mypr.domain.defaultWeight
import com.hayderameen.mypr.domain.displayWeight
import com.hayderameen.mypr.domain.dragResistance
import com.hayderameen.mypr.domain.formatWeight
import com.hayderameen.mypr.domain.fromKg
import com.hayderameen.mypr.domain.roundTo
import com.hayderameen.mypr.domain.toKg
import com.hayderameen.mypr.domain.weightRuler
import com.hayderameen.mypr.ui.appViewModel
import com.hayderameen.mypr.ui.components.BackHeader
import com.hayderameen.mypr.ui.components.EquipmentVisual
import com.hayderameen.mypr.ui.components.ExerciseIconTile
import com.hayderameen.mypr.ui.components.IconCircleButton
import com.hayderameen.mypr.ui.components.LmCard
import com.hayderameen.mypr.ui.components.PrCelebration
import com.hayderameen.mypr.ui.components.PrimaryButton
import com.hayderameen.mypr.ui.components.RulerSlider
import com.hayderameen.mypr.ui.components.equipmentScene
import com.hayderameen.mypr.ui.components.pressable
import com.hayderameen.mypr.ui.theme.LmText
import com.hayderameen.mypr.ui.theme.Radius
import com.hayderameen.mypr.ui.theme.Space
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SetChip(val id: String, val label: String)

data class LogSetUi(
    val exercise: ExerciseEntity? = null,
    val dayName: String = "",
    val unit: WeightUnit = WeightUnit.KG,
    val best: String? = null,
    val sessionSets: List<SetChip> = emptyList(),
)

data class Celebration(val newWeight: Double, val previousWeight: Double, val reps: Int)

class LogSetViewModel(
    private val repo: LiftRepository,
    private val settings: SettingsStore,
    private val dayId: String,
    private val exerciseId: String,
) : ViewModel() {

    val state = combine(
        repo.observeExercise(exerciseId),
        repo.observeDay(dayId),
        settings.unit,
        repo.observeExerciseSets(exerciseId),
        repo.observeActiveSession(),
    ) { exercise, day, unit, sets, active ->
        val best = sets.maxWithOrNull(compareBy({ it.weightKg }, { it.reps }))
        val sessionId = active?.session?.takeIf { it.dayId == dayId }?.id
        val prefix = if (exercise?.equipment == Equipment.BODYWEIGHT) "+" else ""
        LogSetUi(
            exercise = exercise,
            dayName = day?.name ?: "",
            unit = unit,
            best = best?.let { "Best $prefix${formatWeight(displayWeight(it.weightKg, it.enteredWeight, it.enteredUnit, unit))} ${unit.label} × ${it.reps}" },
            sessionSets = sets.filter { it.sessionId == sessionId }.map {
                SetChip(it.id, "$prefix${formatWeight(displayWeight(it.weightKg, it.enteredWeight, it.enteredUnit, unit))} × ${it.reps}")
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LogSetUi())

    var weight by mutableDoubleStateOf(0.0)
        private set
    var reps by mutableIntStateOf(8)
    var ready by mutableStateOf(false)
        private set
    var saving by mutableStateOf(false)
        private set
    var celebration by mutableStateOf<Celebration?>(null)
        private set

    private var weightUnit = WeightUnit.KG

    init {
        viewModelScope.launch {
            val exercise = repo.observeExercise(exerciseId).first { it != null }!!
            val unit = settings.unit.first()
            weightUnit = unit
            val last = repo.lastSet(exerciseId)
            weight = last?.let { displayWeight(it.weightKg, it.enteredWeight, it.enteredUnit, unit) } ?: defaultWeight(exercise.equipment, unit)
            weight = clampToSpec(weight, exercise.equipment, unit)
            reps = last?.reps ?: 8
            ready = true
            // Keep the dialled weight equivalent when the unit is switched elsewhere.
            settings.unit.collect { newUnit ->
                if (newUnit != weightUnit) {
                    weight = clampToSpec(roundTo(fromKg(toKg(weight, weightUnit), newUnit), 0.1), exercise.equipment, newUnit)
                    weightUnit = newUnit
                }
            }
        }
    }

    private fun clampToSpec(w: Double, eq: Equipment, unit: WeightUnit): Double {
        val spec = weightRuler(eq, unit)
        return w.coerceIn(spec.min, spec.max)
    }

    fun updateWeight(w: Double) {
        weight = w
    }

    /** Returns true when the screen should close straight away (no celebration). */
    suspend fun confirm(): Boolean {
        if (saving) return false
        saving = true
        try {
            val unit = weightUnit
            val result = repo.logSet(dayId, exerciseId, weight, unit, reps)
            if (result.isNewPr && result.previousBestKg != null) {
                celebration = Celebration(weight, roundTo(fromKg(result.previousBestKg, unit), 0.1), reps)
                return false
            }
            return true
        } finally {
            saving = false
        }
    }

    fun deleteSet(id: String) = viewModelScope.launch { repo.deleteSet(id) }
}

@Composable
fun LogSetScreen(dayId: String, exerciseId: String, onBack: () -> Unit, onDone: () -> Unit, onPlates: () -> Unit) {
    val vm = appViewModel(key = "log-$dayId-$exerciseId") { LogSetViewModel(it.repository, it.settings, dayId, exerciseId) }
    val ui by vm.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var editWeight by remember { mutableStateOf(false) }
    var editReps by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Pair<Int, SetChip>?>(null) }
    val exercise = ui.exercise
    val cs = MaterialTheme.colorScheme

    Box(Modifier.fillMaxSize().background(cs.surface)) {
        Column(Modifier.fillMaxSize()) {
            BackHeader(onBack = onBack, label = ui.dayName) {
                if (exercise?.equipment == Equipment.BARBELL || exercise?.equipment == Equipment.BODYWEIGHT) {
                    IconCircleButton(Icons.Outlined.Info, "Plate reference", onPlates, tint = cs.onSurfaceVariant)
                } else {
                    Spacer(Modifier.width(44.dp))
                }
            }
            if (exercise != null && vm.ready) {
                val unit = ui.unit
                val spec = weightRuler(exercise.equipment, unit)
                val isBodyweight = exercise.equipment == Equipment.BODYWEIGHT
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Space.page),
                    verticalArrangement = Arrangement.spacedBy(Space.lg),
                ) {
                    Spacer(Modifier.height(Space.xs))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ExerciseIconTile(exercise.equipment, size = 64.dp)
                        Spacer(Modifier.width(Space.md))
                        Column(Modifier.weight(1f)) {
                            Text(exercise.name, style = LmText.headlineLgMobile)
                            Text(ui.best ?: "First time — set your baseline", style = LmText.monoSm, color = cs.onSurfaceVariant)
                        }
                    }
                    if (ui.sessionSets.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.sm), contentPadding = PaddingValues(end = Space.page)) {
                            itemsIndexed(ui.sessionSets, key = { _, s -> s.id }) { i, chip ->
                                Text(
                                    "Set ${i + 1} · ${chip.label}",
                                    style = LmText.monoSm,
                                    modifier = Modifier
                                        .clip(Radius.full)
                                        .background(cs.surfaceContainer)
                                        .pressable { deleting = i to chip }
                                        .padding(horizontal = Space.md, vertical = Space.xs + 2.dp),
                                )
                            }
                        }
                    }

                    LmCard(Modifier.fillMaxWidth()) {
                        ValueHeader(
                            label = if (isBodyweight) "Added Weight" else if (exercise.equipment == Equipment.DUMBBELL) "Weight per Dumbbell" else "Weight",
                            value = (if (isBodyweight) "+" else "") + formatWeight(vm.weight),
                            unit = unit.label,
                            onEdit = { editWeight = true },
                        )
                        Spacer(Modifier.height(Space.md))
                        EquipmentVisual(equipmentScene(exercise.equipment, vm.weight, unit), Modifier.padding(vertical = Space.sm))
                        Spacer(Modifier.height(Space.md))
                        RulerSlider(
                            value = vm.weight,
                            onValueChange = vm::updateWeight,
                            spec = spec,
                            contentDescription = "Weight in ${unit.label}",
                            resistance = { w -> dragResistance(spec, toKg(w, unit)) },
                        )
                    }

                    LmCard(Modifier.fillMaxWidth()) {
                        ValueHeader("Reps", vm.reps.toString(), "reps", onEdit = { editReps = true })
                        Spacer(Modifier.height(Space.md))
                        RulerSlider(
                            value = vm.reps.toDouble(),
                            onValueChange = { vm.reps = it.toInt() },
                            spec = RepsRuler,
                            contentDescription = "Reps",
                            stepWidth = 26.dp,
                        )
                    }
                    Spacer(Modifier.height(Space.sm))
                }
                PrimaryButton(
                    "Confirm Set",
                    onClick = { scope.launch { if (vm.confirm()) onDone() } },
                    leadingIcon = Icons.Filled.Check,
                    enabled = !vm.saving,
                    modifier = Modifier.navigationBarsPadding().padding(Space.page),
                )
            }
        }

        vm.celebration?.let { c ->
            PrCelebration(
                exerciseName = exercise?.name ?: "",
                newWeight = c.newWeight,
                previousWeight = c.previousWeight,
                unitLabel = ui.unit.label,
                reps = c.reps,
                onDone = onDone,
            )
        }
    }

    if (editWeight && exercise != null) {
        val spec = weightRuler(exercise.equipment, ui.unit)
        NumberDialog(
            title = "Weight (${ui.unit.label})",
            initial = formatWeight(vm.weight),
            spec = spec,
            decimals = true,
            onDismiss = { editWeight = false },
            onConfirm = { vm.updateWeight(it); editWeight = false },
        )
    }
    if (editReps) {
        NumberDialog(
            title = "Reps",
            initial = vm.reps.toString(),
            spec = RepsRuler,
            decimals = false,
            onDismiss = { editReps = false },
            onConfirm = { vm.reps = it.toInt(); editReps = false },
        )
    }
    deleting?.let { (index, chip) ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete set ${index + 1}?") },
            text = { Text("${chip.label} will be removed from this workout and your history.") },
            confirmButton = { TextButton(onClick = { vm.deleteSet(chip.id); deleting = null }) { Text("Delete", color = cs.error) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ValueHeader(label: String, value: String, unit: String, onEdit: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = LmText.labelLg, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
        Row(
            Modifier
                .clip(Radius.default)
                .border(1.dp, cs.outlineVariant.copy(alpha = 0.0f), Radius.default)
                .pressable(onClick = onEdit)
                .padding(horizontal = Space.xs),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(value, style = LmText.monoMetricMobile, color = cs.onSurface)
            Spacer(Modifier.width(Space.xs))
            Text(unit, style = LmText.titleMd, color = cs.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}

@Composable
private fun NumberDialog(
    title: String,
    initial: String,
    spec: RulerSpec,
    decimals: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    val parsed = text.replace(',', '.').toDoubleOrNull()
    val valid = parsed != null && parsed >= spec.min && parsed <= spec.max && (decimals || parsed % 1.0 == 0.0)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 7) text = it },
                singleLine = true,
                isError = !valid,
                supportingText = { Text("${formatRange(spec.min, decimals)} – ${formatRange(spec.max, decimals)}") },
                keyboardOptions = KeyboardOptions(keyboardType = if (decimals) KeyboardType.Decimal else KeyboardType.Number),
                textStyle = LmText.monoMetricMobile,
            )
        },
        confirmButton = { TextButton(onClick = { parsed?.let { onConfirm(roundTo(it, 0.05)) } }, enabled = valid) { Text("Set") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun formatRange(v: Double, decimals: Boolean) = if (decimals) formatWeight(v) else v.toInt().toString()
