package com.alkapa.circuloquintas.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FunctionsTest {

    private fun degree(k: Key, index: Int) = k.diatonicField(ChordLevel.TRIADS)[index - 1]

    @Test
    fun funcionesEnMayor() {
        val k = key("C", ScaleType.MAJOR)
        for ((i, expected) in mapOf(
            1 to TonalFunction.TONIC, 3 to TonalFunction.TONIC, 6 to TonalFunction.TONIC,
            2 to TonalFunction.SUBDOMINANT, 4 to TonalFunction.SUBDOMINANT,
            5 to TonalFunction.DOMINANT, 7 to TonalFunction.DOMINANT,
        )) {
            assertEquals(expected, degree(k, i).tonalFunction, "Grado $i")
        }
        assertFalse(degree(k, 1).isAmbiguous)
        assertTrue(degree(k, 3).isAmbiguous)
        assertTrue(degree(k, 6).isAmbiguous)
        assertFalse(degree(k, 2).isAmbiguous)
        assertFalse(degree(k, 4).isAmbiguous)
        assertFalse(degree(k, 5).isAmbiguous)
        assertTrue(degree(k, 7).isAmbiguous)
    }

    @Test
    fun funcionesEnMenorNatural() {
        val k = key("A", ScaleType.NATURAL_MINOR)
        assertEquals(TonalFunction.TONIC, degree(k, 1).tonalFunction)
        assertEquals(TonalFunction.SUBDOMINANT, degree(k, 2).tonalFunction)
        assertEquals(TonalFunction.TONIC, degree(k, 3).tonalFunction)
        assertEquals(TonalFunction.SUBDOMINANT, degree(k, 4).tonalFunction)
        // v natural: dominante ambigua (tensión suave, sin sensible)
        assertEquals(TonalFunction.DOMINANT, degree(k, 5).tonalFunction)
        assertTrue(degree(k, 5).isAmbiguous)
        assertEquals(TonalFunction.SUBDOMINANT, degree(k, 6).tonalFunction)
        assertTrue(degree(k, 6).isAmbiguous)
        assertEquals(TonalFunction.DOMINANT, degree(k, 7).tonalFunction)
        assertTrue(degree(k, 7).isAmbiguous)
    }

    @Test
    fun funcionesEnMenorArmonica() {
        val k = key("A", ScaleType.HARMONIC_MINOR)
        // V armónica: dominante fuerte (aparece la sensible), NO ambigua
        assertEquals(TonalFunction.DOMINANT, degree(k, 5).tonalFunction)
        assertFalse(degree(k, 5).isAmbiguous)
        // III+ sin función clásica clara
        assertNull(degree(k, 3).tonalFunction)
        assertTrue(degree(k, 3).isAmbiguous)
        assertEquals(TonalFunction.DOMINANT, degree(k, 7).tonalFunction)
        assertFalse(degree(k, 7).isAmbiguous)
    }

    @Test
    fun funcionesEnMenorMelodica() {
        val k = key("A", ScaleType.MELODIC_MINOR)
        assertEquals(TonalFunction.SUBDOMINANT, degree(k, 2).tonalFunction)
        assertNull(degree(k, 3).tonalFunction)
        assertEquals(TonalFunction.SUBDOMINANT, degree(k, 4).tonalFunction)
        assertTrue(degree(k, 4).isAmbiguous, "IV melódica: subdominante con color dominante")
        assertEquals(TonalFunction.DOMINANT, degree(k, 5).tonalFunction)
        assertFalse(degree(k, 5).isAmbiguous)
        assertNull(degree(k, 6).tonalFunction, "vi° puente")
        assertTrue(degree(k, 6).isAmbiguous)
    }

    @Test
    fun lenteModalDorico() {
        val k = key("D", ScaleType.DORIAN)
        val field = k.diatonicField(ChordLevel.TRIADS)
        assertEquals(ModalFunction.CENTER, field[0].modalFunction)
        // Característicos: exactamente los acordes que contienen la 6ª mayor (B en D dórico)
        val b = note("B")
        for (d in field.drop(1)) {
            val containsB = d.chord.notes.any { it.pitchClass == b.pitchClass }
            val expected = if (containsB) ModalFunction.CHARACTERISTIC else ModalFunction.NEUTRAL
            assertEquals(expected, d.modalFunction, "Grado ${d.index}")
        }
        assertEquals(
            listOf(2, 4, 6),
            field.filter { it.modalFunction == ModalFunction.CHARACTERISTIC }.map { it.index },
        )
        // En lente modal no hay función tonal
        assertTrue(field.all { it.tonalFunction == null })
    }

    @Test
    fun notaCaracteristicaPorModo() {
        assertEquals(note("B"), key("D", ScaleType.DORIAN).characteristicNote())    // 6ª mayor
        assertEquals(note("F"), key("E", ScaleType.PHRYGIAN).characteristicNote())  // 2ª menor
        assertEquals(note("B"), key("F", ScaleType.LYDIAN).characteristicNote())    // 4ª aumentada
        assertEquals(note("F"), key("G", ScaleType.MIXOLYDIAN).characteristicNote()) // 7ª menor
        assertEquals(note("F"), key("B", ScaleType.LOCRIAN).characteristicNote())   // 5ª disminuida
        assertNull(key("C", ScaleType.MAJOR).characteristicNote())
    }

    @Test
    fun jonicoYEolicoUsanLenteTonal() {
        val ionian = key("C", ScaleType.IONIAN).diatonicField(ChordLevel.TRIADS)
        assertEquals(Lens.TONAL, ScaleType.IONIAN.lens)
        assertEquals(TonalFunction.TONIC, ionian[0].tonalFunction)
        assertNull(ionian[0].modalFunction)

        val aeolian = key("A", ScaleType.AEOLIAN).diatonicField(ChordLevel.TRIADS)
        assertEquals(Lens.TONAL, ScaleType.AEOLIAN.lens)
        assertEquals(TonalFunction.DOMINANT, aeolian[4].tonalFunction)
        assertTrue(aeolian[4].isAmbiguous)
        assertNull(aeolian[4].modalFunction)
    }
}
