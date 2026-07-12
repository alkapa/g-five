package com.alkapa.circuloquintas.ui.progressions

import androidx.lifecycle.SavedStateHandle
import com.alkapa.circuloquintas.AppContainer
import com.alkapa.circuloquintas.FakeAudioEngine
import com.alkapa.circuloquintas.FakePreferencesRepository
import com.alkapa.circuloquintas.FakeProgressionRepository
import com.alkapa.circuloquintas.domain.ChordQuality
import com.alkapa.circuloquintas.domain.Key
import com.alkapa.circuloquintas.domain.Note
import com.alkapa.circuloquintas.domain.ScaleType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProgressionsViewModelTest {

    private lateinit var audio: FakeAudioEngine
    private lateinit var repo: FakeProgressionRepository
    private lateinit var container: AppContainer

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        audio = FakeAudioEngine()
        repo = FakeProgressionRepository()
        container = AppContainer.forTest(audio, repo, FakePreferencesRepository())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()) =
        ProgressionsViewModel(handle, container)

    private val cMajor = Key(Note('C', 0), ScaleType.MAJOR)

    private fun draftPop(vm: ProgressionsViewModel) {
        container.draftHolder.addChord(cMajor, 1, ChordQuality.MAJOR)
        container.draftHolder.addChord(cMajor, 5, ChordQuality.MAJOR)
        container.draftHolder.addChord(cMajor, 6, ChordQuality.MINOR)
        container.draftHolder.addChord(cMajor, 4, ChordQuality.MAJOR)
    }

    @Test
    fun siembraLasNueveFamosasSoloLectura() = runTest {
        val vm = viewModel()
        assertEquals(9, vm.state.value.famous.size)
        assertTrue(vm.state.value.famous.all { it.readOnly })
        assertTrue(vm.state.value.saved.isEmpty())
    }

    @Test
    fun edicionDelBorrador() = runTest {
        val vm = viewModel()
        draftPop(vm)
        assertEquals(4, vm.state.value.draft.chords.size)

        vm.updateChord(1, ChordQuality.DOM7, beats = 2)
        assertEquals(ChordQuality.DOM7, vm.state.value.draft.chords[1].quality)
        assertEquals(2, vm.state.value.draft.chords[1].beats)

        vm.moveChord(0, 3)
        assertEquals(listOf(5, 6, 4, 1), vm.state.value.draft.chords.map { it.degreeIndex })

        vm.removeChord(0)
        assertEquals(listOf(6, 4, 1), vm.state.value.draft.chords.map { it.degreeIndex })
    }

    @Test
    fun guardarYReabrir() = runTest {
        val vm = viewModel()
        draftPop(vm)
        vm.save("Mi primera")
        val saved = vm.state.value.saved
        assertEquals(1, saved.size)
        assertEquals("Mi primera", saved[0].name)
        assertNotNull(vm.state.value.draft.id)

        vm.newDraft()
        assertTrue(vm.state.value.draft.chords.isEmpty())
        vm.open(saved[0])
        assertEquals(4, vm.state.value.draft.chords.size)
        assertEquals("Mi primera", vm.state.value.draft.name)
    }

    @Test
    fun transporteMantieneGrados() = runTest {
        val vm = viewModel()
        draftPop(vm)
        vm.transposeTo(Note('G', 0))
        val draft = vm.state.value.draft
        assertEquals(Note('G', 0), draft.key?.tonic)
        assertEquals(listOf(1, 5, 6, 4), draft.chords.map { it.degreeIndex })
    }

    @Test
    fun reproducirGeneraEventosPorAcorde() = runTest {
        val vm = viewModel()
        draftPop(vm)
        vm.setBpm(120)
        vm.togglePlay()
        assertEquals(1, audio.playedProgressions.size)
        val (events, bpm, loop) = audio.playedProgressions[0]
        assertEquals(4, events.size)
        assertEquals(120, bpm)
        assertEquals(false, loop)
        // I de C mayor en fundamental cerrada sobre C3 (48)
        assertEquals(listOf(48, 52, 55), events[0].midis)
        assertEquals(4, events[0].beats)
    }

    @Test
    fun elIndicadorDeAcordeSonandoSePropaga() = runTest {
        val vm = viewModel()
        draftPop(vm)
        vm.togglePlay()
        vm.onChordStarted(1) // V = G
        assertEquals(1, vm.state.value.playingIndex)
        assertEquals(7, container.playbackHighlight.value) // pc de G
        vm.onPlaybackEnded()
        assertEquals(null, container.playbackHighlight.value)
    }

    @Test
    fun famosaAbiertaEsSoloLecturaYDuplicarLaLibera() = runTest {
        val vm = viewModel()
        val famous = vm.state.value.famous.first { it.name == "Cadencia andaluza" }
        vm.open(famous)
        assertTrue(vm.state.value.draft.readOnly)

        vm.updateChord(0, ChordQuality.MAJOR, 8)
        assertEquals(ChordQuality.MINOR, vm.state.value.draft.chords[0].quality) // sin cambios

        vm.duplicate(famous)
        val draft = vm.state.value.draft
        assertEquals("Cadencia andaluza (copia)", draft.name)
        assertTrue(!draft.readOnly)
        assertEquals(10, vm.state.value.saved.size + vm.state.value.famous.size) // 9 + copia
    }

    @Test
    fun elBorradorSobreviveALaMuerteDeProceso() = runTest {
        val handle = SavedStateHandle()
        val vm = viewModel(handle)
        draftPop(vm)
        vm.setBpm(150)

        // Nuevo proceso: contenedor nuevo (holder vacío), mismo SavedStateHandle.
        val newContainer = AppContainer.forTest(
            FakeAudioEngine(), FakeProgressionRepository(), FakePreferencesRepository(),
        )
        val restored = ProgressionsViewModel(handle, newContainer)
        val draft = restored.state.value.draft
        assertEquals(4, draft.chords.size)
        assertEquals(150, draft.bpm)
        assertEquals(cMajor, draft.key)
    }
}
