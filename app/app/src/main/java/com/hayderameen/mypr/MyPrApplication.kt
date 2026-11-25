package com.hayderameen.mypr

import android.app.Application
import com.hayderameen.mypr.data.LiftRepository
import com.hayderameen.mypr.data.SettingsStore
import com.hayderameen.mypr.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async

class MyPrApplication : Application() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val settings by lazy { SettingsStore(this) }
    val database by lazy { AppDatabase.build(this) }
    val repository by lazy { LiftRepository(database, settings) }

    /** Completes once the built-in catalog and preset days are in the database. */
    lateinit var seeded: Deferred<Unit>
        private set

    override fun onCreate() {
        super.onCreate()
        seeded = appScope.async { repository.ensureSeeded() }
    }
}
