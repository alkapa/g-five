package com.alkapa.circuloquintas.domain.instrument

import com.alkapa.circuloquintas.domain.Chord
import com.alkapa.circuloquintas.domain.ChordQuality
import com.alkapa.circuloquintas.domain.Note

/** Rol de una voz dentro del acorde (colorea diapasón y piano). */
enum class ChordRole { ROOT, THIRD, FIFTH, SEVENTH, EXTENSION }

/** Voz del acorde: nota + rol + semitonos sobre la fundamental (voicing cerrado). */
data class ChordTone(val note: Note, val offset: Int, val role: ChordRole)

/** Inversión: qué voz va en el bajo. */
data class Inversion(val index: Int, val bass: ChordTone, val name: String)

/**
 * Modelo de voces e inversiones para visualización en instrumento.
 *
 * Los acordes extendidos se reducen a sus 4 VOCES ESENCIALES para digitación
 * e inversión (fundamental, 3ª o sus, 7ª — o 5ª/6ª si no hay 7ª — y la
 * extensión que da nombre al acorde): es la práctica común en guitarra,
 * donde 5ªs y 9ªs intermedias se omiten. La UI lo declara.
 */
object ChordVoicingModel {

    fun role(offset: Int): ChordRole = when {
        offset == 0 -> ChordRole.ROOT
        offset in 2..5 -> ChordRole.THIRD      // 3ª mayor/menor y voces sus (2ª/4ª)
        offset in 6..8 -> ChordRole.FIFTH
        offset in 9..11 -> ChordRole.SEVENTH   // 7ªs (y 6ª/°7 como voz de séptima)
        else -> ChordRole.EXTENSION
    }

    /** Etiqueta corta del intervalo para la UI ("F", "3ª", "7ª", "9ª"...). */
    fun intervalLabel(offset: Int, quality: ChordQuality): String = when (offset) {
        0 -> "F"
        2 -> "2ª"
        3, 4 -> "3ª"
        5 -> "4ª"
        6 -> if (quality == ChordQuality.AUGMENTED || quality == ChordQuality.MAJ7SHARP5) "4ª+" else "5ª"
        7 -> "5ª"
        8 -> "5ª+"
        9 -> if (quality == ChordQuality.DIM7) "7ª" else "6ª"
        10, 11 -> "7ª"
        13, 14 -> "9ª"
        17, 18 -> "11ª"
        20, 21 -> "13ª"
        else -> "$offset"
    }

    /** Todas las voces del acorde con rol (para el mapa completo del diapasón). */
    fun allTones(chord: Chord): List<ChordTone> =
        chord.notes.mapIndexed { i, note ->
            ChordTone(note, chord.stackOffsets[i], role(chord.stackOffsets[i]))
        }

    /**
     * Voces esenciales (≤4) sobre las que se calculan inversiones y
     * digitaciones: tríadas y cuatríadas completas; extensiones reducidas a
     * fundamental + voz de 3ª (o sus) + voz de 7ª (o 5ª/6ª) + extensión.
     */
    fun coreTones(chord: Chord): List<ChordTone> {
        val all = allTones(chord)
        if (all.size <= 4) return all
        val result = mutableListOf<ChordTone>()
        all.firstOrNull { it.role == ChordRole.ROOT }?.let { result += it }
        all.firstOrNull { it.role == ChordRole.THIRD }?.let { result += it }
        val seventh = all.firstOrNull { it.role == ChordRole.SEVENTH }
        if (seventh != null) {
            result += seventh
        } else {
            all.firstOrNull { it.role == ChordRole.FIFTH }?.let { result += it }
        }
        // La extensión que da nombre: la voz más aguda.
        all.lastOrNull { it.role == ChordRole.EXTENSION }?.let { result += it }
        return result.distinct().sortedBy { it.offset }
    }

    fun inversions(chord: Chord): List<Inversion> =
        coreTones(chord).mapIndexed { i, tone ->
            Inversion(i, tone, if (i == 0) "Fundamental" else "${i}ª inversión")
        }

    /**
     * Colocaciones en piano de la inversión: posición cerrada con el bajo
     * elegido, repetida en cada octava donde quepa dentro de [lowMidi..highMidi].
     * Devuelve una lista de acordes (midis absolutos), del grave al agudo.
     */
    fun pianoPlacements(
        chord: Chord,
        inversionIndex: Int,
        lowMidi: Int = 36,
        highMidi: Int = 95,
    ): List<List<Int>> {
        val core = coreTones(chord)
        if (core.isEmpty()) return emptyList()
        val idx = inversionIndex.coerceIn(0, core.lastIndex)
        // Orden cíclico de voces empezando por el bajo elegido.
        val rotated = List(core.size) { core[(idx + it) % core.size] }
        // Apilado cerrado: cada voz por encima de la anterior.
        val offsets = mutableListOf(0)
        for (i in 1 until rotated.size) {
            val step = (rotated[i].note.pitchClass - rotated[i - 1].note.pitchClass).mod(12)
            offsets += offsets.last() + if (step == 0) 12 else step
        }
        val bassPc = rotated.first().note.pitchClass
        val placements = mutableListOf<List<Int>>()
        for (bassMidi in lowMidi..highMidi) {
            if (bassMidi.mod(12) != bassPc) continue
            val midis = offsets.map { bassMidi + it }
            if (midis.last() <= highMidi) placements += midis
        }
        return placements
    }

    /** Voces (con rol) de la inversión en el mismo orden que [pianoPlacements]. */
    fun rotatedTones(chord: Chord, inversionIndex: Int): List<ChordTone> {
        val core = coreTones(chord)
        if (core.isEmpty()) return emptyList()
        val idx = inversionIndex.coerceIn(0, core.lastIndex)
        return List(core.size) { core[(idx + it) % core.size] }
    }
}
