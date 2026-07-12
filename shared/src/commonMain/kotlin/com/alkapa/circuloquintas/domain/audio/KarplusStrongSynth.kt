package com.alkapa.circuloquintas.domain.audio

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow

/**
 * Síntesis Karplus-Strong (cuerda pulsada) en Kotlin puro — decisión-default
 * de la spec: timbre coherente con una app de guitarra, sin samples.
 *
 * Es la síntesis de referencia del proyecto: la usa AudioTrackAudioEngine
 * (camino activo) y está portada 1:1 a C++ para el camino Oboe (flag
 * `-PenableOboe=true`). Al ser común y determinista, se somete a smoke tests
 * en jvmTest (buffer no silente, sin NaN, decaimiento).
 */
class KarplusStrongVoice(
    midi: Int,
    sampleRate: Int,
    amplitude: Float = DEFAULT_AMPLITUDE,
) {
    private val delay: FloatArray
    private var pos = 0
    private var envelope: Float

    init {
        val freq = midiToFrequency(midi)
        val length = max(2, (sampleRate / freq).toInt())
        // Ruido inicial determinista (LCG): reproducible en tests y entre plataformas.
        var state = midi * 1103515245 + 12345
        delay = FloatArray(length) {
            state = state * 1103515245 + 12345
            (((state ushr 8) and 0xFFFF).toFloat() / 32768f - 1f) * amplitude
        }
        envelope = amplitude
    }

    /** La voz decae naturalmente; muerta cuando su envolvente es inaudible. */
    val isActive: Boolean get() = envelope > SILENCE_THRESHOLD

    fun nextSample(): Float {
        val current = delay[pos]
        val nextIndex = (pos + 1) % delay.size
        // Filtro promedio de 2 puntos + amortiguación: el corazón de Karplus-Strong.
        delay[pos] = DAMPING * 0.5f * (current + delay[nextIndex])
        pos = nextIndex
        envelope = max(abs(current), envelope * ENVELOPE_DECAY)
        return current
    }

    companion object {
        const val DEFAULT_AMPLITUDE = 0.5f
        private const val DAMPING = 0.996f
        private const val ENVELOPE_DECAY = 0.9999f
        private const val SILENCE_THRESHOLD = 1e-4f

        fun midiToFrequency(midi: Int): Double = 440.0 * 2.0.pow((midi - 69) / 12.0)

        /** Render de conveniencia de una sola cuerda (tests y previews). */
        fun renderPluck(midi: Int, sampleRate: Int, seconds: Float): FloatArray {
            val voice = KarplusStrongVoice(midi, sampleRate)
            return FloatArray((sampleRate * seconds).toInt()) { voice.nextSample() }
        }
    }
}

/**
 * Mezclador simple de voces: suma con atenuación y saturación suave para que
 * los acordes de 6-7 notas no recorten.
 */
class VoiceMixer {
    private val voices = mutableListOf<KarplusStrongVoice>()

    val activeVoices: Int get() = voices.size

    fun pluck(midi: Int, sampleRate: Int) {
        voices += KarplusStrongVoice(midi, sampleRate)
        // Techo de polifonía defensivo: las voces más viejas se descartan.
        while (voices.size > MAX_VOICES) voices.removeAt(0)
    }

    fun renderInto(buffer: FloatArray, frames: Int) {
        for (i in 0 until frames) {
            var sum = 0f
            for (v in voices) sum += v.nextSample()
            buffer[i] = softClip(sum * MIX_GAIN)
        }
        voices.removeAll { !it.isActive }
    }

    fun clear() = voices.clear()

    /** Saturación cúbica suave: lineal cerca de 0, techo en ±1. */
    private fun softClip(x: Float): Float {
        val c = x.coerceIn(-1.5f, 1.5f)
        return c - (c * c * c) / 6.75f
    }

    companion object {
        private const val MAX_VOICES = 24
        private const val MIX_GAIN = 0.35f
    }
}
