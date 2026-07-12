package com.alkapa.circuloquintas.domain.audio

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SynthSmokeTest {

    private val sampleRate = 44100

    @Test
    fun laCuerdaSuenaYNoProduceNaN() {
        val buffer = KarplusStrongVoice.renderPluck(midi = 57, sampleRate = sampleRate, seconds = 1.0f)
        assertTrue(buffer.isNotEmpty())
        assertTrue(buffer.none { it.isNaN() || it.isInfinite() }, "La síntesis produjo NaN/Inf")
        val peak = buffer.maxOf { abs(it) }
        assertTrue(peak > 0.01f, "Buffer silente: pico $peak")
        assertTrue(peak <= 1.0f, "Buffer fuera de rango: pico $peak")
    }

    @Test
    fun laCuerdaDecaeComoCuerdaPulsada() {
        val buffer = KarplusStrongVoice.renderPluck(midi = 57, sampleRate = sampleRate, seconds = 2.0f)
        val head = buffer.take(buffer.size / 10).sumOf { (it * it).toDouble() }
        val tail = buffer.takeLast(buffer.size / 10).sumOf { (it * it).toDouble() }
        assertTrue(tail < head * 0.5, "No decae: head=$head tail=$tail")
    }

    @Test
    fun elMixerMezclaAcordesSinRecortar() {
        val mixer = VoiceMixer()
        // Acorde de 7 notas (nivel 13) — el caso más denso de la app.
        listOf(55, 59, 62, 65, 69, 72, 76).forEach { mixer.pluck(it, sampleRate) }
        val buffer = FloatArray(sampleRate)
        mixer.renderInto(buffer, buffer.size)
        assertTrue(buffer.none { it.isNaN() || it.isInfinite() })
        assertTrue(buffer.all { abs(it) <= 1.0f })
        assertTrue(buffer.maxOf { abs(it) } > 0.01f)
    }

    @Test
    fun lasVocesInactivasSeLiberan() {
        val mixer = VoiceMixer()
        mixer.pluck(69, sampleRate)
        val buffer = FloatArray(sampleRate)
        repeat(30) { mixer.renderInto(buffer, buffer.size) } // 30 s: decaída total
        assertEquals(0, mixer.activeVoices)
    }

    @Test
    fun agendaDeEscalaYProgresion() {
        val scale = PlaybackSchedule.forScale(listOf(48, 50, 52), bpm = 120, sampleRate = sampleRate)
        assertEquals(3, scale.triggers.size)
        assertEquals(0L, scale.triggers[0].startSample)
        assertEquals(22050L, scale.triggers[1].startSample) // 120 bpm = medio segundo
        assertEquals(66150L, scale.totalSamples)

        val prog = PlaybackSchedule.forProgression(
            listOf(ChordEvent(listOf(48, 52, 55), beats = 4), ChordEvent(listOf(55, 59, 62), beats = 2)),
            bpm = 60, strumMs = 40, sampleRate = sampleRate,
        )
        // 6 cuerdas en total, segundo acorde arranca en el beat 4 (4 s a 60 bpm)
        assertEquals(6, prog.triggers.size)
        assertEquals(4L * sampleRate, prog.triggers[3].startSample)
        assertEquals(1, prog.triggers[3].eventIndex)
        assertEquals(6L * sampleRate, prog.totalSamples)
        // Rasgueo de 40 ms entre cuerdas del primer acorde
        assertEquals((sampleRate * 40) / 1000L, prog.triggers[1].startSample)
    }
}
