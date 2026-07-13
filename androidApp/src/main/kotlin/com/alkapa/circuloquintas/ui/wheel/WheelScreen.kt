@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package com.alkapa.circuloquintas.ui.wheel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alkapa.circuloquintas.domain.DefaultNoteFormatter
import com.alkapa.circuloquintas.domain.Key
import com.alkapa.circuloquintas.domain.ScaleCategory
import com.alkapa.circuloquintas.domain.wheel.WheelModel
import com.alkapa.circuloquintas.domain.wheel.WheelVoicings
import com.alkapa.circuloquintas.ui.theme.LocalFnPalette
import com.alkapa.circuloquintas.ui.theme.Wheel

@Composable
fun WheelScreen(viewModel: WheelViewModel) {
    val state by viewModel.state.collectAsState()
    if (!state.restored) return

    val flats = remember(state.rootPc, state.scale) { WheelModel.usesFlats(state.rootPc, state.scale) }
    val field = remember(state.rootPc, state.scale, state.seventh) {
        WheelModel.field(state.rootPc, state.scale, state.seventh)
    }
    val tonicName = WheelModel.pcName(state.rootPc, flats, state.notation)
    val keyShort = tonicName + when (WheelModel.triadFamily(field.triads[0].chord.quality)) {
        'm' -> "m"; 'd' -> "°"; else -> ""
    }
    val keyName = "$tonicName ${WheelModel.keyLabel(state.scale)}"
    val progs = WheelModel.progressionsFor(state.scale)
    val progDegrees = state.prog.takeIf { it >= 0 }?.let { progs.getOrNull(it)?.degrees?.toSet() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Wheel.Bg),
    ) {
        HeaderBar(
            title = if (state.tab == 0) "Círculo" else "Progresiones",
            keyName = keyName,
            onOpenSheet = { viewModel.openSheet(true) },
            onOpenSettings = { viewModel.openSettings(true) },
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            if (state.tab == 0) {
                CircleTab(state, field, flats, keyShort, progs, progDegrees, viewModel)
            } else {
                ProgressionsTab(state, field, flats, keyName, progs, viewModel)
            }
            Spacer(Modifier.padding(bottom = 20.dp))
        }
    }

    if (state.sheet) {
        KeySheet(state, flats, viewModel)
    }
    if (state.settings) {
        WheelSettingsSheet(state, viewModel)
    }
    state.fichaDegree?.let { degree ->
        FichaSheet(degree, state, viewModel)
    }
}

// -------------------------------------------------------------------- header

@Composable
private fun HeaderBar(
    title: String,
    keyName: String,
    onOpenSheet: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Wheel.TextPrimary, maxLines = 1)
        Spacer(Modifier.width(10.dp))
        // La pastilla cede espacio (con elipsis) antes que empujar el engranaje
        // fuera de la pantalla en tonalidades largas o pantallas angostas.
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .background(Wheel.CardAlt, RoundedCornerShape(999.dp))
                    .border(1.dp, Wheel.Border, RoundedCornerShape(999.dp))
                    .clickable(onClick = onOpenSheet)
                    .padding(horizontal = 15.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    keyName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = Wheel.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text("▼", fontSize = 9.sp, color = Wheel.TextSecondary)
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.semantics { contentDescription = "Ajustes" },
            ) {
                Icon(Icons.Filled.Settings, contentDescription = null, tint = Wheel.TextSecondary)
            }
        }
    }
}

// --------------------------------------------------------------- tab círculo

@Composable
private fun CircleTab(
    state: WheelUiState,
    field: WheelModel.WheelField,
    flats: Boolean,
    keyShort: String,
    progs: List<WheelModel.WheelProg>,
    progDegrees: Set<Int>?,
    viewModel: WheelViewModel,
) {
    val palette = LocalFnPalette.current

    WheelCanvas(
        field = field,
        flats = flats,
        sel = state.sel,
        progDegrees = progDegrees,
        showDegrees = state.showDegrees,
        keyShort = keyShort,
        scaleLabel = WheelModel.scaleLabel(state.scale),
        notation = state.notation,
        onTapDegree = viewModel::selectChord,
    )

    // Leyenda + conmutador Tríadas/Séptimas. FlowRow: en pantallas angostas el
    // conmutador baja a una segunda línea en vez de cortarse por la derecha.
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        for (fn in WheelModel.Fn.entries) {
            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(Modifier.size(8.dp).background(palette.of(fn), CircleShape))
                Text(fnLabel(fn), fontSize = 11.sp, color = Wheel.TextSecondary)
            }
        }
        Box(Modifier.align(Alignment.CenterVertically)) {
            SegPill(
                options = listOf("Tríadas", "Séptimas"),
                selected = if (state.seventh) 1 else 0,
                onSelect = { viewModel.setSeventh(it == 1) },
            )
        }
    }

    // Banner de progresión activa.
    if (state.prog >= 0) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Wheel.SurfaceAlt, RoundedCornerShape(10.dp))
                .border(1.dp, Wheel.Border, RoundedCornerShape(10.dp))
                .clickable { viewModel.clearProgression() }
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Progresión activa:", fontSize = 11.sp, color = Wheel.TextSecondary)
            Text(
                progs.getOrNull(state.prog)?.name.orEmpty(),
                fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Wheel.TextPrimary,
            )
            Spacer(Modifier.weight(1f))
            Text("quitar ✕", fontSize = 11.sp, color = Wheel.TextMuted)
        }
        Spacer(Modifier.padding(top = 8.dp))
    }

    val selChord = field.active[state.sel]
    val selFn = WheelModel.FN7[state.sel]
    val selColor = palette.of(selFn)

    // Tira de escala.
    SectionLabel("ESCALA", WheelModel.alterationsText(state.scale))
    val stripKey = remember(state.rootPc, state.scale) {
        Key(WheelModel.tonicFor(state.rootPc, state.scale), state.scale)
    }
    val stripNotes = remember(stripKey) { stripKey.notes() }
    val formulas = remember(state.scale) { WheelModel.formulas(state.scale) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        stripNotes.forEachIndexed { i, note ->
            val pc = note.pitchClass
            val inChord = selChord.chord.notes.any { it.pitchClass == pc }
            val isRoot = pc == selChord.chord.root.pitchClass
            val bg = when {
                isRoot -> selColor
                inChord -> selColor.copy(alpha = 0.19f)
                else -> Wheel.Surface
            }
            val borderColor = if (inChord) selColor.copy(alpha = 0.4f) else Wheel.BorderSoft
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(bg, RoundedCornerShape(9.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(9.dp))
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    DefaultNoteFormatter.format(note, state.notation),
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = when {
                        isRoot -> Wheel.OnAccent
                        inChord -> Wheel.TextPrimary
                        else -> Wheel.TextSecondary
                    },
                )
                Text(
                    formulas.getOrElse(i) { "" },
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = when {
                        isRoot -> Wheel.OnAccent.copy(alpha = 0.7f)
                        formulas.getOrElse(i) { "" }.length > 1 -> Wheel.TextPrimary
                        else -> Wheel.TextMuted
                    },
                )
            }
        }
    }
    WheelModel.pentatonicHint(state.scale)?.let {
        Text(it, fontSize = 10.sp, color = Wheel.TextMuted, modifier = Modifier.padding(top = 5.dp, start = 2.dp))
    }

    // Rail de acordes (I–VII).
    SectionLabel("ACORDES", "uno por cada grado (I–VII) · toca para verlo", topPadding = 10)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        field.active.forEachIndexed { i, degree ->
            val fn = WheelModel.FN7[i]
            val col = palette.of(fn)
            val isSel = i == state.sel
            val dimmed = progDegrees != null && i !in progDegrees
            val name = wheelChordName(degree.chord.root.pitchClass, degree.chord.quality, flats, state.notation)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(if (isSel) col else Wheel.CardAlt, RoundedCornerShape(10.dp))
                    .border(1.dp, if (isSel) col else Wheel.Border, RoundedCornerShape(10.dp))
                    .combinedClickable(
                        onClick = { viewModel.selectChord(i) },
                        onLongClick = { viewModel.openFicha(degree) },
                        onClickLabel = "Acorde $name",
                    )
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                val alpha = if (dimmed) 0.4f else 1f
                Text(
                    field.designDeg(i),
                    fontSize = 10.sp, fontFamily = FontFamily.Monospace,
                    color = (if (isSel) Wheel.OnAccent.copy(alpha = 0.72f) else col).copy(alpha = alpha),
                )
                Text(
                    name,
                    fontSize = if (state.seventh) (if (name.length > 5) 10.5.sp else 12.sp) else 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = (if (isSel) Wheel.OnAccent else Wheel.TextPrimary).copy(alpha = alpha),
                )
                Text(
                    WheelModel.qualityLabel(degree.chord.quality),
                    fontSize = 8.sp,
                    color = (if (isSel) Wheel.OnAccent.copy(alpha = 0.6f) else Wheel.TextMuted).copy(alpha = alpha),
                )
            }
        }
    }

    DetailCard(state, field, flats, selColor, selFn, viewModel)
}

@Composable
private fun SectionLabel(label: String, hint: String, topPadding: Int = 0) {
    Row(
        modifier = Modifier.padding(start = 2.dp, end = 2.dp, top = topPadding.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(label, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Wheel.TextMuted, letterSpacing = 0.5.sp)
        Text(hint, fontSize = 10.sp, color = Wheel.TextMuted)
    }
}

/** Conmutador tipo pill del diseño (Tríadas/Séptimas, Guitarra/Piano/Diapasón). */
@Composable
fun SegPill(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    corner: Int = 999,
) {
    Row(
        modifier = Modifier
            .background(Wheel.Surface, RoundedCornerShape(corner.dp))
            .border(1.dp, Wheel.BorderSoft, RoundedCornerShape(corner.dp))
            .padding(2.dp),
    ) {
        options.forEachIndexed { i, label ->
            val sel = i == selected
            Text(
                label,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (sel) Wheel.TextPrimary else Wheel.TextMuted,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .background(if (sel) Wheel.Border else Color.Transparent, RoundedCornerShape((corner - 2).coerceAtLeast(4).dp))
                    .clickable { onSelect(i) }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}

// ------------------------------------------------------------------- detalle

@Composable
private fun DetailCard(
    state: WheelUiState,
    field: WheelModel.WheelField,
    flats: Boolean,
    color: Color,
    fn: WheelModel.Fn,
    viewModel: WheelViewModel,
) {
    val degree = field.active[state.sel]
    val chord = degree.chord
    val invData = remember(chord, state.inv) { WheelModel.inversion(chord, state.inv) }
    val voicings = remember(chord, state.inv) { WheelVoicings.groupVoicings(chord, state.inv) }
    val setIx = if (state.setIx < 0) (if (state.inv > 0) 0 else -1) else state.setIx.coerceAtMost((voicings.size - 1).coerceAtLeast(0))
    val current = if (setIx >= 0) voicings.getOrNull(setIx) else null

    val baseName = wheelChordName(chord.root.pitchClass, chord.quality, flats, state.notation)
    val displayName = if (invData.isRootPosition) baseName
    else "$baseName/${WheelModel.pcName(invData.bassPc, flats, state.notation)}"

    // Fórmulas del acorde dentro de la escala (1 · 3 · 5).
    val fieldScale = if (state.scale.category == ScaleCategory.PENTATONIC) {
        if (state.scale == com.alkapa.circuloquintas.domain.ScaleType.PENT_MAJOR) {
            com.alkapa.circuloquintas.domain.ScaleType.MAJOR
        } else {
            com.alkapa.circuloquintas.domain.ScaleType.NATURAL_MINOR
        }
    } else {
        state.scale
    }
    val fieldFormulas = remember(fieldScale) { WheelModel.formulas(fieldScale) }
    val fieldSteps = remember(fieldScale) {
        var acc = 0
        listOf(0) + fieldScale.pattern.dropLast(1).map { s -> acc += s; acc }
    }
    val inScale = chord.notes.joinToString(" · ") { n ->
        val rel = (n.pitchClass - state.rootPc).mod(12)
        val ix = fieldSteps.indexOf(rel)
        if (ix >= 0) fieldFormulas[ix] else "·"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .background(Wheel.Card, RoundedCornerShape(14.dp))
            .border(1.dp, Wheel.BorderSoft, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                displayName,
                fontSize = 22.sp, fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace, color = Wheel.TextPrimary,
            )
            Text(
                fnLabel(fn),
                fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = color,
                modifier = Modifier
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
            Spacer(Modifier.weight(1f))
            Text(
                "▶",
                fontSize = 14.sp, color = color,
                modifier = Modifier
                    .clickable { viewModel.playInversion() }
                    .semantics { contentDescription = "Escuchar $displayName" }
                    .padding(6.dp),
            )
        }
        val notesText = invData.orderPcs.joinToString(" · ") { WheelModel.pcName(it, flats, state.notation) }
        Text(
            buildString {
                append(notesText)
                append(" · ")
                append(field.designDeg(state.sel))
                append(" — ")
                append(fnLong(fn))
            },
            fontSize = 12.sp, color = Wheel.TextSecondary, modifier = Modifier.padding(top = 3.dp),
        )
        Text(
            "Se construye apilando terceras sobre ${WheelModel.pcName(chord.root.pitchClass, flats, state.notation)} → grados $inScale de la escala",
            fontSize = 11.sp, color = Wheel.TextMuted, modifier = Modifier.padding(top = 2.dp),
        )

        // Chips de inversión. El selector de vista va en su propia fila: juntos
        // no caben en pantallas angostas y quedaban cortados.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text("INVERSIÓN", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Wheel.TextMuted, letterSpacing = 0.5.sp)
            val pcs = chord.notes.map { it.pitchClass }.distinct()
            pcs.forEachIndexed { k, pc ->
                val sel = k == state.inv
                Column(
                    modifier = Modifier
                        .background(if (sel) color else Wheel.Surface, RoundedCornerShape(8.dp))
                        .border(1.dp, if (sel) color else Wheel.Border, RoundedCornerShape(8.dp))
                        .clickable { viewModel.setInversion(k) }
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        if (k == 0) "Fund." else "${k}ª inv",
                        fontSize = 8.5.sp,
                        color = if (sel) Wheel.OnAccent.copy(alpha = 0.7f) else Wheel.TextMuted,
                    )
                    Text(
                        WheelModel.pcName(pc, flats, state.notation),
                        fontSize = 11.5.sp, fontWeight = FontWeight.Bold,
                        color = if (sel) Wheel.OnAccent else Wheel.TextPrimary,
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 9.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            SegPill(
                options = listOf("Guitarra", "Piano", "Diapasón"),
                selected = state.view,
                onSelect = viewModel::setView,
                corner = 8,
            )
        }

        when (state.view) {
            0 -> GuitarView(state, chord, invData, voicings, setIx, current, color, viewModel)
            1 -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                val abs = if (invData.isRootPosition) {
                    chord.stackOffsets.map { chord.root.pitchClass + it }
                } else {
                    invData.abs
                }
                PianoKeys(abs, color)
            }
            else -> FretboardView(state, chord, invData, voicings, setIx, current, color, viewModel)
        }

        Text(fnDesc(fn), fontSize = 11.5.sp, color = Wheel.TextSecondary, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun GuitarView(
    state: WheelUiState,
    chord: com.alkapa.circuloquintas.domain.Chord,
    invData: WheelModel.InversionData,
    voicings: List<WheelVoicings.GroupVoicing>,
    setIx: Int,
    current: WheelVoicings.GroupVoicing?,
    color: Color,
    viewModel: WheelViewModel,
) {
    val showStandard = state.inv == 0 || current == null
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (showStandard && state.inv == 0) {
            val shape = remember(chord) { WheelVoicings.standardShape(chord) }
            ChordDiagram(
                shape, color,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clickable { viewModel.playShape(shape) },
            )
        } else if (current != null) {
            VoicingDiagram(
                current, chord.quality, chord.root.pitchClass, color,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clickable { viewModel.playVoicing(current) },
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text("cuerdas", fontSize = 9.5.sp, color = Wheel.TextMuted)
                voicings.forEachIndexed { i, v ->
                    val sel = i == setIx
                    Text(
                        v.label,
                        fontSize = 10.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace,
                        color = if (sel) Wheel.TextPrimary else Wheel.TextMuted,
                        modifier = Modifier
                            .background(if (sel) Wheel.Border else Wheel.Surface, RoundedCornerShape(999.dp))
                            .border(1.dp, if (sel) Wheel.BorderStrong else Wheel.BorderSoft, RoundedCornerShape(999.dp))
                            .clickable { viewModel.setVoicingSet(i) }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            Text(
                "${state.inv}ª inversión · ${WheelModel.pcName(invData.bassPc, true, state.notation)} en el bajo (anillo) · " +
                    "los números son los grados del acorde" +
                    if (state.seventh) " · voicing drop-2" else "",
                fontSize = 10.sp, color = Wheel.TextMuted,
                modifier = Modifier.padding(top = 6.dp),
            )
        } else {
            Text(
                "Esta inversión no tiene una digitación cómoda en un grupo de cuerdas — revisa la pestaña Diapasón.",
                fontSize = 10.sp, color = Wheel.TextMuted,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun FretboardView(
    state: WheelUiState,
    chord: com.alkapa.circuloquintas.domain.Chord,
    invData: WheelModel.InversionData,
    voicings: List<WheelVoicings.GroupVoicing>,
    setIx: Int,
    current: WheelVoicings.GroupVoicing?,
    color: Color,
    viewModel: WheelViewModel,
) {
    // Digitación resaltada: la del grupo activo, o la forma estándar en fundamental.
    val voice = remember(chord, state.inv, setIx) {
        if (current != null) {
            val keys = current.strings.mapIndexed { i, s -> "$s:${current.frets[i]}" }.toSet()
            keys to "${current.strings[0]}:${current.frets[0]}"
        } else {
            val shape = WheelVoicings.standardShape(chord)
            val keys = mutableSetOf<String>()
            var bass: String? = null
            shape.frets.forEachIndexed { s, f ->
                if (f >= 0) {
                    keys += "$s:$f"
                    if (bass == null) bass = "$s:$f"
                }
            }
            keys.toSet() to bass
        }
    }
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        FretboardFull(
            chordPcs = chord.notes.map { it.pitchClass },
            chordOffsets = chord.stackOffsets,
            voicingKeys = voice.first,
            bassKey = voice.second,
            rootPc = chord.root.pitchClass,
            color = color,
            modifier = Modifier.padding(top = 12.dp),
        )
        Row(
            modifier = Modifier
                .padding(top = 6.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text("posición", fontSize = 9.5.sp, color = Wheel.TextMuted)
            if (state.inv == 0) {
                PosChip("abierta", "forma común", setIx < 0) { viewModel.setVoicingSet(-1) }
            }
            voicings.forEachIndexed { i, v ->
                PosChip(v.zoneLabel, "cuerdas ${v.label}", setIx == i) { viewModel.setVoicingSet(i) }
            }
        }
        Text(
            "sólido = digitación en esa posición · tenue = resto del acorde · anillo = bajo",
            fontSize = 10.sp, color = Wheel.TextMuted, modifier = Modifier.padding(top = 5.dp),
        )
    }
}

@Composable
private fun PosChip(main: String, top: String, selected: Boolean, onTap: () -> Unit) {
    Column(
        modifier = Modifier
            .background(if (selected) Wheel.Border else Wheel.Surface, RoundedCornerShape(9.dp))
            .border(1.dp, if (selected) Wheel.BorderStrong else Wheel.BorderSoft, RoundedCornerShape(9.dp))
            .clickable(onClick = onTap)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            main,
            fontSize = 10.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace,
            color = if (selected) Wheel.TextPrimary else Wheel.TextSecondary,
        )
        Text(top, fontSize = 8.sp, color = if (selected) Wheel.TextSecondary else Wheel.TextMuted)
    }
}
