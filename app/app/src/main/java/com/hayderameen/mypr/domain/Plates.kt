package com.hayderameen.mypr.domain

enum class PlateColor(val label: String) { RED("Red"), BLUE("Blue"), YELLOW("Yellow"), GREEN("Green"), WHITE("White"), BLACK("Black"), CHROME("Chrome") }

/**
 * One plate denomination. [height]/[width] are in the 300x84 drawing units used by the Stitch
 * equipment SVGs; [diameterNote] is the competition diameter shown on the plates screen
 * (blank for lb plates, whose sizes vary by manufacturer).
 */
data class Plate(
    val weight: Double,
    val unit: WeightUnit,
    val color: PlateColor,
    val height: Float,
    val width: Float,
    val diameterNote: String,
)

val KG_PLATES = listOf(
    Plate(25.0, WeightUnit.KG, PlateColor.RED, 66f, 12f, "450 mm"),
    Plate(20.0, WeightUnit.KG, PlateColor.BLUE, 62f, 11f, "450 mm"),
    Plate(15.0, WeightUnit.KG, PlateColor.YELLOW, 54f, 10f, "400 mm"),
    Plate(10.0, WeightUnit.KG, PlateColor.GREEN, 46f, 9f, "325 mm"),
    Plate(5.0, WeightUnit.KG, PlateColor.WHITE, 36f, 7f, "228 mm"),
    Plate(2.5, WeightUnit.KG, PlateColor.BLACK, 28f, 6f, "190 mm"),
    Plate(1.25, WeightUnit.KG, PlateColor.CHROME, 22f, 5f, "160 mm"),
)

/** Standard pound plates, coloured like lb bumper sets (45 blue, 35 yellow, 25 green, 10 white). */
val LB_PLATES = listOf(
    Plate(45.0, WeightUnit.LBS, PlateColor.BLUE, 66f, 12f, ""),
    Plate(35.0, WeightUnit.LBS, PlateColor.YELLOW, 58f, 11f, ""),
    Plate(25.0, WeightUnit.LBS, PlateColor.GREEN, 50f, 10f, ""),
    Plate(10.0, WeightUnit.LBS, PlateColor.WHITE, 40f, 8f, ""),
    Plate(5.0, WeightUnit.LBS, PlateColor.BLACK, 32f, 7f, ""),
    Plate(2.5, WeightUnit.LBS, PlateColor.CHROME, 24f, 5f, ""),
)

fun platesFor(unit: WeightUnit) = if (unit == WeightUnit.KG) KG_PLATES else LB_PLATES

fun barWeight(unit: WeightUnit) = if (unit == WeightUnit.KG) 20.0 else 45.0

/** Greedy plate breakdown for [load] (any remainder that no plate can make is ignored). */
fun breakdown(load: Double, unit: WeightUnit): List<Plate> {
    var rem = load
    val out = mutableListOf<Plate>()
    for (p in platesFor(unit)) {
        while (rem >= p.weight - 1e-6) {
            out += p
            rem -= p.weight
        }
    }
    return out
}

/** Plates on ONE side of a loaded barbell weighing [total] (in [unit]). */
fun platesPerSide(total: Double, unit: WeightUnit): List<Plate> =
    breakdown(((total - barWeight(unit)) / 2).coerceAtLeast(0.0), unit)
