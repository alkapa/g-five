package com.alkapa.circuloquintas.data.db

import com.alkapa.circuloquintas.content.FamousProgressions
import com.alkapa.circuloquintas.domain.ChordQuality
import com.alkapa.circuloquintas.domain.Key
import com.alkapa.circuloquintas.domain.Note
import com.alkapa.circuloquintas.domain.Progression
import com.alkapa.circuloquintas.domain.ProgressionChord
import com.alkapa.circuloquintas.domain.ScaleType
import com.alkapa.circuloquintas.domain.repo.ProgressionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomProgressionRepository(
    private val dao: ProgressionDao,
    private val now: () -> Long = { System.currentTimeMillis() },
) : ProgressionRepository {

    private val seedMutex = Mutex()
    private var seeded = false

    override fun observeAll(): Flow<List<Progression>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun get(id: Long): Progression? = dao.get(id)?.toDomain()

    override suspend fun save(progression: Progression): Long {
        val timestamp = now()
        val entity = ProgressionEntity(
            id = progression.id ?: 0L,
            name = progression.name,
            tonicLetter = progression.key.tonic.letter.toString(),
            tonicAccidental = progression.key.tonic.accidental,
            scaleType = progression.key.scale.name,
            bpm = progression.bpm,
            readOnly = progression.readOnly,
            createdAt = timestamp,
            updatedAt = timestamp,
        )
        val chords = progression.chords.mapIndexed { i, c ->
            ProgressionChordEntity(
                progressionId = entity.id,
                position = i,
                degreeIndex = c.degreeIndex,
                quality = c.quality.name,
                beats = c.beats,
            )
        }
        return dao.upsertWithChords(entity, chords)
    }

    override suspend fun rename(id: Long, newName: String) {
        dao.rename(id, newName, now())
    }

    override suspend fun duplicate(id: Long, newName: String): Long {
        val original = get(id) ?: return -1L
        return save(original.copy(id = null, name = newName, readOnly = false))
    }

    override suspend fun delete(id: Long) {
        dao.delete(id)
    }

    /** Siembra las 9 famosas (readOnly) en la primera ejecución (§9). Idempotente. */
    override suspend fun seedDefaults() {
        seedMutex.withLock {
            if (seeded) return
            if (dao.count() == 0) {
                for (famous in FamousProgressions.all) {
                    save(famous.progression)
                }
            }
            seeded = true
        }
    }

    private fun ProgressionWithChords.toDomain(): Progression = Progression(
        id = progression.id,
        name = progression.name,
        key = Key(
            Note(progression.tonicLetter.first(), progression.tonicAccidental),
            ScaleType.valueOf(progression.scaleType),
        ),
        bpm = progression.bpm,
        chords = chords.sortedBy { it.position }.map {
            ProgressionChord(it.degreeIndex, ChordQuality.valueOf(it.quality), it.beats)
        },
        readOnly = progression.readOnly,
    )
}
