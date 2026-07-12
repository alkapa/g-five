package com.alkapa.circuloquintas.ui.circle

import androidx.lifecycle.SavedStateHandle
import com.alkapa.circuloquintas.AppContainer
import com.alkapa.circuloquintas.FakeAudioEngine
import com.alkapa.circuloquintas.FakePreferencesRepository
import com.alkapa.circuloquintas.FakeProgressionRepository
import com.alkapa.circuloquintas.domain.ChordLevel
import com.alkapa.circuloquintas.domain.ChordQuality
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CircleViewModelTest {

    private lateinit var audio: FakeAudioEngine
    private lateinit var prefs: FakePreferencesRepository
    private lateinit var container: AppContainer

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        audio = FakeAudioEngine()
        prefs = FakePreferencesRepository()
        container = AppContainer.forTest(audio, FakeProgressionRepository(), prefs)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()) =
        CircleViewModel(handle, container)

    @Test
    fun estadoInicialEsCMayorTriadasConCapaFunciones() = runTest {
        val vm = viewModel()
        val s = vm.state.value
        assertTrue(s.restored)
        assertEquals(Note('C', 0), s.tonic)
        assertEquals(ScaleType.MAJOR, s.scale)
        assertEquals(ChordLevel.TRIADS, s.level)
    }

    @Test
    fun tapEnSectorNoDiatonicoCambiaTonalidad() = runTest {
        val vm = viewModel()
        // Posición 3 = A; A mayor no es diatónico en C mayor → nueva tónica.
        vm.onSectorTap(position = 3, outerRing = true)
        assertEquals(Note('A', 0), vm.state.value.tonic)
        assertEquals(ScaleType.MAJOR, vm.state.value.scale)
        // Anillo interior → menor natural.
        vm.onSectorTap(position = 11, outerRing = false) // interior de F = Dm
        // Dm no era diatónico en A mayor → cambia a D menor natural
        assertEquals(Note('D', 0), vm.state.value.tonic)
        assertEquals(ScaleType.NATURAL_MINOR, vm.state.value.scale)
    }

    @Test
    fun tapEnCeldaDiatonicaSuenaElAcorde() = runTest {
        val vm = viewModel()
        // Celda interior de la posición 0 (C) = Am = vi de C mayor → suena.
        vm.onSectorTap(position = 0, outerRing = false)
        assertEquals(listOf(listOf(57, 60, 64)), audio.playedChords)
        assertEquals(Note('C', 0), vm.state.value.tonic) // no cambió la tonalidad
        assertEquals(6, vm.state.value.highlightedDegreeIndex)
    }

    @Test
    fun hacerTonicaDesdeGradoMenorVaAMenorNatural() = runTest {
        val vm = viewModel()
        val vi = vm.state.value.key.diatonicField(ChordLevel.TRIADS)[5]
        vm.makeTonic(vi)
        assertEquals(Note('A', 0), vm.state.value.tonic)
        assertEquals(ScaleType.NATURAL_MINOR, vm.state.value.scale)
    }

    @Test
    fun toggleEnarmonicoAlternaFSostenidoYGb() = runTest {
        val vm = viewModel()
        vm.onSectorTap(position = 6, outerRing = true) // F#/Gb no diatónico en C
        assertEquals(Note('F', 1), vm.state.value.tonic)
        vm.toggleEnharmonic()
        assertEquals(Note('G', -1), vm.state.value.tonic)
        vm.toggleEnharmonic()
        assertEquals(Note('F', 1), vm.state.value.tonic)
    }

    @Test
    fun agregarAProgresionPueblaElBorradorPorGrado() = runTest {
        val vm = viewModel()
        val field = vm.state.value.key.diatonicField(ChordLevel.TRIADS)
        vm.addToProgression(field[0])
        vm.addToProgression(field[4])
        val draft = container.draftHolder.draft.value
        assertEquals(2, draft.chords.size)
        assertEquals(1, draft.chords[0].degreeIndex)
        assertEquals(ChordQuality.MAJOR, draft.chords[0].quality)
        assertEquals(5, draft.chords[1].degreeIndex)
        assertEquals(vm.state.value.key, draft.key)
    }

    @Test
    fun laEscalaYNivelPersistenEnPreferencias() = runTest {
        val vm = viewModel()
        vm.setScale(ScaleType.DORIAN)
        vm.setLevel(ChordLevel.SEVENTHS)
        assertEquals(ScaleType.DORIAN, prefs.current.lastScale)
        assertEquals(ChordLevel.SEVENTHS, prefs.current.lastChordLevel)
    }

    @Test
    fun laMuerteDeProcesoRestauraDesdeSavedState() = runTest {
        val handle = SavedStateHandle()
        val vm = viewModel(handle)
        vm.setScale(ScaleType.MIXOLYDIAN)
        vm.onSectorTap(position = 1, outerRing = true) // G ya es diatónico… usa D
        // El estado quedó respaldado en el handle: un VM nuevo con el mismo
        // handle debe restaurar la misma escala.
        val restored = viewModel(handle)
        assertEquals(vm.state.value.scale, restored.state.value.scale)
        assertEquals(vm.state.value.tonic, restored.state.value.tonic)
    }
}
