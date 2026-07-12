package com.alkapa.circuloquintas.audio

import com.alkapa.circuloquintas.BuildConfig
import com.alkapa.circuloquintas.domain.audio.AudioEngine

/**
 * Selección del motor de audio (§8): Oboe si el build lo incluyó
 * (-PenableOboe=true) y la librería nativa carga; si no, AudioTrack con la
 * misma síntesis en Kotlin. La app compila y suena por cualquiera de los dos
 * caminos.
 */
object AudioEngineFactory {

    fun create(): AudioEngine =
        if (BuildConfig.ENABLE_OBOE && OboeAudioEngine.isAvailable()) {
            OboeAudioEngine()
        } else {
            AudioTrackAudioEngine()
        }
}
