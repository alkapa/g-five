package com.alkapa.circuloquintas.domain.repo

import com.alkapa.circuloquintas.domain.ChordLevel
import com.alkapa.circuloquintas.domain.Notation
import com.alkapa.circuloquintas.domain.Note
import com.alkapa.circuloquintas.domain.Progression
import com.alkapa.circuloquintas.domain.ScaleType
import kotlinx.coroutines.flow.Flow

/** Capas superpuestas del círculo (§6.2.4). Acordes es la base, siempre visible. */
enum class CircleLayer { DEGREES, FUNCTIONS }

/** Preferencias persistidas (§9). */
data class UserPreferences(
    val notation: Notation = Notation.AMERICAN,
    val preferFlatEnharmonic: Boolean = false,
    val lastTonic: Note = Note('C', 0),
    val lastScale: ScaleType = ScaleType.MAJOR,
    val lastChordLevel: ChordLevel = ChordLevel.TRIADS,
    val activeLayers: Set<CircleLayer> = setOf(CircleLayer.FUNCTIONS),
)

interface PreferencesRepository {
    val preferences: Flow<UserPreferences>
    suspend fun update(transform: (UserPreferences) -> UserPreferences)
}

/** CRUD de progresiones (§9). Implementación Android: Room. */
interface ProgressionRepository {
    fun observeAll(): Flow<List<Progression>>
    suspend fun get(id: Long): Progression?
    suspend fun save(progression: Progression): Long
    suspend fun rename(id: Long, newName: String)
    suspend fun duplicate(id: Long, newName: String): Long
    suspend fun delete(id: Long)
}
