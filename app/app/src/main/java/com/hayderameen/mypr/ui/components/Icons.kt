package com.hayderameen.mypr.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Rowing
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.hayderameen.mypr.domain.Equipment

/** A single dumbbell (Material Symbols "exercise" has no Compose equivalent). */
val DumbbellIcon: ImageVector by lazy {
    ImageVector.Builder("Dumbbell", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color.Black)) {
            // handle
            moveTo(8f, 11f); lineTo(16f, 11f); lineTo(16f, 13f); lineTo(8f, 13f); close()
            // inner plates
            moveTo(5.5f, 6.5f); lineTo(8f, 6.5f); lineTo(8f, 17.5f); lineTo(5.5f, 17.5f); close()
            moveTo(16f, 6.5f); lineTo(18.5f, 6.5f); lineTo(18.5f, 17.5f); lineTo(16f, 17.5f); close()
            // outer plates
            moveTo(3f, 8.5f); lineTo(5f, 8.5f); lineTo(5f, 15.5f); lineTo(3f, 15.5f); close()
            moveTo(19f, 8.5f); lineTo(21f, 8.5f); lineTo(21f, 15.5f); lineTo(19f, 15.5f); close()
        }
    }.build()
}

fun equipmentIcon(equipment: Equipment): ImageVector = when (equipment) {
    Equipment.BARBELL -> Icons.Filled.FitnessCenter
    Equipment.DUMBBELL -> DumbbellIcon
    Equipment.CABLE -> Icons.Filled.Cable
    Equipment.MACHINE -> Icons.Filled.PrecisionManufacturing
    Equipment.BODYWEIGHT -> Icons.Filled.SportsGymnastics
}

/** Icons a workout day can use. Keys are stored in the database — don't rename them. */
val DAY_ICONS: List<Pair<String, ImageVector>> = listOf(
    "bolt" to Icons.Filled.Bolt,
    "barbell" to Icons.Filled.FitnessCenter,
    "rowing" to Icons.Filled.Rowing,
    "run" to Icons.AutoMirrored.Filled.DirectionsRun,
    "walk" to Icons.AutoMirrored.Filled.DirectionsWalk,
    "martial" to Icons.Filled.SportsMartialArts,
    "gymnastics" to Icons.Filled.SportsGymnastics,
    "accessibility" to Icons.Filled.AccessibilityNew,
    "core" to Icons.Filled.SelfImprovement,
    "full" to Icons.Filled.Accessibility,
    "dumbbell" to DumbbellIcon,
)

fun dayIcon(key: String): ImageVector = DAY_ICONS.firstOrNull { it.first == key }?.second ?: Icons.Filled.FitnessCenter
