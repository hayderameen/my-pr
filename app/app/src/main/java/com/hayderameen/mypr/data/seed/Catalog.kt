package com.hayderameen.mypr.data.seed

import com.hayderameen.mypr.domain.Equipment
import com.hayderameen.mypr.domain.Equipment.BARBELL
import com.hayderameen.mypr.domain.Equipment.BODYWEIGHT
import com.hayderameen.mypr.domain.Equipment.CABLE
import com.hayderameen.mypr.domain.Equipment.DUMBBELL
import com.hayderameen.mypr.domain.Equipment.MACHINE
import com.hayderameen.mypr.domain.MuscleGroup
import com.hayderameen.mypr.domain.MuscleGroup.BACK
import com.hayderameen.mypr.domain.MuscleGroup.BICEPS
import com.hayderameen.mypr.domain.MuscleGroup.CALVES
import com.hayderameen.mypr.domain.MuscleGroup.CHEST
import com.hayderameen.mypr.domain.MuscleGroup.CORE
import com.hayderameen.mypr.domain.MuscleGroup.FOREARMS
import com.hayderameen.mypr.domain.MuscleGroup.FULL_BODY
import com.hayderameen.mypr.domain.MuscleGroup.GLUTES
import com.hayderameen.mypr.domain.MuscleGroup.HAMSTRINGS
import com.hayderameen.mypr.domain.MuscleGroup.QUADS
import com.hayderameen.mypr.domain.MuscleGroup.SHOULDERS
import com.hayderameen.mypr.domain.MuscleGroup.TRICEPS

data class CatalogExercise(
    val id: String,
    val name: String,
    val equipment: Equipment,
    val muscleGroup: MuscleGroup,
    val target: String,
)

data class PresetDay(val id: String, val name: String, val iconKey: String, val exerciseIds: List<String>)

/**
 * Bump when exercises are ADDED to [BUILT_IN_EXERCISES]; new rows are inserted on next launch
 * without touching anything the user changed. Never reuse or rename an id.
 */
const val CATALOG_VERSION = 1

private fun ex(id: String, name: String, eq: Equipment, group: MuscleGroup, target: String) =
    CatalogExercise(id, name, eq, group, target)

val BUILT_IN_EXERCISES: List<CatalogExercise> = listOf(
    // ---- Chest ----
    ex("barbell_bench_press", "Barbell Bench Press", BARBELL, CHEST, "Mid Chest"),
    ex("incline_barbell_bench_press", "Incline Barbell Bench Press", BARBELL, CHEST, "Upper Chest"),
    ex("decline_barbell_bench_press", "Decline Barbell Bench Press", BARBELL, CHEST, "Lower Chest"),
    ex("incline_dumbbell_press", "Incline Dumbbell Press", DUMBBELL, CHEST, "Upper Chest"),
    ex("flat_dumbbell_press", "Flat Dumbbell Press", DUMBBELL, CHEST, "Mid Chest"),
    ex("decline_dumbbell_press", "Decline Dumbbell Press", DUMBBELL, CHEST, "Lower Chest"),
    ex("dumbbell_chest_fly", "Dumbbell Chest Fly", DUMBBELL, CHEST, "Chest Stretch"),
    ex("incline_dumbbell_fly", "Incline Dumbbell Fly", DUMBBELL, CHEST, "Upper Chest"),
    ex("dumbbell_pullover", "Dumbbell Pullover", DUMBBELL, CHEST, "Chest & Lats"),
    ex("cable_crossover_fly", "Cable Crossover Fly", CABLE, CHEST, "Inner Chest"),
    ex("low_to_high_cable_fly", "Low-to-High Cable Fly", CABLE, CHEST, "Upper Chest"),
    ex("high_to_low_cable_fly", "High-to-Low Cable Fly", CABLE, CHEST, "Lower Chest"),
    ex("pec_deck_fly", "Pec Deck Fly Machine", MACHINE, CHEST, "Chest Isolation"),
    ex("seated_chest_press_machine", "Seated Chest Press Machine", MACHINE, CHEST, "Chest Press"),
    ex("incline_chest_press_machine", "Incline Chest Press Machine", MACHINE, CHEST, "Upper Chest"),
    ex("smith_machine_bench_press", "Smith Machine Bench Press", MACHINE, CHEST, "Mid Chest"),
    ex("smith_machine_incline_press", "Smith Machine Incline Press", MACHINE, CHEST, "Upper Chest"),
    ex("weighted_chest_dip", "Weighted Chest Dip", BODYWEIGHT, CHEST, "Lower Chest"),
    ex("weighted_push_up", "Weighted Push-Up", BODYWEIGHT, CHEST, "Chest & Triceps"),

    // ---- Back ----
    ex("conventional_deadlift", "Conventional Deadlift", BARBELL, BACK, "Posterior Chain"),
    ex("rack_pull", "Rack Pull", BARBELL, BACK, "Upper Back & Traps"),
    ex("barbell_bent_over_row", "Barbell Bent-Over Row", BARBELL, BACK, "Mid Back"),
    ex("pendlay_row", "Pendlay Row", BARBELL, BACK, "Mid Back"),
    ex("t_bar_row", "T-Bar Row", BARBELL, BACK, "Back Thickness"),
    ex("barbell_shrug", "Barbell Shrug", BARBELL, BACK, "Traps"),
    ex("weighted_pull_up", "Weighted Pull-Up", BODYWEIGHT, BACK, "Lats"),
    ex("weighted_chin_up", "Weighted Chin-Up", BODYWEIGHT, BACK, "Lats & Biceps"),
    ex("weighted_back_extension", "Weighted Back Extension", BODYWEIGHT, BACK, "Lower Back"),
    ex("wide_grip_lat_pulldown", "Wide-Grip Lat Pulldown", CABLE, BACK, "Lats"),
    ex("close_grip_lat_pulldown", "Close-Grip Lat Pulldown", CABLE, BACK, "Lats"),
    ex("single_arm_lat_pulldown", "Single-Arm Lat Pulldown", CABLE, BACK, "Lats"),
    ex("seated_cable_row", "Seated Cable Row", CABLE, BACK, "Mid Back"),
    ex("straight_arm_cable_pulldown", "Straight-Arm Cable Pulldown", CABLE, BACK, "Lats"),
    ex("single_arm_dumbbell_row", "Single-Arm Dumbbell Row", DUMBBELL, BACK, "Lats"),
    ex("chest_supported_dumbbell_row", "Chest-Supported Dumbbell Row", DUMBBELL, BACK, "Upper Back"),
    ex("dumbbell_shrug", "Dumbbell Shrugs", DUMBBELL, BACK, "Traps"),
    ex("chest_supported_machine_row", "Chest-Supported Machine Row", MACHINE, BACK, "Mid Back"),
    ex("iso_lateral_row_machine", "Iso-Lateral Row Machine", MACHINE, BACK, "Lats & Mid Back"),
    ex("lat_pulldown_machine", "Lat Pulldown Machine", MACHINE, BACK, "Lats"),

    // ---- Shoulders ----
    ex("overhead_barbell_press", "Overhead Barbell Press", BARBELL, SHOULDERS, "Front & Side Delts"),
    ex("push_press", "Push Press", BARBELL, SHOULDERS, "Delts & Drive"),
    ex("landmine_press", "Landmine Press", BARBELL, SHOULDERS, "Front Delts"),
    ex("barbell_upright_row", "Barbell Upright Row", BARBELL, SHOULDERS, "Side Delts & Traps"),
    ex("seated_dumbbell_shoulder_press", "Seated Dumbbell Shoulder Press", DUMBBELL, SHOULDERS, "Deltoids"),
    ex("arnold_dumbbell_press", "Arnold Dumbbell Press", DUMBBELL, SHOULDERS, "All 3 Delt Heads"),
    ex("dumbbell_lateral_raise", "Dumbbell Lateral Raise", DUMBBELL, SHOULDERS, "Side Delts"),
    ex("dumbbell_front_raise", "Dumbbell Front Raise", DUMBBELL, SHOULDERS, "Front Delts"),
    ex("dumbbell_rear_delt_fly", "Dumbbell Rear Delt Fly", DUMBBELL, SHOULDERS, "Rear Delts"),
    ex("cable_lateral_raise", "Cable Lateral Raise", CABLE, SHOULDERS, "Side Delts"),
    ex("cable_face_pull", "Cable Face Pull", CABLE, SHOULDERS, "Rear Delts"),
    ex("cable_rear_delt_fly", "Cable Rear Delt Fly", CABLE, SHOULDERS, "Rear Delts"),
    ex("shoulder_press_machine", "Shoulder Press Machine", MACHINE, SHOULDERS, "Deltoids"),
    ex("machine_lateral_raise", "Machine Lateral Raise", MACHINE, SHOULDERS, "Side Delts"),
    ex("reverse_pec_deck", "Reverse Pec Deck Fly", MACHINE, SHOULDERS, "Rear Delts"),
    ex("smith_machine_shoulder_press", "Smith Machine Shoulder Press", MACHINE, SHOULDERS, "Deltoids"),

    // ---- Biceps ----
    ex("standing_barbell_curl", "Standing Barbell Curl", BARBELL, BICEPS, "Biceps"),
    ex("ez_bar_curl", "EZ-Bar Curl", BARBELL, BICEPS, "Biceps"),
    ex("ez_bar_preacher_curl", "EZ-Bar Preacher Curl", BARBELL, BICEPS, "Short Head"),
    ex("alternating_dumbbell_curl", "Alternating Dumbbell Curl", DUMBBELL, BICEPS, "Biceps"),
    ex("incline_dumbbell_curl", "Incline Dumbbell Curl", DUMBBELL, BICEPS, "Long Head"),
    ex("dumbbell_hammer_curl", "Dumbbell Hammer Curl", DUMBBELL, BICEPS, "Brachialis"),
    ex("dumbbell_concentration_curl", "Dumbbell Concentration Curl", DUMBBELL, BICEPS, "Peak"),
    ex("dumbbell_preacher_curl", "Dumbbell Preacher Curl", DUMBBELL, BICEPS, "Short Head"),
    ex("cable_bayesian_curl", "Cable Bayesian Curl", CABLE, BICEPS, "Long Head"),
    ex("cable_curl", "Cable Bar Curl", CABLE, BICEPS, "Biceps"),
    ex("cable_rope_hammer_curl", "Cable Rope Hammer Curl", CABLE, BICEPS, "Brachialis"),
    ex("machine_preacher_curl", "Machine Preacher Curl", MACHINE, BICEPS, "Short Head"),

    // ---- Triceps ----
    ex("close_grip_bench_press", "Close-Grip Bench Press", BARBELL, TRICEPS, "Triceps & Chest"),
    ex("ez_bar_skullcrusher", "EZ-Bar Skullcrusher", BARBELL, TRICEPS, "Long & Medial Head"),
    ex("cable_tricep_pushdown", "Cable Tricep Pushdown", CABLE, TRICEPS, "Rope Attachment"),
    ex("cable_bar_pushdown", "Cable Bar Pushdown", CABLE, TRICEPS, "Lateral Head"),
    ex("single_arm_cable_pushdown", "Single-Arm Cable Pushdown", CABLE, TRICEPS, "Lateral Head"),
    ex("overhead_cable_rope_extension", "Overhead Cable Rope Extension", CABLE, TRICEPS, "Long Head"),
    ex("overhead_dumbbell_extension", "Overhead Dumbbell Extension", DUMBBELL, TRICEPS, "Long Head"),
    ex("dumbbell_kickback", "Dumbbell Kickback", DUMBBELL, TRICEPS, "Lateral Head"),
    ex("weighted_tricep_dip", "Weighted Tricep Dip", BODYWEIGHT, TRICEPS, "Lower Chest & Tris"),
    ex("seated_dip_machine", "Seated Dip Machine", MACHINE, TRICEPS, "Triceps"),

    // ---- Forearms ----
    ex("barbell_wrist_curl", "Barbell Wrist Curl", BARBELL, FOREARMS, "Wrist Flexors"),
    ex("reverse_barbell_curl", "Reverse Barbell Curl", BARBELL, FOREARMS, "Brachioradialis"),
    ex("dumbbell_farmer_walk", "Heavy Dumbbell Farmer Walk", DUMBBELL, FOREARMS, "Grip"),

    // ---- Quads ----
    ex("barbell_back_squat", "Barbell Back Squat", BARBELL, QUADS, "Quads & Glutes"),
    ex("barbell_front_squat", "Barbell Front Squat", BARBELL, QUADS, "Quads"),
    ex("barbell_reverse_lunge", "Barbell Reverse Lunge", BARBELL, QUADS, "Quads & Glutes"),
    ex("leg_press", "45° Leg Press Machine", MACHINE, QUADS, "Quads"),
    ex("hack_squat", "Hack Squat Machine", MACHINE, QUADS, "Quads"),
    ex("smith_machine_squat", "Smith Machine Squat", MACHINE, QUADS, "Quads"),
    ex("pendulum_squat", "Pendulum Squat Machine", MACHINE, QUADS, "Quads"),
    ex("seated_leg_extension", "Seated Leg Extension", MACHINE, QUADS, "Quads Isolation"),
    ex("bulgarian_split_squat", "Bulgarian Split Squat", DUMBBELL, QUADS, "Quads & Glutes"),
    ex("dumbbell_walking_lunge", "Dumbbell Walking Lunge", DUMBBELL, QUADS, "Quads & Glutes"),
    ex("goblet_squat", "Goblet Squat", DUMBBELL, QUADS, "Quads"),
    ex("dumbbell_step_up", "Dumbbell Step-Up", DUMBBELL, QUADS, "Quads & Glutes"),

    // ---- Hamstrings ----
    ex("romanian_deadlift", "Romanian Deadlift (RDL)", BARBELL, HAMSTRINGS, "Hamstrings"),
    ex("good_morning", "Good Morning", BARBELL, HAMSTRINGS, "Hamstrings & Lower Back"),
    ex("dumbbell_stiff_leg_deadlift", "Dumbbell Stiff-Leg Deadlift", DUMBBELL, HAMSTRINGS, "Hamstrings"),
    ex("seated_leg_curl", "Seated Leg Curl Machine", MACHINE, HAMSTRINGS, "Hamstrings"),
    ex("lying_leg_curl", "Lying Leg Curl Machine", MACHINE, HAMSTRINGS, "Hamstrings"),

    // ---- Glutes ----
    ex("barbell_hip_thrust", "Barbell Hip Thrust", BARBELL, GLUTES, "Glutes"),
    ex("sumo_deadlift", "Sumo Deadlift", BARBELL, GLUTES, "Glutes & Adductors"),
    ex("cable_glute_pull_through", "Cable Glute Pull-Through", CABLE, GLUTES, "Glutes"),
    ex("cable_glute_kickback", "Cable Glute Kickback", CABLE, GLUTES, "Glutes"),
    ex("hip_abduction_machine", "Hip Abduction Machine", MACHINE, GLUTES, "Glute Medius"),
    ex("hip_adduction_machine", "Hip Adduction Machine", MACHINE, GLUTES, "Adductors"),

    // ---- Calves ----
    ex("standing_calf_raise", "Standing Calf Raise", MACHINE, CALVES, "Gastrocnemius"),
    ex("seated_calf_raise", "Seated Calf Raise", MACHINE, CALVES, "Soleus"),
    ex("leg_press_calf_raise", "Leg Press Calf Raise", MACHINE, CALVES, "Calves"),
    ex("single_leg_dumbbell_calf_raise", "Single-Leg Dumbbell Calf Raise", DUMBBELL, CALVES, "Calves"),

    // ---- Core ----
    ex("hanging_leg_raise", "Hanging Leg Raise", BODYWEIGHT, CORE, "Lower Abs"),
    ex("decline_weighted_sit_up", "Decline Weighted Sit-Up", BODYWEIGHT, CORE, "Upper Abs"),
    ex("weighted_russian_twist", "Weighted Russian Twist", BODYWEIGHT, CORE, "Obliques"),
    ex("kneeling_cable_crunch", "Kneeling Cable Crunch", CABLE, CORE, "Abs"),
    ex("cable_pallof_press", "Cable Pallof Press", CABLE, CORE, "Anti-Rotation"),
    ex("cable_woodchopper", "Cable Woodchopper", CABLE, CORE, "Obliques"),
    ex("ab_crunch_machine", "Ab Crunch Machine", MACHINE, CORE, "Abs"),
    ex("dumbbell_side_bend", "Dumbbell Side Bend", DUMBBELL, CORE, "Obliques"),

    // ---- Full body ----
    ex("trap_bar_deadlift", "Trap Bar Deadlift", BARBELL, FULL_BODY, "Legs & Back"),
    ex("power_clean", "Power Clean", BARBELL, FULL_BODY, "Explosive Power"),
    ex("barbell_thruster", "Barbell Thruster", BARBELL, FULL_BODY, "Legs & Shoulders"),
)

val PRESET_DAYS: List<PresetDay> = listOf(
    PresetDay(
        "push", "Push Day", "bolt",
        listOf("barbell_bench_press", "incline_dumbbell_press", "overhead_barbell_press", "cable_tricep_pushdown", "dumbbell_lateral_raise"),
    ),
    PresetDay(
        "pull", "Pull Day", "rowing",
        listOf("conventional_deadlift", "weighted_pull_up", "barbell_bent_over_row", "wide_grip_lat_pulldown", "seated_cable_row", "cable_face_pull", "dumbbell_hammer_curl"),
    ),
    PresetDay(
        "legs", "Leg Day", "run",
        listOf("barbell_back_squat", "romanian_deadlift", "leg_press", "seated_leg_curl", "seated_leg_extension", "standing_calf_raise"),
    ),
    PresetDay(
        "upper", "Upper Body", "martial",
        listOf("barbell_bench_press", "barbell_bent_over_row", "overhead_barbell_press", "wide_grip_lat_pulldown", "ez_bar_curl", "cable_tricep_pushdown"),
    ),
    PresetDay(
        "lower", "Lower Body", "walk",
        listOf("barbell_back_squat", "romanian_deadlift", "barbell_hip_thrust", "bulgarian_split_squat", "lying_leg_curl", "seated_calf_raise"),
    ),
    PresetDay(
        "arms", "Arms", "gymnastics",
        listOf("ez_bar_curl", "incline_dumbbell_curl", "dumbbell_hammer_curl", "close_grip_bench_press", "ez_bar_skullcrusher", "cable_tricep_pushdown", "overhead_cable_rope_extension"),
    ),
    PresetDay(
        "shoulders", "Shoulders", "accessibility",
        listOf("overhead_barbell_press", "seated_dumbbell_shoulder_press", "dumbbell_lateral_raise", "cable_lateral_raise", "reverse_pec_deck", "dumbbell_shrug"),
    ),
    PresetDay(
        "chest", "Chest Day", "barbell",
        listOf("barbell_bench_press", "incline_barbell_bench_press", "incline_dumbbell_press", "cable_crossover_fly", "pec_deck_fly", "weighted_chest_dip"),
    ),
    PresetDay(
        "back", "Back Day", "rowing",
        listOf("conventional_deadlift", "weighted_pull_up", "pendlay_row", "t_bar_row", "close_grip_lat_pulldown", "single_arm_dumbbell_row", "straight_arm_cable_pulldown"),
    ),
    PresetDay(
        "core", "Core", "core",
        listOf("hanging_leg_raise", "kneeling_cable_crunch", "cable_pallof_press", "decline_weighted_sit_up", "cable_woodchopper"),
    ),
    PresetDay(
        "full_body", "Full Body", "full",
        listOf("barbell_back_squat", "barbell_bench_press", "conventional_deadlift", "overhead_barbell_press", "weighted_pull_up"),
    ),
)
