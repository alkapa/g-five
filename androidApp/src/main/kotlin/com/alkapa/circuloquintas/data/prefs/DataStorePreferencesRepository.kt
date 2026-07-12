package com.alkapa.circuloquintas.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alkapa.circuloquintas.domain.ChordLevel
import com.alkapa.circuloquintas.domain.Notation
import com.alkapa.circuloquintas.domain.Note
import com.alkapa.circuloquintas.domain.ScaleType
import com.alkapa.circuloquintas.domain.repo.CircleLayer
import com.alkapa.circuloquintas.domain.repo.PreferencesRepository
import com.alkapa.circuloquintas.domain.repo.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "circulo_prefs")

/** Preferencias §9: cifrado, enarmonía default y última sesión. */
class DataStorePreferencesRepository(private val context: Context) : PreferencesRepository {

    private object Keys {
        val notation = stringPreferencesKey("notation")
        val preferFlat = booleanPreferencesKey("enharmonicDefaultFlat")
        val lastTonicLetter = stringPreferencesKey("lastTonicLetter")
        val lastTonicAccidental = intPreferencesKey("lastTonicAccidental")
        val lastScale = stringPreferencesKey("lastScale")
        val lastChordLevel = stringPreferencesKey("lastChordLevel")
        val activeLayers = stringPreferencesKey("activeLayers")
    }

    override val preferences: Flow<UserPreferences> = context.dataStore.data.map { p ->
        // Estado inicial de primera apertura (§6.1): C mayor, tríadas, capa
        // Funciones activa, cifrado americano.
        UserPreferences(
            notation = p[Keys.notation]?.let { runCatching { Notation.valueOf(it) }.getOrNull() }
                ?: Notation.AMERICAN,
            preferFlatEnharmonic = p[Keys.preferFlat] ?: false,
            lastTonic = Note(
                (p[Keys.lastTonicLetter] ?: "C").first(),
                p[Keys.lastTonicAccidental] ?: 0,
            ),
            lastScale = p[Keys.lastScale]?.let { runCatching { ScaleType.valueOf(it) }.getOrNull() }
                ?: ScaleType.MAJOR,
            lastChordLevel = p[Keys.lastChordLevel]
                ?.let { runCatching { ChordLevel.valueOf(it) }.getOrNull() }
                ?: ChordLevel.TRIADS,
            activeLayers = (p[Keys.activeLayers] ?: CircleLayer.FUNCTIONS.name)
                .split(',')
                .filter { it.isNotBlank() }
                .mapNotNull { name -> runCatching { CircleLayer.valueOf(name) }.getOrNull() }
                .toSet(),
        )
    }

    override suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        val current = preferences.first()
        val next = transform(current)
        context.dataStore.edit { p ->
            p[Keys.notation] = next.notation.name
            p[Keys.preferFlat] = next.preferFlatEnharmonic
            p[Keys.lastTonicLetter] = next.lastTonic.letter.toString()
            p[Keys.lastTonicAccidental] = next.lastTonic.accidental
            p[Keys.lastScale] = next.lastScale.name
            p[Keys.lastChordLevel] = next.lastChordLevel.name
            p[Keys.activeLayers] = next.activeLayers.joinToString(",") { it.name }
        }
    }
}
