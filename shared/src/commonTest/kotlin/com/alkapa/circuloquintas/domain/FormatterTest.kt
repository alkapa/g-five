package com.alkapa.circuloquintas.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatterTest {

    @Test
    fun cifradoAmericanoYLatinoDeAcorde() {
        val chord = ChordBuilder.build(note("F#"), ChordQuality.MIN7B5)
        assertEquals("F#m7b5", DefaultNoteFormatter.format(chord, Notation.AMERICAN))
        assertEquals("Fa# m7b5", DefaultNoteFormatter.format(chord, Notation.LATIN))
    }

    @Test
    fun escalaDeCMayorEnLatino() {
        val names = key("C", ScaleType.MAJOR).notes()
            .map { DefaultNoteFormatter.format(it, Notation.LATIN) }
        assertEquals(listOf("Do", "Re", "Mi", "Fa", "Sol", "La", "Si"), names)
    }

    @Test
    fun triadaMayorLatinaSinSufijo() {
        val c = ChordBuilder.build(note("C"), ChordQuality.MAJOR)
        assertEquals("Do", DefaultNoteFormatter.format(c, Notation.LATIN))
        val am = ChordBuilder.build(note("A"), ChordQuality.MINOR)
        assertEquals("La m", DefaultNoteFormatter.format(am, Notation.LATIN))
    }

    @Test
    fun toggleEnarmonicoGbProduceSeisBemoles() {
        val alt = CircleOfFifths.enharmonicAlternative(note("F#"))
        assertEquals(note("Gb"), alt)
        val flats = Key(alt!!, ScaleType.MAJOR).notes().count { it.accidental < 0 }
        assertEquals(6, flats)
        // Y la vuelta: F# mayor tiene 6 sostenidos
        val sharps = key("F#", ScaleType.MAJOR).notes().count { it.accidental > 0 }
        assertEquals(6, sharps)
        // Par enarmónico menor
        assertEquals(note("Eb"), CircleOfFifths.enharmonicAlternative(note("D#")))
        assertEquals(note("D#"), CircleOfFifths.enharmonicAlternative(note("Eb")))
    }

    @Test
    fun dobleAccidenteSeFormatea() {
        assertEquals("F##", am(note("F##")))
        assertEquals("Bbb", am(note("Bbb")))
    }
}
