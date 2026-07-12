package com.alkapa.circuloquintas.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchByNeedTest {

    @Test
    fun descansoEnCMayorDevuelveIPrimero() {
        val results = key("C", ScaleType.MAJOR).searchByNeed(Need.REST, ChordLevel.TRIADS)
        assertEquals(1, results.first().index)
        assertEquals(setOf(3, 6), results.drop(1).map { it.index }.toSet())
    }

    @Test
    fun tensionConSeptimasDevuelveG7Primero() {
        val results = key("C", ScaleType.MAJOR).searchByNeed(Need.TENSION, ChordLevel.SEVENTHS)
        assertEquals("G7", am(results.first().chord))
        assertEquals("Bm7b5", am(results[1].chord))
    }

    @Test
    fun movimientoEnCMayor() {
        val results = key("C", ScaleType.MAJOR).searchByNeed(Need.MOTION, ChordLevel.TRIADS)
        assertEquals(listOf(2, 4), results.map { it.index }.sorted())
    }

    @Test
    fun colorEnCMayorTraeExtensionesDeGradosEstables() {
        val results = key("C", ScaleType.MAJOR).searchByNeed(Need.COLOR, ChordLevel.TRIADS)
        assertTrue(results.isNotEmpty())
        // Todos los resultados son extensiones sobre grados de función tónica
        assertTrue(results.all { it.tonalFunction == TonalFunction.TONIC })
        assertTrue(results.any { am(it.chord) == "Cadd9" })
        // El I va primero
        assertEquals(1, results.first().index)
    }

    @Test
    fun descansoModalDevuelveCentro() {
        val results = key("D", ScaleType.DORIAN).searchByNeed(Need.REST, ChordLevel.TRIADS)
        assertEquals(listOf(1), results.map { it.index })
        assertEquals("Dm", am(results.first().chord))
    }

    @Test
    fun tensionModalSonLosAcordesConElTritonoDeLaMadre() {
        // En D dórico (madre C mayor) el tritono es F-B: con tríadas solo vi° (B°);
        // con séptimas se suma IV7 (G7 = G B D F) y va primero por ser dominante.
        val triads = key("D", ScaleType.DORIAN).searchByNeed(Need.TENSION, ChordLevel.TRIADS)
        assertEquals(listOf("B°"), triads.map { am(it.chord) })

        val sevenths = key("D", ScaleType.DORIAN).searchByNeed(Need.TENSION, ChordLevel.SEVENTHS)
        assertEquals("G7", am(sevenths.first().chord))
        assertTrue(sevenths.any { am(it.chord) == "Bm7b5" })
    }

    @Test
    fun colorModalTraeCaracteristicosPrimero() {
        val results = key("D", ScaleType.DORIAN).searchByNeed(Need.COLOR, ChordLevel.TRIADS)
        val characteristic = results.takeWhile { it.modalFunction == ModalFunction.CHARACTERISTIC }
        assertEquals(listOf(2, 4, 6), characteristic.map { it.index })
    }

    @Test
    fun pentatonicaDelegaEnLaMadre() {
        val fromPent = key("A", ScaleType.PENT_MINOR).searchByNeed(Need.REST, ChordLevel.TRIADS)
        val fromParent = key("A", ScaleType.NATURAL_MINOR).searchByNeed(Need.REST, ChordLevel.TRIADS)
        assertEquals(fromParent, fromPent)
    }
}
