package com.hayderameen.mypr.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises WHERE deletedAt IS NULL ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun get(id: String): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE id = :id")
    fun observe(id: String): Flow<ExerciseEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(items: List<ExerciseEntity>)

    @Upsert
    suspend fun upsert(item: ExerciseEntity)
}

@Dao
interface DayDao {
    @Query(
        """
        SELECT d.*,
          (SELECT COUNT(*) FROM day_exercises de JOIN exercises e ON e.id = de.exerciseId
             WHERE de.dayId = d.id AND de.deletedAt IS NULL AND e.deletedAt IS NULL) AS exerciseCount,
          (SELECT MAX(s.startedAt) FROM sessions s
             WHERE s.dayId = d.id AND s.deletedAt IS NULL AND s.endedAt IS NOT NULL) AS lastTrainedAt
        FROM workout_days d
        WHERE d.deletedAt IS NULL
        ORDER BY d.position, d.createdAt
        """
    )
    fun observeDays(): Flow<List<DayRow>>

    @Query(
        """
        SELECT de.dayId AS dayId, e.muscleGroup AS muscleGroup
        FROM day_exercises de JOIN exercises e ON e.id = de.exerciseId
        WHERE de.deletedAt IS NULL AND e.deletedAt IS NULL
        ORDER BY de.dayId, de.orderScore
        """
    )
    fun observeDayMuscles(): Flow<List<DayMuscleRow>>

    @Query("SELECT * FROM workout_days WHERE id = :id")
    suspend fun get(id: String): WorkoutDayEntity?

    @Query("SELECT * FROM workout_days WHERE id = :id")
    fun observe(id: String): Flow<WorkoutDayEntity?>

    @Query("SELECT * FROM workout_days WHERE deletedAt IS NULL ORDER BY position")
    suspend fun getAll(): List<WorkoutDayEntity>

    @Query("SELECT COALESCE(MAX(position), -1) FROM workout_days WHERE deletedAt IS NULL")
    suspend fun maxPosition(): Int

    @Upsert
    suspend fun upsert(day: WorkoutDayEntity)

    @Upsert
    suspend fun upsertAll(days: List<WorkoutDayEntity>)

    // ----- exercises within a day -----

    @Query(
        """
        SELECT e.*, de.orderScore AS orderScore
        FROM day_exercises de JOIN exercises e ON e.id = de.exerciseId
        WHERE de.dayId = :dayId AND de.deletedAt IS NULL AND e.deletedAt IS NULL
        ORDER BY de.orderScore, de.addedAt
        """
    )
    fun observeDayExercises(dayId: String): Flow<List<DayExerciseRow>>

    @Query("SELECT * FROM day_exercises WHERE dayId = :dayId AND deletedAt IS NULL")
    suspend fun getDayExercises(dayId: String): List<DayExerciseEntity>

    @Query("SELECT * FROM day_exercises WHERE dayId = :dayId AND exerciseId = :exerciseId")
    suspend fun getDayExercise(dayId: String, exerciseId: String): DayExerciseEntity?

    @Query("SELECT COALESCE(MAX(orderScore), -1) FROM day_exercises WHERE dayId = :dayId AND deletedAt IS NULL")
    suspend fun maxScore(dayId: String): Double

    @Upsert
    suspend fun upsertDayExercises(items: List<DayExerciseEntity>)
}

@Dao
interface SessionDao {
    @Query(
        """
        SELECT s.*, d.name AS dayName FROM sessions s JOIN workout_days d ON d.id = s.dayId
        WHERE s.endedAt IS NULL AND s.deletedAt IS NULL
        ORDER BY s.startedAt DESC LIMIT 1
        """
    )
    fun observeActive(): Flow<ActiveSessionRow?>

    @Query("SELECT * FROM sessions WHERE endedAt IS NULL AND deletedAt IS NULL ORDER BY startedAt DESC")
    suspend fun getOpen(): List<SessionEntity>

    @Upsert
    suspend fun upsert(session: SessionEntity)
}

@Dao
interface SetDao {
    @Insert
    suspend fun insert(set: SetEntity)

    @Query("SELECT * FROM sets WHERE id = :id")
    suspend fun get(id: String): SetEntity?

    @Upsert
    suspend fun upsert(set: SetEntity)

    @Query("SELECT * FROM sets WHERE sessionId = :sessionId AND deletedAt IS NULL ORDER BY performedAt")
    fun observeForSession(sessionId: String): Flow<List<SetEntity>>

    @Query("SELECT COUNT(*) FROM sets WHERE sessionId = :sessionId AND deletedAt IS NULL")
    suspend fun countForSession(sessionId: String): Int

    @Query("SELECT MAX(weightKg) FROM sets WHERE exerciseId = :exerciseId AND deletedAt IS NULL")
    suspend fun bestKg(exerciseId: String): Double?

    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId AND deletedAt IS NULL ORDER BY performedAt DESC LIMIT 1")
    suspend fun lastSet(exerciseId: String): SetEntity?

    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId AND deletedAt IS NULL ORDER BY performedAt")
    fun observeForExercise(exerciseId: String): Flow<List<SetEntity>>

    @Query(
        """
        SELECT exerciseId, MIN(performedAt) AS firstAt FROM sets
        WHERE sessionId = :sessionId AND deletedAt IS NULL
        GROUP BY exerciseId ORDER BY firstAt
        """
    )
    suspend fun performedOrder(sessionId: String): List<FirstPerformed>

    @Query(
        """
        SELECT e.*, MAX(s.weightKg) AS bestKg, MAX(s.performedAt) AS lastAt
        FROM sets s JOIN exercises e ON e.id = s.exerciseId
        WHERE s.deletedAt IS NULL
        GROUP BY e.id
        ORDER BY lastAt DESC
        """
    )
    fun observePrExercises(): Flow<List<PrExerciseRow>>
}
