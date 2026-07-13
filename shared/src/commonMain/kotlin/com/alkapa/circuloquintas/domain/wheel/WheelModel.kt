package com.alkapa.circuloquintas.domain.wheel

import com.alkapa.circuloquintas.domain.Chord
import com.alkapa.circuloquintas.domain.ChordLevel
import com.alkapa.circuloquintas.domain.ChordQuality
import com.alkapa.circuloquintas.domain.Degree
import com.alkapa.circuloquintas.domain.Key
import com.alkapa.circuloquintas.domain.Notation
import com.alkapa.circuloquintas.domain.Note
import com.alkapa.circuloquintas.domain.ScaleCategory
import com.alkapa.circuloquintas.domain.ScaleType

/**
 * Modelo del refactor «2a — Claridad+» (Refactor Círculo.dc.html): la rueda
 * como donut de tres anillos, funciones simplificadas a Reposo/Movimiento/
 * Tensión, fórmulas de escala, inversiones con nombre slash y progresiones
 * conectadas. Portado 1:1 de la lógica del diseño para fidelidad visual;
 * el motor §3-§5 sigue siendo la fuente para fichas y contenido.
 */
object WheelModel {

    /** Función simplificada del diseño (leyenda de 3 colores). */
    enum class Fn { T, S, D }

    /** Tabla FN7 del diseño: función por grado (I..VII), igual para toda escala. */
    val FN7 = listOf(Fn.T, Fn.S, Fn.T, Fn.S, Fn.D, Fn.T, Fn.D)

    /** Orden de quintas del anillo exterior (posición 0 = arriba = C). */
    val FIFTHS = listOf(0, 7, 2, 9, 4, 11, 6, 1, 8, 3, 10, 5)

    /** Tónicas mayores que se escriben con bemoles (regla del diseño). */
    private val FLAT_MAJ = setOf(5, 10, 3, 8, 1, 6)

    enum class Ring { OUTER, INNER, DIM }

    data class Placement(val ring: Ring, val position: Int)

    /**
     * Colocación de un acorde en la rueda: mayores en el anillo exterior por
     * quintas; menores en el interior bajo su relativo mayor (pc+3);
     * disminuidos en el anillo chico bajo pc+1. Aumentados no se colocan
     * (viven solo en el rail), igual que en el diseño.
     */
    fun placement(chord: Chord): Placement? {
        val pc = chord.root.pitchClass
        return when (triadFamily(chord.quality)) {
            'M' -> Placement(Ring.OUTER, FIFTHS.indexOf(pc))
            'm' -> Placement(Ring.INNER, FIFTHS.indexOf((pc + 3) % 12))
            'd' -> Placement(Ring.DIM, FIFTHS.indexOf((pc + 1) % 12))
            else -> null
        }
    }

    /** Familia de tríada de una calidad ('M', 'm', 'd', 'A'). */
    fun triadFamily(quality: ChordQuality): Char = when (quality) {
        ChordQuality.MAJOR, ChordQuality.MAJ7, ChordQuality.DOM7 -> 'M'
        ChordQuality.MINOR, ChordQuality.MIN7, ChordQuality.MINMAJ7 -> 'm'
        ChordQuality.DIMINISHED, ChordQuality.MIN7B5, ChordQuality.DIM7 -> 'd'
        ChordQuality.AUGMENTED, ChordQuality.MAJ7SHARP5 -> 'A'
        else -> 'M'
    }

    /** Sufijo de cifrado del diseño para séptimas (ø7, m(maj7), +maj7...). */
    fun designSuffix(quality: ChordQuality): String = when (quality) {
        ChordQuality.MAJOR -> ""
        ChordQuality.MINOR -> "m"
        ChordQuality.DIMINISHED -> "°"
        ChordQuality.AUGMENTED -> "+"
        ChordQuality.MAJ7 -> "maj7"
        ChordQuality.DOM7 -> "7"
        ChordQuality.MIN7 -> "m7"
        ChordQuality.MIN7B5 -> "ø7"
        ChordQuality.DIM7 -> "°7"
        ChordQuality.MINMAJ7 -> "m(maj7)"
        ChordQuality.MAJ7SHARP5 -> "+maj7"
        else -> quality.symbol
    }

    /** Etiqueta de calidad del rail (mayor/menor/dism/aum). */
    fun qualityLabel(quality: ChordQuality): String = when (triadFamily(quality)) {
        'M' -> "mayor"; 'm' -> "menor"; 'd' -> "dism"; else -> "aum"
    }

    /** Etiqueta corta de intervalo dentro del acorde (grados del acorde). */
    fun intervalGlyph(offset: Int): String = when (offset % 12) {
        0 -> "1"; 3 -> "♭3"; 4 -> "3"; 6 -> "♭5"; 7 -> "5"; 8 -> "♯5"
        9 -> "𝄫7"; 10 -> "♭7"; 11 -> "7"; 2 -> "2"; 5 -> "4"
        else -> "${offset % 12}"
    }

    // ----------------------------------------------------------- tonalidades

    /** Escalas del selector del diseño, mapeadas al motor. */
    val SHEET_TONAL = listOf(ScaleType.MAJOR, ScaleType.NATURAL_MINOR, ScaleType.HARMONIC_MINOR, ScaleType.MELODIC_MINOR)
    val SHEET_MODES = listOf(
        ScaleType.MAJOR to "Jónico", ScaleType.DORIAN to "Dórico", ScaleType.PHRYGIAN to "Frigio",
        ScaleType.LYDIAN to "Lidio", ScaleType.MIXOLYDIAN to "Mixolidio",
        ScaleType.NATURAL_MINOR to "Eólico", ScaleType.LOCRIAN to "Locrio",
    )
    val SHEET_PENTAS = listOf(ScaleType.PENT_MAJOR to "Pent. mayor", ScaleType.PENT_MINOR to "Pent. menor")

    /** Descripciones breves del selector (texto del diseño). */
    fun scaleDesc(scale: ScaleType): String = when (scale) {
        ScaleType.MAJOR -> "brillante, estable"
        ScaleType.NATURAL_MINOR -> "oscura, melancólica"
        ScaleType.HARMONIC_MINOR -> "tensión dramática, 7ª elevada"
        ScaleType.MELODIC_MINOR -> "menor con 6ª y 7ª elevadas, suave"
        ScaleType.DORIAN -> "menor con 6ª mayor · jazz, funk"
        ScaleType.PHRYGIAN -> "menor con ♭2 · flamenco"
        ScaleType.LYDIAN -> "mayor con ♯4 · soñador"
        ScaleType.MIXOLYDIAN -> "mayor con ♭7 · rock, blues"
        ScaleType.LOCRIAN -> "inestable, tónica disminuida"
        ScaleType.PENT_MAJOR -> "cinco notas, sin semitonos"
        ScaleType.PENT_MINOR -> "la escala del rock y del blues"
        else -> ""
    }

    /** Etiqueta de tonalidad ("mayor", "dórico"...) para el pill superior. */
    fun keyLabel(scale: ScaleType): String = when (scale) {
        ScaleType.MAJOR -> "mayor"
        ScaleType.NATURAL_MINOR -> "menor"
        ScaleType.HARMONIC_MINOR -> "menor armónica"
        ScaleType.MELODIC_MINOR -> "menor melódica"
        ScaleType.DORIAN -> "dórico"
        ScaleType.PHRYGIAN -> "frigio"
        ScaleType.LYDIAN -> "lidio"
        ScaleType.MIXOLYDIAN -> "mixolidio"
        ScaleType.LOCRIAN -> "locrio"
        ScaleType.PENT_MAJOR -> "pent. mayor"
        ScaleType.PENT_MINOR -> "pent. menor"
        ScaleType.IONIAN -> "mayor"
        ScaleType.AEOLIAN -> "menor"
    }

    /** Nombre corto del selector ("Mayor", "Menor natural"...). */
    fun scaleLabel(scale: ScaleType): String = when (scale) {
        ScaleType.MAJOR -> "Mayor"
        ScaleType.NATURAL_MINOR -> "Menor natural"
        ScaleType.HARMONIC_MINOR -> "Menor armónica"
        ScaleType.MELODIC_MINOR -> "Menor melódica"
        ScaleType.DORIAN -> "Dórico"
        ScaleType.PHRYGIAN -> "Frigio"
        ScaleType.LYDIAN -> "Lidio"
        ScaleType.MIXOLYDIAN -> "Mixolidio"
        ScaleType.LOCRIAN -> "Locrio"
        ScaleType.PENT_MAJOR -> "Pentatónica mayor"
        ScaleType.PENT_MINOR -> "Pentatónica menor"
        ScaleType.IONIAN -> "Mayor"
        ScaleType.AEOLIAN -> "Menor natural"
    }

    /** Semitonos de la tónica del modo respecto de su mayor de referencia. */
    private fun relativeMajorOffset(scale: ScaleType): Int = when (scale) {
        ScaleType.NATURAL_MINOR, ScaleType.HARMONIC_MINOR, ScaleType.MELODIC_MINOR,
        ScaleType.AEOLIAN, ScaleType.PENT_MINOR,
        -> 9
        ScaleType.DORIAN -> 2
        ScaleType.PHRYGIAN -> 4
        ScaleType.LYDIAN -> 5
        ScaleType.MIXOLYDIAN -> 7
        ScaleType.LOCRIAN -> 11
        else -> 0
    }

    /** Regla del diseño: la tonalidad se escribe con bemoles si su mayor de referencia cae en F, Bb, Eb, Ab, Db o Gb. */
    fun usesFlats(rootPc: Int, scale: ScaleType): Boolean =
        ((rootPc - relativeMajorOffset(scale)).mod(12)) in FLAT_MAJ

    private val SHARP_NOTES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val FLAT_NOTES = listOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")

    /** Tónica deletreada para (clase de altura, escala) según la regla de bemoles. */
    fun tonicFor(rootPc: Int, scale: ScaleType): Note {
        val name = if (usesFlats(rootPc, scale)) FLAT_NOTES[rootPc.mod(12)] else SHARP_NOTES[rootPc.mod(12)]
        val accidental = when {
            name.endsWith("#") -> 1
            name.endsWith("b") -> -1
            else -> 0
        }
        return Note(name[0], accidental)
    }

    /** Nombre de nota por clase de altura para la UI (grilla de raíces usa bemoles). */
    fun pcName(pc: Int, flats: Boolean, notation: Notation): String {
        val base = if (flats) FLAT_NOTES[pc.mod(12)] else SHARP_NOTES[pc.mod(12)]
        if (notation == Notation.AMERICAN) return base
        val latin = mapOf('C' to "Do", 'D' to "Re", 'E' to "Mi", 'F' to "Fa", 'G' to "Sol", 'A' to "La", 'B' to "Si")
        return latin.getValue(base[0]) + base.drop(1)
    }

    // -------------------------------------------------------------- fórmulas

    private val MAJOR_STEPS = listOf(0, 2, 4, 5, 7, 9, 11)

    /** Fórmula de grado (1, ♭3, ♯4, 𝄫7…) comparando contra la escala mayor. */
    fun formulas(scale: ScaleType): List<String> {
        if (scale == ScaleType.PENT_MAJOR) return listOf("1", "2", "3", "5", "6")
        if (scale == ScaleType.PENT_MINOR) return listOf("1", "♭3", "4", "5", "♭7")
        var acc = 0
        val steps = mutableListOf(0)
        for (s in scale.pattern.dropLast(1)) {
            acc += s
            steps += acc
        }
        return steps.mapIndexed { i, st ->
            val delta = st - MAJOR_STEPS[i]
            val glyph = when {
                delta <= -2 -> "𝄫"
                delta == -1 -> "♭"
                delta == 1 -> "♯"
                delta >= 2 -> "𝄪"
                else -> ""
            }
            "$glyph${i + 1}"
        }
    }

    /** Texto de alteraciones de la tira de escala. */
    fun alterationsText(scale: ScaleType): String = when (scale) {
        ScaleType.PENT_MAJOR -> "omite 4 y 7"
        ScaleType.PENT_MINOR -> "omite 2 y 6"
        else -> {
            val alts = formulas(scale).filter { it.length > 1 }
            if (alts.isEmpty()) "sin alteraciones · referencia mayor" else "alteraciones: " + alts.joinToString(" · ")
        }
    }

    /** Nota al pie para pentatónicas (capa melódica sobre la madre). */
    fun pentatonicHint(scale: ScaleType): String? = when (scale) {
        ScaleType.PENT_MAJOR -> "Cinco notas para melodía, sobre los acordes de la escala mayor."
        ScaleType.PENT_MINOR -> "Cinco notas para melodía, sobre los acordes de la escala menor natural."
        else -> null
    }

    // ----------------------------------------------------------- inversiones

    /** Inversión k del acorde: orden cerrado de clases de altura + apilado absoluto. */
    data class InversionData(
        val index: Int,
        val bassPc: Int,
        val orderPcs: List<Int>,
        /** Semitonos absolutos (apilado cerrado desde el bajo, base C=0). */
        val abs: List<Int>,
    ) {
        val isRootPosition: Boolean get() = index == 0
    }

    fun inversion(chord: Chord, inv: Int): InversionData {
        val pcs = chord.notes.map { it.pitchClass }.distinct()
        val k = inv.coerceIn(0, pcs.size - 1)
        val order = List(pcs.size) { pcs[(k + it) % pcs.size] }
        val abs = mutableListOf(order[0])
        for (i in 1 until order.size) {
            val step = (order[i] - (abs[i - 1] % 12)).mod(12)
            abs += abs[i - 1] + if (step == 0) 12 else step
        }
        return InversionData(index = k, bassPc = order[0], orderPcs = order, abs = abs)
    }

    // ---------------------------------------------------------- progresiones

    data class WheelProg(val name: String, val degrees: List<Int>, val desc: String)

    private val PROGS_MAJ = listOf(
        WheelProg("Pop clásico", listOf(0, 4, 5, 3), "I–V–vi–IV · miles de canciones"),
        WheelProg("Jazz", listOf(1, 4, 0), "ii–V–I · la cadencia del jazz"),
        WheelProg("Rock", listOf(0, 3, 4), "I–IV–V · rock y blues"),
        WheelProg("Balada", listOf(5, 3, 0, 4), "vi–IV–I–V · emotiva"),
    )
    private val PROGS_MIN = listOf(
        WheelProg("Épica", listOf(0, 5, 2, 6), "i–VI–III–VII · himnos"),
        WheelProg("Clásica menor", listOf(0, 3, 4), "i–iv–v · la base"),
        WheelProg("Descendente", listOf(0, 6, 5, 4), "i–VII–VI–v · aire andaluz"),
    )
    private val PROGS_HARM = listOf(
        WheelProg("Cadencia menor", listOf(0, 3, 4), "i–iv–V · resuelve fuerte"),
        WheelProg("Jazz menor", listOf(1, 4, 0), "ii°–V–i · tensión y llegada"),
        WheelProg("Dramática", listOf(5, 6, 0), "VI–vii°–i · suspenso"),
    )

    /** Progresiones sugeridas según la familia de la escala activa (PROGKEY del diseño). */
    fun progressionsFor(scale: ScaleType): List<WheelProg> = when (scale) {
        ScaleType.MAJOR, ScaleType.IONIAN, ScaleType.LYDIAN, ScaleType.MIXOLYDIAN, ScaleType.PENT_MAJOR -> PROGS_MAJ
        ScaleType.HARMONIC_MINOR, ScaleType.MELODIC_MINOR -> PROGS_HARM
        else -> PROGS_MIN
    }

    // ------------------------------------------------------------ el campo

    /** Campo del nivel activo con el campo de tríadas en paralelo (para deg/qual del diseño). */
    data class WheelField(
        val key: Key,
        val triads: List<Degree>,
        val active: List<Degree>,
        val seventh: Boolean,
    ) {
        /** Romano del diseño: el de la tríada (I, ii, vii°, III+), sin sufijo de 7ª. */
        fun designDeg(i: Int): String = triads[i].roman
    }

    fun field(rootPc: Int, scale: ScaleType, seventh: Boolean): WheelField {
        val tonic = tonicFor(rootPc, scale)
        val key = Key(tonic, scale)
        val fieldKey = if (scale.category == ScaleCategory.PENTATONIC) key.parentKey()!! else key
        return WheelField(
            key = key,
            triads = fieldKey.diatonicField(ChordLevel.TRIADS),
            active = fieldKey.diatonicField(if (seventh) ChordLevel.SEVENTHS else ChordLevel.TRIADS),
            seventh = seventh,
        )
    }
}
