package com.alkapa.circuloquintas

import com.alkapa.circuloquintas.content.FamousProgressions
import com.alkapa.circuloquintas.domain.Progression
import com.alkapa.circuloquintas.domain.audio.AudioEngine
import com.alkapa.circuloquintas.domain.audio.ChordEvent
import com.alkapa.circuloquintas.domain.repo.PreferencesRepository
import com.alkapa.circuloquintas.domain.repo.ProgressionRepository
import com.alkapa.circuloquintas.domain.repo.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeAudioEngine : AudioEngine {
    val playedNotes = mutableListOf<Int>()
    val playedChords = mutableListOf<List<Int>>()
    val playedScales = mutableListOf<Pair<List<Int>, Int>>()
    val playedProgressions = mutableListOf<Triple<List<ChordEvent>, Int, Boolean>>()
    var stopped = 0
    var listener: AudioEngine.PlaybackListener? = null

    override fun playNote(midi: Int) { playedNotes += midi }
    override fun playChord(midis: List<Int>, strumMs: Int) { playedChords += midis }
    override fun playScale(midis: List<Int>, bpm: Int) { playedScales += midis to bpm }
    override fun playProgression(events: List<ChordEvent>, bpm: Int, loop: Boolean) {
        playedProgressions += Triple(events, bpm, loop)
    }
    override fun stop() { stopped++ }
    override fun setPlaybackListener(listener: AudioEngine.PlaybackListener?) {
        this.listener = listener
    }
}

class FakePreferencesRepository : PreferencesRepository {
    private val state = MutableStateFlow(UserPreferences())
    override val preferences: Flow<UserPreferences> = state
    val current: UserPreferences get() = state.value
    override suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        state.update(transform)
    }
}

class FakeProgressionRepository : ProgressionRepository {
    private val state = MutableStateFlow<List<Progression>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<Progression>> = state

    override suspend fun get(id: Long): Progression? = state.value.firstOrNull { it.id == id }

    override suspend fun save(progression: Progression): Long {
        val id = progression.id ?: nextId++
        state.update { list -> list.filter { it.id != id } + progression.copy(id = id) }
        return id
    }

    override suspend fun rename(id: Long, newName: String) {
        state.update { list -> list.map { if (it.id == id) it.copy(name = newName) else it } }
    }

    override suspend fun duplicate(id: Long, newName: String): Long {
        val original = get(id) ?: return -1
        return save(original.copy(id = null, name = newName, readOnly = false))
    }

    override suspend fun delete(id: Long) {
        state.update { list -> list.filter { it.id != id } }
    }

    override suspend fun seedDefaults() {
        if (state.value.isEmpty()) {
            for (famous in FamousProgressions.all) save(famous.progression)
        }
    }
}
