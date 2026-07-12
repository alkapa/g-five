package com.alkapa.circuloquintas.domain

/** Funciones armónicas clásicas — lente tonal (§4). */
enum class TonalFunction { TONIC, SUBDOMINANT, DOMINANT }

/** Categorías de la lente modal (§5). */
enum class ModalFunction { CENTER, CHARACTERISTIC, NEUTRAL }

/** Necesidad expresiva del buscador por sensación (§6.2.7). */
enum class Need { REST, MOTION, TENSION, COLOR }

/** Nivel del selector de complejidad de acordes. */
enum class ChordLevel { TRIADS, SEVENTHS, EXTENSIONS }

/**
 * Asignación de funciones tonales por grado (§4.1/§4.2). `function == null`
 * significa "sin función clásica clara" (III+ en armónica/melódica, vi° puente
 * en melódica): la ficha lo explica y la UI lo presenta como color/inestable.
 */
internal object TonalFunctionTable {

    data class Entry(val function: TonalFunction?, val isAmbiguous: Boolean)

    private val T = Entry(TonalFunction.TONIC, false)
    private val Ta = Entry(TonalFunction.TONIC, true)
    private val S = Entry(TonalFunction.SUBDOMINANT, false)
    private val Sa = Entry(TonalFunction.SUBDOMINANT, true)
    private val D = Entry(TonalFunction.DOMINANT, false)
    private val Da = Entry(TonalFunction.DOMINANT, true)
    private val Xa = Entry(null, true)

    private val MAJOR_TABLE = listOf(T, S, Ta, S, D, Ta, Da)
    private val NATURAL_MINOR_TABLE = listOf(T, S, Ta, S, Da, Sa, Da)
    private val HARMONIC_MINOR_TABLE = listOf(T, S, Xa, S, D, Sa, D)
    private val MELODIC_MINOR_TABLE = listOf(T, S, Xa, Sa, D, Xa, D)

    /** [degreeIndex] es 1..7. Solo para escalas de lente tonal. */
    fun entryFor(scale: ScaleType, degreeIndex: Int): Entry {
        val table = when (scale.pedagogicalAlias) {
            ScaleType.MAJOR -> MAJOR_TABLE
            ScaleType.NATURAL_MINOR -> NATURAL_MINOR_TABLE
            ScaleType.HARMONIC_MINOR -> HARMONIC_MINOR_TABLE
            ScaleType.MELODIC_MINOR -> MELODIC_MINOR_TABLE
            else -> throw IllegalArgumentException("Sin tabla tonal para $scale")
        }
        return table[degreeIndex - 1]
    }
}

/**
 * Grado de la escala del modo donde vive la nota característica (§5.1):
 * la que distingue al modo de su referencia mayor/menor.
 */
internal val CHARACTERISTIC_DEGREE: Map<ScaleType, Int> = mapOf(
    ScaleType.DORIAN to 6,       // 6ª mayor (vs. eólico)
    ScaleType.PHRYGIAN to 2,     // 2ª menor (vs. eólico)
    ScaleType.LYDIAN to 4,       // 4ª aumentada (vs. jónico)
    ScaleType.MIXOLYDIAN to 7,   // 7ª menor (vs. jónico)
    ScaleType.LOCRIAN to 5,      // 5ª disminuida (vs. eólico)
)

/**
 * Grado del campo armónico: número, romano según convención §3.3/§3.4,
 * acorde del nivel activo, funciones según la lente, ambigüedad y clave de
 * ficha pedagógica. En nivel EXTENSIONS, [extensions] trae los acordes de
 * color generados por regla sobre este grado.
 */
data class Degree(
    val index: Int,
    val roman: String,
    val chord: Chord,
    val tonalFunction: TonalFunction?,
    val modalFunction: ModalFunction?,
    val isAmbiguous: Boolean,
    val pedagogicalKey: String,
    val extensions: List<Chord> = emptyList(),
)

/** Romano del grado [index] (1..7) con la calidad dada (§3.3/§3.4). */
fun romanFor(index: Int, quality: ChordQuality): String {
    val base = listOf("I", "II", "III", "IV", "V", "VI", "VII")[index - 1]
    val cased = if (quality.upperRoman) base else base.lowercase()
    // La tríada menor no lleva sufijo: la minúscula ya lo dice (ii, no iim).
    val suffix = if (quality == ChordQuality.MINOR) "" else quality.symbol
    return cased + suffix
}
