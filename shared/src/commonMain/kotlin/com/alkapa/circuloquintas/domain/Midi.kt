package com.alkapa.circuloquintas.domain

/**
 * Conversión Note → MIDI (decisión-default §7): la tónica vive en 48..59
 * (C3..B3 en convención MIDI C-1 = 0) y los acordes se tocan en fundamental
 * cerrada, extensiones por encima.
 */
object MidiMapper {

    private const val BASE_C = 48

    fun tonicMidi(tonic: Note): Int = BASE_C + tonic.pitchClass

    /**
     * Escala ascendente desde la tónica, terminando en la octava (todos los
     * patrones de §3.2 suman 12 semitonos).
     */
    fun scaleMidis(key: Key): List<Int> {
        val start = tonicMidi(key.tonic)
        val result = mutableListOf(start)
        var acc = start
        for (step in key.scale.pattern) {
            acc += step
            result += acc
        }
        return result
    }

    /**
     * Voicing del acorde de un grado: fundamental dentro de la octava de la
     * tónica (tónica..tónica+11) y el resto apilado según [Chord.stackOffsets].
     */
    fun chordMidis(key: Key, chord: Chord): List<Int> {
        val tonic = tonicMidi(key.tonic)
        val root = tonic + (chord.root.pitchClass - key.tonic.pitchClass).mod(12)
        return chord.stackOffsets.map { root + it }
    }

    fun noteMidi(key: Key, note: Note): Int {
        val tonic = tonicMidi(key.tonic)
        return tonic + (note.pitchClass - key.tonic.pitchClass).mod(12)
    }
}
