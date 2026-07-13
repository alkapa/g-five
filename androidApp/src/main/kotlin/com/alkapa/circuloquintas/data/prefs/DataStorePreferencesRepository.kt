package com.alkapa.circuloquintas.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alkapa.circuloquintas.domain.Notation
import com.alkapa.circuloquintas.domain.ScaleType
import com.alkapa.circuloquintas.domain.repo.PreferencesRepository
import com.alkapa.circuloquintas.domain.repo.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "circulo_prefs")

/** Preferencias del refactor 2a: cifrado, paleta, grados y última sesión. */
class DataStorePreferencesRepository(private val context: Context) : PreferencesRepository {

    private object Keys {
        val notation = stringPreferencesKey("notation")
        val palette = intPreferencesKey("palette")
        val showDegrees = booleanPreferencesKey("showDegrees")
        val lastRootPc = intPreferencesKey("lastRootPc")
        val lastScale = stringPreferencesKey("lastScale")
        val lastSeventh = booleanPreferencesKey("lastSeventh")
    }

    override val preferences: Flow<UserPreferences> = context.dataStore.data.map { p ->
        UserPreferences(
            notation = p[Keys.notation]?.let { runCatching { Notation.valueOf(it) }.getOrNull() }
                ?: Notation.AMERICAN,
            palette = (p[Keys.palette] ?: 0).coerceIn(0, 2),
            showDegrees = p[Keys.showDegrees] ?: true,
            lastRootPc = (p[Keys.lastRootPc] ?: 0).mod(12),
            lastScale = p[Keys.lastScale]?.let { runCatching { ScaleType.valueOf(it) }.getOrNull() }
                ?: ScaleType.MAJOR,
            lastSeventh = p[Keys.lastSeventh] ?: false,
        )
    }

    override suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        val next = transform(preferences.first())
        context.dataStore.edit { p ->
            p[Keys.notation] = next.notation.name
            p[Keys.palette] = next.palette
            p[Keys.showDegrees] = next.showDegrees
            p[Keys.lastRootPc] = next.lastRootPc
            p[Keys.lastScale] = next.lastScale.name
            p[Keys.lastSeventh] = next.lastSeventh
        }
    }
}
