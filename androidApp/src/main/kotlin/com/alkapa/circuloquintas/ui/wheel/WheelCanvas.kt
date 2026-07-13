package com.alkapa.circuloquintas.ui.wheel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import com.alkapa.circuloquintas.domain.Notation
import com.alkapa.circuloquintas.domain.wheel.WheelModel
import com.alkapa.circuloquintas.ui.theme.LocalFnPalette
import com.alkapa.circuloquintas.ui.theme.Wheel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Cuña resuelta de la rueda (datos listos para dibujar). */
private data class WedgeInfo(
    val ring: WheelModel.Ring,
    val position: Int,
    val degreeIndex: Int?,   // null = no diatónica
    val name: String,
    val deg: String,
    val fn: WheelModel.Fn?,
)

/**
 * Donut del refactor 2a: anillo exterior (mayores por quintas), interior
 * (relativas menores), cuñas chicas para disminuidos y centro con la
 * tonalidad. Geometría y colores portados del diseño (viewBox 344).
 */
@Composable
fun WheelCanvas(
    field: WheelModel.WheelField,
    flats: Boolean,
    sel: Int,
    progDegrees: Set<Int>?,
    showDegrees: Boolean,
    keyShort: String,
    scaleLabel: String,
    notation: Notation,
    onTapDegree: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalFnPalette.current
    val textMeasurer = rememberTextMeasurer()

    // Colocación de los 7 grados en la rueda (los aumentados no se colocan).
    val placements = remember(field, flats, notation) {
        val map = mutableMapOf<Pair<WheelModel.Ring, Int>, Pair<Int, String>>()
        field.active.forEachIndexed { i, degree ->
            WheelModel.placement(degree.chord)?.let { p ->
                val name = wheelChordName(degree.chord.root.pitchClass, degree.chord.quality, flats, notation)
                map[p.ring to p.position] = i to name
            }
        }
        map
    }

    val wedges = remember(placements, showDegrees) {
        val list = mutableListOf<WedgeInfo>()
        for (ring in listOf(WheelModel.Ring.OUTER, WheelModel.Ring.INNER, WheelModel.Ring.DIM)) {
            for (p in 0 until 12) {
                val hit = placements[ring to p]
                if (ring == WheelModel.Ring.DIM && hit == null) continue
                val pc = WheelModel.FIFTHS[p]
                val fallback = when (ring) {
                    WheelModel.Ring.OUTER -> WheelModel.pcName(pc, p > 5, notation)
                    else -> WheelModel.pcName((pc + 9) % 12, p > 5, notation) + "m"
                }
                list += WedgeInfo(
                    ring = ring,
                    position = p,
                    degreeIndex = hit?.first,
                    name = hit?.second ?: fallback,
                    deg = if (hit != null && showDegrees) field.designDeg(hit.first) else "",
                    fn = hit?.let { WheelModel.FN7[it.first] },
                )
            }
        }
        list
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .semantics {
                contentDescription = "Círculo de quintas: $keyShort $scaleLabel. " +
                    "Usa la fila de acordes para recorrer los grados."
            }
            .pointerInput(placements) {
                detectTapGestures { offset ->
                    val scale = size.width / 344f
                    val dx = (offset.x - size.width / 2f) / scale
                    val dy = (offset.y - size.height / 2f) / scale
                    val r = sqrt(dx * dx + dy * dy)
                    val ring = when {
                        r in 124f..170f -> WheelModel.Ring.OUTER
                        r in 78f..122f -> WheelModel.Ring.INNER
                        r in 56f..76f -> WheelModel.Ring.DIM
                        else -> null
                    } ?: return@detectTapGestures
                    val deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                    val pos = (((deg + 105.0).mod(360.0)) / 30.0).toInt().mod(12)
                    placements[ring to pos]?.let { onTapDegree(it.first) }
                }
            },
    ) {
        val scale = size.width / 344f
        val center = Offset(size.width / 2f, size.height / 2f)

        fun sectorPath(r0: Float, r1: Float, i: Int): Path {
            val start = -105f + 30f * i
            val outer = Rect(center - Offset(r1 * scale, r1 * scale), Size(r1 * 2 * scale, r1 * 2 * scale))
            val inner = Rect(center - Offset(r0 * scale, r0 * scale), Size(r0 * 2 * scale, r0 * 2 * scale))
            return Path().apply {
                arcTo(outer, start, 30f, forceMoveTo = true)
                val endRad = Math.toRadians((start + 30f).toDouble())
                lineTo(
                    center.x + r0 * scale * cos(endRad).toFloat(),
                    center.y + r0 * scale * sin(endRad).toFloat(),
                )
                arcTo(inner, start + 30f, -30f, forceMoveTo = false)
                close()
            }
        }

        fun ringRadii(ring: WheelModel.Ring): Pair<Float, Float> = when (ring) {
            WheelModel.Ring.OUTER -> 124f to 170f
            WheelModel.Ring.INNER -> 78f to 122f
            WheelModel.Ring.DIM -> 56f to 76f
        }

        // Cuñas.
        for (w in wedges) {
            val (r0, r1) = ringRadii(w.ring)
            val selWedge = w.degreeIndex == sel
            val dimmed = progDegrees != null && w.degreeIndex != null && w.degreeIndex !in progDegrees
            val fill = when {
                w.fn == null -> Wheel.CardAlt
                selWedge -> palette.of(w.fn)
                else -> palette.of(w.fn).copy(alpha = 0.19f)
            }
            val alpha = if (dimmed) 0.32f else 1f
            val path = sectorPath(r0, r1, w.position)
            drawPath(path, fill, alpha = alpha)
            drawPath(path, Wheel.Bg, style = Stroke(width = 2.5f * scale))
        }

        // Centro.
        drawCircle(Wheel.Hub, radius = 53f * scale, center = center)
        drawCircle(Wheel.Border, radius = 53f * scale, center = center, style = Stroke(1.5f * scale))

        // Etiquetas de cuñas.
        for (w in wedges) {
            val rMid = when (w.ring) {
                WheelModel.Ring.OUTER -> 147f
                WheelModel.Ring.INNER -> 99.5f
                WheelModel.Ring.DIM -> 66f
            }
            val angle = Math.toRadians((-90 + 30 * w.position).toDouble())
            val pos = center + Offset(
                (rMid * scale * cos(angle)).toFloat(),
                (rMid * scale * sin(angle)).toFloat(),
            )
            val selWedge = w.degreeIndex == sel
            val dimmed = progDegrees != null && w.degreeIndex != null && w.degreeIndex !in progDegrees
            val nameColor = when {
                w.fn == null -> Wheel.TextMuted
                selWedge -> Wheel.OnAccent
                else -> Wheel.TextPrimary
            }
            val long = w.name.length > 4
            val basePx = when (w.ring) {
                WheelModel.Ring.OUTER -> if (long) 12.5f else 16f
                WheelModel.Ring.INNER -> if (long) 10.5f else 13.5f
                WheelModel.Ring.DIM -> 10f
            }
            val nameStyle = TextStyle(
                color = nameColor,
                fontSize = with(this) { (basePx * scale).toSp() },
                fontWeight = FontWeight.SemiBold,
            )
            val name = textMeasurer.measure(w.name, nameStyle)
            val showDeg = w.deg.isNotEmpty() && w.ring != WheelModel.Ring.DIM
            var top = pos - Offset(name.size.width / 2f, name.size.height / 2f)
            if (showDeg) top -= Offset(0f, name.size.height * 0.32f)
            drawText(name, topLeft = top, alpha = if (dimmed) 0.32f else 1f)
            if (showDeg) {
                val degColor = if (selWedge) Wheel.OnAccent.copy(alpha = 0.72f) else palette.of(w.fn!!)
                val degStyle = TextStyle(
                    color = degColor,
                    fontSize = with(this) { ((if (w.ring == WheelModel.Ring.OUTER) 10.5f else 9.5f) * scale).toSp() },
                    fontFamily = FontFamily.Monospace,
                )
                val deg = textMeasurer.measure(w.deg, degStyle)
                drawText(
                    deg,
                    topLeft = Offset(pos.x - deg.size.width / 2f, top.y + name.size.height),
                    alpha = if (dimmed) 0.32f else 1f,
                )
            }
        }

        // Texto del centro.
        val keyStyle = TextStyle(
            color = Wheel.TextPrimary,
            fontSize = with(this) { (21f * scale).toSp() },
            fontWeight = FontWeight.Bold,
        )
        val key = textMeasurer.measure(keyShort, keyStyle)
        drawText(textMeasurer, keyShort, topLeft = center - Offset(key.size.width / 2f, key.size.height * 0.95f), style = keyStyle)
        val subStyle = TextStyle(color = Wheel.TextSecondary, fontSize = with(this) { (11f * scale).toSp() })
        val sub = textMeasurer.measure(scaleLabel, subStyle)
        drawText(textMeasurer, scaleLabel, topLeft = Offset(center.x - sub.size.width / 2f, center.y + key.size.height * 0.08f), style = subStyle)
        val hintStyle = TextStyle(color = Wheel.TextMuted, fontSize = with(this) { (9f * scale).toSp() })
        val hint = textMeasurer.measure("toca un acorde", hintStyle)
        drawText(textMeasurer, "toca un acorde", topLeft = Offset(center.x - hint.size.width / 2f, center.y + key.size.height * 0.08f + sub.size.height + 2f * scale), style = hintStyle)
    }
}
