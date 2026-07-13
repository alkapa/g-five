package com.alkapa.circuloquintas.domain.repo

import com.alkapa.circuloquintas.domain.Notation
import com.alkapa.circuloquintas.domain.Progression
import com.alkapa.circuloquintas.domain.ScaleType
import kotlinx.coroutines.flow.Flow

/**
 * Preferencias persistidas (§9, adaptadas al refactor 2a): cifrado, paleta de
 * funciones, mostrar grados, y última sesión (raíz por clase de altura +
 * escala + Tríadas/Séptimas — el deletreo lo decide WheelModel).
 */
data class UserPreferences(
    val notation: Notation = Notation.AMERICAN,
    val palette: Int = 0,
    val showDegrees: Boolean = true,
    val lastRootPc: Int = 0,
    val lastScale: ScaleType = ScaleType.MAJOR,
    val lastSeventh: Boolean = false,
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

    /** Siembra idempotente del contenido precargado (9 famosas, §9). */
    suspend fun seedDefaults()
}
