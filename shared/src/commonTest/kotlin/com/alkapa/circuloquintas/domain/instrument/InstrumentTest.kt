package com.alkapa.circuloquintas.domain.instrument

import com.alkapa.circuloquintas.domain.ChordBuilder
import com.alkapa.circuloquintas.domain.ChordLevel
import com.alkapa.circuloquintas.domain.ChordQuality
import com.alkapa.circuloquintas.domain.ScaleType
import com.alkapa.circuloquintas.domain.key
import com.alkapa.circuloquintas.domain.note
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InstrumentTest {

    private val cMajor = ChordBuilder.build(note("C"), ChordQuality.MAJOR)
    private val g7 = ChordBuilder.build(note("G"), ChordQuality.DOM7)

    // ------------------------------------------------------------ inversiones

    @Test
    fun inversionesDeTriadaYCuatriada() {
        val triad = ChordVoicingModel.inversions(cMajor)
        assertEquals(3, triad.size)
        assertEquals("Fundamental", triad[0].name)
        assertEquals("1ª inversión", triad[1].name)
        assertEquals("2ª inversión", triad[2].name)
        assertEquals(note("C"), triad[0].bass.note)
        assertEquals(note("E"), triad[1].bass.note)
        assertEquals(note("G"), triad[2].bass.note)

        val seventh = ChordVoicingModel.inversions(g7)
        assertEquals(4, seventh.size)
        assertEquals(note("F"), seventh[3].bass.note)
    }

    @Test
    fun vocesEsencialesDeExtendidos() {
        // G13 diatónico (C mayor) incluye 7 voces; para digitar quedan 4:
        // fundamental, 3ª, 7ª y 13ª (la práctica común de guitarra).
        val g13 = key("C", ScaleType.MAJOR).diatonicField(ChordLevel.EXTENSIONS)[4]
            .extensions.first { it.quality == ChordQuality.THIRTEEN }
        val core = ChordVoicingModel.coreTones(g13)
        assertEquals(listOf(0, 4, 10, 21), core.map { it.offset })
        assertEquals(
            listOf(ChordRole.ROOT, ChordRole.THIRD, ChordRole.SEVENTH, ChordRole.EXTENSION),
            core.map { it.role },
        )
        assertEquals(4, ChordVoicingModel.inversions(g13).size)
    }

    @Test
    fun etiquetasDeIntervalo() {
        assertEquals("F", ChordVoicingModel.intervalLabel(0, ChordQuality.MAJOR))
        assertEquals("3ª", ChordVoicingModel.intervalLabel(4, ChordQuality.MAJOR))
        assertEquals("6ª", ChordVoicingModel.intervalLabel(9, ChordQuality.SIX))
        assertEquals("7ª", ChordVoicingModel.intervalLabel(9, ChordQuality.DIM7))
        assertEquals("13ª", ChordVoicingModel.intervalLabel(21, ChordQuality.THIRTEEN))
    }

    // ------------------------------------------------------------------ piano

    @Test
    fun colocacionesDePianoPorOctavas() {
        // C mayor fundamental entre C2 (36) y B6 (95): una por octava.
        val fundamental = ChordVoicingModel.pianoPlacements(cMajor, 0)
        assertEquals(5, fundamental.size)
        assertEquals(listOf(36, 40, 43), fundamental.first())
        assertTrue(fundamental.all { (it[1] - it[0]) == 4 && (it[2] - it[0]) == 7 })

        // 1ª inversión: bajo E, apilado E-G-C.
        val first = ChordVoicingModel.pianoPlacements(cMajor, 1)
        assertEquals(listOf(40, 43, 48), first.first())
        // 2ª inversión: bajo G, apilado G-C-E.
        val second = ChordVoicingModel.pianoPlacements(cMajor, 2)
        assertEquals(listOf(43, 48, 52), second.first())
    }

    // --------------------------------------------------------------- diapasón

    @Test
    fun mapaDeNotasDelAcordeEnElDiapason() {
        val positions = GuitarFretboard.chordToneMap(cMajor)
        // C en 5ª cuerda traste 3 (A2=45 → 48) con rol de fundamental.
        assertTrue(
            positions.any { it.string == 1 && it.fret == 3 && it.midi == 48 && it.role == ChordRole.ROOT },
        )
        // E al aire en 6ª cuerda (rol de 3ª) y G en 1ª cuerda traste 3.
        assertTrue(positions.any { it.string == 0 && it.fret == 0 && it.role == ChordRole.THIRD })
        assertTrue(positions.any { it.string == 5 && it.fret == 3 && it.role == ChordRole.FIFTH })
        // Solo notas del acorde.
        assertTrue(positions.all { it.midi.mod(12) in setOf(0, 4, 7) })
    }

    @Test
    fun digitacionesDeCFundamentalIncluyenLaFormaAbierta() {
        val voicings = GuitarFretboard.voicings(cMajor, inversionIndex = 0)
        assertTrue(voicings.isNotEmpty())
        // Todas: bajo C, todas las voces presentes, tocables.
        for (v in voicings) {
            assertEquals(0, v.midis.first().mod(12), "bajo equivocado en ${v.frets}")
            assertEquals(setOf(0, 4, 7), v.midis.map { it.mod(12) }.toSet())
            val fretted = v.frets.filterNotNull().filter { it > 0 }
            if (fretted.isNotEmpty()) {
                assertTrue(fretted.max() - fretted.min() <= 4)
            }
        }
        // La clásica x-3-2-0-1-0 aparece en la zona abierta.
        assertTrue(
            voicings.any { it.frets == listOf(null, 3, 2, 0, 1, 0) },
            "No encontró la forma abierta clásica: ${voicings.map { it.frets }}",
        )
        // Distribuidas: al menos dos zonas distintas del mástil.
        assertTrue(voicings.map { it.minFret }.toSet().size >= 2)
    }

    @Test
    fun lasInversionesDeGuitarraRespetanElBajo() {
        for (inversion in 0..2) {
            val expectedBassPc = ChordVoicingModel.coreTones(cMajor)[inversion].note.pitchClass
            val voicings = GuitarFretboard.voicings(cMajor, inversion)
            assertTrue(voicings.isNotEmpty(), "Sin digitaciones para inversión $inversion")
            assertTrue(voicings.all { it.midis.first().mod(12) == expectedBassPc })
        }
    }

    @Test
    fun digitacionesDeCuatriada() {
        val voicings = GuitarFretboard.voicings(g7, inversionIndex = 0)
        assertTrue(voicings.isNotEmpty())
        for (v in voicings) {
            assertEquals(7, v.midis.first().mod(12))
            assertEquals(setOf(7, 11, 2, 5), v.midis.map { it.mod(12) }.toSet())
            assertTrue(v.frets.count { it != null } >= 4)
        }
    }

    @Test
    fun tablaturaConConvencionEstandar() {
        val open = GuitarFretboard.voicings(cMajor, 0)
            .first { it.frets == listOf(null, 3, 2, 0, 1, 0) }
        val tab = GuitarFretboard.tablature(open)
        assertEquals(6, tab.size)
        assertTrue(tab[0].startsWith("e|"))
        assertTrue(tab[5].startsWith("E|"))
        assertTrue(tab[5].contains("x"))   // 6ª cuerda muda
        assertTrue(tab[1].contains("1"))   // B en traste 1
        assertTrue(tab[4].contains("3"))   // A en traste 3
    }

    @Test
    fun extendidosTambienGeneranDigitaciones() {
        val g13 = key("C", ScaleType.MAJOR).diatonicField(ChordLevel.EXTENSIONS)[4]
            .extensions.first { it.quality == ChordQuality.THIRTEEN }
        val voicings = GuitarFretboard.voicings(g13, inversionIndex = 0)
        assertTrue(voicings.isNotEmpty())
        // Las 4 voces esenciales (G, B, F, E) presentes.
        for (v in voicings) {
            assertEquals(setOf(7, 11, 5, 4), v.midis.map { it.mod(12) }.toSet())
        }
    }
}
