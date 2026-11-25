package com.hayderameen.mypr.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.hayderameen.mypr.domain.WeightUnit
import com.hayderameen.mypr.ui.rememberApp
import com.hayderameen.mypr.ui.components.LmCard
import com.hayderameen.mypr.ui.components.PrimaryButton
import com.hayderameen.mypr.ui.components.pressable
import com.hayderameen.mypr.ui.theme.LmText
import com.hayderameen.mypr.ui.theme.Motion
import com.hayderameen.mypr.ui.theme.Radius
import com.hayderameen.mypr.ui.theme.Space
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val app = rememberApp()
    val scope = rememberCoroutineScope()
    var unit by remember { mutableStateOf(WeightUnit.KG) }
    fun finish(u: WeightUnit) = scope.launch {
        app.settings.completeOnboarding(u)
        onDone()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = Space.page, vertical = Space.md), horizontalArrangement = Arrangement.End) {
            Text(
                "Skip",
                style = LmText.labelLg,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clip(Radius.full).pressable { finish(WeightUnit.KG) }.padding(Space.sm),
            )
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.page),
            verticalArrangement = Arrangement.spacedBy(Space.lg),
        ) {
            Spacer(Modifier.height(Space.xl))
            Text("Pick your weight unit", style = LmText.displayLgMobile)
            Text(
                "This sets your ruler steps and barbell plate breakdown. You can switch anytime.",
                style = LmText.bodyMd,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Space.sm))
            UnitCard("Kilograms (kg)", "20 kg bar • 2.5 kg ruler steps", unit == WeightUnit.KG) { unit = WeightUnit.KG }
            UnitCard("Pounds (lbs)", "45 lb bar • 5 lb ruler steps", unit == WeightUnit.LBS) { unit = WeightUnit.LBS }
            LmCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.FitnessCenter, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(Space.md))
                    Column(Modifier.weight(1f)) {
                        Text("Standard Barbell", style = LmText.titleMd)
                        Text(
                            if (unit == WeightUnit.KG) "20 kg Olympic bar" else "45 lb Standard bar",
                            style = LmText.bodyMd,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        if (unit == WeightUnit.KG) "IPF" else "US",
                        style = LmText.monoSm,
                        modifier = Modifier
                            .clip(Radius.full)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = Space.sm, vertical = 2.dp),
                    )
                }
            }
        }
        PrimaryButton(
            "Start Lifting",
            onClick = { finish(unit) },
            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
            modifier = Modifier.padding(Space.page),
        )
    }
}

@Composable
private fun UnitCard(title: String, detail: String, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (selected) cs.primary else cs.surfaceContainerLowest, tween(Motion.STATE_MS), label = "bg")
    val fg = if (selected) cs.onPrimary else cs.onSurface
    Box(
        Modifier
            .fillMaxWidth()
            .clip(Radius.xl)
            .background(bg)
            .then(if (selected) Modifier else Modifier.border(1.dp, cs.outlineVariant, Radius.xl))
            .pressable(role = Role.RadioButton, onClick = onClick)
            .padding(Space.xl),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = LmText.headlineMd, color = fg)
                Text(detail, style = LmText.monoSm, color = if (selected) fg.copy(alpha = 0.9f) else cs.onSurfaceVariant)
            }
            Icon(
                if (selected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                null,
                tint = if (selected) fg else cs.outline,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}
