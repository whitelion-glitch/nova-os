package com.novaos.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(name = "nova_launcher_prefs")

class LauncherPreferences(private val context: Context) {
    enum class ThemeMode { MIDNIGHT, GRAPHITE, OLED }

    private object Keys {
        val theme = stringPreferencesKey("theme_mode")
    }

    val themeMode: Flow<ThemeMode> = context.launcherDataStore.data.map { prefs ->
        val value = prefs[Keys.theme] ?: ThemeMode.MIDNIGHT.name
        ThemeMode.valueOf(value)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.theme] = mode.name
        }
    }
}
