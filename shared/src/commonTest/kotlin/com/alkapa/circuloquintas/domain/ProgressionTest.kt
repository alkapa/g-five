package com.alkapa.circuloquintas.domain

import com.alkapa.circuloquintas.content.FamousProgressions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProgressionTest {

    @Test
    fun transporteDeCAGProduceGDEmC() {
        val pop = Progression(
            id = null, name = "Pop", key = key("C", ScaleType.MAJOR), bpm = 90,
            chords = listOf(
                ProgressionChord(1, ChordQuality.MAJOR),
                ProgressionChord(5, ChordQuality.MAJOR),
                ProgressionChord(6, ChordQuality.MINOR),
                ProgressionChord(4, ChordQuality.MAJOR),
            ),
        )
        assertEquals(listOf("C", "G", "Am", "F"), pop.renderChords().map { am(it) })
        val transposed = pop.transposeTo(note("G"))
        assertEquals(listOf("G", "D", "Em", "C"), transposed.renderChords().map { am(it) })
    }

    @Test
    fun famosasSembradasNueveYSoloLectura() {
        assertEquals(9, FamousProgressions.all.size)
        assertTrue(FamousProgressions.all.all { it.progression.readOnly })
        assertTrue(FamousProgressions.all.all { it.ficha.isNotBlank() })
    }

    @Test
    fun cadenciaAndaluzaTieneVMayorPrestado() {
        val andaluza = FamousProgressions.all.first { it.name == "Cadencia andaluza" }
        assertEquals(listOf("Am", "G", "F", "E"), andaluza.progression.renderChords().map { am(it) })
        // La ficha explica el préstamo de la menor armónica
        assertTrue(andaluza.ficha.contains("armónica", ignoreCase = true))
    }

    @Test
    fun vampsModalesRenderizanCorrecto() {
        val dorico = FamousProgressions.all.first { it.name == "Vamp dórico" }
        assertEquals(listOf("Dm", "G"), dorico.progression.renderChords().map { am(it) })
        val mixolidio = FamousProgressions.all.first { it.name == "Vamp mixolidio" }
        assertEquals(listOf("G", "F"), mixolidio.progression.renderChords().map { am(it) })
        assertTrue(mixolidio.ficha.contains("bVII"))
    }

    @Test
    fun transporteDeFamosaDuplicada() {
        // "Duplicar" una famosa la vuelve editable; transportarla mantiene grados.
        val jazz = FamousProgressions.all.first { it.romanSummary == "ii–V–I" }
        val copy = jazz.progression.copy(id = null, readOnly = false).transposeTo(note("F"))
        assertEquals(listOf("Gm7", "C7", "Fmaj7"), copy.renderChords().map { am(it) })
    }
}
