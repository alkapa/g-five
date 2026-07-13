package com.alkapa.circuloquintas.ui.wheel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alkapa.circuloquintas.domain.wheel.WheelModel
import com.alkapa.circuloquintas.domain.wheel.WheelVoicings
import com.alkapa.circuloquintas.ui.theme.LocalFnPalette
import com.alkapa.circuloquintas.ui.theme.Wheel

/**
 * Tab Progresiones del refactor 2a: cards por familia de escala, con acordes
 * coloreados por función, filas expandibles con diagrama y «Ver en el
 * círculo →» que proyecta la progresión sobre la rueda. El ▶ reproduce la
 * progresión con el motor de audio (la app ya lo tiene; el mock lo dejaba
 * para fase 2).
 */
@Composable
fun ProgressionsTab(
    state: WheelUiState,
    field: WheelModel.WheelField,
    flats: Boolean,
    keyName: String,
    progs: List<WheelModel.WheelProg>,
    viewModel: WheelViewModel,
) {
    val palette = LocalFnPalette.current

    Text(
        buildString {
            append("En ")
            append(keyName)
            append(" · ")
            append(WheelModel.scaleLabel(state.scale))
            if (state.seventh) append(" · con séptimas")
        },
        fontSize = 12.sp, color = Wheel.TextSecondary,
        modifier = Modifier.padding(top = 6.dp, bottom = 10.dp, start = 2.dp),
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        progs.forEachIndexed { index, prog ->
            val open = state.openProg == index
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Wheel.Card, RoundedCornerShape(13.dp))
                    .border(1.5.dp, if (open) Wheel.BorderStrong else Wheel.BorderSoft, RoundedCornerShape(13.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleProgCard(index) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(prog.name, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Wheel.TextPrimary)
                    Text(
                        prog.degrees.joinToString("–") { field.designDeg(it) },
                        fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, color = Wheel.TextMuted,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "▶",
                        fontSize = 13.sp, color = palette.rest,
                        modifier = Modifier
                            .clickable { viewModel.playProgression(index) }
                            .semantics { contentDescription = "Escuchar ${prog.name}" }
                            .padding(4.dp),
                    )
                    Text(if (open) "▲" else "▼", fontSize = 11.sp, color = Wheel.TextMuted)
                }
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    for (d in prog.degrees) {
                        val degree = field.active[d]
                        val col = palette.of(WheelModel.FN7[d])
                        Column(
                            modifier = Modifier
                                .background(col.copy(alpha = 0.165f), RoundedCornerShape(7.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .widthIn(min = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(field.designDeg(d), fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = col)
                            Text(
                                wheelChordName(degree.chord.root.pitchClass, degree.chord.quality, flats, state.notation),
                                fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Wheel.TextPrimary,
                            )
                        }
                    }
                }
                Text(prog.desc, fontSize = 10.5.sp, color = Wheel.TextMuted, modifier = Modifier.padding(top = 7.dp))

                if (open) {
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 11.dp, bottom = 11.dp),
                        color = Wheel.Divider,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        for (d in prog.degrees) {
                            val degree = field.active[d]
                            val col = palette.of(WheelModel.FN7[d])
                            val name = wheelChordName(degree.chord.root.pitchClass, degree.chord.quality, flats, state.notation)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(11.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(col.copy(alpha = 0.15f), RoundedCornerShape(9.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        field.designDeg(d),
                                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace, color = col,
                                    )
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Wheel.TextPrimary)
                                    Text(
                                        degree.chord.notes.joinToString(" · ") {
                                            WheelModel.pcName(it.pitchClass, flats, state.notation)
                                        },
                                        fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Wheel.TextSecondary,
                                    )
                                }
                                ChordDiagram(
                                    shape = WheelVoicings.standardShape(degree.chord),
                                    color = col,
                                    widthDp = 84,
                                    modifier = Modifier.clickable {
                                        viewModel.playShape(WheelVoicings.standardShape(degree.chord))
                                    },
                                )
                            }
                        }
                        Text(
                            "Ver en el círculo →",
                            fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = palette.rest,
                            modifier = Modifier
                                .clickable { viewModel.projectProgression(index) }
                                .padding(vertical = 4.dp, horizontal = 2.dp),
                        )
                    }
                }
            }
        }
    }
    Text(
        "Los colores siguen la misma leyenda del círculo: reposo, movimiento y tensión.",
        fontSize = 10.5.sp, color = Wheel.TextMuted,
        modifier = Modifier.padding(top = 10.dp, start = 2.dp),
    )
}
