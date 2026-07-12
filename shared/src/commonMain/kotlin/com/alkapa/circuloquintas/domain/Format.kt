package com.alkapa.circuloquintas.domain

/** Cifrado de notas: SOLO formateo, el modelo interno no cambia (§3.1). */
enum class Notation { AMERICAN, LATIN }

interface NoteFormatter {
    fun format(n: Note, notation: Notation): String
    fun format(c: Chord, notation: Notation): String
}

object DefaultNoteFormatter : NoteFormatter {

    private val LATIN_NAMES = mapOf(
        'C' to "Do", 'D' to "Re", 'E' to "Mi", 'F' to "Fa",
        'G' to "Sol", 'A' to "La", 'B' to "Si",
    )

    fun accidentalString(accidental: Int): String = when (accidental) {
        -2 -> "bb"; -1 -> "b"; 0 -> ""; 1 -> "#"; 2 -> "##"
        else -> throw IllegalArgumentException("Alteración fuera de rango: $accidental")
    }

    override fun format(n: Note, notation: Notation): String = when (notation) {
        Notation.AMERICAN -> "${n.letter}${accidentalString(n.accidental)}"
        Notation.LATIN -> "${LATIN_NAMES.getValue(n.letter)}${accidentalString(n.accidental)}"
    }

    /**
     * Americano: sufijo pegado (F#m7b5). Latino: sufijo separado con espacio
     * (Fa# m7b5); la tríada mayor no lleva sufijo (Do).
     */
    override fun format(c: Chord, notation: Notation): String {
        val rootName = format(c.root, notation)
        val suffix = c.quality.symbol
        return when {
            suffix.isEmpty() -> rootName
            notation == Notation.AMERICAN -> "$rootName$suffix"
            else -> "$rootName $suffix"
        }
    }
}
