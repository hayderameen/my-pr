package com.hayderameen.mypr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.hayderameen.mypr.ui.components.DAY_ICONS
import com.hayderameen.mypr.ui.components.pressable
import com.hayderameen.mypr.ui.theme.LmText
import com.hayderameen.mypr.ui.theme.Radius
import com.hayderameen.mypr.ui.theme.Space

sealed interface DayDialogTarget {
    data object Create : DayDialogTarget
    data class Edit(val day: DayUi) : DayDialogTarget
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayDialog(
    target: DayDialogTarget,
    onDismiss: () -> Unit,
    onSave: (name: String, iconKey: String) -> Unit,
    onDelete: (dayId: String) -> Unit,
) {
    val editing = target as? DayDialogTarget.Edit
    var name by remember { mutableStateOf(editing?.day?.name ?: "") }
    var icon by remember { mutableStateOf(editing?.day?.iconKey ?: "barbell") }
    var confirmDelete by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme

    if (confirmDelete && editing != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete ${editing.day.name}?") },
            text = { Text("The day is removed from your rotation. Your logged sets and PRs are kept.") },
            confirmButton = { TextButton(onClick = { onDelete(editing.day.id) }) { Text("Delete", color = cs.error) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing == null) "New Day" else "Edit Day", style = LmText.headlineMd) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Space.lg)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 32) name = it },
                    label = { Text("Day name") },
                    placeholder = { Text("e.g. Glutes & Hams") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Icon", style = LmText.labelLg, color = cs.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.sm), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    DAY_ICONS.forEach { (key, vector) ->
                        val selected = key == icon
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(Radius.default)
                                .background(if (selected) cs.primary else cs.surfaceContainerLow)
                                .then(if (selected) Modifier else Modifier.border(1.dp, cs.outlineVariant, Radius.default))
                                .pressable(role = Role.RadioButton) { icon = key },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(vector, key, tint = if (selected) cs.onPrimary else cs.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, icon) }, enabled = name.isNotBlank()) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (editing != null) TextButton(onClick = { confirmDelete = true }) { Text("Delete", color = cs.error) }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}
