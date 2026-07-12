package com.alkapa.circuloquintas.domain.instrument

import com.alkapa.circuloquintas.domain.Chord

/**
 * Diapasón de guitarra en afinación estándar: mapa completo de las notas del
 * acorde y generador de digitaciones tocables por inversión, distribuidas a
 * lo largo de todo el mástil.
 */
object GuitarFretboard {

    /** Afinación estándar, de la 6ª cuerda (E2, índice 0) a la 1ª (E4, índice 5). */
    val STANDARD_TUNING = listOf(40, 45, 50, 55, 59, 64)

    const val FRETS = 15

    /** Trastes con marcador tradicional (el 12 doble). */
    val MARKER_FRETS = setOf(3, 5, 7, 9, 12, 15)

    /** Nota del acorde en una posición concreta del diapasón. */
    data class Position(
        val string: Int,
        val fret: Int,
        val midi: Int,
        val offset: Int,
        val role: ChordRole,
    )

    /**
     * Digitación: traste por cuerda (null = cuerda muda, 0 = al aire).
     * [minFret]: traste pisado más grave (0 si es posición abierta).
     */
    data class Voicing(
        val frets: List<Int?>,
        val midis: List<Int>,
        val inversionIndex: Int,
        val minFret: Int,
    )

    /**
     * Mapa completo: todas las posiciones del diapasón cuya nota pertenece al
     * acorde (todas las voces, no solo las esenciales), con su rol.
     */
    fun chordToneMap(chord: Chord): List<Position> {
        val byPc = ChordVoicingModel.allTones(chord).associateBy { it.note.pitchClass }
        val result = mutableListOf<Position>()
        for (string in STANDARD_TUNING.indices) {
            for (fret in 0..FRETS) {
                val midi = STANDARD_TUNING[string] + fret
                val tone = byPc[midi.mod(12)] ?: continue
                result += Position(string, fret, midi, tone.offset, tone.role)
            }
        }
        return result
    }

    /**
     * Digitaciones tocables de la inversión pedida, elegidas para cubrir todo
     * el mástil (la mejor de cada zona, hasta [maxResults]).
     *
     * Reglas de tocabilidad: cuerdas contiguas (las demás mudas), todas las
     * voces esenciales presentes, el bajo es la voz de la inversión, span de
     * trastes pisados ≤ [MAX_SPAN] y ≤ 4 dedos (la cejilla en el traste más
     * grave cuenta como un dedo).
     */
    fun voicings(chord: Chord, inversionIndex: Int, maxResults: Int = 4): List<Voicing> {
        val core = ChordVoicingModel.coreTones(chord)
        if (core.isEmpty()) return emptyList()
        val idx = inversionIndex.coerceIn(0, core.lastIndex)
        val corePcs = core.map { it.note.pitchClass }.toSet()
        val bassPc = core[idx].note.pitchClass
        val minStrings = core.size

        val candidates = mutableMapOf<List<Int?>, Voicing>()
        for (firstString in 0..STANDARD_TUNING.size - minStrings) {
            for (lastString in (firstString + minStrings - 1) until STANDARD_TUNING.size) {
                for (window in 0..(FRETS - MAX_SPAN)) {
                    search(
                        strings = firstString..lastString,
                        window = window,
                        corePcs = corePcs,
                        bassPc = bassPc,
                        inversionIndex = idx,
                        into = candidates,
                    )
                }
            }
        }

        // Distribución por zonas del mástil: la mejor digitación de cada zona.
        val ranked = candidates.values.sortedWith(
            compareBy(
                { zoneOf(it.minFret) },
                { -it.frets.count { f -> f != null } },
                { span(it.frets) },
                { it.minFret },
            ),
        )
        val result = mutableListOf<Voicing>()
        for (voicing in ranked) {
            if (result.size >= maxResults) break
            if (result.none { zoneOf(it.minFret) == zoneOf(voicing.minFret) }) {
                result += voicing
            }
        }
        // Si quedaron zonas vacías, completa con las siguientes mejores.
        for (voicing in ranked) {
            if (result.size >= maxResults) break
            if (voicing !in result) result += voicing
        }
        return result.sortedBy { it.minFret }
    }

    // ------------------------------------------------------------------ interno

    private const val MAX_SPAN = 4

    private fun zoneOf(minFret: Int): Int = when {
        minFret <= 1 -> 0
        minFret <= 4 -> 1
        minFret <= 8 -> 2
        else -> 3
    }

    private fun span(frets: List<Int?>): Int {
        val fretted = frets.filterNotNull().filter { it > 0 }
        if (fretted.isEmpty()) return 0
        return fretted.max() - fretted.min()
    }

    private fun search(
        strings: IntRange,
        window: Int,
        corePcs: Set<Int>,
        bassPc: Int,
        inversionIndex: Int,
        into: MutableMap<List<Int?>, Voicing>,
    ) {
        // Opciones por cuerda: al aire o trastes dentro de la ventana.
        val options = strings.map { string ->
            val open = STANDARD_TUNING[string]
            buildList {
                if (open.mod(12) in corePcs) add(0)
                for (fret in maxOf(1, window)..(window + MAX_SPAN)) {
                    if (fret <= FRETS && (open + fret).mod(12) in corePcs) add(fret)
                }
            }
        }
        if (options.any { it.isEmpty() }) return

        fun recurse(stringOffset: Int, chosen: List<Int>) {
            if (stringOffset == options.size) {
                emit(strings, chosen, corePcs, bassPc, inversionIndex, into)
                return
            }
            for (fret in options[stringOffset]) {
                recurse(stringOffset + 1, chosen + fret)
            }
        }
        recurse(0, emptyList())
    }

    private fun emit(
        strings: IntRange,
        chosen: List<Int>,
        corePcs: Set<Int>,
        bassPc: Int,
        inversionIndex: Int,
        into: MutableMap<List<Int?>, Voicing>,
    ) {
        val midis = chosen.mapIndexed { i, fret -> STANDARD_TUNING[strings.first + i] + fret }
        // El bajo (cuerda sonante más grave) define la inversión.
        if (midis.first().mod(12) != bassPc) return
        // Todas las voces esenciales presentes.
        if (!corePcs.all { pc -> midis.any { it.mod(12) == pc } }) return

        val fretted = chosen.filter { it > 0 }
        if (fretted.isNotEmpty()) {
            if (fretted.max() - fretted.min() > MAX_SPAN) return
            // Dedos: cejilla en el traste más grave cuenta como uno.
            val minFret = fretted.min()
            val fingers = 1 + fretted.count { it > minFret }
            if (fingers > 4) return
        }

        val frets: List<Int?> = List(STANDARD_TUNING.size) { s ->
            if (s in strings) chosen[s - strings.first] else null
        }
        val voicing = Voicing(
            frets = frets,
            midis = midis,
            inversionIndex = inversionIndex,
            minFret = fretted.minOrNull() ?: 0,
        )
        val existing = into[frets]
        if (existing == null) into[frets] = voicing
    }

    /**
     * Tablatura de la digitación: 6 líneas de texto, 1ª cuerda arriba
     * (convención estándar: e B G D A E).
     */
    fun tablature(voicing: Voicing): List<String> {
        val labels = listOf("e", "B", "G", "D", "A", "E")
        return labels.mapIndexed { row, label ->
            val string = STANDARD_TUNING.size - 1 - row
            val fret = voicing.frets[string]
            val cell = when {
                fret == null -> "x"
                else -> fret.toString()
            }
            "$label|--${cell.padStart(2, '-')}--|"
        }
    }
}
