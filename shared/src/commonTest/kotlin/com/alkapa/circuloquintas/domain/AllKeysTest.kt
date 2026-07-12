package com.alkapa.circuloquintas.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AllKeysTest {

    @Test
    fun las24TonalidadesTonalesGeneranCamposCompletos() {
        val majors = CircleOfFifths.sectors.flatMap { listOfNotNull(it.major, it.majorAlt) }
        val minors = CircleOfFifths.sectors.flatMap { listOfNotNull(it.minor, it.minorAlt) }
        for (tonic in majors) {
            for (level in ChordLevel.entries) {
                val field = Key(tonic, ScaleType.MAJOR).diatonicField(level)
                assertEquals(7, field.size, "${am(tonic)} mayor $level")
            }
        }
        for (tonic in minors) {
            for (scale in listOf(ScaleType.NATURAL_MINOR, ScaleType.HARMONIC_MINOR, ScaleType.MELODIC_MINOR)) {
                for (level in ChordLevel.entries) {
                    val field = Key(tonic, scale).diatonicField(level)
                    assertEquals(7, field.size, "${am(tonic)} $scale $level")
                }
            }
        }
    }

    @Test
    fun losSieteModosGeneranCamposCompletos() {
        val modes = listOf(
            ScaleType.IONIAN, ScaleType.DORIAN, ScaleType.PHRYGIAN, ScaleType.LYDIAN,
            ScaleType.MIXOLYDIAN, ScaleType.AEOLIAN, ScaleType.LOCRIAN,
        )
        // Los 7 modos con tónica C…
        for (mode in modes) {
            val field = Key(note("C"), mode).diatonicField(ChordLevel.SEVENTHS)
            assertEquals(7, field.size, "C $mode")
        }
        // …y los 7 modos derivados de la escala de C mayor.
        val cMajor = key("C", ScaleType.MAJOR).notes()
        for ((i, mode) in modes.withIndex()) {
            val k = Key(cMajor[i], mode)
            assertEquals(7, k.diatonicField(ChordLevel.TRIADS).size, "${am(cMajor[i])} $mode")
            assertEquals(cMajor.toSet(), k.notes().toSet(), "${am(cMajor[i])} $mode debe usar las notas de C mayor")
        }
    }

    @Test
    fun arcoContiguoDeQuintasParaMayorYModos() {
        // Hecho visual pedagógico (§6.2): mayor y modos ocupan 7 posiciones
        // contiguas del círculo; armónica/melódica no.
        assertNotNull(contiguousArcStart(key("C", ScaleType.MAJOR).circlePositions()))
        assertNotNull(contiguousArcStart(key("D", ScaleType.DORIAN).circlePositions()))
        assertNotNull(contiguousArcStart(key("A", ScaleType.NATURAL_MINOR).circlePositions()))
        assertNull(contiguousArcStart(key("A", ScaleType.HARMONIC_MINOR).circlePositions()))
        assertNull(contiguousArcStart(key("A", ScaleType.MELODIC_MINOR).circlePositions()))
        // C mayor: el arco arranca en F (posición 11)
        assertEquals(11, contiguousArcStart(key("C", ScaleType.MAJOR).circlePositions()))
    }

    @Test
    fun midiDentroDelRangoEsperado() {
        val k = key("C", ScaleType.MAJOR)
        assertEquals(48, MidiMapper.tonicMidi(k.tonic))
        assertEquals(listOf(48, 50, 52, 53, 55, 57, 59, 60), MidiMapper.scaleMidis(k))
        val g7 = k.diatonicField(ChordLevel.SEVENTHS)[4].chord
        assertEquals(listOf(55, 59, 62, 65), MidiMapper.chordMidis(k, g7))
        // Tónica siempre en 48..59
        for (sector in CircleOfFifths.sectors) {
            assertTrue(MidiMapper.tonicMidi(sector.major) in 48..59)
        }
    }
}
