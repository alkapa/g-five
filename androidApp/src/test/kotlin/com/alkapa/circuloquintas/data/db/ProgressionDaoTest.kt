package com.alkapa.circuloquintas.data.db

import androidx.test.core.app.ApplicationProvider
import com.alkapa.circuloquintas.domain.ChordQuality
import com.alkapa.circuloquintas.domain.Key
import com.alkapa.circuloquintas.domain.Note
import com.alkapa.circuloquintas.domain.Progression
import com.alkapa.circuloquintas.domain.ProgressionChord
import com.alkapa.circuloquintas.domain.ScaleType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProgressionDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: RoomProgressionRepository

    @Before
    fun setUp() {
        db = AppDatabase.inMemory(ApplicationProvider.getApplicationContext())
        repo = RoomProgressionRepository(db.progressionDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun siembraNueveFamosasSoloLecturaUnaVez() = runTest {
        repo.seedDefaults()
        repo.seedDefaults() // idempotente
        val all = repo.observeAll().first()
        assertEquals(9, all.size)
        assertTrue(all.all { it.readOnly })
    }

    @Test
    fun guardarYLeerConservaElOrdenDeAcordes() = runTest {
        val id = repo.save(
            Progression(
                id = null, name = "Pop", key = Key(Note('C', 0), ScaleType.MAJOR), bpm = 96,
                chords = listOf(
                    ProgressionChord(1, ChordQuality.MAJOR, 4),
                    ProgressionChord(5, ChordQuality.MAJOR, 2),
                    ProgressionChord(6, ChordQuality.MINOR, 4),
                    ProgressionChord(4, ChordQuality.MAJOR, 8),
                ),
            ),
        )
        val loaded = repo.get(id)!!
        assertEquals("Pop", loaded.name)
        assertEquals(96, loaded.bpm)
        assertEquals(listOf(1, 5, 6, 4), loaded.chords.map { it.degreeIndex })
        assertEquals(listOf(4, 2, 4, 8), loaded.chords.map { it.beats })
        assertEquals(ScaleType.MAJOR, loaded.key.scale)
    }

    @Test
    fun actualizarReemplazaLosAcordes() = runTest {
        val id = repo.save(
            Progression(
                null, "X", Key(Note('A', 0), ScaleType.NATURAL_MINOR), 90,
                listOf(ProgressionChord(1, ChordQuality.MINOR)),
            ),
        )
        repo.save(
            Progression(
                id, "X2", Key(Note('A', 0), ScaleType.NATURAL_MINOR), 100,
                listOf(
                    ProgressionChord(1, ChordQuality.MINOR),
                    ProgressionChord(7, ChordQuality.MAJOR),
                ),
            ),
        )
        val loaded = repo.get(id)!!
        assertEquals("X2", loaded.name)
        assertEquals(2, loaded.chords.size)
        assertEquals(1, repo.observeAll().first().size)
    }

    @Test
    fun duplicarRenombrarBorrar() = runTest {
        repo.seedDefaults()
        val famous = repo.observeAll().first().first { it.name == "Vamp dórico" }
        val copyId = repo.duplicate(famous.id!!, "Mi vamp")
        val copy = repo.get(copyId)!!
        assertTrue(!copy.readOnly)
        assertEquals(famous.chords, copy.chords)

        repo.rename(copyId, "Vamp propio")
        assertEquals("Vamp propio", repo.get(copyId)!!.name)

        repo.delete(copyId)
        assertNull(repo.get(copyId))
        assertEquals(9, repo.observeAll().first().size)
    }

    @Test
    fun transporteDeGuardadaRecalculaPorGrado() = runTest {
        val id = repo.save(
            Progression(
                null, "Pop", Key(Note('C', 0), ScaleType.MAJOR), 90,
                listOf(
                    ProgressionChord(1, ChordQuality.MAJOR),
                    ProgressionChord(5, ChordQuality.MAJOR),
                    ProgressionChord(6, ChordQuality.MINOR),
                    ProgressionChord(4, ChordQuality.MAJOR),
                ),
            ),
        )
        val transposed = repo.get(id)!!.transposeTo(Note('G', 0))
        assertEquals(
            listOf("G", "D", "Em", "C"),
            transposed.renderChords().map { chord ->
                com.alkapa.circuloquintas.domain.DefaultNoteFormatter.format(
                    chord, com.alkapa.circuloquintas.domain.Notation.AMERICAN,
                )
            },
        )
    }
}
