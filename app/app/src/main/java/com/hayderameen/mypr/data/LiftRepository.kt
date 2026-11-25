package com.hayderameen.mypr.data

import androidx.room.withTransaction
import com.hayderameen.mypr.data.db.ActiveSessionRow
import com.hayderameen.mypr.data.db.AppDatabase
import com.hayderameen.mypr.data.db.DayExerciseEntity
import com.hayderameen.mypr.data.db.DayExerciseRow
import com.hayderameen.mypr.data.db.DayMuscleRow
import com.hayderameen.mypr.data.db.DayRow
import com.hayderameen.mypr.data.db.ExerciseEntity
import com.hayderameen.mypr.data.db.PrExerciseRow
import com.hayderameen.mypr.data.db.SessionEntity
import com.hayderameen.mypr.data.db.SetEntity
import com.hayderameen.mypr.data.db.WorkoutDayEntity
import com.hayderameen.mypr.data.seed.BUILT_IN_EXERCISES
import com.hayderameen.mypr.data.seed.CATALOG_VERSION
import com.hayderameen.mypr.data.seed.PRESET_DAYS
import com.hayderameen.mypr.domain.Equipment
import com.hayderameen.mypr.domain.MuscleGroup
import com.hayderameen.mypr.domain.WeightUnit
import com.hayderameen.mypr.domain.learnOrder
import com.hayderameen.mypr.domain.manualOrderScores
import com.hayderameen.mypr.domain.toKg
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.UUID

/** Result of logging a set. [previousBestKg] is null when this was the first set ever for the lift. */
data class LogResult(val set: SetEntity, val isNewPr: Boolean, val previousBestKg: Double?)

class LiftRepository(
    private val db: AppDatabase,
    private val settings: SettingsStore,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val exercises = db.exercises()
    private val days = db.days()
    private val sessions = db.sessions()
    private val sets = db.sets()

    private fun newId() = UUID.randomUUID().toString()

    // ---------------- Seeding ----------------

    suspend fun ensureSeeded() {
        val now = clock()
        if (settings.catalogVersion() < CATALOG_VERSION) {
            exercises.insertIgnore(
                BUILT_IN_EXERCISES.map {
                    ExerciseEntity(it.id, it.name, it.equipment, it.muscleGroup, it.target, isCustom = false, createdAt = now, updatedAt = now)
                }
            )
            settings.setCatalogVersion(CATALOG_VERSION)
        }
        if (!settings.presetsSeeded()) {
            db.withTransaction {
                PRESET_DAYS.forEachIndexed { index, preset ->
                    days.upsert(WorkoutDayEntity(preset.id, preset.name, preset.iconKey, index, isPreset = true, createdAt = now, updatedAt = now))
                    days.upsertDayExercises(
                        preset.exerciseIds.mapIndexed { i, exId ->
                            DayExerciseEntity(preset.id, exId, i.toDouble(), addedAt = now + i, updatedAt = now)
                        }
                    )
                }
            }
            settings.setPresetsSeeded()
        }
    }

    // ---------------- Days ----------------

    fun observeDays(): Flow<List<DayRow>> = days.observeDays()
    fun observeDayMuscles(): Flow<List<DayMuscleRow>> = days.observeDayMuscles()
    fun observeDay(dayId: String): Flow<WorkoutDayEntity?> = days.observe(dayId)

    suspend fun createDay(name: String, iconKey: String): String {
        val now = clock()
        val id = newId()
        days.upsert(WorkoutDayEntity(id, name.trim(), iconKey, days.maxPosition() + 1, isPreset = false, createdAt = now, updatedAt = now))
        return id
    }

    suspend fun updateDay(dayId: String, name: String, iconKey: String) {
        val day = days.get(dayId) ?: return
        days.upsert(day.copy(name = name.trim(), iconKey = iconKey, updatedAt = clock()))
    }

    suspend fun deleteDay(dayId: String) = db.withTransaction {
        val now = clock()
        val day = days.get(dayId) ?: return@withTransaction
        sessions.getOpen().filter { it.dayId == dayId }.forEach { endSessionInternal(it, now) }
        days.upsert(day.copy(deletedAt = now, updatedAt = now))
    }

    suspend fun reorderDays(orderedIds: List<String>) = db.withTransaction {
        val now = clock()
        val byId = days.getAll().associateBy { it.id }
        days.upsertAll(orderedIds.mapIndexedNotNull { i, id -> byId[id]?.copy(position = i, updatedAt = now) })
    }

    // ---------------- Exercises in a day ----------------

    fun observeDayExercises(dayId: String): Flow<List<DayExerciseRow>> = days.observeDayExercises(dayId)

    suspend fun addExerciseToDay(dayId: String, exerciseId: String) {
        val now = clock()
        val existing = days.getDayExercise(dayId, exerciseId)
        if (existing != null && existing.deletedAt == null) return
        val score = days.maxScore(dayId) + 1
        days.upsertDayExercises(listOf(DayExerciseEntity(dayId, exerciseId, score, addedAt = now, updatedAt = now)))
    }

    suspend fun removeExerciseFromDay(dayId: String, exerciseId: String) {
        val existing = days.getDayExercise(dayId, exerciseId) ?: return
        val now = clock()
        days.upsertDayExercises(listOf(existing.copy(deletedAt = now, updatedAt = now)))
    }

    suspend fun reorderDayExercises(dayId: String, orderedExerciseIds: List<String>) = db.withTransaction {
        applyScores(dayId, manualOrderScores(orderedExerciseIds))
    }

    private suspend fun applyScores(dayId: String, scores: Map<String, Double>) {
        val now = clock()
        val updated = days.getDayExercises(dayId).mapNotNull { de ->
            scores[de.exerciseId]?.let { de.copy(orderScore = it, updatedAt = now) }
        }
        days.upsertDayExercises(updated)
    }

    // ---------------- Catalog ----------------

    fun observeCatalog(): Flow<List<ExerciseEntity>> = exercises.observeAll()
    fun observeExercise(id: String): Flow<ExerciseEntity?> = exercises.observe(id)

    suspend fun createCustomExercise(name: String, equipment: Equipment, muscleGroup: MuscleGroup): String {
        val now = clock()
        val id = newId()
        exercises.upsert(ExerciseEntity(id, name.trim(), equipment, muscleGroup, muscleGroup.label, isCustom = true, createdAt = now, updatedAt = now))
        return id
    }

    // ---------------- Sessions ----------------

    fun observeActiveSession(): Flow<ActiveSessionRow?> = sessions.observeActive()

    suspend fun activeSession(): SessionEntity? = sessions.getOpen().firstOrNull()

    /** Starts (or resumes) [dayId]'s session. Any other open session is ended first. */
    suspend fun startDay(dayId: String): SessionEntity = db.withTransaction {
        val now = clock()
        val open = sessions.getOpen()
        open.firstOrNull { it.dayId == dayId }?.let { current ->
            open.filter { it.id != current.id }.forEach { endSessionInternal(it, now) }
            return@withTransaction current
        }
        open.forEach { endSessionInternal(it, now) }
        SessionEntity(newId(), dayId, startedAt = now, updatedAt = now).also { sessions.upsert(it) }
    }

    suspend fun endActiveSession() = db.withTransaction {
        val now = clock()
        sessions.getOpen().forEach { endSessionInternal(it, now) }
    }

    /** Ends a session, learning the exercise order from it; empty sessions are discarded. */
    private suspend fun endSessionInternal(session: SessionEntity, now: Long) {
        if (sets.countForSession(session.id) == 0) {
            sessions.upsert(session.copy(endedAt = now, deletedAt = now, updatedAt = now))
            return
        }
        val performed = sets.performedOrder(session.id).map { it.exerciseId }
        val current = days.getDayExercises(session.dayId).associate { it.exerciseId to it.orderScore }
        applyScores(session.dayId, learnOrder(current, performed))
        sessions.upsert(session.copy(endedAt = now, updatedAt = now))
    }

    // ---------------- Sets ----------------

    fun observeSessionSets(sessionId: String?): Flow<List<SetEntity>> =
        if (sessionId == null) emptyFlow() else sets.observeForSession(sessionId)

    fun observeExerciseSets(exerciseId: String): Flow<List<SetEntity>> = sets.observeForExercise(exerciseId)

    fun observePrExercises(): Flow<List<PrExerciseRow>> = sets.observePrExercises()

    suspend fun lastSet(exerciseId: String): SetEntity? = sets.lastSet(exerciseId)

    suspend fun bestKg(exerciseId: String): Double? = sets.bestKg(exerciseId)

    /**
     * Logs a set into [dayId]'s session (starting it if needed). A PR is the heaviest weight ever
     * lifted for the exercise; matching the old best is not a new PR.
     */
    suspend fun logSet(dayId: String, exerciseId: String, weight: Double, unit: WeightUnit, reps: Int): LogResult =
        db.withTransaction {
            val session = startDay(dayId)
            val now = clock()
            val previousBest = sets.bestKg(exerciseId)
            val kg = toKg(weight, unit)
            val set = SetEntity(
                id = newId(),
                sessionId = session.id,
                exerciseId = exerciseId,
                weightKg = kg,
                enteredWeight = weight,
                enteredUnit = unit,
                reps = reps,
                performedAt = now,
                updatedAt = now,
            )
            sets.insert(set)
            LogResult(set, isNewPr = previousBest != null && kg > previousBest + 1e-6, previousBestKg = previousBest)
        }

    suspend fun deleteSet(setId: String) {
        val set = sets.get(setId) ?: return
        val now = clock()
        sets.upsert(set.copy(deletedAt = now, updatedAt = now))
    }
}
