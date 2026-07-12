package com.alkapa.circuloquintas.audio

import android.os.Handler
import android.os.Looper
import com.alkapa.circuloquintas.domain.audio.AudioEngine
import com.alkapa.circuloquintas.domain.audio.ChordEvent
import com.alkapa.circuloquintas.domain.audio.PlaybackSchedule

/**
 * Motor Oboe (NDK/C++): stream de baja latencia con la misma síntesis
 * Karplus-Strong portada a C++ (src/main/cpp). Solo participa del build con
 * `-PenableOboe=true`; en runtime se usa únicamente si la librería nativa
 * cargó (ver [AudioEngineFactory]).
 *
 * La agenda de disparos se calcula en Kotlin común ([PlaybackSchedule]) y se
 * cruza por JNI como arrays planos; C++ solo reproduce. El índice del acorde
 * sonando se sondea desde un hilo liviano (evita upcalls JNI).
 */
class OboeAudioEngine : AudioEngine {

    private val handle: Long = nativeCreate()
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile private var listener: AudioEngine.PlaybackListener? = null
    @Volatile private var running = true

    private val pollThread = Thread(::pollLoop, "oboe-poll").apply {
        isDaemon = true
        start()
    }

    override fun playNote(midi: Int) {
        submit(PlaybackSchedule.forChord(listOf(midi), 0, sampleRate()), loop = false, notify = false)
    }

    override fun playChord(midis: List<Int>, strumMs: Int) {
        submit(PlaybackSchedule.forChord(midis, strumMs, sampleRate()), loop = false, notify = false)
    }

    override fun playScale(midis: List<Int>, bpm: Int) {
        submit(PlaybackSchedule.forScale(midis, bpm, sampleRate()), loop = false, notify = false)
    }

    override fun playProgression(events: List<ChordEvent>, bpm: Int, loop: Boolean) {
        submit(PlaybackSchedule.forProgression(events, bpm, 40, sampleRate()), loop, notify = true)
    }

    override fun stop() {
        nativeStop(handle)
    }

    override fun setPlaybackListener(listener: AudioEngine.PlaybackListener?) {
        this.listener = listener
    }

    fun release() {
        running = false
        nativeRelease(handle)
    }

    private fun sampleRate(): Int = nativeSampleRate(handle)

    private fun submit(schedule: PlaybackSchedule.Schedule, loop: Boolean, notify: Boolean) {
        val starts = LongArray(schedule.triggers.size)
        val midis = IntArray(schedule.triggers.size)
        val indices = IntArray(schedule.triggers.size)
        schedule.triggers.forEachIndexed { i, t ->
            starts[i] = t.startSample
            midis[i] = t.midi
            indices[i] = t.eventIndex
        }
        nativeSubmit(handle, starts, midis, indices, schedule.totalSamples, loop, notify)
    }

    private fun pollLoop() {
        var last = -1
        while (running) {
            val current = nativeCurrentEventIndex(handle)
            if (current != last) {
                if (current >= 0) {
                    mainHandler.post { listener?.onChordStarted(current) }
                } else if (last >= 0) {
                    mainHandler.post { listener?.onPlaybackEnded() }
                }
                last = current
            }
            try {
                Thread.sleep(30)
            } catch (_: InterruptedException) {
                return
            }
        }
    }

    private external fun nativeCreate(): Long
    private external fun nativeSampleRate(handle: Long): Int
    private external fun nativeSubmit(
        handle: Long,
        startSamples: LongArray,
        midis: IntArray,
        eventIndices: IntArray,
        totalSamples: Long,
        loop: Boolean,
        notify: Boolean,
    )
    private external fun nativeStop(handle: Long)
    private external fun nativeCurrentEventIndex(handle: Long): Int
    private external fun nativeRelease(handle: Long)

    companion object {
        @Volatile private var loaded: Boolean? = null

        /** True si la librería nativa está presente y carga (build con -PenableOboe). */
        fun isAvailable(): Boolean {
            loaded?.let { return it }
            return try {
                System.loadLibrary("circuloaudio")
                loaded = true
                true
            } catch (_: UnsatisfiedLinkError) {
                loaded = false
                false
            }
        }
    }
}
