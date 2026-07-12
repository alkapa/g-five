package com.alkapa.circuloquintas.domain

/** "F##" → Note('F', 2); "Bb" → Note('B', -1). */
fun note(s: String): Note {
    val letter = s[0]
    val accidental = when (s.drop(1)) {
        "" -> 0; "#" -> 1; "##" -> 2; "b" -> -1; "bb" -> -2
        else -> throw IllegalArgumentException("Nota ilegible: $s")
    }
    return Note(letter, accidental)
}

fun key(tonic: String, scale: ScaleType) = Key(note(tonic), scale)

/** Cifrado americano del acorde. */
fun am(c: Chord): String = DefaultNoteFormatter.format(c, Notation.AMERICAN)

fun am(n: Note): String = DefaultNoteFormatter.format(n, Notation.AMERICAN)

/** Nombres americanos del campo armónico completo. */
fun fieldNames(k: Key, level: ChordLevel = ChordLevel.TRIADS): List<String> =
    k.diatonicField(level).map { am(it.chord) }

fun fieldRomans(k: Key, level: ChordLevel = ChordLevel.TRIADS): List<String> =
    k.diatonicField(level).map { it.roman }

fun noteNames(k: Key): List<String> = k.notes().map { am(it) }
