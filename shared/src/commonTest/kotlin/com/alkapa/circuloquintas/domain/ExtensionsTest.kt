package com.alkapa.circuloquintas.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExtensionsTest {

    private fun extensionsOf(k: Key, degreeIndex: Int): List<Chord> =
        k.diatonicField(ChordLevel.EXTENSIONS)[degreeIndex - 1].extensions

    @Test
    fun cadd9ValidoSinPrecaucion() {
        val exts = extensionsOf(key("C", ScaleType.MAJOR), 1)
        val cadd9 = exts.first { it.quality == ChordQuality.ADD9 }
        assertEquals("Cadd9", am(cadd9))
        assertFalse(cadd9.caution)
    }

    @Test
    fun oncenaNaturalSobreTerceraMayorGeneraPrecaucion() {
        // Regla §3.5 directa: 11ª natural (17) contra 3ª mayor (4) = 9ª menor.
        assertTrue(hasMinorNinthClash(listOf(0, 4, 7, 10, 14, 17, 21)))
        // Y el caso generado: G13 en C mayor incluye la 11ª de la escala (C natural)
        // sobre la 3ª mayor (B) → se muestra con precaución, no se oculta.
        val g13 = extensionsOf(key("C", ScaleType.MAJOR), 5)
            .first { it.quality == ChordQuality.THIRTEEN }
        assertEquals("G13", am(g13))
        assertTrue(g13.caution)
    }

    @Test
    fun sinNotasFueraDeLaEscalaActiva() {
        // Restricción §3.5: nada fuera de la escala en niveles 1-3.
        for (scale in listOf(
            ScaleType.MAJOR, ScaleType.NATURAL_MINOR, ScaleType.HARMONIC_MINOR,
            ScaleType.MELODIC_MINOR, ScaleType.DORIAN, ScaleType.MIXOLYDIAN,
        )) {
            val k = key(if (scale == ScaleType.MAJOR || scale.modeIndex != null) "C" else "A", scale)
            val scalePcs = k.notes().map { it.pitchClass }.toSet()
            for (degree in k.diatonicField(ChordLevel.EXTENSIONS)) {
                for (ext in degree.extensions) {
                    assertTrue(
                        ext.notes.all { it.pitchClass in scalePcs },
                        "$scale ${degree.roman} ${am(ext)} usa notas fuera de la escala",
                    )
                }
            }
        }
    }

    @Test
    fun vocabularioEnCMayor() {
        val k = key("C", ScaleType.MAJOR)
        val byDegree = k.diatonicField(ChordLevel.EXTENSIONS).associate { d ->
            d.index to d.extensions.map { am(it) }
        }
        // I: color de tónica mayor
        assertTrue("C6" in byDegree.getValue(1))
        assertTrue("C6/9" in byDegree.getValue(1))
        assertTrue("Cadd9" in byDegree.getValue(1))
        assertTrue("Cmaj9" in byDegree.getValue(1))
        assertTrue("Csus2" in byDegree.getValue(1))
        assertTrue("Csus4" in byDegree.getValue(1))
        // ii: menor con 9ª y 11ª
        assertTrue("Dm9" in byDegree.getValue(2))
        assertTrue("Dm11" in byDegree.getValue(2))
        // V: dominante con 9ª y 7sus4
        assertTrue("G9" in byDegree.getValue(5))
        assertTrue("G7sus4" in byDegree.getValue(5))
        // iii (Em): la 9ª de la escala es menor (F) → sin m9 ni add9
        assertFalse(byDegree.getValue(3).any { it.contains("m9") || it.contains("add9") })
    }

    @Test
    fun m6EsMarcaDorica() {
        // En D dórico el i admite m6 (6ª mayor); en A menor natural el i no.
        val doricoI = extensionsOf(key("D", ScaleType.DORIAN), 1)
        assertTrue(doricoI.any { it.quality == ChordQuality.MIN6 })
        val naturalI = extensionsOf(key("A", ScaleType.NATURAL_MINOR), 1)
        assertFalse(naturalI.any { it.quality == ChordQuality.MIN6 })
    }

    @Test
    fun m11SobreIiiOmiteLaNovenaMenor() {
        // Em11 en C mayor: la 9ª de la escala sobre E es F (menor) → se omite
        // del voicing y no hay choque de 9ª menor.
        val em11 = extensionsOf(key("C", ScaleType.MAJOR), 3)
            .firstOrNull { it.quality == ChordQuality.MIN11 }
        assertTrue(em11 != null)
        assertFalse(em11.caution)
        assertFalse(em11.stackOffsets.contains(13))
    }
}
