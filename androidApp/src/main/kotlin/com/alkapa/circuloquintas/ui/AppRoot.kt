@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.alkapa.circuloquintas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.alkapa.circuloquintas.AppContainer
import com.alkapa.circuloquintas.content.PedagogicalContent
import com.alkapa.circuloquintas.domain.Notation
import com.alkapa.circuloquintas.ui.circle.CircleScreen
import com.alkapa.circuloquintas.ui.circle.CircleViewModel
import com.alkapa.circuloquintas.ui.progressions.ProgressionsScreen
import com.alkapa.circuloquintas.ui.progressions.ProgressionsViewModel
import kotlinx.coroutines.launch

@Composable
fun AppRoot(container: AppContainer) {
    val circleViewModel: CircleViewModel = viewModel(factory = viewModelFactory {
        initializer { CircleViewModel(createSavedStateHandle(), container) }
    })
    val progressionsViewModel: ProgressionsViewModel = viewModel(factory = viewModelFactory {
        initializer { ProgressionsViewModel(createSavedStateHandle(), container) }
    })

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedTab == 0) "Círculo" else "Progresiones") },
                actions = {
                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier.semantics { contentDescription = "Ajustes" },
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = null)
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.DonutLarge, contentDescription = null) },
                    label = { Text("Círculo") },
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null) },
                    label = { Text("Progresiones") },
                )
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> CircleScreen(circleViewModel)
                else -> ProgressionsScreen(progressionsViewModel)
            }
        }
    }

    if (showSettings) {
        SettingsSheet(container, onDismiss = { showSettings = false })
    }
}

@Composable
private fun SettingsSheet(container: AppContainer, onDismiss: () -> Unit) {
    val prefs by container.preferencesRepository.preferences
        .collectAsState(initial = null)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var showHelp by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Ajustes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            val current = prefs ?: return@Column
            Text("Cifrado", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = current.notation == Notation.AMERICAN,
                    onClick = {
                        scope.launch {
                            container.preferencesRepository.update { it.copy(notation = Notation.AMERICAN) }
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) { Text("Americano (C, D, E)") }
                SegmentedButton(
                    selected = current.notation == Notation.LATIN,
                    onClick = {
                        scope.launch {
                            container.preferencesRepository.update { it.copy(notation = Notation.LATIN) }
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) { Text("Latino (Do, Re, Mi)") }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Grafía enarmónica con bemoles", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Usar Gb/Ebm por defecto en la posición enarmónica (en vez de F#/D#m).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = current.preferFlatEnharmonic,
                    onCheckedChange = { checked ->
                        scope.launch {
                            container.preferencesRepository.update { it.copy(preferFlatEnharmonic = checked) }
                        }
                    },
                    modifier = Modifier.semantics { contentDescription = "Preferir bemoles enarmónicos" },
                )
            }

            TextButton(onClick = { showHelp = true }) { Text("Cómo usar la app") }
        }
    }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Cómo usar") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "1. Elige una tónica tocando el círculo (anillo exterior = mayor, " +
                            "interior = menor) y una escala en el selector.\n" +
                            "2. Toca un acorde resaltado para escucharlo; mantén presionado para su ficha.\n" +
                            "3. Activa las capas Grados y Funciones para leer el círculo en tres niveles.\n" +
                            "4. Usa el buscador por sensación (Descanso, De paso, Tensión, Color) para " +
                            "elegir acordes por lo que quieres que suene.\n" +
                            "5. Agrega acordes con el botón + y arma tu progresión en el otro tab: " +
                            "reprodúcela, ajústale el tempo, guárdala o transpórtala.",
                    )
                    Text(
                        "El modelo de funciones",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(PedagogicalContent.functionModelIntro)
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) { Text("Entendido") }
            },
        )
    }
}
