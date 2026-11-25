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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.hayderameen.mypr.data.LiftRepository
import com.hayderameen.mypr.data.SettingsStore
import com.hayderameen.mypr.data.db.PrExerciseRow
import com.hayderameen.mypr.domain.Equipment
import com.hayderameen.mypr.domain.WeightUnit
import com.hayderameen.mypr.ui.appViewModel
import com.hayderameen.mypr.ui.components.BottomNavBar
import com.hayderameen.mypr.ui.components.BrandHeader
import com.hayderameen.mypr.ui.components.FilterPill
import com.hayderameen.mypr.ui.components.MainTab
import com.hayderameen.mypr.ui.components.pressable
import com.hayderameen.mypr.ui.theme.LmText
import com.hayderameen.mypr.ui.theme.Radius
import com.hayderameen.mypr.ui.theme.Space
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PrHistoryUi(val rows: List<PrExerciseRow> = emptyList(), val unit: WeightUnit = WeightUnit.KG, val loaded: Boolean = false)

class PrHistoryViewModel(repo: LiftRepository, private val settings: SettingsStore) : ViewModel() {
    val state = combine(repo.observePrExercises(), settings.unit) { rows, unit -> PrHistoryUi(rows, unit, true) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrHistoryUi())

    fun toggleUnit() = viewModelScope.launch { settings.setUnit(state.value.unit.other()) }
}

@Composable
fun PrHistoryScreen(onOpen: (exerciseId: String) -> Unit, onTab: (MainTab) -> Unit) {
    val vm = appViewModel { PrHistoryViewModel(it.repository, it.settings) }
    val ui by vm.state.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf<Equipment?>(null) }
    val cs = MaterialTheme.colorScheme
    val available = Equipment.entries.filter { eq -> ui.rows.any { it.exercise.equipment == eq } }
    val rows = ui.rows.filter { filter == null || it.exercise.equipment == filter }

    Scaffold(
        topBar = { BrandHeader(ui.unit, vm::toggleUnit) },
        bottomBar = { BottomNavBar(MainTab.HISTORY, onTab) },
        containerColor = cs.surface,
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Space.page, end = Space.page,
                top = padding.calculateTopPadding() + Space.xl,
                bottom = padding.calculateBottomPadding() + Space.xl,
            ),
            verticalArrangement = Arrangement.spacedBy(Space.md),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    Text("PR History", style = LmText.displayLgMobile)
                    Text("Pick a lift to reveal your PR and weight progress.", style = LmText.bodyMd, color = cs.onSurfaceVariant)
                }
            }
            if (available.size > 1) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                        item { FilterPill("All", filter == null, { filter = null }) }
                        items(available) { eq -> FilterPill(eq.label, filter == eq, { filter = eq }) }
                    }
                }
            }
            if (ui.loaded && ui.rows.isEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().padding(vertical = Space.section), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.EmojiEvents, null, tint = cs.outline, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(Space.md))
                        Text("No PRs yet", style = LmText.headlineMd)
                        Text(
                            "Log your first set and it shows up here.",
                            style = LmText.bodyMd,
                            color = cs.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            items(rows, key = { it.exercise.id }) { row ->
                Row(
                    Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .clip(Radius.lg)
                        .background(cs.surfaceContainerLowest)
                        .border(1.dp, cs.outlineVariant.copy(alpha = 0.6f), Radius.lg)
                        .pressable { onOpen(row.exercise.id) }
                        .padding(horizontal = Space.lg, vertical = Space.lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(row.exercise.name, style = LmText.titleLg, modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = cs.onSurfaceVariant)
                }
            }
        }
    }
}
