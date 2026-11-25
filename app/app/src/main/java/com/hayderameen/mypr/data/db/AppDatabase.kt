package com.hayderameen.mypr.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutDayEntity::class,
        DayExerciseEntity::class,
        SessionEntity::class,
        SetEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exercises(): ExerciseDao
    abstract fun days(): DayDao
    abstract fun sessions(): SessionDao
    abstract fun sets(): SetDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "mypr.db").build()
    }
}
