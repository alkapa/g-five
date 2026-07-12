package com.alkapa.circuloquintas.domain

/**
 * Calidades de acorde. Niveles 1-2 (tríadas y séptimas) + vocabulario cerrado
 * del nivel 3 (extensiones/color, §3.5), generado por regla desde la escala.
 *
 * [symbol] es el sufijo de cifrado (C + "m7b5" = Cm7b5) y también el sufijo
 * del número romano. [upperRoman] decide mayúscula/minúscula del romano
 * (§3.3: mayúscula = base mayor, minúscula = base menor).
 */
enum class ChordQuality(val symbol: String, val upperRoman: Boolean) {
    // Tríadas
    MAJOR("", true),
    MINOR("m", false),
    DIMINISHED("°", false),
    AUGMENTED("+", true),

    // Cuatríadas
    MAJ7("maj7", true),
    DOM7("7", true),
    MIN7("m7", false),
    MIN7B5("m7b5", false),
    DIM7("°7", false),
    MINMAJ7("mMaj7", false),
    MAJ7SHARP5("maj7#5", true),

    // Nivel 3: extensiones y color (vocabulario cerrado §3.5)
    SIX("6", true),
    MIN6("m6", false),
    SIX_NINE("6/9", true),
    ADD9("add9", true),
    NINE("9", true),
    MAJ9("maj9", true),
    MIN9("m9", false),
    SUS2("sus2", true),
    SUS4("sus4", true),
    DOM7SUS4("7sus4", true),
    MIN11("m11", false),
    THIRTEEN("13", true),
    ;

    companion object {
        /** Calidades del nivel tríadas. */
        val TRIAD_QUALITIES = setOf(MAJOR, MINOR, DIMINISHED, AUGMENTED)
    }
}

/**
 * Acorde: fundamental + calidad + notas (grafía correcta, sin octava).
 *
 * [stackOffsets]: semitonos de cada nota sobre la fundamental en el voicing
 * apilado (fundamental cerrada; extensiones por encima de la 7ª). Sirve para
 * el playback MIDI y para la regla de precaución.
 *
 * [caution]: alguna nota agregada forma una 9ª menor con otra nota del acorde
 * (caso típico: 11ª natural sobre 3ª mayor, §3.5). No se oculta: se etiqueta.
 */
data class Chord(
    val root: Note,
    val quality: ChordQuality,
    val notes: List<Note>,
    val stackOffsets: List<Int>,
    val caution: Boolean = false,
)

/**
 * Regla de precaución (§3.5): true si algún par de notas del voicing apilado
 * forma exactamente una 9ª menor (13 semitonos).
 */
fun hasMinorNinthClash(stackOffsets: List<Int>): Boolean {
    for (i in stackOffsets.indices) {
        for (j in i + 1 until stackOffsets.size) {
            if (stackOffsets[j] - stackOffsets[i] == 13) return true
        }
    }
    return false
}

/**
 * Construcción de acordes por plantilla (fundamental + calidad), con grafía
 * por letra: 3ª = dos letras arriba, 5ª = cuatro, etc.
 *
 * Se usa para acordes NO tomados del campo diatónico: los préstamos de las
 * progresiones famosas (p. ej. el V mayor de la cadencia andaluza) y el
 * re-cálculo de progresiones al transportar. Los acordes diatónicos se
 * construyen apilando notas de la escala (ver [Key.diatonicField]).
 *
 * Nota: la plantilla de `13` omite la 11ª (voicing práctico); la versión
 * diatónica del nivel extensiones sí la incluye y la etiqueta con precaución
 * cuando choca (§3.5, contenido pedagógico).
 */
object ChordBuilder {

    /** Pares (letras de distancia, semitonos sobre la fundamental). */
    private val TEMPLATES: Map<ChordQuality, List<Pair<Int, Int>>> = mapOf(
        ChordQuality.MAJOR to listOf(0 to 0, 2 to 4, 4 to 7),
        ChordQuality.MINOR to listOf(0 to 0, 2 to 3, 4 to 7),
        ChordQuality.DIMINISHED to listOf(0 to 0, 2 to 3, 4 to 6),
        ChordQuality.AUGMENTED to listOf(0 to 0, 2 to 4, 4 to 8),
        ChordQuality.MAJ7 to listOf(0 to 0, 2 to 4, 4 to 7, 6 to 11),
        ChordQuality.DOM7 to listOf(0 to 0, 2 to 4, 4 to 7, 6 to 10),
        ChordQuality.MIN7 to listOf(0 to 0, 2 to 3, 4 to 7, 6 to 10),
        ChordQuality.MIN7B5 to listOf(0 to 0, 2 to 3, 4 to 6, 6 to 10),
        ChordQuality.DIM7 to listOf(0 to 0, 2 to 3, 4 to 6, 6 to 9),
        ChordQuality.MINMAJ7 to listOf(0 to 0, 2 to 3, 4 to 7, 6 to 11),
        ChordQuality.MAJ7SHARP5 to listOf(0 to 0, 2 to 4, 4 to 8, 6 to 11),
        ChordQuality.SIX to listOf(0 to 0, 2 to 4, 4 to 7, 5 to 9),
        ChordQuality.MIN6 to listOf(0 to 0, 2 to 3, 4 to 7, 5 to 9),
        ChordQuality.SIX_NINE to listOf(0 to 0, 2 to 4, 4 to 7, 5 to 9, 8 to 14),
        ChordQuality.ADD9 to listOf(0 to 0, 2 to 4, 4 to 7, 8 to 14),
        ChordQuality.NINE to listOf(0 to 0, 2 to 4, 4 to 7, 6 to 10, 8 to 14),
        ChordQuality.MAJ9 to listOf(0 to 0, 2 to 4, 4 to 7, 6 to 11, 8 to 14),
        ChordQuality.MIN9 to listOf(0 to 0, 2 to 3, 4 to 7, 6 to 10, 8 to 14),
        ChordQuality.SUS2 to listOf(0 to 0, 1 to 2, 4 to 7),
        ChordQuality.SUS4 to listOf(0 to 0, 3 to 5, 4 to 7),
        ChordQuality.DOM7SUS4 to listOf(0 to 0, 3 to 5, 4 to 7, 6 to 10),
        ChordQuality.MIN11 to listOf(0 to 0, 2 to 3, 4 to 7, 6 to 10, 8 to 14, 10 to 17),
        ChordQuality.THIRTEEN to listOf(0 to 0, 2 to 4, 4 to 7, 6 to 10, 8 to 14, 12 to 21),
    )

    fun build(root: Note, quality: ChordQuality): Chord {
        val template = TEMPLATES.getValue(quality)
        val notes = template.map { (letterSteps, semitones) ->
            Note.spelled(
                Note.letterAt(root.letter, letterSteps),
                (root.pitchClass + semitones).mod(12),
            )
        }
        val offsets = template.map { it.second }
        return Chord(root, quality, notes, offsets, caution = hasMinorNinthClash(offsets))
    }
}
