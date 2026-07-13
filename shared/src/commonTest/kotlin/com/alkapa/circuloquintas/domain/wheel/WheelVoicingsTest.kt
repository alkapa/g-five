package com.alkapa.circuloquintas.domain.wheel

import com.alkapa.circuloquintas.domain.ChordBuilder
import com.alkapa.circuloquintas.domain.ChordQuality
import com.alkapa.circuloquintas.domain.note
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WheelVoicingsTest {

    private val tuningPc = listOf(4, 9, 2, 7, 11, 4)

    @Test
    fun formasEstandarConocidas() {
        assertEquals(
            listOf(-1, 3, 2, 0, 1, 0),
            WheelVoicings.standardShape(ChordBuilder.build(note("C"), ChordQuality.MAJOR)).frets,
        )
        assertEquals(
            listOf(-1, 0, 2, 2, 1, 0),
            WheelVoicings.standardShape(ChordBuilder.build(note("A"), ChordQuality.MINOR)).frets,
        )
        assertEquals(
            listOf(1, 3, 3, 2, 1, 1),
            WheelVoicings.standardShape(ChordBuilder.build(note("F"), ChordQuality.MAJOR)).frets,
        )
        // B mayor: cejilla en 2 con forma de A.
        assertEquals(
            listOf(-1, 2, 4, 4, 4, 2),
            WheelVoicings.standardShape(ChordBuilder.build(note("B"), ChordQuality.MAJOR)).frets,
        )
        // Cmaj7 abierto y G7 abierto del diccionario de séptimas.
        assertEquals(
            listOf(-1, 3, 2, 0, 0, 0),
            WheelVoicings.standardShape(ChordBuilder.build(note("C"), ChordQuality.MAJ7)).frets,
        )
        assertEquals(
            listOf(3, 2, 0, 0, 0, 1),
            WheelVoicings.standardShape(ChordBuilder.build(note("G"), ChordQuality.DOM7)).frets,
        )
    }

    @Test
    fun elDiagramaConCejillaLejosDeLaCejuelaIndicaTrasteBase() {
        val b = WheelVoicings.standardShape(ChordBuilder.build(note("B"), ChordQuality.MAJOR))
        assertEquals(1, b.baseFret) // max traste 4 → cabe con cejuela
        val c7At8 = WheelVoicings.Shape(listOf(8, 10, 10, 9, 8, 8))
        assertEquals(8, c7At8.baseFret)
    }

    @Test
    fun primeraInversionDeCEnCuerdas432() {
        val c = ChordBuilder.build(note("C"), ChordQuality.MAJOR)
        val voicings = WheelVoicings.groupVoicings(c, inv = 1)
        assertTrue(voicings.isNotEmpty())
        // La clásica C/E en cuerdas 4·3·2: trastes 2-0-1.
        val v = voicings.first { it.strings == listOf(2, 3, 4) }
        assertEquals(listOf(2, 0, 1), v.frets)
        assertEquals("4·3·2", v.label)
        // El bajo del grupo es la voz de la inversión (E).
        assertEquals(4, (tuningPc[v.strings[0]] + v.frets[0]).mod(12))
    }

    @Test
    fun losVoicingsRespetanBajoVocesYSpan() {
        val g7 = ChordBuilder.build(note("G"), ChordQuality.DOM7)
        for (inv in 0..3) {
            val expectedBass = WheelModel.inversion(g7, inv).bassPc
            for (v in WheelVoicings.groupVoicings(g7, inv)) {
                assertEquals(expectedBass, (tuningPc[v.strings[0]] + v.frets[0]).mod(12), "bajo en ${v.label}")
                val pcs = v.strings.mapIndexed { i, s -> (tuningPc[s] + v.frets[i]).mod(12) }.toSet()
                assertEquals(setOf(7, 11, 2, 5), pcs, "voces en ${v.label}")
                assertTrue(v.maxFret <= 12)
                assertTrue(v.maxFret - v.minFret <= 4, "span en ${v.label}: ${v.frets}")
            }
        }
    }

    @Test
    fun septimasUsanDrop2() {
        // G7 fundamental drop-2 en cuerdas 4·3·2·1: D como segunda voz desde abajo.
        val g7 = ChordBuilder.build(note("G"), ChordQuality.DOM7)
        val v = WheelVoicings.groupVoicings(g7, inv = 0).first { it.strings == listOf(2, 3, 4, 5) }
        assertEquals(listOf(7, 2, 5, 11), v.orderPcs) // G D F B: la 2ª voz desde arriba bajó una octava
        assertEquals(listOf(5, 7, 6, 7), v.frets)
    }

    @Test
    fun triadasOfrecenHastaCuatroGruposYSeptimasTres() {
        val c = ChordBuilder.build(note("C"), ChordQuality.MAJOR)
        assertTrue(WheelVoicings.groupVoicings(c, 0).size <= 4)
        val g7 = ChordBuilder.build(note("G"), ChordQuality.DOM7)
        assertTrue(WheelVoicings.groupVoicings(g7, 0).size <= 3)
        // Etiquetas de zona legibles.
        for (v in WheelVoicings.groupVoicings(c, 2)) {
            assertTrue(v.zoneLabel.isNotBlank())
        }
    }
}
