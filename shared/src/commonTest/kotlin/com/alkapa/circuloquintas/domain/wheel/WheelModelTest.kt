package com.alkapa.circuloquintas.domain.wheel

import com.alkapa.circuloquintas.domain.ChordBuilder
import com.alkapa.circuloquintas.domain.ChordLevel
import com.alkapa.circuloquintas.domain.ChordQuality
import com.alkapa.circuloquintas.domain.ScaleType
import com.alkapa.circuloquintas.domain.key
import com.alkapa.circuloquintas.domain.note
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WheelModelTest {

    private val cMajorField = key("C", ScaleType.MAJOR).diatonicField(ChordLevel.TRIADS)

    @Test
    fun colocacionEnLaRueda() {
        // C mayor: mayores afuera por quintas, menores bajo su relativo, B° en el anillo dim bajo C.
        val byName = cMajorField.associateBy { it.roman }
        assertEquals(WheelModel.Placement(WheelModel.Ring.OUTER, 0), WheelModel.placement(byName.getValue("I").chord))
        assertEquals(WheelModel.Placement(WheelModel.Ring.OUTER, 1), WheelModel.placement(byName.getValue("V").chord))
        assertEquals(WheelModel.Placement(WheelModel.Ring.OUTER, 11), WheelModel.placement(byName.getValue("IV").chord))
        assertEquals(WheelModel.Placement(WheelModel.Ring.INNER, 0), WheelModel.placement(byName.getValue("vi").chord))
        assertEquals(WheelModel.Placement(WheelModel.Ring.INNER, 11), WheelModel.placement(byName.getValue("ii").chord))
        assertEquals(WheelModel.Placement(WheelModel.Ring.INNER, 1), WheelModel.placement(byName.getValue("iii").chord))
        assertEquals(WheelModel.Placement(WheelModel.Ring.DIM, 0), WheelModel.placement(byName.getValue("vii°").chord))
        // Aumentados no se colocan (viven en el rail).
        assertNull(WheelModel.placement(ChordBuilder.build(note("C"), ChordQuality.AUGMENTED)))
    }

    @Test
    fun deletreoDeTonicaPorRegla() {
        assertEquals(note("Gb"), WheelModel.tonicFor(6, ScaleType.MAJOR))
        assertEquals(note("Db"), WheelModel.tonicFor(1, ScaleType.MAJOR))
        assertEquals(note("F#"), WheelModel.tonicFor(6, ScaleType.NATURAL_MINOR))
        assertEquals(note("F#"), WheelModel.tonicFor(6, ScaleType.DORIAN))
        assertEquals(note("Bb"), WheelModel.tonicFor(10, ScaleType.NATURAL_MINOR))
        assertEquals(note("Eb"), WheelModel.tonicFor(3, ScaleType.MAJOR))
        assertEquals(note("A"), WheelModel.tonicFor(9, ScaleType.MAJOR))
    }

    @Test
    fun formulasDeEscala() {
        assertEquals(listOf("1", "2", "3", "4", "5", "6", "7"), WheelModel.formulas(ScaleType.MAJOR))
        assertEquals(listOf("1", "2", "♭3", "4", "5", "♭6", "♭7"), WheelModel.formulas(ScaleType.NATURAL_MINOR))
        assertEquals(listOf("1", "2", "♭3", "4", "5", "♭6", "7"), WheelModel.formulas(ScaleType.HARMONIC_MINOR))
        assertEquals(listOf("1", "2", "3", "♯4", "5", "6", "7"), WheelModel.formulas(ScaleType.LYDIAN))
        assertEquals(listOf("1", "♭2", "♭3", "4", "♭5", "♭6", "♭7"), WheelModel.formulas(ScaleType.LOCRIAN))
        assertEquals(listOf("1", "2", "3", "5", "6"), WheelModel.formulas(ScaleType.PENT_MAJOR))
        assertEquals("sin alteraciones · referencia mayor", WheelModel.alterationsText(ScaleType.MAJOR))
        assertEquals("alteraciones: ♭3 · ♭6 · ♭7", WheelModel.alterationsText(ScaleType.NATURAL_MINOR))
        assertEquals("omite 4 y 7", WheelModel.alterationsText(ScaleType.PENT_MAJOR))
    }

    @Test
    fun inversionesConApiladoCerrado() {
        val c = ChordBuilder.build(note("C"), ChordQuality.MAJOR)
        val fund = WheelModel.inversion(c, 0)
        assertEquals(0, fund.bassPc)
        assertEquals(listOf(0, 4, 7), fund.abs)
        assertTrue(fund.isRootPosition)

        val first = WheelModel.inversion(c, 1)
        assertEquals(4, first.bassPc)
        assertEquals(listOf(4, 7, 12), first.abs)

        val second = WheelModel.inversion(c, 2)
        assertEquals(7, second.bassPc)
        assertEquals(listOf(7, 12, 16), second.abs)

        val g7 = ChordBuilder.build(note("G"), ChordQuality.DOM7)
        val third = WheelModel.inversion(g7, 3)
        assertEquals(5, third.bassPc) // F en el bajo
        assertEquals(listOf(5, 7, 11, 14), third.abs)
    }

    @Test
    fun progresionesPorFamilia() {
        assertEquals(4, WheelModel.progressionsFor(ScaleType.MAJOR).size)
        assertEquals(WheelModel.progressionsFor(ScaleType.MAJOR), WheelModel.progressionsFor(ScaleType.MIXOLYDIAN))
        assertEquals(3, WheelModel.progressionsFor(ScaleType.NATURAL_MINOR).size)
        assertEquals(WheelModel.progressionsFor(ScaleType.NATURAL_MINOR), WheelModel.progressionsFor(ScaleType.DORIAN))
        assertEquals(WheelModel.progressionsFor(ScaleType.HARMONIC_MINOR), WheelModel.progressionsFor(ScaleType.MELODIC_MINOR))
        assertEquals("Pop clásico", WheelModel.progressionsFor(ScaleType.MAJOR)[0].name)
        assertEquals(listOf(0, 4, 5, 3), WheelModel.progressionsFor(ScaleType.MAJOR)[0].degrees)
    }

    @Test
    fun campoDeLaRuedaConRomanosDeTriada() {
        val f = WheelModel.field(rootPc = 0, scale = ScaleType.MAJOR, seventh = true)
        assertEquals("V", f.designDeg(4))
        assertEquals(ChordQuality.DOM7, f.active[4].chord.quality)
        assertEquals("vii°", f.designDeg(6))
        // Pentatónica: el campo es el de la madre.
        val p = WheelModel.field(rootPc = 9, scale = ScaleType.PENT_MINOR, seventh = false)
        assertEquals(7, p.active.size)
        assertEquals("Am", com.alkapa.circuloquintas.domain.am(p.active[0].chord))
    }

    @Test
    fun sufijosYEtiquetasDelDiseno() {
        assertEquals("ø7", WheelModel.designSuffix(ChordQuality.MIN7B5))
        assertEquals("m(maj7)", WheelModel.designSuffix(ChordQuality.MINMAJ7))
        assertEquals("7", WheelModel.designSuffix(ChordQuality.DOM7))
        assertEquals("dism", WheelModel.qualityLabel(ChordQuality.MIN7B5))
        assertEquals("♭7", WheelModel.intervalGlyph(10))
        assertEquals("♭3", WheelModel.intervalGlyph(3))
    }
}
