package com.hayderameen.mypr.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hayderameen.mypr.domain.Equipment
import com.hayderameen.mypr.domain.WeightUnit
import com.hayderameen.mypr.ui.theme.LmText
import com.hayderameen.mypr.ui.theme.Motion
import com.hayderameen.mypr.ui.theme.Radius
import com.hayderameen.mypr.ui.theme.Space

/** Clickable with the design system's 80 ms press-scale feedback (`active:scale-95`). */
@Composable
fun Modifier.pressable(enabled: Boolean = true, role: Role = Role.Button, onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, tween(Motion.TICK_MS), label = "press")
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interaction, LocalIndication.current, enabled = enabled, role = role, onClick = onClick)
}

@Composable
fun LmCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = Radius.xl,
    border: Boolean = true,
    padding: Dp = Space.lg,
    content: @Composable ColumnScope.() -> Unit,
) {
    val base = modifier
        .clip(shape)
        .let { if (onClick != null) it.pressable(onClick = onClick) else it }
    Surface(
        modifier = base,
        shape = shape,
        color = color,
        contentColor = contentColor,
        border = if (border) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)) else null,
    ) {
        Column(Modifier.padding(padding), content = content)
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    container: Color = MaterialTheme.colorScheme.primary,
    content: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(Radius.xl)
            .background(if (enabled) container else container.copy(alpha = 0.4f))
            .pressable(enabled = enabled, onClick = onClick)
            .padding(horizontal = Space.xl, vertical = Space.lg),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, null, tint = content, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(Space.sm))
        }
        Text(text, style = LmText.labelLg, color = content)
        if (trailingIcon != null) {
            Spacer(Modifier.width(Space.sm))
            Icon(trailingIcon, null, tint = content, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun SecondaryPill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, mono: Boolean = false) {
    Row(
        modifier = modifier
            .clip(Radius.full)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, Radius.full)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .pressable(onClick = onClick)
            .padding(horizontal = Space.md, vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(Space.xs))
        }
        Text(text, style = if (mono) LmText.monoSm else LmText.labelLg, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun IconCircleButton(icon: ImageVector, contentDescription: String?, onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.onSurface) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(Radius.full)
            .pressable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint)
    }
}

@Composable
fun FilterPill(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(Radius.full)
            .background(if (selected) cs.primary else cs.surfaceContainerLowest)
            .then(if (selected) Modifier else Modifier.border(1.dp, cs.outlineVariant, Radius.full))
            .pressable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = Space.md, vertical = Space.xs + 2.dp),
    ) {
        Text(text, style = LmText.labelLg, color = if (selected) cs.onPrimary else cs.onSurface)
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), style = LmText.labelSm, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = modifier)
}

@Composable
fun ExerciseIconTile(equipment: Equipment, modifier: Modifier = Modifier, size: Dp = 48.dp) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .size(size)
            .clip(Radius.default)
            .background(cs.surfaceContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(equipmentIcon(equipment), null, tint = cs.primary, modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
fun BrandMark() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(36.dp)
                .clip(Radius.default)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.FitnessCenter, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(Space.sm))
        Text("My PR", style = LmText.titleLg, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** Top bar used on the two main tabs: brand on the left, KG/LBS toggle on the right. */
@Composable
fun BrandHeader(unit: WeightUnit, onToggleUnit: () -> Unit) {
    Column(Modifier.background(MaterialTheme.colorScheme.surface).statusBarsPadding()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.page, vertical = Space.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BrandMark()
            SecondaryPill(unit.short, onToggleUnit, mono = true)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun BackHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    label: String? = null,
    trailing: @Composable RowScope.() -> Unit = { Spacer(Modifier.size(44.dp)) },
) {
    Column(modifier.background(MaterialTheme.colorScheme.surface).statusBarsPadding()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = Space.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconCircleButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack)
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                when {
                    title != null -> Text(title, style = LmText.titleLg, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    label != null -> SectionLabel(label)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

enum class MainTab { WORKOUTS, HISTORY }

@Composable
fun BottomNavBar(current: MainTab, onSelect: (MainTab) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.background(cs.surfaceContainerLowest)) {
        HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.5f))
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Space.page, vertical = Space.sm),
            horizontalArrangement = Arrangement.spacedBy(Space.sm),
        ) {
            NavItem("Workouts", Icons.Filled.FitnessCenter, Icons.Filled.FitnessCenter, current == MainTab.WORKOUTS) { onSelect(MainTab.WORKOUTS) }
            NavItem("PR History", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, current == MainTab.HISTORY) { onSelect(MainTab.HISTORY) }
        }
    }
}

@Composable
private fun RowScope.NavItem(label: String, selectedIcon: ImageVector, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val color = if (selected) cs.primary else cs.onSurfaceVariant
    Column(
        Modifier
            .weight(1f)
            .clip(Radius.md)
            .background(if (selected) cs.surfaceContainerLow else Color.Transparent)
            .pressable(role = Role.Tab, onClick = onClick)
            .padding(vertical = Space.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(if (selected) selectedIcon else icon, null, tint = color)
        Spacer(Modifier.height(2.dp))
        Text(label, style = LmText.labelSm, color = color)
    }
}
