package com.alkapa.circuloquintas.domain

/**
 * Círculo de quintas fijo (§3.1): 12 posiciones en sentido horario desde C.
 * Anillo exterior: mayores con las grafías convencionales del anillo; anillo
 * interior: relativas menores. La posición 6 es enarmónica (F#/Gb, D#m/Ebm).
 */
object CircleOfFifths {

    /**
     * Sector del círculo. [majorAlt]/[minorAlt] son las grafías alternativas
     * de la posición enarmónica (null en el resto).
     */
    data class Sector(
        val position: Int,
        val major: Note,
        val majorAlt: Note?,
        val minor: Note,
        val minorAlt: Note?,
    )

    private fun n(letter: Char, accidental: Int = 0) = Note(letter, accidental)

    val sectors: List<Sector> = listOf(
        Sector(0, n('C'), null, n('A'), null),
        Sector(1, n('G'), null, n('E'), null),
        Sector(2, n('D'), null, n('B'), null),
        Sector(3, n('A'), null, n('F', 1), null),
        Sector(4, n('E'), null, n('C', 1), null),
        Sector(5, n('B'), null, n('G', 1), null),
        Sector(6, n('F', 1), n('G', -1), n('D', 1), n('E', -1)),
        Sector(7, n('D', -1), null, n('B', -1), null),
        Sector(8, n('A', -1), null, n('F'), null),
        Sector(9, n('E', -1), null, n('C'), null),
        Sector(10, n('B', -1), null, n('G'), null),
        Sector(11, n('F'), null, n('D'), null),
    )

    /** Posición (0..11) cuya clase de altura mayor coincide con [pitchClass]. */
    fun positionOfPitchClass(pitchClass: Int): Int =
        sectors.first { it.major.pitchClass == pitchClass.mod(12) }.position

    /**
     * Grafía enarmónica alternativa de la posición 6 del círculo
     * (F# ↔ Gb como tónica mayor; D# ↔ Eb como tónica menor). Null si la
     * nota no participa del par enarmónico del círculo.
     */
    fun enharmonicAlternative(tonic: Note): Note? = when (tonic) {
        Note('F', 1) -> Note('G', -1)
        Note('G', -1) -> Note('F', 1)
        Note('D', 1) -> Note('E', -1)
        Note('E', -1) -> Note('D', 1)
        else -> null
    }
}

/** Posiciones del círculo ocupadas por las notas de la escala (para resaltar). */
fun Key.circlePositions(): Set<Int> =
    notes().map { CircleOfFifths.positionOfPitchClass(it.pitchClass) }.toSet()

/**
 * Para mayor y modos, las 7 notas forman un arco contiguo de quintas (hecho
 * visual pedagógico clave, §6.2). Devuelve la posición inicial del arco de
 * [length] posiciones contiguas, o null si las posiciones no son contiguas
 * (armónica/melódica).
 */
fun contiguousArcStart(positions: Set<Int>, length: Int = 7): Int? {
    for (start in 0 until 12) {
        val arc = (0 until length).map { (start + it).mod(12) }.toSet()
        if (arc == positions) return start
    }
    return null
}
