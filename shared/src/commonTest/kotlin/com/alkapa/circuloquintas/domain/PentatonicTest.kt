package com.alkapa.circuloquintas.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PentatonicTest {

    @Test
    fun aPentatonicaMenor() {
        assertEquals(listOf("A", "C", "D", "E", "G"), noteNames(key("A", ScaleType.PENT_MINOR)))
    }

    @Test
    fun cPentatonicaMayor() {
        assertEquals(listOf("C", "D", "E", "G", "A"), noteNames(key("C", ScaleType.PENT_MAJOR)))
    }

    @Test
    fun overlayMarcaCincoDeSietePosiciones() {
        val overlay = key("A", ScaleType.PENT_MINOR).pentatonicOverlay()!!
        assertEquals(key("A", ScaleType.NATURAL_MINOR), overlay.parent)
        assertEquals(setOf(1, 3, 4, 5, 7), overlay.includedDegrees)
        assertEquals(5, overlay.includedDegrees.size)
        assertEquals(2, (1..7).count { it !in overlay.includedDegrees })
    }

    @Test
    fun parentKeyCorrecto() {
        assertEquals(key("A", ScaleType.NATURAL_MINOR), key("A", ScaleType.PENT_MINOR).parentKey())
        assertEquals(key("C", ScaleType.MAJOR), key("C", ScaleType.PENT_MAJOR).parentKey())
        assertNull(key("C", ScaleType.MAJOR).pentatonicOverlay())
    }

    @Test
    fun campoArmonicoEsElDeLaMadre() {
        assertEquals(
            fieldNames(key("A", ScaleType.NATURAL_MINOR)),
            fieldNames(key("A", ScaleType.PENT_MINOR)),
        )
    }
}
