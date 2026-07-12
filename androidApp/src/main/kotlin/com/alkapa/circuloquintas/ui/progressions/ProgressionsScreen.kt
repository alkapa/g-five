@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package com.alkapa.circuloquintas.ui.progressions

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alkapa.circuloquintas.ProgressionDraftHolder
import com.alkapa.circuloquintas.domain.ChordLevel
import com.alkapa.circuloquintas.domain.ChordQuality
import com.alkapa.circuloquintas.domain.CircleOfFifths
import com.alkapa.circuloquintas.domain.DefaultNoteFormatter
import com.alkapa.circuloquintas.domain.Key
import com.alkapa.circuloquintas.domain.Progression
import com.alkapa.circuloquintas.domain.ScaleCategory
import com.alkapa.circuloquintas.domain.ScaleType
import com.alkapa.circuloquintas.domain.romanFor
import com.alkapa.circuloquintas.content.PedagogicalContent
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ProgressionsScreen(viewModel: ProgressionsViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DraftSection(state, viewModel)
        HorizontalDivider()
        LibrarySection(state, viewModel)
        Spacer(Modifier.height(24.dp))
    }

    Dialogs(state, viewModel)
}

// ------------------------------------------------------------------ borrador

@Composable
private fun DraftSection(state: ProgressionsUiState, viewModel: ProgressionsViewModel) {
    val draft = state.draft
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = draft.name.ifBlank { "Progresión sin guardar" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            draft.key?.let { key ->
                Text(
                    "${DefaultNoteFormatter.format(key.tonic, state.notation)} " +
                        PedagogicalContent.scaleName(key.scale) + " · ${draft.bpm} BPM",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        TextButton(onClick = viewModel::newDraft) { Text("Nueva") }
    }

    if (draft.readOnly) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Progresión famosa: solo lectura. Duplícala para editarla.",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

    if (draft.chords.isEmpty()) {
        Text(
            "Agrega acordes desde el círculo, el buscador por sensación o la ficha de un grado (botón +).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        ChordChipsRow(state, viewModel)
        PlaybackControls(state, viewModel)
    }
}

@Composable
private fun ChordChipsRow(state: ProgressionsUiState, viewModel: ProgressionsViewModel) {
    val draft = state.draft
    val key = draft.key ?: return
    val rendered = remember(draft.key, draft.chords) {
        Progression(null, "", key, draft.bpm, draft.chords).renderChords()
    }
    val slotWidthPx = with(LocalDensity.current) { 96.dp.toPx() }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(draft.chords, key = { i, c -> "$i-${c.degreeIndex}-${c.quality}" }) { index, chord ->
            var offsetX by remember { mutableFloatStateOf(0f) }
            var offsetY by remember { mutableFloatStateOf(0f) }
            val playing = state.playingIndex == index
            Card(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = offsetX
                        translationY = offsetY.coerceAtMost(0f)
                    }
                    .pointerInput(index, draft.readOnly) {
                        if (draft.readOnly) return@pointerInput
                        // Arrastre tras pulsación larga: horizontal = reordenar,
                        // vertical hacia arriba = eliminar (§6.4).
                        detectDragGesturesAfterLongPress(
                            onDrag = { change, amount ->
                                change.consume()
                                offsetX += amount.x
                                offsetY += amount.y
                            },
                            onDragEnd = {
                                when {
                                    offsetY < -120f -> viewModel.removeChord(index)
                                    abs(offsetX) > slotWidthPx / 2 -> {
                                        val shift = (offsetX / slotWidthPx).roundToInt()
                                        viewModel.moveChord(index, index + shift)
                                    }
                                }
                                offsetX = 0f
                                offsetY = 0f
                            },
                            onDragCancel = {
                                offsetX = 0f
                                offsetY = 0f
                            },
                        )
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (playing) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                ),
                onClick = { if (!draft.readOnly) viewModel.editChord(index) },
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Text(
                        romanFor(chord.degreeIndex, chord.quality),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        DefaultNoteFormatter.format(rendered[index], state.notation),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "${chord.beats} beats",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaybackControls(state: ProgressionsUiState, viewModel: ProgressionsViewModel) {
    val draft = state.draft
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledIconButton(
            onClick = viewModel::togglePlay,
            modifier = Modifier.semantics {
                contentDescription = if (state.isPlaying) "Pausar" else "Reproducir progresión"
            },
        ) {
            Icon(
                if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
            )
        }
        IconToggleButton(
            checked = state.loop,
            onCheckedChange = { viewModel.toggleLoop() },
            modifier = Modifier.semantics { contentDescription = "Repetir en bucle" },
        ) {
            Icon(
                Icons.Filled.Repeat,
                contentDescription = null,
                tint = if (state.loop) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!draft.readOnly) {
            IconButton(
                onClick = { viewModel.showTransposeDialog(true) },
                modifier = Modifier.semantics { contentDescription = "Transportar tonalidad" },
            ) {
                Icon(Icons.Filled.SwapHoriz, contentDescription = null)
            }
            IconButton(
                onClick = { viewModel.showSaveDialog(true) },
                modifier = Modifier.semantics { contentDescription = "Guardar progresión" },
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
            }
        } else {
            IconButton(
                onClick = {
                    state.famous.firstOrNull { it.id == draft.id }?.let { viewModel.duplicate(it) }
                },
                modifier = Modifier.semantics { contentDescription = "Duplicar para editar" },
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null)
            }
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("BPM ${draft.bpm}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(72.dp))
        Slider(
            value = draft.bpm.toFloat(),
            onValueChange = { viewModel.setBpm(it.roundToInt()) },
            valueRange = 40f..200f,
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "Tempo en BPM" },
        )
    }
}

// ----------------------------------------------------------------- biblioteca

@Composable
private fun LibrarySection(state: ProgressionsUiState, viewModel: ProgressionsViewModel) {
    Text("Guardadas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    if (state.saved.isEmpty()) {
        Text(
            "Todavía no guardaste progresiones.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    for (progression in state.saved) {
        LibraryRow(progression, state, viewModel, famous = false)
    }

    Text("Famosas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    for (progression in state.famous) {
        LibraryRow(progression, state, viewModel, famous = true)
    }
}

@Composable
private fun LibraryRow(
    progression: Progression,
    state: ProgressionsUiState,
    viewModel: ProgressionsViewModel,
    famous: Boolean,
) {
    val summary = viewModel.famousFicha(progression.name)?.romanSummary
        ?: progression.chords.joinToString("–") { romanFor(it.degreeIndex, it.quality) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { viewModel.open(progression) },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(progression.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "$summary · ${DefaultNoteFormatter.format(progression.key.tonic, state.notation)} " +
                        PedagogicalContent.scaleName(progression.key.scale),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (famous) {
                IconButton(
                    onClick = { viewModel.showFamousFicha(progression.name) },
                    modifier = Modifier.semantics { contentDescription = "Ficha de ${progression.name}" },
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null)
                }
            } else {
                IconButton(
                    onClick = { viewModel.showRenameDialog(progression) },
                    modifier = Modifier.semantics { contentDescription = "Renombrar ${progression.name}" },
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                }
            }
            IconButton(
                onClick = { viewModel.duplicate(progression) },
                modifier = Modifier.semantics { contentDescription = "Duplicar ${progression.name}" },
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null)
            }
            if (!famous) {
                IconButton(
                    onClick = { viewModel.delete(progression) },
                    modifier = Modifier.semantics { contentDescription = "Eliminar ${progression.name}" },
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                }
            }
        }
    }
}

// ------------------------------------------------------------------- diálogos

@Composable
private fun Dialogs(state: ProgressionsUiState, viewModel: ProgressionsViewModel) {
    state.editingIndex?.let { index ->
        EditChordDialog(index, state, viewModel)
    }
    if (state.showSaveDialog) {
        NameDialog(
            title = "Guardar progresión",
            initial = state.draft.name,
            onConfirm = viewModel::save,
            onDismiss = { viewModel.showSaveDialog(false) },
        )
    }
    state.renameTarget?.let { target ->
        NameDialog(
            title = "Renombrar",
            initial = target.name,
            onConfirm = { viewModel.rename(target, it) },
            onDismiss = { viewModel.showRenameDialog(null) },
        )
    }
    if (state.showTransposeDialog) {
        TransposeDialog(state, viewModel)
    }
    state.fichaFamousName?.let { name ->
        val ficha = viewModel.famousFicha(name)
        AlertDialog(
            onDismissRequest = { viewModel.showFamousFicha(null) },
            title = { Text(name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ficha?.let {
                        Text(it.romanSummary, fontWeight = FontWeight.Bold)
                        Text(it.ficha)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.showFamousFicha(null) }) { Text("Cerrar") }
            },
        )
    }
}

@Composable
private fun EditChordDialog(index: Int, state: ProgressionsUiState, viewModel: ProgressionsViewModel) {
    val draft = state.draft
    val chord = draft.chords.getOrNull(index) ?: return
    val key = draft.key ?: return
    val fieldKey = if (key.scale.category == ScaleCategory.PENTATONIC) key.parentKey()!! else key

    // Vocabulario permitido: tríada y cuatríada diatónicas del grado + sus
    // extensiones por regla (§6.4). Si el acorde actual es un préstamo (famosa
    // duplicada), se conserva como opción.
    val options = remember(fieldKey, chord.degreeIndex) {
        val degreeIdx = chord.degreeIndex - 1
        val triad = fieldKey.diatonicField(ChordLevel.TRIADS)[degreeIdx].chord.quality
        val seventh = fieldKey.diatonicField(ChordLevel.SEVENTHS)[degreeIdx].chord.quality
        val extensions = fieldKey.diatonicField(ChordLevel.EXTENSIONS)[degreeIdx]
            .extensions.map { it.quality }
        (listOf(triad, seventh) + extensions).distinct()
    }
    var quality by remember { mutableStateOf(chord.quality) }
    var beats by remember { mutableFloatStateOf(chord.beats.toFloat()) }

    AlertDialog(
        onDismissRequest = { viewModel.editChord(null) },
        title = { Text("Editar acorde ${romanFor(chord.degreeIndex, quality)}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Calidad (vocabulario del grado):", style = MaterialTheme.typography.labelLarge)
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    for (option in (if (chord.quality in options) options else listOf(chord.quality) + options)) {
                        FilterChip(
                            selected = quality == option,
                            onClick = { quality = option },
                            label = {
                                Text(option.symbol.ifEmpty { "mayor" } + if (option == chord.quality && option !in options) " (préstamo)" else "")
                            },
                        )
                    }
                }
                Text("Duración: ${beats.roundToInt()} beats", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = beats,
                    onValueChange = { beats = it },
                    valueRange = 1f..8f,
                    steps = 6,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.updateChord(index, quality, beats.roundToInt())
                viewModel.editChord(null)
            }) { Text("Aplicar") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { viewModel.removeChord(index) }) { Text("Eliminar") }
                TextButton(onClick = { viewModel.editChord(null) }) { Text("Cancelar") }
            }
        },
    )
}

@Composable
private fun NameDialog(
    title: String,
    initial: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
private fun TransposeDialog(state: ProgressionsUiState, viewModel: ProgressionsViewModel) {
    val scale = state.draft.key?.scale ?: return
    val minorish = scale in setOf(
        ScaleType.NATURAL_MINOR, ScaleType.HARMONIC_MINOR, ScaleType.MELODIC_MINOR,
        ScaleType.AEOLIAN, ScaleType.DORIAN, ScaleType.PHRYGIAN, ScaleType.LOCRIAN,
        ScaleType.PENT_MINOR,
    )
    AlertDialog(
        onDismissRequest = { viewModel.showTransposeDialog(false) },
        title = { Text("Transportar a…") },
        text = {
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (sector in CircleOfFifths.sectors) {
                    val tonic = if (minorish) sector.minor else sector.major
                    AssistChip(
                        onClick = { viewModel.transposeTo(tonic) },
                        label = { Text(DefaultNoteFormatter.format(tonic, state.notation)) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.showTransposeDialog(false) }) { Text("Cancelar") }
        },
    )
}
