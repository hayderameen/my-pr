package com.hayderameen.mypr.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hayderameen.mypr.domain.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

data class Settings(val unit: WeightUnit, val onboarded: Boolean)

class SettingsStore(private val context: Context) {
    private val unitKey = stringPreferencesKey("unit")
    private val onboardedKey = booleanPreferencesKey("onboarded")
    private val catalogVersionKey = intPreferencesKey("catalog_version")
    private val presetsSeededKey = booleanPreferencesKey("presets_seeded")

    val settings: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            unit = p[unitKey]?.let { runCatching { WeightUnit.valueOf(it) }.getOrNull() } ?: WeightUnit.KG,
            onboarded = p[onboardedKey] ?: false,
        )
    }

    val unit: Flow<WeightUnit> = settings.map { it.unit }

    suspend fun setUnit(unit: WeightUnit) {
        context.dataStore.edit { it[unitKey] = unit.name }
    }

    suspend fun completeOnboarding(unit: WeightUnit) {
        context.dataStore.edit {
            it[unitKey] = unit.name
            it[onboardedKey] = true
        }
    }

    suspend fun catalogVersion(): Int = context.dataStore.data.first()[catalogVersionKey] ?: 0
    suspend fun setCatalogVersion(v: Int) {
        context.dataStore.edit { it[catalogVersionKey] = v }
    }

    suspend fun presetsSeeded(): Boolean = context.dataStore.data.first()[presetsSeededKey] ?: false
    suspend fun setPresetsSeeded() {
        context.dataStore.edit { it[presetsSeededKey] = true }
    }
}
