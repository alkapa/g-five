package com.alkapa.circuloquintas.domain.wheel

import com.alkapa.circuloquintas.domain.Chord
import com.alkapa.circuloquintas.domain.ChordQuality

/**
 * Digitaciones de guitarra del refactor 2a, portadas 1:1 del diseño:
 * formas estándar (abiertas + cejilla E/A) para la posición fundamental, y
 * voicings por grupo de cuerdas para las inversiones — tríadas cerradas y
 * drop-2 en séptimas — resueltos melódicamente sobre la afinación estándar.
 */
object WheelVoicings {

    /** Afinación estándar como clases de altura, 6ª→1ª: E A D G B E. */
    private val TUNING_PC = listOf(4, 9, 2, 7, 11, 4)

    /** Digitación estándar: traste por cuerda, -1 = muda, 0 = al aire. */
    data class Shape(val frets: List<Int>) {
        val minFretted: Int get() = frets.filter { it > 0 }.minOrNull() ?: 1
        val maxFretted: Int get() = frets.filter { it > 0 }.maxOrNull() ?: 1

        /** Traste base del diagrama (1 = con cejuela; >1 = etiqueta «Nfr»). */
        val baseFret: Int get() = if (maxFretted <= 5) 1 else minFretted
    }

    private val OPEN_SHAPES = mapOf(
        "0M" to "x32010", "2M" to "xx0232", "4M" to "022100", "5M" to "133211",
        "7M" to "320003", "9M" to "x02220", "2m" to "xx0231", "4m" to "022000", "9m" to "x02210",
    )

    private val OPEN7: Map<String, Map<Int, String>> = mapOf(
        "maj7" to mapOf(0 to "x32000", 2 to "xx0222", 4 to "021100", 5 to "xx3210", 7 to "320002", 9 to "x02120"),
        "dom7" to mapOf(0 to "x32310", 2 to "xx0212", 4 to "020100", 7 to "320001", 9 to "x02020", 11 to "x21202"),
        "m7" to mapOf(2 to "xx0211", 4 to "020000", 9 to "x02010"),
    )

    private fun parse(s: String): List<Int> = s.map { if (it == 'x') -1 else it.digitToInt() }

    private fun sevenType(quality: ChordQuality): String = when (quality) {
        ChordQuality.MAJ7 -> "maj7"
        ChordQuality.DOM7 -> "dom7"
        ChordQuality.MIN7 -> "m7"
        ChordQuality.MIN7B5 -> "hd7"
        ChordQuality.DIM7 -> "dim7"
        ChordQuality.MINMAJ7 -> "mM7"
        ChordQuality.MAJ7SHARP5 -> "augM7"
        else -> "maj7"
    }

    /** Forma estándar (abierta o cejilla) del acorde en posición fundamental. */
    fun standardShape(chord: Chord): Shape {
        val pc = chord.root.pitchClass
        val family = WheelModel.triadFamily(chord.quality)
        val isSeventh = chord.notes.size >= 4 &&
            chord.quality !in setOf(ChordQuality.MAJOR, ChordQuality.MINOR, ChordQuality.DIMINISHED, ChordQuality.AUGMENTED)
        if (!isSeventh) {
            OPEN_SHAPES["$pc$family"]?.let { return Shape(parse(it)) }
            if (family == 'M' || family == 'm') {
                val fE = (pc - 4).mod(12)
                val fA = (pc - 9).mod(12)
                val useE = fE >= 1 && (fA < 1 || fE <= fA)
                if (useE) {
                    return Shape(
                        if (family == 'M') listOf(fE, fE + 2, fE + 2, fE + 1, fE, fE)
                        else listOf(fE, fE + 2, fE + 2, fE, fE, fE),
                    )
                }
                val f = if (fA < 1) fA + 12 else fA
                return Shape(
                    if (family == 'M') listOf(-1, f, f + 2, f + 2, f + 2, f)
                    else listOf(-1, f, f + 2, f + 2, f + 1, f),
                )
            }
            val f = (pc - 9).mod(12)
            return if (family == 'd') {
                Shape(listOf(-1, f, f + 1, f + 2, f + 1, -1))
            } else {
                Shape(listOf(-1, f, f + 3, f + 2, f + 2, f + 1))
            }
        }
        val type = sevenType(chord.quality)
        OPEN7[type]?.get(pc)?.let { return Shape(parse(it)) }
        val f = (pc - 9).mod(12)
        return Shape(
            when (type) {
                "maj7" -> listOf(-1, f, f + 2, f + 1, f + 2, -1)
                "dom7" -> listOf(-1, f, f + 2, f, f + 2, f)
                "m7" -> listOf(-1, f, f + 2, f, f + 1, f)
                "hd7" -> listOf(-1, f, f + 1, f, f + 1, -1)
                "dim7" -> listOf(-1, f, f + 1, f + 2, f + 1, f + 2)
                "mM7" -> listOf(-1, f, f + 2, f + 1, f + 1, -1)
                else -> listOf(-1, f, f + 3, f + 1, f + 2, -1)
            },
        )
    }

    /**
     * Voicing de una inversión sobre un grupo de cuerdas contiguas.
     * [strings]: índices de cuerda (0 = 6ª grave). [frets]: traste por cuerda
     * del grupo (0 = al aire). [orderPcs]: voz por cuerda, del bajo arriba.
     */
    data class GroupVoicing(
        val strings: List<Int>,
        val frets: List<Int>,
        val orderPcs: List<Int>,
    ) {
        /** Etiqueta «6·5·4» (números de cuerda, la 6ª es la más grave). */
        val label: String get() = strings.joinToString("·") { (6 - it).toString() }

        val minFret: Int get() = frets.min()
        val maxFret: Int get() = frets.max()

        /** Etiqueta de zona («abierta», «traste 5», «tr. 3–5»). */
        val zoneLabel: String
            get() = when {
                minFret == maxFret -> if (minFret == 0) "abierta" else "traste $minFret"
                else -> "tr. $minFret–$maxFret"
            }
    }

    /**
     * Voicings de la inversión [inv] por grupo de cuerdas (regla del diseño):
     * tríadas en posición cerrada sobre 4 grupos de 3 cuerdas; séptimas en
     * drop-2 sobre 3 grupos de 4. Se descartan los que exceden el traste 12
     * o abren la mano más de 4 trastes.
     */
    fun groupVoicings(chord: Chord, inv: Int): List<GroupVoicing> {
        val pcs = chord.notes.map { it.pitchClass }.distinct()
        val n = pcs.size
        if (n !in 3..4) return emptyList()
        val order = if (n == 3) {
            listOf(pcs[inv % 3], pcs[(inv + 1) % 3], pcs[(inv + 2) % 3])
        } else {
            listOf(pcs[inv % 4], pcs[(inv + 2) % 4], pcs[(inv + 3) % 4], pcs[(inv + 1) % 4])
        }
        val sets = if (n == 3) {
            listOf(listOf(3, 4, 5), listOf(2, 3, 4), listOf(1, 2, 3), listOf(0, 1, 2))
        } else {
            listOf(listOf(2, 3, 4, 5), listOf(1, 2, 3, 4), listOf(0, 1, 2, 3))
        }
        val out = mutableListOf<GroupVoicing>()
        for (set in sets) {
            var frets = mutableListOf((order[0] - TUNING_PC[set[0]]).mod(12))
            for (i in 1 until set.size) {
                val ni = (order[i] - order[i - 1]).mod(12)
                val si = (TUNING_PC[set[i]] - TUNING_PC[set[i - 1]]).mod(12)
                frets += frets[i - 1] + ni - si
            }
            var mn = frets.min()
            if (mn < 0) {
                frets = frets.map { it + 12 }.toMutableList()
                mn += 12
            }
            val mx = frets.max()
            if (mx > 12 || mx - mn > 4) continue
            out += GroupVoicing(set, frets, order)
        }
        return out
    }
}
