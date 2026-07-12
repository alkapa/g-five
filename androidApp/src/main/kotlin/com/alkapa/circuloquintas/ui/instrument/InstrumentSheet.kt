@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.alkapa.circuloquintas.ui.instrument

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alkapa.circuloquintas.domain.Chord
import com.alkapa.circuloquintas.domain.DefaultNoteFormatter
import com.alkapa.circuloquintas.domain.Notation
import com.alkapa.circuloquintas.domain.instrument.ChordRole
import com.alkapa.circuloquintas.domain.instrument.ChordVoicingModel
import com.alkapa.circuloquintas.domain.instrument.GuitarFretboard
import com.alkapa.circuloquintas.ui.theme.LocalFunctionColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * "En el instrumento" (§ampliación): el acorde y sus inversiones sobre el
 * diapasón completo de la guitarra (mapa de notas + digitaciones por zona,
 * con tablatura) y sobre el teclado del piano (repetidas por octava).
 */
@Composable
fun InstrumentSheet(
    chord: Chord,
    notation: Notation,
    onPlayMidis: (List<Int>) -> Unit,
    onDismiss: () -> Unit,
) {
    val inversions = remember(chord) { ChordVoicingModel.inversions(chord) }
    var inversionIndex by rememberSaveable(chord) { androidx.compose.runtime.mutableStateOf(0) }
    var instrument by rememberSaveable(chord) { androidx.compose.runtime.mutableStateOf(0) } // 0 = guitarra, 1 = piano

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                DefaultNoteFormatter.format(chord, notation),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            if (chord.notes.size > 4) {
                Text(
                    "Acorde extendido: para digitar e invertir se usan sus 4 voces " +
                        "esenciales (fundamental, 3ª, 7ª y la extensión), como en la práctica común.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Selector de inversión.
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (inversion in inversions) {
                    FilterChip(
                        selected = inversionIndex == inversion.index,
                        onClick = { inversionIndex = inversion.index },
                        label = {
                            Text(
                                "${inversion.name} · bajo " +
                                    DefaultNoteFormatter.format(inversion.bass.note, notation),
                            )
                        },
                    )
                }
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = instrument == 0,
                    onClick = { instrument = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) { Text("Guitarra") }
                SegmentedButton(
                    selected = instrument == 1,
                    onClick = { instrument = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) { Text("Piano") }
            }

            RoleLegend(chord)

            if (instrument == 0) {
                GuitarSection(chord, inversionIndex, notation, onPlayMidis)
            } else {
                PianoSection(chord, inversionIndex, onPlayMidis)
            }
        }
    }
}

// ------------------------------------------------------------------- leyenda

private data class RolePalette(val colors: Map<ChordRole, Color>)

@Composable
private fun rolePalette(): RolePalette {
    val fn = LocalFunctionColors.current
    return RolePalette(
        mapOf(
            ChordRole.ROOT to MaterialTheme.colorScheme.primary,
            ChordRole.THIRD to fn.subdominant,
            ChordRole.FIFTH to fn.neutral,
            ChordRole.SEVENTH to fn.dominant,
            ChordRole.EXTENSION to fn.modalColor,
        ),
    )
}

@Composable
private fun RoleLegend(chord: Chord) {
    val palette = rolePalette()
    val roles = remember(chord) {
        ChordVoicingModel.allTones(chord).map { it.role to it.offset }.distinctBy { it.first }
    }
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for ((role, offset) in roles) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(palette.colors.getValue(role), CircleShape),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    ChordVoicingModel.intervalLabel(offset, chord.quality),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

// ------------------------------------------------------------------ guitarra

@Composable
private fun GuitarSection(
    chord: Chord,
    inversionIndex: Int,
    notation: Notation,
    onPlayMidis: (List<Int>) -> Unit,
) {
    val toneMap = remember(chord) { GuitarFretboard.chordToneMap(chord) }
    val voicings by produceState(
        initialValue = emptyList<GuitarFretboard.Voicing>(),
        chord, inversionIndex,
    ) {
        value = withContext(Dispatchers.Default) {
            GuitarFretboard.voicings(chord, inversionIndex)
        }
    }
    var selectedVoicing by remember(chord, inversionIndex) { mutableIntStateOf(0) }
    val voicing = voicings.getOrNull(selectedVoicing)

    FretboardCanvas(toneMap, voicing, chord)

    if (voicings.isEmpty()) {
        Text(
            "Buscando digitaciones…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        voicings.forEachIndexed { index, v ->
            FilterChip(
                selected = index == selectedVoicing,
                onClick = { selectedVoicing = index },
                label = {
                    Text(if (v.minFret == 0) "Abierta" else "Traste ${v.minFret}")
                },
            )
        }
    }

    voicing?.let { v ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                for (line in GuitarFretboard.tablature(v)) {
                    Text(
                        line,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            FilledTonalButton(onClick = { onPlayMidis(v.midis) }) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Escuchar")
            }
        }
        Text(
            "Notas: " + v.midis.joinToString(" · ") { midi ->
                val tone = ChordVoicingModel.allTones(chord)
                    .firstOrNull { it.note.pitchClass == midi.mod(12) }
                tone?.let { DefaultNoteFormatter.format(it.note, notation) } ?: "?"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FretboardCanvas(
    toneMap: List<GuitarFretboard.Position>,
    voicing: GuitarFretboard.Voicing?,
    chord: Chord,
) {
    val palette = rolePalette()
    val textMeasurer = rememberTextMeasurer()
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceDim = MaterialTheme.colorScheme.surfaceVariant
    val onColor = MaterialTheme.colorScheme.surface

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
    ) {
        val leftPad = 34.dp.toPx()
        val topPad = 14.dp.toPx()
        val bottomPad = 22.dp.toPx()
        val boardWidth = size.width - leftPad
        val boardHeight = size.height - topPad - bottomPad
        val fretWidth = boardWidth / GuitarFretboard.FRETS
        val stringGap = boardHeight / 5f

        fun stringY(string: Int) = topPad + (5 - string) * stringGap
        fun fretCenterX(fret: Int) =
            if (fret == 0) leftPad - 14.dp.toPx() else leftPad + (fret - 0.5f) * fretWidth

        // Marcadores de trastes.
        for (fret in GuitarFretboard.MARKER_FRETS) {
            val x = leftPad + (fret - 0.5f) * fretWidth
            val centerY = topPad + boardHeight / 2f
            if (fret == 12) {
                drawCircle(surfaceDim, 5.dp.toPx(), Offset(x, centerY - stringGap))
                drawCircle(surfaceDim, 5.dp.toPx(), Offset(x, centerY + stringGap))
            } else {
                drawCircle(surfaceDim, 5.dp.toPx(), Offset(x, centerY))
            }
            val label = textMeasurer.measure(
                fret.toString(),
                TextStyle(color = lineColor, fontSize = 9.sp),
            )
            drawText(
                textMeasurer, fret.toString(),
                topLeft = Offset(x - label.size.width / 2f, size.height - bottomPad + 4.dp.toPx()),
                style = TextStyle(color = lineColor, fontSize = 9.sp),
            )
        }

        // Cejuela y trastes.
        drawLine(
            lineColor, Offset(leftPad, topPad), Offset(leftPad, topPad + boardHeight),
            strokeWidth = 4.dp.toPx(),
        )
        for (fret in 1..GuitarFretboard.FRETS) {
            val x = leftPad + fret * fretWidth
            drawLine(lineColor.copy(alpha = 0.4f), Offset(x, topPad), Offset(x, topPad + boardHeight))
        }
        // Cuerdas.
        for (string in 0..5) {
            val y = stringY(string)
            drawLine(
                lineColor, Offset(leftPad - 24.dp.toPx(), y), Offset(size.width, y),
                strokeWidth = (1 + (5 - string) * 0.3f).dp.toPx(),
            )
        }

        // Mapa completo de notas del acorde (atenuado).
        for (position in toneMap) {
            drawCircle(
                palette.colors.getValue(position.role).copy(alpha = 0.30f),
                radius = 6.dp.toPx(),
                center = Offset(fretCenterX(position.fret), stringY(position.string)),
            )
        }

        // Digitación seleccionada (resaltada, con etiqueta de intervalo).
        if (voicing != null) {
            val sounding = voicing.frets.withIndex().filter { it.value != null }
            val bassString = sounding.minOfOrNull { it.index }
            for ((string, fret) in sounding) {
                val midi = GuitarFretboard.STANDARD_TUNING[string] + fret!!
                val tone = ChordVoicingModel.allTones(chord)
                    .firstOrNull { it.note.pitchClass == midi.mod(12) } ?: continue
                val center = Offset(fretCenterX(fret), stringY(string))
                drawCircle(palette.colors.getValue(tone.role), 10.dp.toPx(), center)
                if (string == bassString) {
                    drawCircle(
                        lineColor, 12.dp.toPx(), center,
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
                val label = ChordVoicingModel.intervalLabel(tone.offset, chord.quality)
                val measured = textMeasurer.measure(
                    label,
                    TextStyle(color = onColor, fontSize = 8.sp, fontWeight = FontWeight.Bold),
                )
                drawText(
                    textMeasurer, label,
                    topLeft = center - Offset(measured.size.width / 2f, measured.size.height / 2f),
                    style = TextStyle(color = onColor, fontSize = 8.sp, fontWeight = FontWeight.Bold),
                )
            }
            // Cuerdas mudas.
            voicing.frets.forEachIndexed { string, fret ->
                if (fret == null) {
                    val style = TextStyle(color = lineColor, fontSize = 10.sp)
                    val measured = textMeasurer.measure("×", style)
                    drawText(
                        textMeasurer, "×",
                        topLeft = Offset(
                            2.dp.toPx(),
                            stringY(string) - measured.size.height / 2f,
                        ),
                        style = style,
                    )
                }
            }
        }
    }
}

// --------------------------------------------------------------------- piano

@Composable
private fun PianoSection(
    chord: Chord,
    inversionIndex: Int,
    onPlayMidis: (List<Int>) -> Unit,
) {
    val placements = remember(chord, inversionIndex) {
        ChordVoicingModel.pianoPlacements(chord, inversionIndex)
    }
    PianoCanvas(chord, placements)
    Text(
        "La misma inversión, repetida en cada octava del teclado. El bajo se marca con borde.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (placements.isNotEmpty()) {
        FilledTonalButton(onClick = { onPlayMidis(placements[placements.size / 2]) }) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Escuchar")
        }
    }
}

private val WHITE_INDEX = mapOf(0 to 0, 2 to 1, 4 to 2, 5 to 3, 7 to 4, 9 to 5, 11 to 6)
private val BLACK_PCS = setOf(1, 3, 6, 8, 10)

@Composable
private fun PianoCanvas(chord: Chord, placements: List<List<Int>>) {
    val palette = rolePalette()
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant
    val low = 36  // C2
    val high = 95 // B6
    val whiteTotal = (low..high).count { it.mod(12) !in BLACK_PCS }

    val highlighted = remember(placements) {
        buildMap {
            for (placement in placements) {
                placement.forEachIndexed { i, midi -> put(midi, i == 0) } // true = bajo
            }
        }
    }
    fun roleOf(midi: Int): ChordRole? = ChordVoicingModel.allTones(chord)
        .firstOrNull { it.note.pitchClass == midi.mod(12) }?.role

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
    ) {
        val keyWidth = size.width / whiteTotal
        val whiteHeight = size.height
        val blackHeight = whiteHeight * 0.6f
        val blackWidth = keyWidth * 0.62f

        fun whiteIndexOf(midi: Int): Int {
            val octaves = (midi - low) / 12
            val pc = midi.mod(12)
            return octaves * 7 + WHITE_INDEX.getValue(pc)
        }

        // Teclas blancas.
        for (midi in low..high) {
            val pc = midi.mod(12)
            if (pc in BLACK_PCS) continue
            val x = whiteIndexOf(midi) * keyWidth
            val bass = highlighted[midi]
            val fill = if (bass != null) {
                palette.colors.getValue(roleOf(midi) ?: ChordRole.ROOT).copy(alpha = 0.85f)
            } else {
                Color.White
            }
            drawRoundRect(
                fill,
                topLeft = Offset(x + 1f, 0f),
                size = Size(keyWidth - 2f, whiteHeight),
                cornerRadius = CornerRadius(3f, 3f),
            )
            drawRoundRect(
                lineColor.copy(alpha = 0.6f),
                topLeft = Offset(x + 1f, 0f),
                size = Size(keyWidth - 2f, whiteHeight),
                cornerRadius = CornerRadius(3f, 3f),
                style = Stroke(width = 1f),
            )
            if (bass == true) {
                drawRoundRect(
                    lineColor,
                    topLeft = Offset(x + 1f, 0f),
                    size = Size(keyWidth - 2f, whiteHeight),
                    cornerRadius = CornerRadius(3f, 3f),
                    style = Stroke(width = 3f),
                )
            }
        }
        // Teclas negras.
        for (midi in low..high) {
            val pc = midi.mod(12)
            if (pc !in BLACK_PCS) continue
            val leftWhite = whiteIndexOf(midi - 1)
            val x = (leftWhite + 1) * keyWidth - blackWidth / 2f
            val bass = highlighted[midi]
            val fill = if (bass != null) {
                palette.colors.getValue(roleOf(midi) ?: ChordRole.ROOT)
            } else {
                Color(0xFF222222)
            }
            drawRoundRect(
                fill,
                topLeft = Offset(x, 0f),
                size = Size(blackWidth, blackHeight),
                cornerRadius = CornerRadius(3f, 3f),
            )
            if (bass == true) {
                drawRoundRect(
                    Color.White,
                    topLeft = Offset(x, 0f),
                    size = Size(blackWidth, blackHeight),
                    cornerRadius = CornerRadius(3f, 3f),
                    style = Stroke(width = 3f),
                )
            }
        }
    }
}
