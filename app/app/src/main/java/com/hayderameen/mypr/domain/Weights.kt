package com.hayderameen.mypr.domain

import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

enum class WeightUnit(val label: String, val short: String) {
    KG("kg", "KG"),
    LBS("lbs", "LBS");

    fun other() = if (this == KG) LBS else KG
}

const val KG_PER_LB = 0.45359237

/** Absolute ceiling for any logged weight (world-record headroom). */
const val MAX_WEIGHT_KG = 1000.0

fun toKg(value: Double, unit: WeightUnit): Double = if (unit == WeightUnit.KG) value else value * KG_PER_LB

fun fromKg(kg: Double, unit: WeightUnit): Double = if (unit == WeightUnit.KG) kg else kg / KG_PER_LB

fun roundTo(value: Double, step: Double): Double = (value / step).roundToLong() * step

/**
 * Shows a stored weight in [unit]. If it was entered in that unit we show exactly what was entered,
 * otherwise we convert and round to 0.1 so 225 lbs never turns into 102.058 kg on screen.
 */
fun displayWeight(kg: Double, enteredValue: Double, enteredUnit: WeightUnit, unit: WeightUnit): Double =
    if (enteredUnit == unit) enteredValue else roundTo(fromKg(kg, unit), 0.1)

fun formatWeight(value: Double): String = String.format(Locale.US, "%.1f", value)

fun formatSignedWeight(value: Double): String = (if (value >= 0) "+" else "") + formatWeight(value)

fun nearlyEqual(a: Double, b: Double, eps: Double = 1e-6) = abs(a - b) < eps
