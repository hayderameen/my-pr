package com.hayderameen.mypr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.hayderameen.mypr.data.LiftRepository
import com.hayderameen.mypr.data.db.ExerciseEntity
import com.hayderameen.mypr.domain.Equipment
import com.hayderameen.mypr.domain.MuscleGroup
import com.hayderameen.mypr.ui.appViewModel
import com.hayderameen.mypr.ui.components.BackHeader
import com.hayderameen.mypr.ui.components.ExerciseIconTile
import com.hayderameen.mypr.ui.components.FilterPill
import com.hayderameen.mypr.ui.components.PrimaryButton
import com.hayderameen.mypr.ui.components.pressable
import com.hayderameen.mypr.ui.theme.LmText
import com.hayderameen.mypr.ui.theme.Radius
import com.hayderameen.mypr.ui.theme.Space
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PickerUi(
    val dayName: String = "",
    val catalog: List<ExerciseEntity> = emptyList(),
    val inDay: Set<String> = emptySet(),
)

class ExercisePickerViewModel(private val repo: LiftRepository, private val dayId: String) : ViewModel() {
    val state = combine(repo.observeDay(dayId), repo.observeCatalog(), repo.observeDayExercises(dayId)) { day, catalog, inDay ->
        PickerUi(day?.name ?: "", catalog, inDay.map { it.exercise.id }.toSet())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PickerUi())

    fun toggle(exerciseId: String) = viewModelScope.launch {
        if (exerciseId in state.value.inDay) repo.removeExerciseFromDay(dayId, exerciseId)
        else repo.addExerciseToDay(dayId, exerciseId)
    }

    fun createCustom(name: String, equipment: Equipment, group: MuscleGroup) = viewModelScope.launch {
        val id = repo.createCustomExercise(name, equipment, group)
        repo.addExerciseToDay(dayId, id)
    }
}

@Composable
fun ExercisePickerScreen(dayId: String, onBack: () -> Unit) {
    val vm = appViewModel(key = "picker-$dayId") { ExercisePickerViewModel(it.repository, dayId) }
    val ui by vm.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableStateOf<MuscleGroup?>(null) }
    var creating by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme

    val filtered = remember(ui.catalog, query, group) {
        val q = query.trim()
        ui.catalog.filter { e ->
            (group == null || e.muscleGroup == group) &&
                (q.isEmpty() || e.name.contains(q, ignoreCase = true) || e.target.contains(q, ignoreCase = true) || e.equipment.label.contains(q, ignoreCase = true))
        }.groupBy { it.muscleGroup }.toSortedMap(compareBy { it.ordinal })
    }

    Column(Modifier.fillMaxSize().background(cs.surface)) {
        BackHeader(onBack = onBack, title = "Add to ${ui.dayName}") {
            Text("${ui.inDay.size} lifts", style = LmText.monoSm, color = cs.onSurfaceVariant, modifier = Modifier.padding(end = Space.md))
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search exercises") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, "Clear search") } },
            singleLine = true,
            shape = Radius.full,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = cs.surfaceContainerLowest,
                focusedContainerColor = cs.surfaceContainerLowest,
                unfocusedBorderColor = cs.outlineVariant,
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = Space.page, vertical = Space.md),
        )
        LazyRow(contentPadding = PaddingValues(horizontal = Space.page), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            item { FilterPill("All", group == null, { group = null }) }
            items(MuscleGroup.entries) { g -> FilterPill(g.label, group == g, { group = if (group == g) null else g }) }
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(Space.page),
            verticalArrangement = Arrangement.spacedBy(Space.sm),
        ) {
            filtered.forEach { (g, list) ->
                item(key = "h-${g.name}") {
                    Row(Modifier.fillMaxWidth().padding(top = Space.md, bottom = Space.xs), verticalAlignment = Alignment.Bottom) {
                        Text(g.label, style = LmText.headlineMd, modifier = Modifier.weight(1f))
                        Text("${list.size} lifts", style = LmText.monoSm, color = cs.onSurfaceVariant)
                    }
                }
                items(list, key = { it.id }) { e ->
                    CatalogRow(e, added = e.id in ui.inDay, onClick = { vm.toggle(e.id) })
                }
            }
            if (filtered.isEmpty()) {
                item {
                    Text(
                        "No match for \"$query\". Create it as a custom exercise below.",
                        style = LmText.bodyMd,
                        color = cs.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = Space.xl),
                    )
                }
            }
        }
        PrimaryButton(
            "Create Custom Exercise",
            onClick = { creating = true },
            leadingIcon = Icons.Filled.Edit,
            container = cs.inverseSurface,
            content = cs.inverseOnSurface,
            modifier = Modifier.navigationBarsPadding().padding(Space.page),
        )
    }

    if (creating) {
        CustomExerciseDialog(
            initialName = query.trim(),
            onDismiss = { creating = false },
            onCreate = { name, eq, g -> vm.createCustom(name, eq, g); creating = false },
        )
    }
}

@Composable
private fun CatalogRow(e: ExerciseEntity, added: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clip(Radius.lg)
            .background(cs.surfaceContainerLowest)
            .border(1.dp, if (added) cs.primary.copy(alpha = 0.6f) else cs.outlineVariant.copy(alpha = 0.6f), Radius.lg)
            .pressable(role = Role.Checkbox, onClick = onClick)
            .padding(Space.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ExerciseIconTile(e.equipment, size = 44.dp)
        Spacer(Modifier.width(Space.md))
        Column(Modifier.weight(1f)) {
            Text(e.name, style = LmText.titleMd, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(e.target, style = LmText.bodyMd, color = cs.onSurfaceVariant, maxLines = 1)
        }
        Text(e.equipment.label, style = LmText.monoSm, color = cs.onSurfaceVariant, modifier = Modifier.padding(horizontal = Space.sm))
        Icon(
            if (added) Icons.Filled.CheckCircle else Icons.Filled.AddCircleOutline,
            if (added) "Added" else "Add",
            tint = if (added) cs.primary else cs.outline,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomExerciseDialog(initialName: String, onDismiss: () -> Unit, onCreate: (String, Equipment, MuscleGroup) -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    var equipment by remember { mutableStateOf(Equipment.BARBELL) }
    var group by remember { mutableStateOf(MuscleGroup.CHEST) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Exercise", style = LmText.headlineMd) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Space.md)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 48) name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Equipment", style = LmText.labelLg)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.sm), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    Equipment.entries.forEach { FilterPill(it.label, equipment == it, { equipment = it }) }
                }
                Text("Muscle group", style = LmText.labelLg)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.sm), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    MuscleGroup.entries.forEach { FilterPill(it.label, group == it, { group = it }) }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onCreate(name, equipment, group) }, enabled = name.isNotBlank()) { Text("Create & Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
