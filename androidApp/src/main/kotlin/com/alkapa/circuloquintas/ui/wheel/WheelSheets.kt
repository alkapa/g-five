@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.alkapa.circuloquintas.ui.wheel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alkapa.circuloquintas.content.PedagogicalContent
import com.alkapa.circuloquintas.domain.Degree
import com.alkapa.circuloquintas.domain.Notation
import com.alkapa.circuloquintas.domain.wheel.WheelModel
import com.alkapa.circuloquintas.ui.theme.FN_PALETTES
import com.alkapa.circuloquintas.ui.theme.LocalFnPalette
import com.alkapa.circuloquintas.ui.theme.Wheel

/** Hoja «Tonalidad y escala» del diseño: raíces, escalas tonales, modos y pentatónicas. */
@Composable
fun KeySheet(state: WheelUiState, flats: Boolean, viewModel: WheelViewModel) {
    val palette = LocalFnPalette.current
    ModalBottomSheet(
        onDismissRequest = { viewModel.openSheet(false) },
        containerColor = Wheel.Card,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 18.dp),
        ) {
            Text("Tonalidad y escala", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Wheel.TextPrimary)

            // Raíces (la grilla usa nombres con bemoles, como el diseño).
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
                maxItemsInEachRow = 6,
            ) {
                for (pc in 0 until 12) {
                    val sel = pc == state.rootPc
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(if (sel) palette.rest else Wheel.Surface, RoundedCornerShape(9.dp))
                            .border(1.dp, if (sel) palette.rest else Wheel.Border, RoundedCornerShape(9.dp))
                            .clickable { viewModel.pickRoot(pc) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            WheelModel.pcName(pc, flats = true, notation = state.notation),
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace,
                            color = if (sel) Wheel.OnAccent else Wheel.TextPrimary,
                        )
                    }
                }
            }

            // Escalas tonales.
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                for (scale in WheelModel.SHEET_TONAL) {
                    val sel = scale == state.scale
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (sel) Wheel.CardAlt else Wheel.Surface, RoundedCornerShape(10.dp))
                            .border(1.dp, if (sel) palette.rest else Wheel.Border, RoundedCornerShape(10.dp))
                            .clickable { viewModel.pickScale(scale) }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(if (sel) palette.rest else Color.Transparent, CircleShape)
                                .border(2.dp, if (sel) palette.rest else Wheel.BorderStrong, CircleShape),
                        )
                        Text(WheelModel.scaleLabel(scale), fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Wheel.TextPrimary)
                        Spacer(Modifier.weight(1f))
                        Text(WheelModel.scaleDesc(scale), fontSize = 11.sp, color = Wheel.TextSecondary)
                    }
                }
            }

            SheetGroupLabel("MODOS")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                for ((scale, label) in WheelModel.SHEET_MODES) {
                    PickChip(label, selected = scale == state.scale, accent = palette.rest) {
                        viewModel.pickScale(scale)
                    }
                }
            }

            SheetGroupLabel("PENTATÓNICAS")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for ((scale, label) in WheelModel.SHEET_PENTAS) {
                    PickChip(label, selected = scale == state.scale, accent = palette.rest) {
                        viewModel.pickScale(scale)
                    }
                }
            }

            Text(
                WheelModel.scaleDesc(state.scale),
                fontSize = 10.5.sp, color = Wheel.TextMuted, modifier = Modifier.padding(top = 9.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(46.dp)
                    .background(palette.rest, RoundedCornerShape(11.dp))
                    .clickable { viewModel.openSheet(false) },
                contentAlignment = Alignment.Center,
            ) {
                Text("Listo", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Wheel.OnAccent)
            }
        }
    }
}

@Composable
private fun SheetGroupLabel(label: String) {
    Text(
        label,
        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Wheel.TextMuted, letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 12.dp, bottom = 7.dp),
    )
}

@Composable
private fun PickChip(label: String, selected: Boolean, accent: Color, onTap: () -> Unit) {
    Text(
        label,
        fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold,
        color = when {
            selected -> Wheel.OnAccent
            else -> Wheel.TextSecondary
        },
        modifier = Modifier
            .background(if (selected) accent else Wheel.Surface, RoundedCornerShape(999.dp))
            .border(1.dp, if (selected) accent else Wheel.Border, RoundedCornerShape(999.dp))
            .clickable(onClick = onTap)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}

/** Ajustes del refactor: cifrado, paleta de funciones y mostrar grados. */
@Composable
fun WheelSettingsSheet(state: WheelUiState, viewModel: WheelViewModel) {
    val palette = LocalFnPalette.current
    ModalBottomSheet(
        onDismissRequest = { viewModel.openSettings(false) },
        containerColor = Wheel.Card,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Ajustes", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Wheel.TextPrimary)

            Text("Cifrado", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Wheel.TextMuted, letterSpacing = 0.5.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PickChip("Anglosajón (C, Dm)", state.notation == Notation.AMERICAN, palette.rest) {
                    viewModel.setNotation(Notation.AMERICAN)
                }
                PickChip("Latino (Do, Rem)", state.notation == Notation.LATIN, palette.rest) {
                    viewModel.setNotation(Notation.LATIN)
                }
            }

            Text("Paleta", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Wheel.TextMuted, letterSpacing = 0.5.sp)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FN_PALETTES.forEachIndexed { i, (name, colors) ->
                    val sel = i == state.palette
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (sel) Wheel.CardAlt else Wheel.Surface, RoundedCornerShape(10.dp))
                            .border(1.dp, if (sel) colors.rest else Wheel.Border, RoundedCornerShape(10.dp))
                            .clickable { viewModel.setPalette(i) }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        for (c in listOf(colors.rest, colors.motion, colors.tension)) {
                            Box(Modifier.size(10.dp).background(c, CircleShape))
                        }
                        Text(name, fontSize = 12.5.sp, color = Wheel.TextPrimary)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Mostrar grados", fontSize = 13.sp, color = Wheel.TextPrimary)
                    Text(
                        "Números romanos bajo cada acorde de la rueda.",
                        fontSize = 10.5.sp, color = Wheel.TextMuted,
                    )
                }
                Switch(
                    checked = state.showDegrees,
                    onCheckedChange = viewModel::setShowDegrees,
                    colors = SwitchDefaults.colors(checkedTrackColor = palette.rest),
                )
            }

            Text(
                PedagogicalContent.functionModelIntro,
                fontSize = 11.sp, color = Wheel.TextSecondary,
            )
        }
    }
}

/** Ficha pedagógica del grado (pulsación larga en el rail). */
@Composable
fun FichaSheet(degree: Degree, state: WheelUiState, viewModel: WheelViewModel) {
    val ficha = PedagogicalContent.degreeFicha(degree)
    ModalBottomSheet(
        onDismissRequest = { viewModel.openFicha(null) },
        containerColor = Wheel.Card,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "${degree.roman} · ${DefaultFormatterName(degree, state)}",
                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Wheel.TextPrimary,
            )
            if (ficha != null) {
                Text(ficha.explanation, fontSize = 13.sp, color = Wheel.TextSecondary)
                Text("Uso típico: ${ficha.typicalUse}", fontSize = 12.sp, color = Wheel.TextMuted)
            }
        }
    }
}

@Composable
private fun DefaultFormatterName(degree: Degree, state: WheelUiState): String =
    com.alkapa.circuloquintas.domain.DefaultNoteFormatter.format(degree.chord, state.notation)
