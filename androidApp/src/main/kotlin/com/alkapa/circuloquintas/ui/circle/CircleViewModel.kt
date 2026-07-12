package com.alkapa.circuloquintas.ui.circle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alkapa.circuloquintas.AppContainer
import com.alkapa.circuloquintas.domain.ChordLevel
import com.alkapa.circuloquintas.domain.Chord
import com.alkapa.circuloquintas.domain.CircleOfFifths
import com.alkapa.circuloquintas.domain.Degree
import com.alkapa.circuloquintas.domain.Key
import com.alkapa.circuloquintas.domain.MidiMapper
import com.alkapa.circuloquintas.domain.Need
import com.alkapa.circuloquintas.domain.Notation
import com.alkapa.circuloquintas.domain.Note
import com.alkapa.circuloquintas.domain.ScaleCategory
import com.alkapa.circuloquintas.domain.ScaleType
import com.alkapa.circuloquintas.domain.repo.CircleLayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado inmutable de la pantalla Círculo (§6.2). El campo armónico y las
 * posiciones se derivan de [tonic]+[scale]+[level] en la capa de UI.
 */
data class CircleUiState(
    val tonic: Note = Note('C', 0),
    val scale: ScaleType = ScaleType.MAJOR,
    val level: ChordLevel = ChordLevel.TRIADS,
    val layers: Set<CircleLayer> = setOf(CircleLayer.FUNCTIONS),
    val notation: Notation = Notation.AMERICAN,
    val preferFlatEnharmonic: Boolean = false,
    val selectedNeed: Need? = null,
    val fichaDegree: Degree? = null,
    /** Acorde abierto en la hoja "En el instrumento" (diapasón/tab/piano). */
    val instrumentChord: Chord? = null,
    val highlightedDegreeIndex: Int? = null,
    val playingRootPc: Int? = null,
    val restored: Boolean = false,
) {
    val key: Key get() = Key(tonic, scale)
}

class CircleViewModel(
    private val savedState: SavedStateHandle,
    private val container: AppContainer,
) : ViewModel() {

    private val _state = MutableStateFlow(CircleUiState())
    val state: StateFlow<CircleUiState> = _state

    private val audio get() = container.audioEngine

    init {
        viewModelScope.launch {
            restoreInitialState()
            // Indicador del acorde sonando (desde el tab Progresiones, §6.4).
            container.playbackHighlight.collect { rootPc ->
                _state.update { it.copy(playingRootPc = rootPc) }
            }
        }
    }

    private suspend fun restoreInitialState() {
        val prefs = container.preferencesRepository.preferences.first()
        // SavedStateHandle (muerte de proceso) tiene prioridad sobre la última
        // sesión persistida en DataStore (§6.1).
        val tonicLetter = savedState.get<String>(KEY_TONIC_LETTER)
        val base = if (tonicLetter != null) {
            CircleUiState(
                tonic = Note(tonicLetter.first(), savedState.get<Int>(KEY_TONIC_ACC) ?: 0),
                scale = enumOr(savedState.get<String>(KEY_SCALE), ScaleType.MAJOR),
                level = enumOr(savedState.get<String>(KEY_LEVEL), ChordLevel.TRIADS),
                layers = decodeLayers(savedState.get<String>(KEY_LAYERS)),
            )
        } else {
            CircleUiState(
                tonic = prefs.lastTonic,
                scale = prefs.lastScale,
                level = prefs.lastChordLevel,
                layers = prefs.activeLayers,
            )
        }
        _state.update {
            base.copy(
                notation = prefs.notation,
                preferFlatEnharmonic = prefs.preferFlatEnharmonic,
                restored = true,
                playingRootPc = it.playingRootPc,
            )
        }
        // Refleja cambios de preferencias hechos desde Ajustes.
        viewModelScope.launch {
            container.preferencesRepository.preferences.collect { p ->
                _state.update {
                    it.copy(notation = p.notation, preferFlatEnharmonic = p.preferFlatEnharmonic)
                }
            }
        }
    }

    // ------------------------------------------------------------ interacción

    /**
     * Toque en el círculo (§6.3): sector diatónico → suena el acorde del
     * grado; sector no diatónico → nueva tónica (exterior = Mayor,
     * interior = Menor natural).
     */
    fun onSectorTap(position: Int, outerRing: Boolean) {
        val s = _state.value
        val degree = degreeAtCell(s, position, outerRing)
        if (degree != null) {
            playDegree(degree)
        } else {
            val sector = CircleOfFifths.sectors[position]
            if (outerRing) {
                val tonic = if (s.preferFlatEnharmonic) sector.majorAlt ?: sector.major else sector.major
                applyChange { it.copy(tonic = tonic, scale = ScaleType.MAJOR, selectedNeed = null) }
            } else {
                val tonic = if (s.preferFlatEnharmonic) sector.minorAlt ?: sector.minor else sector.minor
                applyChange { it.copy(tonic = tonic, scale = ScaleType.NATURAL_MINOR, selectedNeed = null) }
            }
        }
    }

    /** Toque largo en sector diatónico → ficha de detalle (§6.3). */
    fun onSectorLongPress(position: Int, outerRing: Boolean) {
        degreeAtCell(_state.value, position, outerRing)?.let { openFicha(it) }
    }

    fun setScale(scale: ScaleType) = applyChange { it.copy(scale = scale, selectedNeed = null) }

    fun setLevel(level: ChordLevel) = applyChange { it.copy(level = level) }

    fun toggleLayer(layer: CircleLayer) = applyChange {
        it.copy(layers = if (layer in it.layers) it.layers - layer else it.layers + layer)
    }

    fun setNeed(need: Need?) = _state.update { it.copy(selectedNeed = need) }

    fun openFicha(degree: Degree) = _state.update { it.copy(fichaDegree = degree) }

    fun closeFicha() = _state.update { it.copy(fichaDegree = null) }

    /** Hoja "En el instrumento": inversiones sobre diapasón, tab y piano. */
    fun openInstrument(chord: Chord) =
        _state.update { it.copy(instrumentChord = chord, fichaDegree = null) }

    fun closeInstrument() = _state.update { it.copy(instrumentChord = null) }

    /** Reproduce midis absolutos (digitaciones de guitarra / colocaciones de piano). */
    fun playMidis(midis: List<Int>) {
        audio.playChord(midis)
    }

    /** Alterna F#↔Gb / D#m↔Ebm en la posición enarmónica del círculo. */
    fun toggleEnharmonic() {
        val alt = CircleOfFifths.enharmonicAlternative(_state.value.tonic) ?: return
        applyChange { it.copy(tonic = alt) }
    }

    /**
     * "Hacer tónica" desde la ficha de un grado (default de diseño: evita el
     * conflicto de gestos con tocar-el-acorde). La familia del acorde decide
     * la escala destino: menor → menor natural, mayor → mayor; el grado 1
     * conserva la escala activa.
     */
    fun makeTonic(degree: Degree) {
        val root = degree.chord.root
        applyChange {
            val scale = when {
                degree.index == 1 -> it.scale
                degree.chord.quality.upperRoman -> ScaleType.MAJOR
                else -> ScaleType.NATURAL_MINOR
            }
            it.copy(tonic = root, scale = scale, fichaDegree = null)
        }
    }

    // -------------------------------------------------------------- playback

    fun playDegree(degree: Degree) {
        playChord(degree.chord)
        _state.update { it.copy(highlightedDegreeIndex = degree.index) }
    }

    fun playChord(chord: Chord) {
        audio.playChord(MidiMapper.chordMidis(fieldKey(_state.value), chord))
    }

    /**
     * Toque en la barra de escala: suena la nota y se resalta su acorde
     * (§6.2.5). La barra dibuja las 7 notas de la escala madre en
     * pentatónicas, así que se indexa esa misma lista.
     */
    fun playScaleNote(noteIndex: Int) {
        val s = _state.value
        val notes = (s.key.pentatonicOverlay()?.parent ?: s.key).notes()
        if (noteIndex !in notes.indices) return
        audio.playNote(MidiMapper.noteMidi(s.key, notes[noteIndex]))
        val degree = fieldKey(s).diatonicField(s.level)
            .firstOrNull { it.chord.root.pitchClass == notes[noteIndex].pitchClass }
        _state.update { it.copy(highlightedDegreeIndex = degree?.index) }
    }

    fun playScale(ascending: Boolean) {
        val midis = MidiMapper.scaleMidis(_state.value.key)
        audio.playScale(if (ascending) midis else midis.reversed(), bpm = 120)
    }

    // ------------------------------------------------------------ progresión

    /** "+" en ficha o buscador: agrega el acorde (por grado) al borrador (§6.2). */
    fun addToProgression(degree: Degree, chord: Chord = degree.chord) {
        container.draftHolder.addChord(fieldKey(_state.value), degree.index, chord.quality)
    }

    // -------------------------------------------------------------- helpers

    /** Tonalidad cuyo campo se usa: la madre para pentatónicas (§3.2). */
    private fun fieldKey(s: CircleUiState): Key =
        if (s.scale.category == ScaleCategory.PENTATONIC) s.key.parentKey()!! else s.key

    /**
     * Grado que ocupa la celda (posición, anillo): familia mayor al anillo
     * exterior, familia menor al interior — misma disposición que dibuja el
     * canvas (en C mayor: F-C-G afuera; Dm-Am-Em-B° adentro).
     */
    private fun degreeAtCell(s: CircleUiState, position: Int, outerRing: Boolean): Degree? {
        val sector = CircleOfFifths.sectors[position]
        val cellPc = if (outerRing) sector.major.pitchClass else sector.minor.pitchClass
        return fieldKey(s).diatonicField(s.level)
            .firstOrNull {
                it.chord.root.pitchClass == cellPc && it.chord.quality.upperRoman == outerRing
            }
            // En pentatónicas, solo los grados presentes en el overlay suenan desde el círculo.
            ?.takeIf { degree ->
                val overlay = s.key.pentatonicOverlay() ?: return@takeIf true
                degree.index in overlay.includedDegrees
            }
    }

    private fun applyChange(transform: (CircleUiState) -> CircleUiState) {
        _state.update { transform(it).copy(highlightedDegreeIndex = null) }
        persist()
    }

    private fun persist() {
        val s = _state.value
        savedState[KEY_TONIC_LETTER] = s.tonic.letter.toString()
        savedState[KEY_TONIC_ACC] = s.tonic.accidental
        savedState[KEY_SCALE] = s.scale.name
        savedState[KEY_LEVEL] = s.level.name
        savedState[KEY_LAYERS] = s.layers.joinToString(",") { it.name }
        viewModelScope.launch {
            container.preferencesRepository.update {
                it.copy(
                    lastTonic = s.tonic,
                    lastScale = s.scale,
                    lastChordLevel = s.level,
                    activeLayers = s.layers,
                )
            }
        }
    }

    private inline fun <reified T : Enum<T>> enumOr(name: String?, fallback: T): T =
        name?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: fallback

    private fun decodeLayers(encoded: String?): Set<CircleLayer> =
        encoded?.split(',')?.filter { it.isNotBlank() }
            ?.mapNotNull { runCatching { CircleLayer.valueOf(it) }.getOrNull() }?.toSet()
            ?: setOf(CircleLayer.FUNCTIONS)

    private companion object {
        const val KEY_TONIC_LETTER = "tonicLetter"
        const val KEY_TONIC_ACC = "tonicAccidental"
        const val KEY_SCALE = "scale"
        const val KEY_LEVEL = "level"
        const val KEY_LAYERS = "layers"
    }
}
