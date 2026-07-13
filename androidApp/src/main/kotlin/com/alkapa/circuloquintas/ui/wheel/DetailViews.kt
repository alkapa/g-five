package com.alkapa.circuloquintas.ui.wheel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.alkapa.circuloquintas.domain.wheel.WheelModel
import com.alkapa.circuloquintas.domain.wheel.WheelVoicings
import com.alkapa.circuloquintas.ui.theme.Wheel

/** Rejilla del diagrama vertical (viewBox 152×182 del diseño). */
private fun DrawScope.chordGrid(scale: Float, nutVisible: Boolean) {
    val xs = listOf(21f, 43f, 65f, 87f, 109f, 131f)
    if (nutVisible) {
        drawRoundRect(
            Wheel.TextSecondary,
            topLeft = Offset(19.5f * scale, 26.5f * scale),
            size = Size(113f * scale, 4.5f * scale),
            cornerRadius = CornerRadius(2f * scale),
        )
    }
    for (x in xs) {
        drawLine(Wheel.BorderStrong, Offset(x * scale, 30f * scale), Offset(x * scale, 170f * scale), strokeWidth = scale)
    }
    for (row in 0..5) {
        val y = (30f + row * 28f) * scale
        drawLine(if (row == 0) Wheel.BorderStrong else Wheel.Border, Offset(21f * scale, y), Offset(131f * scale, y), strokeWidth = scale)
    }
}

private val STRING_X = listOf(21f, 43f, 65f, 87f, 109f, 131f)

/** Diagrama estándar del acorde (posición fundamental): puntos sin etiqueta. */
@Composable
fun ChordDiagram(
    shape: WheelVoicings.Shape,
    color: Color,
    modifier: Modifier = Modifier,
    widthDp: Int = 150,
) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = modifier
            .width(widthDp.dp)
            .height((widthDp * 182f / 152f).dp),
    ) {
        val scale = size.width / 152f
        val base = shape.baseFret
        chordGrid(scale, nutVisible = base == 1)
        shape.frets.forEachIndexed { string, fret ->
            when {
                fret < 0 -> drawMute(textMeasurer, STRING_X[string] * scale, 20f * scale)
                fret == 0 -> drawCircle(
                    Wheel.TextSecondary, 4f * scale, Offset(STRING_X[string] * scale, 18f * scale),
                    style = Stroke(1.5f * scale),
                )
                else -> drawCircle(
                    color, 8f * scale,
                    Offset(STRING_X[string] * scale, (30f + (fret - base + 0.5f) * 28f) * scale),
                )
            }
        }
        if (base > 1) {
            drawBaseLabel(textMeasurer, base, scale)
        }
    }
}

/** Diagrama de voicing por grupo de cuerdas: puntos etiquetados + bajo anillado. */
@Composable
fun VoicingDiagram(
    voicing: WheelVoicings.GroupVoicing,
    quality: com.alkapa.circuloquintas.domain.ChordQuality,
    rootPc: Int,
    color: Color,
    modifier: Modifier = Modifier,
    widthDp: Int = 150,
) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = modifier
            .width(widthDp.dp)
            .height((widthDp * 182f / 152f).dp),
    ) {
        val scale = size.width / 152f
        val fretted = voicing.frets.filter { it > 0 }
        val maxF = fretted.maxOrNull() ?: 1
        val minF = fretted.minOrNull() ?: 1
        val base = if (maxF <= 5) 1 else minF
        chordGrid(scale, nutVisible = base == 1)
        // Cuerdas mudas (las fuera del grupo).
        for (s in 0..5) {
            if (s !in voicing.strings) drawMute(textMeasurer, STRING_X[s] * scale, 20f * scale)
        }
        voicing.strings.forEachIndexed { i, s ->
            val fret = voicing.frets[i]
            val pc = voicing.orderPcs[i]
            val offset = (pc - rootPc).mod(12)
            val label = WheelModel.intervalGlyph(offset)
            val center = if (fret == 0) {
                Offset(STRING_X[s] * scale, 18f * scale)
            } else {
                Offset(STRING_X[s] * scale, (30f + (fret - base + 0.5f) * 28f) * scale)
            }
            drawCircle(if (fret == 0) Wheel.Card else color, 8.5f * scale, center)
            drawCircle(color, 8.5f * scale, center, style = Stroke(1.5f * scale))
            if (i == 0) {
                drawCircle(Wheel.TextPrimary, 10.5f * scale, center, style = Stroke(2f * scale))
            }
            val style = TextStyle(
                color = if (fret == 0) color else Wheel.OnAccent,
                fontSize = with(this) { ((if (label.length > 1) 8f else 9.5f) * scale).toSp() },
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
            val m = textMeasurer.measure(label, style)
            drawText(textMeasurer, label, topLeft = center - Offset(m.size.width / 2f, m.size.height / 2f), style = style)
        }
        if (base > 1) drawBaseLabel(textMeasurer, base, scale)
    }
}

private fun DrawScope.drawMute(textMeasurer: TextMeasurer, x: Float, y: Float) {
    val style = TextStyle(color = Wheel.TextMuted, fontSize = with(this) { (12f * (size.width / 152f)).toSp() })
    val m = textMeasurer.measure("×", style)
    drawText(textMeasurer, "×", topLeft = Offset(x - m.size.width / 2f, y - m.size.height / 2f), style = style)
}

private fun DrawScope.drawBaseLabel(textMeasurer: TextMeasurer, base: Int, scale: Float) {
    val style = TextStyle(
        color = Wheel.TextSecondary,
        fontSize = with(this) { (10f * scale).toSp() },
        fontFamily = FontFamily.Monospace,
    )
    drawText(textMeasurer, "${base}fr", topLeft = Offset(0f, 44f * scale), style = style)
}

/** Teclado de dos octavas (14 blancas) con las voces marcadas. */
@Composable
fun PianoKeys(
    absSemitones: List<Int>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    // Ancho fluido con tope: en pantallas angostas se encoge (el dibujo escala
    // con size.width) en vez de desbordar la tarjeta.
    Canvas(
        modifier = modifier
            .widthIn(max = 310.dp)
            .fillMaxWidth()
            .aspectRatio(310f / 90f),
    ) {
        val scale = size.width / 310f
        val whitePcs = listOf(0, 2, 4, 5, 7, 9, 11)
        val blacks = listOf(1 to 0, 3 to 1, 6 to 3, 8 to 4, 10 to 5)
        for (wi in 0 until 14) {
            val abs = (wi / 7) * 12 + whitePcs[wi % 7]
            drawRoundRect(
                if (abs in absSemitones) color else Wheel.KeyWhite,
                topLeft = Offset((1f + wi * 22f) * scale, scale),
                size = Size(21f * scale, 86f * scale),
                cornerRadius = CornerRadius(2.5f * scale),
            )
            drawRoundRect(
                Wheel.Surface,
                topLeft = Offset((1f + wi * 22f) * scale, scale),
                size = Size(21f * scale, 86f * scale),
                cornerRadius = CornerRadius(2.5f * scale),
                style = Stroke(scale),
            )
        }
        for (oct in 0 until 2) {
            for ((pc, w) in blacks) {
                val abs = oct * 12 + pc
                drawRoundRect(
                    if (abs in absSemitones) color else Wheel.KeyBlack,
                    topLeft = Offset((1f + (oct * 7 + w) * 22f + 15.5f) * scale, scale),
                    size = Size(13f * scale, 52f * scale),
                    cornerRadius = CornerRadius(2f * scale),
                )
            }
        }
    }
}

/**
 * Diapasón completo (12 trastes): todas las notas del acorde, con la
 * digitación activa sólida, el resto tenue y el bajo anillado.
 */
@Composable
fun FretboardFull(
    chordPcs: List<Int>,
    chordOffsets: List<Int>,
    voicingKeys: Set<String>,
    bassKey: String?,
    rootPc: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    // Igual que el piano: fluido hasta 330dp para no cortar los trastes altos.
    Canvas(
        modifier = modifier
            .widthIn(max = 330.dp)
            .fillMaxWidth()
            .aspectRatio(330f / 132f),
    ) {
        val scale = size.width / 330f
        val tuningPc = listOf(4, 9, 2, 7, 11, 4)
        // Cejuela.
        drawRoundRect(
            Wheel.TextMuted,
            topLeft = Offset(21f * scale, 11f * scale),
            size = Size(3f * scale, 92f * scale),
            cornerRadius = CornerRadius(1.5f * scale),
        )
        for (f in 1..12) {
            val x = (22f + f * 24f) * scale
            drawLine(Wheel.Border, Offset(x, 12f * scale), Offset(x, 102f * scale), strokeWidth = scale)
        }
        for (s in 0..5) {
            val y = (12f + (5 - s) * 18f) * scale
            drawLine(Wheel.BorderStrong, Offset(22f * scale, y), Offset(310f * scale, y), strokeWidth = (1f + (5 - s) * 0.08f) * scale)
        }
        // Números de traste.
        for (f in listOf(3, 5, 7, 9, 12)) {
            val style = TextStyle(
                color = Wheel.TextMuted,
                fontSize = with(this) { (8.5f * scale).toSp() },
                fontFamily = FontFamily.Monospace,
            )
            val m = textMeasurer.measure(f.toString(), style)
            drawText(
                textMeasurer, f.toString(),
                topLeft = Offset((22f + (f - 0.5f) * 24f) * scale - m.size.width / 2f, 108f * scale),
                style = style,
            )
        }
        // Notas del acorde en todo el mástil.
        for (s in 0..5) {
            val y = (12f + (5 - s) * 18f) * scale
            for (f in 0..12) {
                val pc = (tuningPc[s] + f) % 12
                val ix = chordPcs.indexOf(pc)
                if (ix < 0) continue
                val x = if (f == 0) 10f * scale else (22f + (f - 0.5f) * 24f) * scale
                val key = "$s:$f"
                val inVoicing = key in voicingKeys
                val isRoot = pc == rootPc
                val bg = if (inVoicing || isRoot) color else Wheel.CardAlt
                val alpha = if (inVoicing) 1f else 0.34f
                val center = Offset(x, y)
                drawCircle(bg, 7f * scale, center, alpha = alpha)
                if (!inVoicing && !isRoot) {
                    drawCircle(color.copy(alpha = 0.53f), 7f * scale, center, style = Stroke(scale), alpha = alpha)
                }
                if (bassKey == key) {
                    drawCircle(Wheel.TextPrimary, 8.5f * scale, center, style = Stroke(1.6f * scale))
                }
                val label = WheelModel.intervalGlyph(chordOffsets[ix])
                val style = TextStyle(
                    color = if (inVoicing || isRoot) Wheel.OnAccent else color,
                    fontSize = with(this) { ((if (label.length > 1) 6.5f else 8f) * scale).toSp() },
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                )
                val m = textMeasurer.measure(label, style)
                drawText(
                    m,
                    topLeft = center - Offset(m.size.width / 2f, m.size.height / 2f),
                    alpha = alpha,
                )
            }
        }
    }
}
