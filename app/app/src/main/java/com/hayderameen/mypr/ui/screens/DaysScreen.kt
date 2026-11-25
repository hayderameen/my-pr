package com.hayderameen.mypr.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.hayderameen.mypr.data.LiftRepository
import com.hayderameen.mypr.data.SettingsStore
import com.hayderameen.mypr.domain.WeightUnit
import com.hayderameen.mypr.ui.appViewModel
import com.hayderameen.mypr.ui.components.BottomNavBar
import com.hayderameen.mypr.ui.components.BrandHeader
import com.hayderameen.mypr.ui.components.IconCircleButton
import com.hayderameen.mypr.ui.components.MainTab
import com.hayderameen.mypr.ui.components.SectionLabel
import com.hayderameen.mypr.ui.components.dayIcon
import com.hayderameen.mypr.ui.components.pressable
import com.hayderameen.mypr.ui.relativeDays
import com.hayderameen.mypr.ui.theme.LmText
import com.hayderameen.mypr.ui.theme.Radius
import com.hayderameen.mypr.ui.theme.Space
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ---------------------------------------------------------------- ViewModel

data class DayUi(
    val id: String,
    val name: String,
    val iconKey: String,
    val subtitle: String,
    val meta: String,
    val isActive: Boolean,
    val isPreset: Boolean,
)

data class ActiveUi(val dayId: String, val dayName: String)

data class DaysUi(val days: List<DayUi> = emptyList(), val active: ActiveUi? = null, val unit: WeightUnit = WeightUnit.KG, val loaded: Boolean = false)

class DaysViewModel(private val repo: LiftRepository, private val settings: SettingsStore) : ViewModel() {
    val state = combine(repo.observeDays(), repo.observeDayMuscles(), repo.observeActiveSession(), settings.unit) { days, muscles, active, unit ->
        val musclesByDay = muscles.groupBy({ it.dayId }, { it.muscleGroup })
        DaysUi(
            days = days.map { row ->
                val isActive = active?.session?.dayId == row.day.id
                // The day's two most-trained muscle groups (ties go to whichever comes first in the day).
                val dayMuscles = musclesByDay[row.day.id].orEmpty()
                val groups = dayMuscles.distinct()
                    .sortedByDescending { g -> dayMuscles.count { it == g } }
                    .take(2)
                    .map { it.label }
                DayUi(
                    id = row.day.id,
                    name = row.day.name,
                    iconKey = row.day.iconKey,
                    subtitle = if (groups.isEmpty()) "Add exercises" else groups.joinToString(", "),
                    meta = when {
                        isActive -> "Active"
                        row.lastTrainedAt != null -> relativeDays(row.lastTrainedAt)
                        else -> "${row.exerciseCount} lifts"
                    },
                    isActive = isActive,
                    isPreset = row.day.isPreset,
                )
            },
            active = active?.let { ActiveUi(it.session.dayId, it.dayName) },
            unit = unit,
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DaysUi())

    fun toggleUnit() = viewModelScope.launch { settings.setUnit(state.value.unit.other()) }
    suspend fun startDay(dayId: String) = repo.startDay(dayId)
    fun endWorkout() = viewModelScope.launch { repo.endActiveSession() }
    fun move(dayId: String, delta: Int) = viewModelScope.launch {
        val ids = state.value.days.map { it.id }.toMutableList()
        val i = ids.indexOf(dayId)
        val j = i + delta
        if (i < 0 || j !in ids.indices) return@launch
        ids.add(j, ids.removeAt(i))
        repo.reorderDays(ids)
    }
    fun createDay(name: String, icon: String) = viewModelScope.launch { repo.createDay(name, icon) }
    fun updateDay(id: String, name: String, icon: String) = viewModelScope.launch { repo.updateDay(id, name, icon) }
    fun deleteDay(id: String) = viewModelScope.launch { repo.deleteDay(id) }
}

// ---------------------------------------------------------------- Screen

@Composable
fun DaysScreen(onOpenDay: (String) -> Unit, onTab: (MainTab) -> Unit) {
    val vm = appViewModel { DaysViewModel(it.repository, it.settings) }
    val ui by vm.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var editing by remember { mutableStateOf(false) }
    var switchTo by remember { mutableStateOf<DayUi?>(null) }
    var dayDialog by remember { mutableStateOf<DayDialogTarget?>(null) }

    fun open(day: DayUi) {
        val active = ui.active
        if (active != null && active.dayId != day.id) {
            switchTo = day
        } else {
            scope.launch { vm.startDay(day.id); onOpenDay(day.id) }
        }
    }

    Scaffold(
        topBar = { BrandHeader(ui.unit, vm::toggleUnit) },
        bottomBar = { BottomNavBar(MainTab.WORKOUTS, onTab) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Space.page, end = Space.page,
                top = padding.calculateTopPadding() + Space.xl,
                bottom = padding.calculateBottomPadding() + Space.xl,
            ),
            horizontalArrangement = Arrangement.spacedBy(Space.md),
            verticalArrangement = Arrangement.spacedBy(Space.md),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    Text("Choose your workout", style = LmText.displayLgMobile)
                    Text(
                        "Tap a training day to start logging sets. It stays active until you end it.",
                        style = LmText.bodyMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            ui.active?.let { active ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ActiveBanner(active, onOpen = { onOpenDay(active.dayId) }, onEnd = vm::endWorkout)
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    Modifier.fillMaxWidth().padding(top = Space.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    SectionLabel("Your Rotation")
                    Row(
                        Modifier
                            .clip(Radius.full)
                            .pressable { editing = !editing }
                            .padding(horizontal = Space.sm, vertical = Space.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(if (editing) Icons.Filled.Check else Icons.Filled.SwapVert, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(Space.xs))
                        Text(if (editing) "Done" else "Reorder", style = LmText.labelSm, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            items(ui.days, key = { it.id }) { day ->
                Column(Modifier.animateItem()) {
                    DayCard(day, onClick = { if (editing) dayDialog = DayDialogTarget.Edit(day) else open(day) })
                    if (editing) {
                        Row(Modifier.fillMaxWidth().padding(top = Space.xs), horizontalArrangement = Arrangement.SpaceBetween) {
                            IconCircleButton(Icons.AutoMirrored.Filled.ArrowBack, "Move ${day.name} earlier", { vm.move(day.id, -1) })
                            IconCircleButton(Icons.Filled.Edit, "Edit ${day.name}", { dayDialog = DayDialogTarget.Edit(day) })
                            IconCircleButton(Icons.AutoMirrored.Filled.ArrowForward, "Move ${day.name} later", { vm.move(day.id, 1) })
                        }
                    }
                }
            }
            if (ui.loaded) {
                item(key = "new-day") {
                    NewDayCard(Modifier.animateItem()) { dayDialog = DayDialogTarget.Create }
                }
            }
        }
    }

    switchTo?.let { target ->
        AlertDialog(
            onDismissRequest = { switchTo = null },
            title = { Text("${ui.active?.dayName} is still active") },
            text = { Text("End it and start ${target.name}? Sets you've logged are kept.") },
            confirmButton = {
                TextButton(onClick = {
                    switchTo = null
                    scope.launch { vm.startDay(target.id); onOpenDay(target.id) }
                }) { Text("End & Start") }
            },
            dismissButton = { TextButton(onClick = { switchTo = null }) { Text("Cancel") } },
        )
    }

    dayDialog?.let { target ->
        DayDialog(
            target = target,
            onDismiss = { dayDialog = null },
            onSave = { name, icon ->
                when (target) {
                    DayDialogTarget.Create -> vm.createDay(name, icon)
                    is DayDialogTarget.Edit -> vm.updateDay(target.day.id, name, icon)
                }
                dayDialog = null
            },
            onDelete = { id -> vm.deleteDay(id); dayDialog = null },
        )
    }
}

@Composable
private fun ActiveBanner(active: ActiveUi, onOpen: () -> Unit, onEnd: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "dot",
    )
    Row(
        Modifier
            .fillMaxWidth()
            .clip(Radius.xl)
            .background(cs.inverseSurface)
            .padding(start = Space.lg, end = Space.sm, top = Space.md, bottom = Space.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).clip(Radius.md).pressable(onClick = onOpen).padding(Space.xs)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(cs.primaryContainer.copy(alpha = pulse)))
                Spacer(Modifier.width(Space.sm))
                Text("IN PROGRESS", style = LmText.labelSm, color = cs.inverseOnSurface.copy(alpha = 0.8f))
            }
            Text("${active.dayName} Active", style = LmText.titleLg, color = cs.inverseOnSurface)
        }
        Text(
            "End",
            style = LmText.labelLg,
            color = cs.onPrimary,
            modifier = Modifier
                .clip(Radius.full)
                .background(cs.primary)
                .pressable(onClick = onEnd)
                .padding(horizontal = Space.lg, vertical = Space.sm),
        )
    }
}

@Composable
private fun DayCard(day: DayUi, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val bg = if (day.isActive) cs.primary else cs.surfaceContainerLowest
    val fg = if (day.isActive) cs.onPrimary else cs.onSurface
    val sub = if (day.isActive) cs.onPrimary.copy(alpha = 0.85f) else cs.onSurfaceVariant
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 148.dp)
            .clip(Radius.xl)
            .background(bg)
            .then(if (day.isActive) Modifier else Modifier.border(1.dp, cs.outlineVariant.copy(alpha = 0.6f), Radius.xl))
            .pressable(onClick = onClick)
            .padding(Space.lg),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Icon(if (day.isActive) dayIcon("bolt") else dayIcon(day.iconKey), null, tint = if (day.isActive) fg else cs.primary)
            Text(day.meta, style = if (day.isActive) LmText.labelSm else LmText.monoSm, color = sub)
        }
        Spacer(Modifier.height(Space.xl))
        Column {
            Text(day.name, style = LmText.headlineMd, color = fg, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(day.subtitle, style = LmText.bodyMd, color = sub, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun NewDayCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val outline = cs.outline
    Column(
        modifier
            .fillMaxWidth()
            .heightIn(min = 148.dp)
            .clip(Radius.xl)
            .drawBehind {
                drawRoundRect(
                    outline,
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))),
                )
            }
            .pressable(onClick = onClick)
            .padding(Space.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(cs.surfaceContainer), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Add, null, tint = cs.primary)
        }
        Spacer(Modifier.height(Space.sm))
        Text("New Day", style = LmText.titleMd, color = Color.Unspecified)
    }
}
