package com.hayderameen.mypr.domain

enum class Equipment(val label: String) {
    BARBELL("Barbell"),
    DUMBBELL("Dumbbell"),
    CABLE("Cable"),
    MACHINE("Machine"),
    /** Bodyweight movement with added load (dip belt, vest, held plate). Logged weight = added weight. */
    BODYWEIGHT("Bodyweight +"),
}

enum class MuscleGroup(val label: String) {
    CHEST("Chest"),
    BACK("Back"),
    SHOULDERS("Shoulders"),
    BICEPS("Biceps"),
    TRICEPS("Triceps"),
    FOREARMS("Forearms"),
    QUADS("Quads"),
    HAMSTRINGS("Hamstrings"),
    GLUTES("Glutes"),
    CALVES("Calves"),
    CORE("Core"),
    FULL_BODY("Full Body"),
}

/** Ruler configuration for one equipment type in one unit. All values in that unit. */
data class RulerSpec(
    val min: Double,
    val max: Double,
    val step: Double,
    val majorInterval: Double,
    val midInterval: Double?,
    /** Weights where dragging starts to feel heavier (in kg), from the Stitch log-set screens. */
    val resistanceKg: List<Double>,
)

fun weightRuler(equipment: Equipment, unit: WeightUnit): RulerSpec {
    val maxLbs = 2205.0 // 1000 kg
    return when (unit) {
        WeightUnit.KG -> when (equipment) {
            Equipment.BARBELL -> RulerSpec(20.0, MAX_WEIGHT_KG, 2.5, 10.0, 5.0, listOf(100.0, 140.0, 180.0))
            Equipment.DUMBBELL -> RulerSpec(1.0, 150.0, 0.5, 5.0, 2.5, listOf(30.0, 40.0, 50.0))
            Equipment.CABLE -> RulerSpec(2.5, 300.0, 2.5, 10.0, 5.0, listOf(50.0, 70.0, 90.0))
            Equipment.MACHINE -> RulerSpec(2.5, MAX_WEIGHT_KG, 2.5, 10.0, 5.0, listOf(100.0, 160.0, 220.0))
            Equipment.BODYWEIGHT -> RulerSpec(0.0, 300.0, 1.25, 5.0, 2.5, listOf(30.0, 50.0, 70.0))
        }
        WeightUnit.LBS -> when (equipment) {
            Equipment.BARBELL -> RulerSpec(45.0, maxLbs, 5.0, 50.0, 25.0, listOf(100.0, 140.0, 180.0))
            Equipment.DUMBBELL -> RulerSpec(2.5, 330.0, 2.5, 10.0, 5.0, listOf(30.0, 40.0, 50.0))
            Equipment.CABLE -> RulerSpec(5.0, 660.0, 5.0, 50.0, 25.0, listOf(50.0, 70.0, 90.0))
            Equipment.MACHINE -> RulerSpec(5.0, maxLbs, 5.0, 50.0, 25.0, listOf(100.0, 160.0, 220.0))
            Equipment.BODYWEIGHT -> RulerSpec(0.0, 660.0, 2.5, 10.0, 5.0, listOf(30.0, 50.0, 70.0))
        }
    }
}

val RepsRuler = RulerSpec(1.0, 50.0, 1.0, 5.0, null, emptyList())

/** Drag multiplier when moving the weight ruler toward heavier values (progressive resistance). */
fun dragResistance(spec: RulerSpec, weightKg: Double): Float {
    val t = spec.resistanceKg
    if (t.size < 3) return 1f
    return when {
        weightKg >= t[2] -> 0.4f
        weightKg >= t[1] -> 0.55f
        weightKg >= t[0] -> 0.75f
        else -> 1f
    }
}

/** Starting value for a lift the user has never logged. */
fun defaultWeight(equipment: Equipment, unit: WeightUnit): Double = when (unit) {
    WeightUnit.KG -> when (equipment) {
        Equipment.BARBELL -> 60.0
        Equipment.DUMBBELL -> 20.0
        Equipment.CABLE -> 30.0
        Equipment.MACHINE -> 50.0
        Equipment.BODYWEIGHT -> 0.0
    }
    WeightUnit.LBS -> when (equipment) {
        Equipment.BARBELL -> 135.0
        Equipment.DUMBBELL -> 40.0
        Equipment.CABLE -> 60.0
        Equipment.MACHINE -> 110.0
        Equipment.BODYWEIGHT -> 0.0
    }
}
