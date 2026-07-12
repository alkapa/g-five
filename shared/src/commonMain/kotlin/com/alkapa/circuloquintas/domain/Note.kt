package com.alkapa.circuloquintas.domain

/**
 * Nota musical como grafía: letra `A..G` + alteración entera en `-2..+2`
 * (bb, b, natural, #, ##). Los dobles accidentes son necesarios: p. ej.
 * Sol# menor armónica contiene Fx (F##).
 *
 * El modelo interno es independiente del cifrado (americano/latino): eso es
 * solo formateo (ver [NoteFormatter]).
 */
data class Note(val letter: Char, val accidental: Int) {

    init {
        require(letter in 'A'..'G') { "Letra inválida: $letter" }
        require(accidental in -2..2) { "Alteración fuera de rango: $accidental" }
    }

    /** Clase de altura 0..11 (C = 0). */
    val pitchClass: Int
        get() = (naturalPitchClass(letter) + accidental).mod(12)

    companion object {
        /** Orden natural de letras para recorrer grados: una letra por grado. */
        const val LETTER_ORDER = "CDEFGAB"

        fun naturalPitchClass(letter: Char): Int = when (letter) {
            'C' -> 0; 'D' -> 2; 'E' -> 4; 'F' -> 5; 'G' -> 7; 'A' -> 9; 'B' -> 11
            else -> throw IllegalArgumentException("Letra inválida: $letter")
        }

        /** Letra a [offset] grados (positivos o negativos) de [start], cíclica. */
        fun letterAt(start: Char, offset: Int): Char {
            val idx = LETTER_ORDER.indexOf(start)
            require(idx >= 0) { "Letra inválida: $start" }
            return LETTER_ORDER[(idx + offset).mod(7)]
        }

        /**
         * Nota con la [letter] dada cuya clase de altura sea [targetPitchClass],
         * eligiendo la alteración mínima. Lanza si requiere más de ±2.
         */
        fun spelled(letter: Char, targetPitchClass: Int): Note {
            val natural = naturalPitchClass(letter)
            val accidental = ((targetPitchClass - natural + 6).mod(12)) - 6
            require(accidental in -2..2) {
                "Grafía imposible: $letter hacia pc=$targetPitchClass exigiría alteración $accidental"
            }
            return Note(letter, accidental)
        }
    }
}
