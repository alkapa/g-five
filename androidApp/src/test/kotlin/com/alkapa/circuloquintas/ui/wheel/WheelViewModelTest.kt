package com.alkapa.circuloquintas.ui.wheel

import androidx.lifecycle.SavedStateHandle
import com.alkapa.circuloquintas.AppContainer
import com.alkapa.circuloquintas.FakeAudioEngine
import com.alkapa.circuloquintas.FakePreferencesRepository
import com.alkapa.circuloquintas.FakeProgressionRepository
import com.alkapa.circuloquintas.domain.Notation
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
class WheelViewModelTest {

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
        WheelViewModel(handle, container)

    @Test
    fun estadoInicialCMayorTriadas() = runTest {
        val vm = viewModel()
        val s = vm.state.value
        assertTrue(s.restored)
        assertEquals(0, s.rootPc)
        assertEquals(ScaleType.MAJOR, s.scale)
        assertEquals(false, s.seventh)
        assertEquals(0, s.sel)
    }

    @Test
    fun tocarUnAcordeLoSeleccionaYReproduce() = runTest {
        val vm = viewModel()
        vm.selectChord(4) // V = G
        assertEquals(4, vm.state.value.sel)
        assertEquals(0, vm.state.value.inv)
        // Suena G en fundamental cerrada sobre la octava de C (48): G=55.
        assertEquals(listOf(55, 59, 62), audio.playedChords.last())
    }

    @Test
    fun conmutadorDeSeptimasReiniciaInversion() = runTest {
        val vm = viewModel()
        vm.setInversion(1)
        assertEquals(1, vm.state.value.inv)
        assertEquals(0, vm.state.value.setIx)
        vm.setSeventh(true)
        assertEquals(0, vm.state.value.inv)
        assertEquals(-1, vm.state.value.setIx)
        assertTrue(prefs.current.lastSeventh)
    }

    @Test
    fun laInversionSuenaConSuApiladoReal() = runTest {
        val vm = viewModel()
        vm.selectChord(0, play = false)
        vm.setInversion(1) // C/E: E-G-C → 52, 55, 60
        assertEquals(listOf(52, 55, 60), audio.playedChords.last())
    }

    @Test
    fun proyectarProgresionSobreLaRueda() = runTest {
        val vm = viewModel()
        vm.goTab(1)
        vm.projectProgression(0) // Pop clásico: I–V–vi–IV
        val s = vm.state.value
        assertEquals(0, s.tab)
        assertEquals(0, s.prog)
        assertEquals(0, s.sel)
        // Tocar un acorde la desactiva (como en el diseño).
        vm.selectChord(2, play = false)
        assertEquals(-1, vm.state.value.prog)
    }

    @Test
    fun reproducirProgresionGeneraEventos() = runTest {
        val vm = viewModel()
        vm.playProgression(0)
        val (events, bpm, loop) = audio.playedProgressions.last()
        assertEquals(4, events.size) // I–V–vi–IV
        assertEquals(90, bpm)
        assertEquals(false, loop)
        assertEquals(listOf(48, 52, 55), events[0].midis)
    }

    @Test
    fun cambiarTonalidadYEscalaPersiste() = runTest {
        val vm = viewModel()
        vm.pickRoot(7)
        vm.pickScale(ScaleType.DORIAN)
        assertEquals(7, prefs.current.lastRootPc)
        assertEquals(ScaleType.DORIAN, prefs.current.lastScale)
    }

    @Test
    fun ajustesSePropaganAlEstado() = runTest {
        val vm = viewModel()
        vm.setNotation(Notation.LATIN)
        vm.setPalette(2)
        vm.setShowDegrees(false)
        val s = vm.state.value
        assertEquals(Notation.LATIN, s.notation)
        assertEquals(2, s.palette)
        assertEquals(false, s.showDegrees)
    }

    @Test
    fun muerteDeProcesoRestauraDesdeSavedState() = runTest {
        val handle = SavedStateHandle()
        val vm = viewModel(handle)
        vm.pickRoot(9)
        vm.pickScale(ScaleType.NATURAL_MINOR)
        vm.setSeventh(true)
        vm.goTab(1)

        val restored = WheelViewModel(
            handle,
            AppContainer.forTest(FakeAudioEngine(), FakeProgressionRepository(), FakePreferencesRepository()),
        )
        val s = restored.state.value
        assertEquals(9, s.rootPc)
        assertEquals(ScaleType.NATURAL_MINOR, s.scale)
        assertTrue(s.seventh)
        assertEquals(1, s.tab)
    }
}
