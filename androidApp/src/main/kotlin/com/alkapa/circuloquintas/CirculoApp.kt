package com.alkapa.circuloquintas

import android.app.Application
import android.content.Context
import com.alkapa.circuloquintas.audio.AudioEngineFactory
import com.alkapa.circuloquintas.data.db.AppDatabase
import com.alkapa.circuloquintas.data.db.RoomProgressionRepository
import com.alkapa.circuloquintas.data.prefs.DataStorePreferencesRepository
import com.alkapa.circuloquintas.domain.ChordQuality
import com.alkapa.circuloquintas.domain.Key
import com.alkapa.circuloquintas.domain.ProgressionChord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class CirculoApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Localizador de servicios simple (sin frameworks de DI, por decisión de dependencias mínimas). */
class AppContainer private constructor(
    audioEngineProvider: () -> AudioEngine,
    progressionRepositoryProvider: () -> ProgressionRepository,
    preferencesRepositoryProvider: () -> PreferencesRepository,
) {
    constructor(context: Context) : this(
        audioEngineProvider = { AudioEngineFactory.create() },
        progressionRepositoryProvider = {
            RoomProgressionRepository(AppDatabase.build(context).progressionDao())
        },
        preferencesRepositoryProvider = { DataStorePreferencesRepository(context) },
    )

    companion object {
        /** Contenedor con dobles de prueba para tests de ViewModels. */
        fun forTest(
            audioEngine: AudioEngine,
            progressionRepository: ProgressionRepository,
            preferencesRepository: PreferencesRepository,
        ): AppContainer = AppContainer(
            { audioEngine }, { progressionRepository }, { preferencesRepository },
        )
    }

    val audioEngine: AudioEngine by lazy(audioEngineProvider)
    val progressionRepository: ProgressionRepository by lazy(progressionRepositoryProvider)
    val preferencesRepository: PreferencesRepository by lazy(preferencesRepositoryProvider)
    val draftHolder = ProgressionDraftHolder()

    /**
     * Clase de altura de la fundamental del acorde sonando en el playback de
     * progresiones (o null): el tab Círculo la observa para resaltar el
     * sector correspondiente (§6.4).
     */
    val playbackHighlight = MutableStateFlow<Int?>(null)
}

/**
 * Progresión en edición, compartida entre el tab Círculo (que agrega acordes)
 * y el tab Progresiones (que la edita y reproduce). Vive en el contenedor de
 * la app; ProgressionsViewModel la respalda en SavedStateHandle para
 * sobrevivir a la muerte de proceso.
 */
class ProgressionDraftHolder {

    data class Draft(
        val id: Long? = null,
        val name: String = "",
        val key: Key? = null,
        val bpm: Int = 90,
        val chords: List<ProgressionChord> = emptyList(),
        /** Una famosa abierta se ve y suena pero no se edita; duplicar la libera. */
        val readOnly: Boolean = false,
    )

    private val _draft = MutableStateFlow(Draft())
    val draft: StateFlow<Draft> = _draft

    /**
     * La tonalidad del borrador queda fijada por el primer acorde agregado.
     * Si había una famosa abierta (solo lectura), agregar un acorde inicia un
     * borrador nuevo propio.
     */
    fun addChord(key: Key, degreeIndex: Int, quality: ChordQuality) {
        _draft.update { d ->
            if (d.readOnly) {
                Draft(key = key, chords = listOf(ProgressionChord(degreeIndex, quality)))
            } else {
                d.copy(
                    key = d.key ?: key,
                    chords = d.chords + ProgressionChord(degreeIndex, quality),
                )
            }
        }
    }

    fun replace(draft: Draft) {
        _draft.value = draft
    }

    fun update(transform: (Draft) -> Draft) {
        _draft.update(transform)
    }

    fun clear() {
        _draft.value = Draft()
    }
}
