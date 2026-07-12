package com.alkapa.circuloquintas.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import com.alkapa.circuloquintas.domain.audio.AudioEngine
import com.alkapa.circuloquintas.domain.audio.ChordEvent
import com.alkapa.circuloquintas.domain.audio.PlaybackSchedule
import com.alkapa.circuloquintas.domain.audio.VoiceMixer
import java.util.concurrent.atomic.AtomicReference

/**
 * Motor de audio activo por defecto (§8, camino de resiliencia): la misma
 * síntesis Karplus-Strong común, servida por AudioTrack en streaming. El
 * camino Oboe/NDK vive detrás del flag de Gradle `-PenableOboe=true`.
 *
 * Un único hilo de render es dueño del estado del secuenciador; la UI se
 * comunica con él mediante referencias atómicas (sin locks en el camino de
 * audio). Los disparos se cuantizan al tamaño de bloque (~12 ms), suficiente
 * para el rasgueo de 40 ms.
 */
class AudioTrackAudioEngine : AudioEngine {

    private data class Program(
        val schedule: PlaybackSchedule.Schedule,
        val loop: Boolean,
        val notifyIndices: Boolean,
    )

    private val sampleRate = 44100
    private val bufferFrames = 512
    private val mixer = VoiceMixer()
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile private var listener: AudioEngine.PlaybackListener? = null
    @Volatile private var running = true
    @Volatile private var stopRequested = false
    private val pendingProgram = AtomicReference<Program?>(null)

    private val renderThread = Thread(::renderLoop, "circulo-audio").apply {
        isDaemon = true
        start()
    }

    override fun playNote(midi: Int) {
        submit(PlaybackSchedule.forChord(listOf(midi), 0, sampleRate), loop = false, notify = false)
    }

    override fun playChord(midis: List<Int>, strumMs: Int) {
        submit(PlaybackSchedule.forChord(midis, strumMs, sampleRate), loop = false, notify = false)
    }

    override fun playScale(midis: List<Int>, bpm: Int) {
        submit(PlaybackSchedule.forScale(midis, bpm, sampleRate), loop = false, notify = false)
    }

    override fun playProgression(events: List<ChordEvent>, bpm: Int, loop: Boolean) {
        submit(PlaybackSchedule.forProgression(events, bpm, 40, sampleRate), loop, notify = true)
    }

    override fun stop() {
        stopRequested = true
    }

    override fun setPlaybackListener(listener: AudioEngine.PlaybackListener?) {
        this.listener = listener
    }

    /** Libera el hilo y el AudioTrack. El engine no puede reutilizarse después. */
    fun release() {
        running = false
    }

    private fun submit(schedule: PlaybackSchedule.Schedule, loop: Boolean, notify: Boolean) {
        pendingProgram.set(Program(schedule, loop, notify))
    }

    private fun renderLoop() {
        val minBytes = AudioTrack.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT,
        )
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setBufferSizeInBytes(maxOf(minBytes, bufferFrames * java.lang.Float.BYTES * 2))
            .build()
        track.play()

        val buffer = FloatArray(bufferFrames)
        var program: Program? = null
        var position = 0L
        var nextTrigger = 0
        var lastNotified = -1

        fun endProgram(notify: Boolean) {
            if (notify && program?.notifyIndices == true) {
                mainHandler.post { listener?.onPlaybackEnded() }
            }
            program = null
            position = 0L
            nextTrigger = 0
            lastNotified = -1
        }

        while (running) {
            pendingProgram.getAndSet(null)?.let {
                program = it
                position = 0L
                nextTrigger = 0
                lastNotified = -1
            }
            if (stopRequested) {
                stopRequested = false
                if (program != null) endProgram(notify = true)
            }
            program?.let { p ->
                val triggers = p.schedule.triggers
                while (nextTrigger < triggers.size &&
                    triggers[nextTrigger].startSample < position + bufferFrames
                ) {
                    val t = triggers[nextTrigger]
                    mixer.pluck(t.midi, sampleRate)
                    if (p.notifyIndices && t.eventIndex != lastNotified) {
                        lastNotified = t.eventIndex
                        val index = t.eventIndex
                        mainHandler.post { listener?.onChordStarted(index) }
                    }
                    nextTrigger++
                }
                position += bufferFrames
                if (position >= p.schedule.totalSamples) {
                    if (p.loop) {
                        position = 0L
                        nextTrigger = 0
                        lastNotified = -1
                    } else {
                        endProgram(notify = true)
                    }
                }
            }
            mixer.renderInto(buffer, bufferFrames)
            track.write(buffer, 0, bufferFrames, AudioTrack.WRITE_BLOCKING)
        }
        track.stop()
        track.release()
    }
}
