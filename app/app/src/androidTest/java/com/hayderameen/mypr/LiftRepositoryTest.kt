package com.hayderameen.mypr

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hayderameen.mypr.data.LiftRepository
import com.hayderameen.mypr.data.SettingsStore
import com.hayderameen.mypr.data.db.AppDatabase
import com.hayderameen.mypr.data.seed.BUILT_IN_EXERCISES
import com.hayderameen.mypr.data.seed.PRESET_DAYS
import com.hayderameen.mypr.domain.WeightUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LiftRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: LiftRepository
    private var now = 1_700_000_000_000L

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        // Fresh in-memory DB, so force seeding regardless of what DataStore remembers.
        repo = LiftRepository(db, SettingsStore(context)) { now++ }
        db.exercises().insertIgnore(BUILT_IN_EXERCISES.map {
            com.hayderameen.mypr.data.db.ExerciseEntity(it.id, it.name, it.equipment, it.muscleGroup, it.target, false, 0, 0)
        })
        PRESET_DAYS.forEachIndexed { i, p ->
            db.days().upsert(com.hayderameen.mypr.data.db.WorkoutDayEntity(p.id, p.name, p.iconKey, i, true, 0, 0))
            db.days().upsertDayExercises(p.exerciseIds.mapIndexed { j, ex ->
                com.hayderameen.mypr.data.db.DayExerciseEntity(p.id, ex, j.toDouble(), j.toLong(), 0)
            })
        }
    }

    @After
    fun tearDown() = db.close()

    private suspend fun dayOrder(dayId: String) = repo.observeDayExercises(dayId).first().map { it.exercise.id }

    @Test
    fun presetIdsAllExistInCatalog() {
        val ids = BUILT_IN_EXERCISES.map { it.id }.toSet()
        assertEquals("duplicate catalog ids", BUILT_IN_EXERCISES.size, ids.size)
        PRESET_DAYS.flatMap { it.exerciseIds }.forEach { assertTrue("missing $it", it in ids) }
    }

    @Test
    fun firstSetIsBaselineThenHeavierIsPr() = runBlocking {
        val first = repo.logSet("push", "barbell_bench_press", 100.0, WeightUnit.KG, 5)
        assertFalse(first.isNewPr)
        assertNull(first.previousBestKg)

        val same = repo.logSet("push", "barbell_bench_press", 100.0, WeightUnit.KG, 8)
        assertFalse("matching the best is not a PR", same.isNewPr)

        val lighter = repo.logSet("push", "barbell_bench_press", 90.0, WeightUnit.KG, 12)
        assertFalse(lighter.isNewPr)

        val heavier = repo.logSet("push", "barbell_bench_press", 102.5, WeightUnit.KG, 3)
        assertTrue(heavier.isNewPr)
        assertEquals(100.0, heavier.previousBestKg!!, 1e-9)
    }

    @Test
    fun prComparesAcrossUnits() = runBlocking {
        repo.logSet("push", "barbell_bench_press", 100.0, WeightUnit.KG, 5)
        // 225 lbs = 102.06 kg -> beats 100 kg
        assertTrue(repo.logSet("push", "barbell_bench_press", 225.0, WeightUnit.LBS, 5).isNewPr)
    }

    @Test
    fun deletedSetsDontCountTowardPr() = runBlocking {
        val big = repo.logSet("push", "barbell_bench_press", 150.0, WeightUnit.KG, 1)
        repo.deleteSet(big.set.id)
        repo.logSet("push", "barbell_bench_press", 100.0, WeightUnit.KG, 5)
        assertTrue(repo.logSet("push", "barbell_bench_press", 110.0, WeightUnit.KG, 5).isNewPr)
    }

    @Test
    fun emptySessionIsDiscardedOnEnd() = runBlocking {
        repo.startDay("pull")
        assertNotNull(repo.activeSession())
        repo.endActiveSession()
        assertNull(repo.activeSession())
        assertNull(repo.observeDays().first().first { it.day.id == "pull" }.lastTrainedAt)
    }

    @Test
    fun startingAnotherDayEndsTheCurrentOne() = runBlocking {
        repo.logSet("push", "barbell_bench_press", 100.0, WeightUnit.KG, 5)
        repo.startDay("legs")
        assertEquals("legs", repo.activeSession()!!.dayId)
        assertNotNull(repo.observeDays().first().first { it.day.id == "push" }.lastTrainedAt)
    }

    @Test
    fun endingWorkoutLearnsOrder() = runBlocking {
        val before = dayOrder("push")
        assertEquals("barbell_bench_press", before.first())
        repeat(2) {
            repo.logSet("push", "dumbbell_lateral_raise", 12.5, WeightUnit.KG, 12)
            repo.logSet("push", "barbell_bench_press", 100.0, WeightUnit.KG, 5)
            repo.endActiveSession()
        }
        assertEquals(listOf("dumbbell_lateral_raise", "barbell_bench_press"), dayOrder("push").take(2))
    }

    @Test
    fun manualReorderAndAddRemove() = runBlocking {
        repo.reorderDayExercises("push", listOf("cable_tricep_pushdown", "barbell_bench_press", "incline_dumbbell_press", "overhead_barbell_press", "dumbbell_lateral_raise"))
        assertEquals("cable_tricep_pushdown", dayOrder("push").first())

        repo.addExerciseToDay("push", "pec_deck_fly")
        assertEquals("pec_deck_fly", dayOrder("push").last())

        repo.removeExerciseFromDay("push", "pec_deck_fly")
        assertFalse("pec_deck_fly" in dayOrder("push"))

        repo.addExerciseToDay("push", "pec_deck_fly")
        assertTrue("re-adding restores it", "pec_deck_fly" in dayOrder("push"))
    }

    @Test
    fun customDayAndExercise() = runBlocking {
        val day = repo.createDay("  Glutes & Hams ", "walk")
        val ex = repo.createCustomExercise("Belt Squat", com.hayderameen.mypr.domain.Equipment.MACHINE, com.hayderameen.mypr.domain.MuscleGroup.QUADS)
        repo.addExerciseToDay(day, ex)
        val row = repo.observeDays().first().first { it.day.id == day }
        assertEquals("Glutes & Hams", row.day.name)
        assertEquals(1, row.exerciseCount)
        assertEquals(PRESET_DAYS.size, row.day.position)

        repo.deleteDay(day)
        assertTrue(repo.observeDays().first().none { it.day.id == day })
    }

    @Test
    fun prHistoryListsBestPerExercise() = runBlocking {
        repo.logSet("push", "barbell_bench_press", 100.0, WeightUnit.KG, 5)
        repo.logSet("push", "barbell_bench_press", 105.0, WeightUnit.KG, 2)
        repo.logSet("pull", "weighted_pull_up", 20.0, WeightUnit.KG, 6)
        val rows = repo.observePrExercises().first().associateBy { it.exercise.id }
        assertEquals(105.0, rows.getValue("barbell_bench_press").bestKg, 1e-9)
        assertEquals(20.0, rows.getValue("weighted_pull_up").bestKg, 1e-9)
    }
}
