package com.alkapa.circuloquintas.ui.progressions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alkapa.circuloquintas.AppContainer
import com.alkapa.circuloquintas.ProgressionDraftHolder
import com.alkapa.circuloquintas.content.FamousProgression
import com.alkapa.circuloquintas.content.FamousProgressions
import com.alkapa.circuloquintas.domain.ChordQuality
import com.alkapa.circuloquintas.domain.Key
import com.alkapa.circuloquintas.domain.MidiMapper
import com.alkapa.circuloquintas.domain.Notation
import com.alkapa.circuloquintas.domain.Note
import com.alkapa.circuloquintas.domain.Progression
import com.alkapa.circuloquintas.domain.ProgressionChord
import com.alkapa.circuloquintas.domain.ScaleCategory
import com.alkapa.circuloquintas.domain.ScaleType
import com.alkapa.circuloquintas.domain.audio.AudioEngine
import com.alkapa.circuloquintas.domain.audio.ChordEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProgressionsUiState(
    val draft: ProgressionDraftHolder.Draft = ProgressionDraftHolder.Draft(),
    val notation: Notation = Notation.AMERICAN,
    val isPlaying: Boolean = false,
    val loop: Boolean = false,
    val playingIndex: Int? = null,
    val saved: List<Progression> = emptyList(),
    val famous: List<Progression> = emptyList(),
    val editingIndex: Int? = null,
    val showSaveDialog: Boolean = false,
    val showTransposeDialog: Boolean = false,
    val fichaFamousName: String? = null,
    val renameTarget: Progression? = null,
)

class ProgressionsViewModel(
    private val savedState: SavedStateHandle,
    private val container: AppContainer,
) : ViewModel(), AudioEngine.PlaybackListener {

    private val _state = MutableStateFlow(ProgressionsUiState())
    val state: StateFlow<ProgressionsUiState> = _state

    private val audio get() = container.audioEngine
    private val repo get() = container.progressionRepository
    private val holder get() = container.draftHolder

    init {
        audio.setPlaybackListener(this)
        restoreDraftIfNeeded()
        viewModelScope.launch {
            holder.draft.collect { draft ->
                _state.update { it.copy(draft = draft) }
                backupDraft(draft)
            }
        }
        viewModelScope.launch {
            runCatching { repo.seedDefaults() }
            repo.observeAll().collect { all ->
                _state.update { s ->
                    s.copy(
                        saved = all.filter { !it.readOnly },
                        famous = all.filter { it.readOnly },
                    )
                }
            }
        }
        viewModelScope.launch {
            container.preferencesRepository.preferences.collect { p ->
                _state.update { it.copy(notation = p.notation) }
            }
        }
    }

    // ------------------------------------------------------------- edición

    fun moveChord(from: Int, to: Int) {
        if (readOnlyDraft()) return
        holder.update { d ->
            val chords = d.chords.toMutableList()
            if (from !in chords.indices) return@update d
            val item = chords.removeAt(from)
            chords.add(to.coerceIn(0, chords.size), item)
            d.copy(chords = chords)
        }
    }

    fun removeChord(index: Int) {
        if (readOnlyDraft()) return
        holder.update { d ->
            if (index !in d.chords.indices) d else d.copy(chords = d.chords.filterIndexed { i, _ -> i != index })
        }
        _state.update { it.copy(editingIndex = null) }
    }

    fun updateChord(index: Int, quality: ChordQuality, beats: Int) {
        if (readOnlyDraft()) return
        holder.update { d ->
            if (index !in d.chords.indices) return@update d
            val chords = d.chords.toMutableList()
            chords[index] = chords[index].copy(quality = quality, beats = beats.coerceIn(1, 8))
            d.copy(chords = chords)
        }
    }

    fun editChord(index: Int?) = _state.update { it.copy(editingIndex = index) }

    fun setBpm(bpm: Int) {
        holder.update { it.copy(bpm = bpm.coerceIn(40, 200)) }
    }

    fun toggleLoop() = _state.update { it.copy(loop = !it.loop) }

    fun newDraft() {
        audio.stop()
        holder.clear()
    }

    private fun readOnlyDraft(): Boolean = _state.value.draft.readOnly

    // ------------------------------------------------------------- playback

    fun togglePlay() {
        val s = _state.value
        if (s.isPlaying) {
            audio.stop()
            return
        }
        val draft = s.draft
        val key = draft.key ?: return
        if (draft.chords.isEmpty()) return
        val progression = Progression(
            id = draft.id, name = draft.name, key = key,
            bpm = draft.bpm, chords = draft.chords, readOnly = draft.readOnly,
        )
        val fieldKey = if (key.scale.category == ScaleCategory.PENTATONIC) key.parentKey()!! else key
        val rendered = progression.renderChords()
        val events = rendered.mapIndexed { i, chord ->
            ChordEvent(MidiMapper.chordMidis(fieldKey, chord), draft.chords[i].beats)
        }
        _state.update { it.copy(isPlaying = true, playingIndex = null) }
        audio.playProgression(events, draft.bpm, s.loop)
    }

    override fun onChordStarted(index: Int) {
        _state.update { it.copy(playingIndex = index, isPlaying = true) }
        val draft = _state.value.draft
        val key = draft.key
        if (key != null && index in draft.chords.indices) {
            val fieldKey = if (key.scale.category == ScaleCategory.PENTATONIC) key.parentKey()!! else key
            val rootPc = fieldKey.notes()[draft.chords[index].degreeIndex - 1].pitchClass
            container.playbackHighlight.value = rootPc
        }
    }

    override fun onPlaybackEnded() {
        _state.update { it.copy(isPlaying = false, playingIndex = null) }
        container.playbackHighlight.value = null
    }

    // ------------------------------------------------------------ biblioteca

    fun showSaveDialog(show: Boolean) = _state.update { it.copy(showSaveDialog = show) }

    fun save(name: String) {
        val draft = _state.value.draft
        val key = draft.key ?: return
        if (draft.readOnly || name.isBlank() || draft.chords.isEmpty()) return
        viewModelScope.launch {
            val id = repo.save(
                Progression(
                    id = draft.id, name = name.trim(), key = key,
                    bpm = draft.bpm, chords = draft.chords, readOnly = false,
                ),
            )
            holder.update { it.copy(id = id, name = name.trim()) }
            _state.update { it.copy(showSaveDialog = false) }
        }
    }

    fun open(progression: Progression) {
        audio.stop()
        holder.replace(
            ProgressionDraftHolder.Draft(
                id = progression.id,
                name = progression.name,
                key = progression.key,
                bpm = progression.bpm,
                chords = progression.chords,
                readOnly = progression.readOnly,
            ),
        )
    }

    fun duplicate(progression: Progression) {
        viewModelScope.launch {
            val id = progression.id ?: return@launch
            val newId = repo.duplicate(id, "${progression.name} (copia)")
            repo.get(newId)?.let { open(it) }
        }
    }

    fun showRenameDialog(target: Progression?) = _state.update { it.copy(renameTarget = target) }

    fun rename(progression: Progression, newName: String) {
        if (newName.isBlank() || progression.readOnly) return
        viewModelScope.launch {
            progression.id?.let { repo.rename(it, newName.trim()) }
            if (_state.value.draft.id == progression.id) {
                holder.update { it.copy(name = newName.trim()) }
            }
            _state.update { it.copy(renameTarget = null) }
        }
    }

    fun delete(progression: Progression) {
        viewModelScope.launch {
            progression.id?.let { repo.delete(it) }
            if (_state.value.draft.id == progression.id) holder.clear()
        }
    }

    fun showFamousFicha(name: String?) = _state.update { it.copy(fichaFamousName = name) }

    fun famousFicha(name: String): FamousProgression? =
        FamousProgressions.all.firstOrNull { it.name == name }

    // ------------------------------------------------------------ transporte

    fun showTransposeDialog(show: Boolean) = _state.update { it.copy(showTransposeDialog = show) }

    /** Transporte (§6.4): nueva tónica manteniendo grados. */
    fun transposeTo(newTonic: Note) {
        if (readOnlyDraft()) return
        holder.update { d ->
            d.copy(key = d.key?.let { Key(newTonic, it.scale) })
        }
        _state.update { it.copy(showTransposeDialog = false) }
    }

    // ------------------------------------------- respaldo ante muerte de proceso

    private fun restoreDraftIfNeeded() {
        val encoded = savedState.get<String>(KEY_DRAFT) ?: return
        if (holder.draft.value.chords.isNotEmpty() || holder.draft.value.key != null) return
        decodeDraft(encoded)?.let { holder.replace(it) }
    }

    private fun backupDraft(draft: ProgressionDraftHolder.Draft) {
        savedState[KEY_DRAFT] = encodeDraft(draft)
    }

    private fun encodeDraft(d: ProgressionDraftHolder.Draft): String {
        val key = d.key ?: return ""
        val chords = d.chords.joinToString(",") { "${it.degreeIndex}:${it.quality.name}:${it.beats}" }
        return listOf(
            key.tonic.letter.toString(), key.tonic.accidental.toString(), key.scale.name,
            d.bpm.toString(), d.id?.toString() ?: "", if (d.readOnly) "1" else "0", d.name, chords,
        ).joinToString("|")
    }

    private fun decodeDraft(encoded: String): ProgressionDraftHolder.Draft? {
        if (encoded.isBlank()) return null
        return runCatching {
            val parts = encoded.split("|")
            val chords = parts[7].split(",").filter { it.isNotBlank() }.map {
                val (d, q, b) = it.split(":")
                ProgressionChord(d.toInt(), ChordQuality.valueOf(q), b.toInt())
            }
            ProgressionDraftHolder.Draft(
                id = parts[4].toLongOrNull(),
                name = parts[6],
                key = Key(Note(parts[0].first(), parts[1].toInt()), ScaleType.valueOf(parts[2])),
                bpm = parts[3].toInt(),
                chords = chords,
                readOnly = parts[5] == "1",
            )
        }.getOrNull()
    }

    override fun onCleared() {
        audio.setPlaybackListener(null)
    }

    private companion object {
        const val KEY_DRAFT = "progressionDraft"
    }
}
