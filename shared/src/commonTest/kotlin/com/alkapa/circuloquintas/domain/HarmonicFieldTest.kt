package com.alkapa.circuloquintas.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class HarmonicFieldTest {

    @Test
    fun campoCMayorTriadas() {
        val k = key("C", ScaleType.MAJOR)
        assertEquals(listOf("C", "Dm", "Em", "F", "G", "Am", "B°"), fieldNames(k))
        assertEquals(listOf("I", "ii", "iii", "IV", "V", "vi", "vii°"), fieldRomans(k))
    }

    @Test
    fun campoAmNatural() {
        val k = key("A", ScaleType.NATURAL_MINOR)
        assertEquals(listOf("Am", "B°", "C", "Dm", "Em", "F", "G"), fieldNames(k))
        assertEquals(listOf("i", "ii°", "III", "iv", "v", "VI", "VII"), fieldRomans(k))
    }

    @Test
    fun campoAmArmonica() {
        val k = key("A", ScaleType.HARMONIC_MINOR)
        assertEquals(listOf("Am", "B°", "C+", "Dm", "E", "F", "G#°"), fieldNames(k))
        assertEquals(listOf("i", "ii°", "III+", "iv", "V", "VI", "vii°"), fieldRomans(k))
    }

    @Test
    fun campoAmMelodica() {
        val k = key("A", ScaleType.MELODIC_MINOR)
        assertEquals(listOf("Am", "Bm", "C+", "D", "E", "F#°", "G#°"), fieldNames(k))
        assertEquals(listOf("i", "ii", "III+", "IV", "V", "vi°", "vii°"), fieldRomans(k))
    }

    @Test
    fun campoDDorico() {
        val k = key("D", ScaleType.DORIAN)
        assertEquals(listOf("Dm", "Em", "F", "G", "Am", "B°", "C"), fieldNames(k))
        assertEquals(listOf("i", "ii", "III", "IV", "v", "vi°", "VII"), fieldRomans(k))
    }

    @Test
    fun campoGMixolidio() {
        val k = key("G", ScaleType.MIXOLYDIAN)
        assertEquals(listOf("G", "Am", "B°", "C", "Dm", "Em", "F"), fieldNames(k))
        assertEquals(listOf("I", "ii", "iii°", "IV", "v", "vi", "VII"), fieldRomans(k))
    }

    @Test
    fun cuatriadasCMayor() {
        val field = key("C", ScaleType.MAJOR).diatonicField(ChordLevel.SEVENTHS)
        assertEquals("G7", am(field[4].chord))
        assertEquals("Bm7b5", am(field[6].chord))
        assertEquals(
            listOf("Imaj7", "iim7", "iiim7", "IVmaj7", "V7", "vim7", "viim7b5"),
            field.map { it.roman },
        )
    }

    @Test
    fun cuatriadasAmNatural() {
        val field = key("A", ScaleType.NATURAL_MINOR).diatonicField(ChordLevel.SEVENTHS)
        assertEquals(
            listOf("im7", "iim7b5", "IIImaj7", "ivm7", "vm7", "VImaj7", "VII7"),
            field.map { it.roman },
        )
    }

    @Test
    fun cuatriadasAmArmonica() {
        val field = key("A", ScaleType.HARMONIC_MINOR).diatonicField(ChordLevel.SEVENTHS)
        assertEquals("AmMaj7", am(field[0].chord))
        assertEquals("G#°7", am(field[6].chord))
        assertEquals(
            listOf("imMaj7", "iim7b5", "IIImaj7#5", "ivm7", "V7", "VImaj7", "vii°7"),
            field.map { it.roman },
        )
    }

    @Test
    fun cuatriadasAmMelodica() {
        val field = key("A", ScaleType.MELODIC_MINOR).diatonicField(ChordLevel.SEVENTHS)
        assertEquals(
            listOf("imMaj7", "iim7", "IIImaj7#5", "IV7", "V7", "vim7b5", "viim7b5"),
            field.map { it.roman },
        )
    }

    @Test
    fun cuatriadasDorico() {
        val field = key("D", ScaleType.DORIAN).diatonicField(ChordLevel.SEVENTHS)
        assertEquals(
            listOf("im7", "iim7", "IIImaj7", "IV7", "vm7", "vim7b5", "VIImaj7"),
            field.map { it.roman },
        )
    }
}
