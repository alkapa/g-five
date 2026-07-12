@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
)

package com.alkapa.circuloquintas.ui.circle

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alkapa.circuloquintas.content.PedagogicalContent
import com.alkapa.circuloquintas.domain.ChordLevel
import com.alkapa.circuloquintas.domain.Chord
import com.alkapa.circuloquintas.domain.CircleOfFifths
import com.alkapa.circuloquintas.domain.DefaultNoteFormatter
import com.alkapa.circuloquintas.domain.Degree
import com.alkapa.circuloquintas.domain.Lens
import com.alkapa.circuloquintas.domain.Need
import com.alkapa.circuloquintas.domain.ScaleCategory
import com.alkapa.circuloquintas.domain.ScaleType
import com.alkapa.circuloquintas.domain.repo.CircleLayer
import com.alkapa.circuloquintas.ui.theme.LocalFunctionColors

@Composable
fun CircleScreen(viewModel: CircleViewModel) {
    val state by viewModel.state.collectAsState()
    if (!state.restored) return

    val fieldKey = remember(state.tonic, state.scale) {
        if (state.scale.category == ScaleCategory.PENTATONIC) state.key.parentKey()!! else state.key
    }
    val field = remember(fieldKey, state.level) { fieldKey.diatonicField(state.level) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TonalityHeader(state, viewModel)
        CircleOfFifthsCanvas(
            state = state,
            field = field,
            onCellTap = viewModel::onSectorTap,
            onCellLongPress = viewModel::onSectorLongPress,
            modifier = Modifier.fillMaxWidth(),
        )
        ScaleBar(state, viewModel)
        ScaleSelector(state.scale, viewModel::setScale)
        LevelSelector(state.level, viewModel::setLevel)
        LayerToggles(state.layers, viewModel::toggleLayer)
        NeedSearch(state, field, viewModel)
        Spacer(Modifier.height(24.dp))
    }

    state.fichaDegree?.let { degree ->
        DegreeFichaSheet(degree, state, viewModel)
    }
    state.instrumentChord?.let { chord ->
        com.alkapa.circuloquintas.ui.instrument.InstrumentSheet(
            chord = chord,
            notation = state.notation,
            onPlayMidis = viewModel::playMidis,
            onDismiss = viewModel::closeInstrument,
        )
    }
}

@Composable
private fun TonalityHeader(state: CircleUiState, viewModel: CircleViewModel) {
    val tonicName = DefaultNoteFormatter.format(state.tonic, state.notation)
    val scaleName = PedagogicalContent.scaleName(state.scale)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$tonicName $scaleName",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        val alternative = CircleOfFifths.enharmonicAlternative(state.tonic)
        if (alternative != null) {
            AssistChip(
                onClick = viewModel::toggleEnharmonic,
                label = {
                    Text("↔ ${DefaultNoteFormatter.format(alternative, state.notation)}")
                },
                leadingIcon = {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = null, Modifier.size(18.dp))
                },
                modifier = Modifier.semantics {
                    contentDescription = "Cambiar grafía enarmónica a " +
                        DefaultNoteFormatter.format(alternative, state.notation)
                },
            )
        }
    }
}

@Composable
private fun ScaleSelector(selected: ScaleType, onSelect: (ScaleType) -> Unit) {
    val groups = listOf(
        "Tonales" to listOf(
            ScaleType.MAJOR, ScaleType.NATURAL_MINOR, ScaleType.HARMONIC_MINOR, ScaleType.MELODIC_MINOR,
        ),
        "Modos" to listOf(
            ScaleType.IONIAN, ScaleType.DORIAN, ScaleType.PHRYGIAN, ScaleType.LYDIAN,
            ScaleType.MIXOLYDIAN, ScaleType.AEOLIAN, ScaleType.LOCRIAN,
        ),
        "Pentatónicas" to listOf(ScaleType.PENT_MAJOR, ScaleType.PENT_MINOR),
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for ((label, scales) in groups) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (scale in scales) {
                    FilterChip(
                        selected = scale == selected,
                        onClick = { onSelect(scale) },
                        label = { Text(PedagogicalContent.scaleName(scale)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelSelector(selected: ChordLevel, onSelect: (ChordLevel) -> Unit) {
    val options = listOf(
        ChordLevel.TRIADS to "Tríadas",
        ChordLevel.SEVENTHS to "Séptimas",
        ChordLevel.EXTENSIONS to "Extensiones",
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (level, label) ->
            SegmentedButton(
                selected = level == selected,
                onClick = { onSelect(level) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
            ) {
                Text(label)
            }
        }
    }
}

@Composable
private fun LayerToggles(layers: Set<CircleLayer>, onToggle: (CircleLayer) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Capas:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.CenterVertically))
        FilterChip(
            selected = CircleLayer.DEGREES in layers,
            onClick = { onToggle(CircleLayer.DEGREES) },
            label = { Text("Grados") },
        )
        FilterChip(
            selected = CircleLayer.FUNCTIONS in layers,
            onClick = { onToggle(CircleLayer.FUNCTIONS) },
            label = { Text("Funciones") },
        )
    }
}

/** Barra de escala (§6.2.5): notas en orden con grado; overlay pentatónico atenúa 2 de 7. */
@Composable
private fun ScaleBar(state: CircleUiState, viewModel: CircleViewModel) {
    val overlay = state.key.pentatonicOverlay()
    val barKey = overlay?.parent ?: state.key
    val notes = remember(barKey) { barKey.notes() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        notes.forEachIndexed { index, note ->
            val included = overlay == null || (index + 1) in overlay.includedDegrees
            val name = DefaultNoteFormatter.format(note, state.notation)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(
                        onClick = { if (included) viewModel.playScaleNote(index) },
                        onClickLabel = "Nota $name, grado ${index + 1}",
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (included) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    },
                ),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                ) {
                    Text(
                        name,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (included) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                    )
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Column {
            IconButton(
                onClick = { viewModel.playScale(ascending = true) },
                modifier = Modifier.semantics { contentDescription = "Reproducir escala ascendente" },
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
            IconButton(
                onClick = { viewModel.playScale(ascending = false) },
                modifier = Modifier.semantics { contentDescription = "Reproducir escala descendente" },
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        }
    }
}

/** Buscador por sensación (§6.2.7). */
@Composable
private fun NeedSearch(state: CircleUiState, field: List<Degree>, viewModel: CircleViewModel) {
    val needs = listOf(
        Need.REST to "Descanso",
        Need.MOTION to "De paso",
        Need.TENSION to "Tensión",
        Need.COLOR to "Color",
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("¿Qué necesitas que suene?", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for ((need, label) in needs) {
                FilterChip(
                    selected = state.selectedNeed == need,
                    onClick = {
                        viewModel.setNeed(if (state.selectedNeed == need) null else need)
                    },
                    label = { Text(label) },
                )
            }
        }
        val need = state.selectedNeed ?: return@Column
        val results = remember(state.tonic, state.scale, state.level, need) {
            state.key.searchByNeed(need, state.level)
        }
        if (need == Need.TENSION && state.key.lens == Lens.MODAL) {
            Text(
                PedagogicalContent.modalTensionNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (results.isEmpty()) {
            Text(
                "No hay acordes con esa sensación en esta escala y nivel.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        for (degree in results) {
            NeedResultRow(degree, state, viewModel)
        }
    }
}

@Composable
private fun NeedResultRow(degree: Degree, state: CircleUiState, viewModel: CircleViewModel) {
    val functionColors = LocalFunctionColors.current
    val chordName = DefaultNoteFormatter.format(degree.chord, state.notation)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { viewModel.playDegree(degree) },
                onLongClick = { viewModel.openFicha(degree) },
                onClickLabel = "Escuchar $chordName",
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = when {
                            degree.modalFunction != null -> when (degree.modalFunction) {
                                com.alkapa.circuloquintas.domain.ModalFunction.CENTER -> functionColors.tonic
                                com.alkapa.circuloquintas.domain.ModalFunction.CHARACTERISTIC -> functionColors.modalColor
                                else -> functionColors.neutral
                            }
                            degree.tonalFunction == com.alkapa.circuloquintas.domain.TonalFunction.TONIC -> functionColors.tonic
                            degree.tonalFunction == com.alkapa.circuloquintas.domain.TonalFunction.SUBDOMINANT -> functionColors.subdominant
                            degree.tonalFunction == com.alkapa.circuloquintas.domain.TonalFunction.DOMINANT -> functionColors.dominant
                            else -> functionColors.modalColor
                        },
                        shape = CircleShape,
                    ),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("$chordName · ${degree.roman}", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (degree.modalFunction != null) {
                            PedagogicalContent.modalFunctionLabel(degree.modalFunction!!)
                        } else {
                            PedagogicalContent.tonalFunctionLabel(degree.tonalFunction)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (degree.isAmbiguous) {
                        Text(
                            "ambiguo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                    if (degree.chord.caution) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Precaución: choque de novena menor",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            IconButton(
                onClick = { viewModel.addToProgression(degree, degree.chord) },
                modifier = Modifier.semantics {
                    contentDescription = "Agregar $chordName a la progresión"
                },
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    }
}

/** Hoja de detalle del grado (§6.2.8). */
@Composable
private fun DegreeFichaSheet(degree: Degree, state: CircleUiState, viewModel: CircleViewModel) {
    val chordName = DefaultNoteFormatter.format(degree.chord, state.notation)
    val ficha = PedagogicalContent.degreeFicha(degree)
    ModalBottomSheet(onDismissRequest = viewModel::closeFicha) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "$chordName · ${degree.roman}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Notas: " + degree.chord.notes.joinToString(" · ") {
                    DefaultNoteFormatter.format(it, state.notation)
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            if (degree.modalFunction != null) {
                                PedagogicalContent.modalFunctionLabel(degree.modalFunction!!)
                            } else {
                                PedagogicalContent.tonalFunctionLabel(degree.tonalFunction)
                            },
                        )
                    },
                )
                if (degree.isAmbiguous) {
                    AssistChip(onClick = {}, label = { Text("Función ambigua") })
                }
            }
            if (ficha != null) {
                Text(ficha.explanation, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Uso típico: ${ficha.typicalUse}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (degree.chord.caution) {
                Text(
                    PedagogicalContent.cautionExplanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (degree.extensions.isNotEmpty()) {
                Text("Colores disponibles sobre este grado:", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (ext in degree.extensions) {
                        ExtensionChip(ext, state, degree, viewModel)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = { viewModel.playDegree(degree) }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Escuchar")
                }
                FilledTonalButton(onClick = { viewModel.addToProgression(degree) }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("A la progresión")
                }
                TextButton(onClick = { viewModel.makeTonic(degree) }) {
                    Text("Hacer tónica")
                }
            }
            TextButton(onClick = { viewModel.openInstrument(degree.chord) }) {
                Text("Ver en el diapasón y el piano (inversiones)")
            }
        }
    }
}

@Composable
private fun ExtensionChip(ext: Chord, state: CircleUiState, degree: Degree, viewModel: CircleViewModel) {
    val name = DefaultNoteFormatter.format(ext, state.notation)
    AssistChip(
        onClick = { viewModel.playChord(ext) },
        label = { Text(name) },
        leadingIcon = if (ext.caution) {
            {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = "Precaución",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            null
        },
        trailingIcon = {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Agregar $name a la progresión",
                modifier = Modifier
                    .size(18.dp)
                    .combinedClickable(onClick = { viewModel.addToProgression(degree, ext) }),
            )
        },
    )
}
