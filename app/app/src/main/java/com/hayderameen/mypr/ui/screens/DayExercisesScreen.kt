package com.hayderameen.mypr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.hayderameen.mypr.data.LiftRepository
import com.hayderameen.mypr.domain.Equipment
import com.hayderameen.mypr.ui.appViewModel
import com.hayderameen.mypr.ui.components.BackHeader
import com.hayderameen.mypr.ui.components.ExerciseIconTile
import com.hayderameen.mypr.ui.components.IconCircleButton
import com.hayderameen.mypr.ui.components.PrimaryButton
import com.hayderameen.mypr.ui.components.SecondaryPill
import com.hayderameen.mypr.ui.components.pressable
import com.hayderameen.mypr.ui.theme.LmText
import com.hayderameen.mypr.ui.theme.Radius
import com.hayderameen.mypr.ui.theme.Space
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DayExerciseUi(val id: String, val name: String, val equipment: Equipment, val target: String, val setsLogged: Int)

data class DayExercisesUi(
    val dayName: String = "",
    val items: List<DayExerciseUi> = emptyList(),
    val sessionActive: Boolean = false,
    val loaded: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class DayExercisesViewModel(private val repo: LiftRepository, val dayId: String) : ViewModel() {
    private val sessionCounts = repo.observeActiveSession().flatMapLatest { active ->
        if (active == null || active.session.dayId != dayId) flowOf(null)
        else repo.observeSessionSets(active.session.id).map { sets -> sets.groupingBy { it.exerciseId }.eachCount() }
    }

    val state = combine(repo.observeDay(dayId), repo.observeDayExercises(dayId), sessionCounts) { day, rows, counts ->
        DayExercisesUi(
            dayName = day?.name ?: "",
            items = rows.map { r ->
                DayExerciseUi(r.exercise.id, r.exercise.name, r.exercise.equipment, r.exercise.target, counts?.get(r.exercise.id) ?: 0)
            },
            sessionActive = counts != null,
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DayExercisesUi())

    fun move(exerciseId: String, delta: Int) = viewModelScope.launch {
        val ids = state.value.items.map { it.id }.toMutableList()
        val i = ids.indexOf(exerciseId)
        val j = i + delta
        if (i < 0 || j !in ids.indices) return@launch
        ids.add(j, ids.removeAt(i))
        repo.reorderDayExercises(dayId, ids)
    }

    fun remove(exerciseId: String) = viewModelScope.launch { repo.removeExerciseFromDay(dayId, exerciseId) }
    suspend fun endWorkout() = repo.endActiveSession()
}

@Composable
fun DayExercisesScreen(
    dayId: String,
    onBack: () -> Unit,
    onLog: (exerciseId: String) -> Unit,
    onAddExercises: () -> Unit,
    onWorkoutEnded: () -> Unit,
) {
    val vm = appViewModel(key = "day-$dayId") { DayExercisesViewModel(it.repository, dayId) }
    val ui by vm.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var editing by remember { mutableStateOf(false) }
    var removing by remember { mutableStateOf<DayExerciseUi?>(null) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        BackHeader(onBack = onBack, title = ui.dayName) {
            if (ui.sessionActive) {
                SecondaryPill("End Workout", { scope.launch { vm.endWorkout(); onWorkoutEnded() } }, Modifier.padding(end = Space.sm))
            }
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(Space.page),
            verticalArrangement = Arrangement.spacedBy(Space.md),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        if (editing) "Reorder or remove lifts" else "Sorted by how you train ${ui.dayName}",
                        style = LmText.bodyMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (ui.items.isNotEmpty()) {
                        Row(
                            Modifier.clip(Radius.full).pressable { editing = !editing }.padding(horizontal = Space.sm, vertical = Space.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(if (editing) Icons.Filled.Check else Icons.Filled.SwapVert, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(Space.xs))
                            Text(if (editing) "Done" else "Reorder", style = LmText.labelSm, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            if (ui.loaded && ui.items.isEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().padding(vertical = Space.section), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No lifts yet", style = LmText.headlineMd)
                        Spacer(Modifier.height(Space.xs))
                        Text(
                            "Add the exercises you do on ${ui.dayName}. They'll sort themselves into the order you train.",
                            style = LmText.bodyMd,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            itemsIndexed(ui.items, key = { _, it -> it.id }) { index, item ->
                ExerciseRow(
                    index = index,
                    item = item,
                    editing = editing,
                    isFirst = index == 0,
                    isLast = index == ui.items.lastIndex,
                    onClick = { if (!editing) onLog(item.id) },
                    onMove = { vm.move(item.id, it) },
                    onRemove = { removing = item },
                    modifier = Modifier.animateItem(),
                )
            }
        }
        PrimaryButton(
            "Add Exercise",
            onClick = onAddExercises,
            leadingIcon = Icons.Filled.Add,
            container = MaterialTheme.colorScheme.inverseSurface,
            content = MaterialTheme.colorScheme.inverseOnSurface,
            modifier = Modifier.navigationBarsPadding().padding(Space.page),
        )
    }

    removing?.let { item ->
        AlertDialog(
            onDismissRequest = { removing = null },
            title = { Text("Remove ${item.name}?") },
            text = { Text("It's taken off ${ui.dayName}. Its PR history stays.") },
            confirmButton = { TextButton(onClick = { vm.remove(item.id); removing = null }) { Text("Remove") } },
            dismissButton = { TextButton(onClick = { removing = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ExerciseRow(
    index: Int,
    item: DayExerciseUi,
    editing: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onMove: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val logged = item.setsLogged > 0
    Row(
        modifier
            .fillMaxWidth()
            .clip(Radius.lg)
            .background(cs.surfaceContainerLowest)
            .border(1.dp, if (logged) cs.primary.copy(alpha = 0.5f) else cs.outlineVariant.copy(alpha = 0.6f), Radius.lg)
            .pressable(enabled = !editing, onClick = onClick)
            .padding(horizontal = Space.md, vertical = Space.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("${index + 1}", style = LmText.monoSm, color = cs.onSurfaceVariant, modifier = Modifier.widthIn(min = 20.dp))
        Spacer(Modifier.width(Space.sm))
        ExerciseIconTile(item.equipment)
        Spacer(Modifier.width(Space.md))
        Column(Modifier.weight(1f)) {
            Text(item.name, style = LmText.titleMd, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                if (logged) "${item.target} • Logged ${item.setsLogged} ${if (item.setsLogged == 1) "set" else "sets"}"
                else "${item.target} • ${item.equipment.label}",
                style = LmText.bodyMd,
                color = if (logged) cs.primary else cs.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (editing) {
            if (!isFirst) IconCircleButton(Icons.Filled.ArrowUpward, "Move ${item.name} up", { onMove(-1) })
            if (!isLast) IconCircleButton(Icons.Filled.ArrowDownward, "Move ${item.name} down", { onMove(1) })
            IconCircleButton(Icons.Filled.RemoveCircleOutline, "Remove ${item.name}", onRemove, tint = cs.error)
        } else if (logged) {
            Icon(Icons.Filled.CheckCircle, null, tint = cs.primary)
        } else {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = cs.onSurfaceVariant)
        }
    }
}
