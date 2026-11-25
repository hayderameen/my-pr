package com.hayderameen.mypr.data.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.hayderameen.mypr.domain.Equipment
import com.hayderameen.mypr.domain.MuscleGroup
import com.hayderameen.mypr.domain.WeightUnit

/*
 * Sync-ready conventions (for the future account backup):
 *  - String UUID primary keys (built-in catalog rows use stable slugs so they match across devices)
 *  - updatedAt on every row, so a sync can push "changed since last sync"
 *  - deletedAt soft-deletes, so deletions can be synced instead of silently disappearing
 */

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val equipment: Equipment,
    val muscleGroup: MuscleGroup,
    /** Finer detail shown under the name, e.g. "Upper Chest". */
    val target: String,
    val isCustom: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
)

@Entity(tableName = "workout_days")
data class WorkoutDayEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconKey: String,
    /** User-saved position in the day grid. */
    val position: Int,
    val isPreset: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
)

@Entity(
    tableName = "day_exercises",
    primaryKeys = ["dayId", "exerciseId"],
    indices = [Index("exerciseId")],
)
data class DayExerciseEntity(
    val dayId: String,
    val exerciseId: String,
    /** Lower = earlier in the day. Set by manual reorder and refined by OrderLearning. */
    val orderScore: Double,
    val addedAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
)

@Entity(tableName = "sessions", indices = [Index("dayId"), Index("endedAt")])
data class SessionEntity(
    @PrimaryKey val id: String,
    val dayId: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val updatedAt: Long,
    val deletedAt: Long? = null,
)

@Entity(tableName = "sets", indices = [Index("sessionId"), Index("exerciseId")])
data class SetEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val exerciseId: String,
    /** Canonical weight for comparisons (PRs, charts). For bodyweight lifts this is the ADDED weight. */
    val weightKg: Double,
    /** Exactly what the lifter dialled in, so it can be shown back without rounding drift. */
    val enteredWeight: Double,
    val enteredUnit: WeightUnit,
    val reps: Int,
    val performedAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
)

// ---------- Query projections ----------

data class DayRow(
    @Embedded val day: WorkoutDayEntity,
    val exerciseCount: Int,
    val lastTrainedAt: Long?,
)

data class DayMuscleRow(val dayId: String, val muscleGroup: MuscleGroup)

data class DayExerciseRow(
    @Embedded val exercise: ExerciseEntity,
    val orderScore: Double,
)

data class ActiveSessionRow(
    @Embedded val session: SessionEntity,
    val dayName: String,
)

data class FirstPerformed(val exerciseId: String, @ColumnInfo(name = "firstAt") val firstAt: Long)

data class PrExerciseRow(
    @Embedded val exercise: ExerciseEntity,
    val bestKg: Double,
    val lastAt: Long,
)

class Converters {
    @TypeConverter fun equipmentToString(v: Equipment): String = v.name
    @TypeConverter fun stringToEquipment(v: String): Equipment = Equipment.valueOf(v)
    @TypeConverter fun muscleToString(v: MuscleGroup): String = v.name
    @TypeConverter fun stringToMuscle(v: String): MuscleGroup = MuscleGroup.valueOf(v)
    @TypeConverter fun unitToString(v: WeightUnit): String = v.name
    @TypeConverter fun stringToUnit(v: String): WeightUnit = WeightUnit.valueOf(v)
}
