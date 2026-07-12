package com.alkapa.circuloquintas.domain.audio

/**
 * Traducción de nota/acorde/escala/progresión a disparos con marca de tiempo
 * en samples. Lógica común a cualquier motor de audio (AudioTrack, Oboe, un
 * futuro iOS) y testeable sin plataforma.
 */
object PlaybackSchedule {

    /** Disparo de una cuerda. [eventIndex] es el índice de acorde/nota para la UI. */
    data class Trigger(val startSample: Long, val midi: Int, val eventIndex: Int)

    data class Schedule(val triggers: List<Trigger>, val totalSamples: Long)

    fun samplesPerBeat(bpm: Int, sampleRate: Int): Long =
        (sampleRate * 60L) / bpm.coerceIn(20, 400)

    /** Escala nota a nota, una por pulso. */
    fun forScale(midis: List<Int>, bpm: Int, sampleRate: Int): Schedule {
        val beat = samplesPerBeat(bpm, sampleRate)
        val triggers = midis.mapIndexed { i, midi -> Trigger(i * beat, midi, i) }
        return Schedule(triggers, beat * midis.size)
    }

    /** Acorde rasgueado: [strumMs] entre cuerda y cuerda. */
    fun forChord(midis: List<Int>, strumMs: Int, sampleRate: Int): Schedule {
        val strum = (sampleRate * strumMs) / 1000L
        val triggers = midis.mapIndexed { i, midi -> Trigger(i * strum, midi, 0) }
        // Duración nominal: el rasgueo + 2 segundos de resonancia.
        return Schedule(triggers, strum * midis.size + sampleRate * 2L)
    }

    /** Progresión: cada evento dura sus beats; el acorde se rasguea al inicio. */
    fun forProgression(
        events: List<ChordEvent>,
        bpm: Int,
        strumMs: Int,
        sampleRate: Int,
    ): Schedule {
        val beat = samplesPerBeat(bpm, sampleRate)
        val strum = (sampleRate * strumMs) / 1000L
        val triggers = mutableListOf<Trigger>()
        var cursor = 0L
        events.forEachIndexed { index, event ->
            event.midis.forEachIndexed { i, midi ->
                triggers += Trigger(cursor + i * strum, midi, index)
            }
            cursor += beat * event.beats
        }
        return Schedule(triggers, cursor)
    }
}
