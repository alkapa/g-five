package com.alkapa.circuloquintas.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScaleSpellingTest {

    @Test
    fun cMayor() {
        assertEquals(listOf("C", "D", "E", "F", "G", "A", "B"), noteNames(key("C", ScaleType.MAJOR)))
    }

    @Test
    fun fMayorContieneBbNuncaASostenido() {
        val notes = key("F", ScaleType.MAJOR).notes()
        assertTrue(note("Bb") in notes)
        assertTrue(note("A#") !in notes)
    }

    @Test
    fun dMayorContieneFSostenidoNuncaGb() {
        val notes = key("D", ScaleType.MAJOR).notes()
        assertTrue(note("F#") in notes)
        assertTrue(note("Gb") !in notes)
    }

    @Test
    fun aMenorNatural() {
        assertEquals(
            listOf("A", "B", "C", "D", "E", "F", "G"),
            noteNames(key("A", ScaleType.NATURAL_MINOR)),
        )
    }

    @Test
    fun gSostenidoMenorArmonicaContieneFDobleSostenido() {
        val notes = key("G#", ScaleType.HARMONIC_MINOR).notes()
        assertTrue(note("F##") in notes, "G# menor armónica debe contener F## — tiene: ${notes.map { am(it) }}")
    }

    @Test
    fun aMenorMelodica() {
        assertEquals(
            listOf("A", "B", "C", "D", "E", "F#", "G#"),
            noteNames(key("A", ScaleType.MELODIC_MINOR)),
        )
    }

    @Test
    fun dDoricoMismasNotasQueCMayor() {
        val dorico = key("D", ScaleType.DORIAN).notes().toSet()
        val cMayor = key("C", ScaleType.MAJOR).notes().toSet()
        assertEquals(cMayor, dorico)
    }

    @Test
    fun fLidioContieneBNatural() {
        val notes = key("F", ScaleType.LYDIAN).notes()
        assertTrue(note("B") in notes)
        assertTrue(note("Bb") !in notes)
    }

    @Test
    fun parentKeyDeModos() {
        assertEquals(key("C", ScaleType.MAJOR), key("D", ScaleType.DORIAN).parentKey())
        assertEquals(key("C", ScaleType.MAJOR), key("G", ScaleType.MIXOLYDIAN).parentKey())
        assertEquals(key("C", ScaleType.MAJOR), key("A", ScaleType.AEOLIAN).parentKey())
        assertEquals(key("C", ScaleType.MAJOR), key("C", ScaleType.IONIAN).parentKey())
        assertNull(key("C", ScaleType.MAJOR).parentKey())
        assertNull(key("A", ScaleType.HARMONIC_MINOR).parentKey())
    }

    @Test
    fun modosPorRotacionCoincidenConSuPatron() {
        // La spec exige derivar modos por rotación; el patrón §3.2 queda como
        // referencia cruzada: ambos caminos deben dar exactamente las mismas notas.
        val modes = listOf(
            ScaleType.IONIAN, ScaleType.DORIAN, ScaleType.PHRYGIAN, ScaleType.LYDIAN,
            ScaleType.MIXOLYDIAN, ScaleType.AEOLIAN, ScaleType.LOCRIAN,
        )
        for (mode in modes) {
            val rotated = key("C", mode).notes()
            val byPattern = Key.byPattern(note("C"), mode.pattern)
            assertEquals(byPattern, rotated, "Modo $mode: rotación vs patrón difieren")
        }
    }

    @Test
    fun escalasDiatonicasNoMezclanAccidentesNiRepitenLetra() {
        // Regla de grafía §3.1 para escalas derivadas de armadura (mayor y sus
        // rotaciones). Las menores armónica/melódica quedan fuera del criterio
        // "no mezclar": su grado elevado puede legítimamente mezclar (ej. D menor
        // armónica = D E F G A Bb C#).
        val majorTonics = CircleOfFifths.sectors.flatMap { listOfNotNull(it.major, it.majorAlt) }
        val diatonicScales = listOf(
            ScaleType.MAJOR, ScaleType.IONIAN, ScaleType.DORIAN, ScaleType.PHRYGIAN,
            ScaleType.LYDIAN, ScaleType.MIXOLYDIAN, ScaleType.AEOLIAN, ScaleType.LOCRIAN,
        )
        for (tonic in majorTonics) {
            for (scale in diatonicScales) {
                val notes = Key(tonic, scale).notes()
                val hasSharps = notes.any { it.accidental > 0 }
                val hasFlats = notes.any { it.accidental < 0 }
                assertTrue(
                    !(hasSharps && hasFlats),
                    "${am(tonic)} $scale mezcla # y b: ${notes.map { am(it) }}",
                )
            }
        }
        // Ninguna escala de 7 notas repite letra (incluye armónica/melódica).
        val minorTonics = CircleOfFifths.sectors.flatMap { listOfNotNull(it.minor, it.minorAlt) }
        val sevenNote = listOf(
            ScaleType.MAJOR, ScaleType.NATURAL_MINOR, ScaleType.HARMONIC_MINOR, ScaleType.MELODIC_MINOR,
        )
        for (tonic in majorTonics + minorTonics) {
            for (scale in sevenNote) {
                val letters = Key(tonic, scale).notes().map { it.letter }
                assertEquals(7, letters.toSet().size, "${am(tonic)} $scale repite letra: $letters")
            }
        }
    }
}
