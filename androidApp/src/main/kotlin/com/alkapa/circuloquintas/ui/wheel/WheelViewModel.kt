package com.alkapa.circuloquintas.ui.wheel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alkapa.circuloquintas.AppContainer
import com.alkapa.circuloquintas.domain.Degree
import com.alkapa.circuloquintas.domain.MidiMapper
import com.alkapa.circuloquintas.domain.Notation
import com.alkapa.circuloquintas.domain.ScaleType
import com.alkapa.circuloquintas.domain.audio.ChordEvent
import com.alkapa.circuloquintas.domain.wheel.WheelModel
import com.alkapa.circuloquintas.domain.wheel.WheelVoicings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de la pantalla única del refactor «2a — Claridad+»: los dos tabs
 * comparten tonalidad, escala, Tríadas/Séptimas y la progresión proyectada
 * (exactamente como el estado `d` del diseño).
 */
data class WheelUiState(
    val tab: Int = 0,                // 0 = Círculo, 1 = Progresiones
    val rootPc: Int = 0,
    val scale: ScaleType = ScaleType.MAJOR,
    val seventh: Boolean = false,
    val sel: Int = 0,                // grado seleccionado (0..6)
    val view: Int = 0,               // 0 Guitarra, 1 Piano, 2 Diapasón
    val inv: Int = 0,
    val setIx: Int = -1,             // -1 = forma estándar (solo en fundamental)
    val prog: Int = -1,              // progresión proyectada sobre la rueda
    val openProg: Int = 0,           // card expandida del tab Progresiones
    val sheet: Boolean = false,      // hoja Tonalidad y escala
    val settings: Boolean = false,
    val fichaDegree: Degree? = null, // ficha pedagógica (pulsación larga en el rail)
    val notation: Notation = Notation.AMERICAN,
    val palette: Int = 0,
    val showDegrees: Boolean = true,
    val restored: Boolean = false,
)

class WheelViewModel(
    private val savedState: SavedStateHandle,
    private val container: AppContainer,
) : ViewModel() {

    private val _state = MutableStateFlow(WheelUiState())
    val state: StateFlow<WheelUiState> = _state

    private val audio get() = container.audioEngine

    init {
        viewModelScope.launch {
            val prefs = container.preferencesRepository.preferences.first()
            val base = if (savedState.contains(KEY_ROOT)) {
                WheelUiState(
                    tab = savedState.get<Int>(KEY_TAB) ?: 0,
                    rootPc = savedState.get<Int>(KEY_ROOT) ?: 0,
                    scale = enumOr(savedState.get<String>(KEY_SCALE), ScaleType.MAJOR),
                    seventh = savedState.get<Boolean>(KEY_SEVENTH) ?: false,
                    sel = savedState.get<Int>(KEY_SEL) ?: 0,
                )
            } else {
                WheelUiState(rootPc = prefs.lastRootPc, scale = prefs.lastScale, seventh = prefs.lastSeventh)
            }
            _state.update {
                base.copy(
                    notation = prefs.notation,
                    palette = prefs.palette,
                    showDegrees = prefs.showDegrees,
                    restored = true,
                )
            }
            container.preferencesRepository.preferences.collect { p ->
                _state.update {
                    it.copy(notation = p.notation, palette = p.palette, showDegrees = p.showDegrees)
                }
            }
        }
    }

    // ------------------------------------------------------------- selección

    fun selectChord(index: Int, play: Boolean = true) {
        _state.update { it.copy(sel = index.coerceIn(0, 6), prog = -1, inv = 0, setIx = -1) }
        backup()
        if (play) playSelected()
    }

    fun setSeventh(seventh: Boolean) {
        _state.update { it.copy(seventh = seventh, inv = 0, setIx = -1) }
        persistSession()
    }

    fun pickRoot(pc: Int) {
        _state.update { it.copy(rootPc = pc.mod(12), sel = 0, prog = -1, inv = 0, setIx = -1) }
        persistSession()
    }

    fun pickScale(scale: ScaleType) {
        _state.update { it.copy(scale = scale, sel = 0, prog = -1, openProg = 0, inv = 0, setIx = -1) }
        persistSession()
    }

    /** Chip de inversión: al salir de la fundamental se preselecciona el primer grupo. */
    fun setInversion(k: Int) {
        _state.update { it.copy(inv = k, setIx = if (k == 0) -1 else 0) }
        playInversion()
    }

    fun setVoicingSet(index: Int) {
        _state.update { it.copy(setIx = index) }
    }

    fun setView(view: Int) = _state.update { it.copy(view = view) }

    fun goTab(tab: Int) {
        _state.update { it.copy(tab = tab) }
        backup()
    }

    fun openSheet(open: Boolean) = _state.update { it.copy(sheet = open) }

    fun openSettings(open: Boolean) = _state.update { it.copy(settings = open) }

    fun openFicha(degree: Degree?) = _state.update { it.copy(fichaDegree = degree) }

    // ---------------------------------------------------------- progresiones

    fun toggleProgCard(index: Int) =
        _state.update { it.copy(openProg = if (it.openProg == index) -1 else index) }

    /** «Ver en el círculo →»: proyecta la progresión sobre la rueda. */
    fun projectProgression(index: Int) {
        val s = _state.value
        val prog = WheelModel.progressionsFor(s.scale).getOrNull(index) ?: return
        _state.update {
            it.copy(tab = 0, prog = index, sel = prog.degrees.first(), inv = 0, setIx = -1)
        }
        backup()
    }

    fun clearProgression() = _state.update { it.copy(prog = -1) }

    fun playProgression(index: Int) {
        val s = _state.value
        val field = WheelModel.field(s.rootPc, s.scale, s.seventh)
        val prog = WheelModel.progressionsFor(s.scale).getOrNull(index) ?: return
        val events = prog.degrees.map { d ->
            ChordEvent(MidiMapper.chordMidis(field.key, field.active[d].chord), beats = 4)
        }
        audio.playProgression(events, bpm = 90, loop = false)
    }

    // -------------------------------------------------------------- playback

    fun playSelected() {
        val s = _state.value
        val field = WheelModel.field(s.rootPc, s.scale, s.seventh)
        audio.playChord(MidiMapper.chordMidis(field.key, field.active[s.sel].chord))
    }

    /** Reproduce la inversión activa con su apilado cerrado real. */
    fun playInversion() {
        val s = _state.value
        val field = WheelModel.field(s.rootPc, s.scale, s.seventh)
        val data = WheelModel.inversion(field.active[s.sel].chord, s.inv)
        audio.playChord(data.abs.map { 48 + it })
    }

    /** Reproduce un voicing de guitarra tal cual suena (midis absolutos). */
    fun playVoicing(voicing: WheelVoicings.GroupVoicing) {
        val midis = voicing.strings.mapIndexed { i, string ->
            GUITAR_MIDI[string] + voicing.frets[i]
        }
        audio.playChord(midis)
    }

    fun playShape(shape: WheelVoicings.Shape) {
        val midis = shape.frets.mapIndexedNotNull { string, fret ->
            if (fret < 0) null else GUITAR_MIDI[string] + fret
        }
        audio.playChord(midis)
    }

    // -------------------------------------------------------------- ajustes

    fun setNotation(notation: Notation) = updatePrefs { it.copy(notation = notation) }

    fun setPalette(palette: Int) = updatePrefs { it.copy(palette = palette.coerceIn(0, 2)) }

    fun setShowDegrees(show: Boolean) = updatePrefs { it.copy(showDegrees = show) }

    private fun updatePrefs(transform: (com.alkapa.circuloquintas.domain.repo.UserPreferences) -> com.alkapa.circuloquintas.domain.repo.UserPreferences) {
        viewModelScope.launch { container.preferencesRepository.update(transform) }
    }

    // ---------------------------------------------------------- persistencia

    private fun persistSession() {
        backup()
        val s = _state.value
        viewModelScope.launch {
            container.preferencesRepository.update {
                it.copy(lastRootPc = s.rootPc, lastScale = s.scale, lastSeventh = s.seventh)
            }
        }
    }

    private fun backup() {
        val s = _state.value
        savedState[KEY_TAB] = s.tab
        savedState[KEY_ROOT] = s.rootPc
        savedState[KEY_SCALE] = s.scale.name
        savedState[KEY_SEVENTH] = s.seventh
        savedState[KEY_SEL] = s.sel
    }

    private inline fun <reified T : Enum<T>> enumOr(name: String?, fallback: T): T =
        name?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: fallback

    private companion object {
        const val KEY_TAB = "tab"
        const val KEY_ROOT = "rootPc"
        const val KEY_SCALE = "scale"
        const val KEY_SEVENTH = "seventh"
        const val KEY_SEL = "sel"

        /** Afinación estándar en midi, 6ª→1ª (E2 A2 D3 G3 B3 E4). */
        val GUITAR_MIDI = listOf(40, 45, 50, 55, 59, 64)
    }
}
