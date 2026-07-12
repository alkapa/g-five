package com.alkapa.circuloquintas.domain.audio

/** Evento de la línea de tiempo de una progresión. */
data class ChordEvent(val midis: List<Int>, val beats: Int)

/**
 * Motor de audio. La interfaz vive en :shared para que cada plataforma
 * aporte su implementación (Android: Oboe/NDK o AudioTrack; un futuro target
 * iOS aportaría la suya). El motor de dominio no conoce la implementación.
 */
interface AudioEngine {

    fun playNote(midi: Int)

    /** Acorde con rasgueo simple: [strumMs] entre nota y nota (default 40 ms). */
    fun playChord(midis: List<Int>, strumMs: Int = 40)

    /** Escala nota a nota al tempo dado. */
    fun playScale(midis: List<Int>, bpm: Int)

    /** Progresión completa; con [loop] repite hasta [stop]. */
    fun playProgression(events: List<ChordEvent>, bpm: Int, loop: Boolean)

    fun stop()

    /**
     * Observador de reproducción para la UI (indicador del acorde sonando,
     * §6.4). [onChordStarted] recibe el índice del evento en curso.
     */
    fun setPlaybackListener(listener: PlaybackListener?)

    interface PlaybackListener {
        fun onChordStarted(index: Int)
        fun onPlaybackEnded()
    }
}
