package com.alkapa.circuloquintas.ui.circle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alkapa.circuloquintas.content.PedagogicalContent
import com.alkapa.circuloquintas.domain.ChordQuality
import com.alkapa.circuloquintas.domain.CircleOfFifths
import com.alkapa.circuloquintas.domain.DefaultNoteFormatter
import com.alkapa.circuloquintas.domain.Degree
import com.alkapa.circuloquintas.domain.Notation
import com.alkapa.circuloquintas.domain.TonalFunction
import com.alkapa.circuloquintas.domain.ModalFunction
import com.alkapa.circuloquintas.domain.circlePositions
import com.alkapa.circuloquintas.domain.contiguousArcStart
import com.alkapa.circuloquintas.domain.repo.CircleLayer
import com.alkapa.circuloquintas.ui.theme.LocalFunctionColors
import kotlin.math.cos
import kotlin.math.sin

/** Celda del círculo: posición 0..11 + anillo (exterior/interior). */
private data class Cell(val position: Int, val outer: Boolean)

/**
 * Mapa de celdas diatónicas: acordes de familia mayor al anillo exterior,
 * familia menor (minúscula romana) al interior — la disposición clásica del
 * círculo (en C mayor: F-C-G afuera, Dm-Am-Em-B° adentro).
 */
private fun cellMap(field: List<Degree>): Map<Cell, Degree> {
    val result = mutableMapOf<Cell, Degree>()
    for (degree in field) {
        val pc = degree.chord.root.pitchClass
        val minorFamily = !degree.chord.quality.upperRoman
        val position = CircleOfFifths.sectors.first {
            val cellPc = if (minorFamily) it.minor.pitchClass else it.major.pitchClass
            cellPc == pc
        }.position
        result[Cell(position, outer = !minorFamily)] = degree
    }
    return result
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CircleOfFifthsCanvas(
    state: CircleUiState,
    field: List<Degree>,
    onCellTap: (position: Int, outer: Boolean) -> Unit,
    onCellLongPress: (position: Int, outer: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val functionColors = LocalFunctionColors.current
    val colorScheme = MaterialTheme.colorScheme
    val textMeasurer = rememberTextMeasurer()

    val cells = remember(field) { cellMap(field) }
    val scalePositions = remember(state.tonic, state.scale) { state.key.circlePositions() }
    val arcStart = remember(scalePositions) {
        contiguousArcStart(scalePositions, state.key.notes().size.coerceAtMost(7))
            .takeIf { state.key.notes().size == 7 }
    }
    val overlay = state.key.pentatonicOverlay()
    val tonicCell = cells.entries.firstOrNull { it.value.index == 1 }?.key

    BoxWithConstraints(modifier = modifier.aspectRatio(1f)) {
        val sizePx = with(LocalDensity.current) { maxWidth.toPx() }
        val radius = sizePx / 2f
        val outerMid = radius * 0.84f
        val innerMid = radius * 0.55f
        val outerThickness = radius * 0.30f
        val innerThickness = radius * 0.26f

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(radius, radius)

            fun cellColor(cell: Cell): Pair<Color, Boolean> {
                val degree = cells[cell]
                if (degree == null || Cell(cell.position, cell.outer) !in cells) {
                    return colorScheme.surfaceVariant.copy(alpha = 0.5f) to false
                }
                val dimmedByOverlay = overlay != null && degree.index !in overlay.includedDegrees
                val base = if (CircleLayer.FUNCTIONS in state.layers) {
                    when {
                        degree.modalFunction != null -> when (degree.modalFunction) {
                            ModalFunction.CENTER -> functionColors.tonic
                            ModalFunction.CHARACTERISTIC -> functionColors.modalColor
                            ModalFunction.NEUTRAL, null -> functionColors.neutral
                        }
                        degree.tonalFunction != null -> when (degree.tonalFunction) {
                            TonalFunction.TONIC -> functionColors.tonic
                            TonalFunction.SUBDOMINANT -> functionColors.subdominant
                            TonalFunction.DOMINANT -> functionColors.dominant
                            null -> functionColors.modalColor
                        }
                        else -> functionColors.modalColor // III+ / vi° puente: color/inestable
                    }
                } else {
                    colorScheme.primaryContainer
                }
                val alpha = when {
                    dimmedByOverlay -> 0.20f
                    degree.isAmbiguous -> 0.45f
                    else -> 0.90f
                }
                return base.copy(alpha = alpha) to degree.isAmbiguous
            }

            fun drawCellArc(cell: Cell) {
                val mid = if (cell.outer) outerMid else innerMid
                val thickness = if (cell.outer) outerThickness else innerThickness
                val startAngle = -90f + cell.position * 30f - 14f
                val rect = Rect(center - Offset(mid, mid), Size(mid * 2, mid * 2))
                val (color, ambiguous) = cellColor(cell)
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = 28f,
                    useCenter = false,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    style = Stroke(width = thickness),
                )
                if (ambiguous) {
                    // Regla §4: ambiguo = misma familia de color con opacidad
                    // reducida + borde punteado.
                    drawArc(
                        color = color.copy(alpha = 1f),
                        startAngle = startAngle,
                        sweepAngle = 28f,
                        useCenter = false,
                        topLeft = Offset(center.x - mid - thickness / 2, center.y - mid - thickness / 2),
                        size = Size((mid + thickness / 2) * 2, (mid + thickness / 2) * 2),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
                        ),
                    )
                }
            }

            for (position in 0 until 12) {
                drawCellArc(Cell(position, outer = true))
                drawCellArc(Cell(position, outer = false))
            }

            // Arco contiguo de quintas (mayor y modos, §6.2.1).
            if (arcStart != null) {
                val startAngle = -90f + arcStart * 30f - 15f
                val arcRadius = radius * 0.995f
                drawArc(
                    color = colorScheme.primary,
                    startAngle = startAngle,
                    sweepAngle = 7 * 30f,
                    useCenter = false,
                    topLeft = center - Offset(arcRadius, arcRadius),
                    size = Size(arcRadius * 2, arcRadius * 2),
                    style = Stroke(width = 3.dp.toPx()),
                )
            }

            // Marcador de tónica.
            if (tonicCell != null) {
                val mid = if (tonicCell.outer) outerMid else innerMid
                val thickness = if (tonicCell.outer) outerThickness else innerThickness
                val angle = Math.toRadians((-90 + tonicCell.position * 30).toDouble())
                val markerRadius = mid + thickness / 2 + 6.dp.toPx()
                val pos = center + Offset(
                    (cos(angle) * markerRadius).toFloat(),
                    (sin(angle) * markerRadius).toFloat(),
                )
                drawCircle(color = colorScheme.primary, radius = 5.dp.toPx(), center = pos)
            }

            // Resaltado del acorde activo (barra de escala o playback).
            val highlightDegree = state.highlightedDegreeIndex
                ?: cells.values.firstOrNull { it.chord.root.pitchClass == state.playingRootPc }?.index
            if (highlightDegree != null) {
                cells.entries.firstOrNull { it.value.index == highlightDegree }?.key?.let { cell ->
                    val mid = if (cell.outer) outerMid else innerMid
                    val thickness = if (cell.outer) outerThickness else innerThickness
                    drawArc(
                        color = colorScheme.tertiary,
                        startAngle = -90f + cell.position * 30f - 14f,
                        sweepAngle = 28f,
                        useCenter = false,
                        topLeft = center - Offset(mid, mid),
                        size = Size(mid * 2, mid * 2),
                        style = Stroke(width = thickness + 4.dp.toPx()),
                    )
                    drawCellArc(cell)
                }
            }

            // Etiquetas.
            fun labelAt(cell: Cell): String {
                val degree = cells[cell]
                if (degree != null) {
                    return DefaultNoteFormatter.format(degree.chord, state.notation)
                }
                val sector = CircleOfFifths.sectors[cell.position]
                return if (cell.outer) {
                    if (sector.majorAlt != null) {
                        "${DefaultNoteFormatter.format(sector.major, state.notation)}/" +
                            DefaultNoteFormatter.format(sector.majorAlt!!, state.notation)
                    } else {
                        DefaultNoteFormatter.format(sector.major, state.notation)
                    }
                } else {
                    val name = DefaultNoteFormatter.format(sector.minor, state.notation)
                    if (state.notation == Notation.AMERICAN) "${name}m" else "$name m"
                }
            }

            fun drawLabel(cell: Cell) {
                val degree = cells[cell]
                val mid = if (cell.outer) outerMid else innerMid
                val angle = Math.toRadians((-90 + cell.position * 30).toDouble())
                val pos = center + Offset((cos(angle) * mid).toFloat(), (sin(angle) * mid).toFloat())
                val label = labelAt(cell)
                val isDiatonic = degree != null
                val style = TextStyle(
                    color = if (isDiatonic) colorScheme.onSurface else colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = if (cell.outer) 13.sp else 11.sp,
                    fontWeight = if (isDiatonic) FontWeight.Bold else FontWeight.Normal,
                )
                val measured = textMeasurer.measure(label, style)
                var top = pos - Offset(measured.size.width / 2f, measured.size.height / 2f)
                val showRoman = degree != null && CircleLayer.DEGREES in state.layers
                if (showRoman) top -= Offset(0f, measured.size.height * 0.4f)
                drawText(textMeasurer, label, topLeft = top, style = style)
                if (showRoman) {
                    val romanStyle = TextStyle(
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                    )
                    val roman = textMeasurer.measure(degree!!.roman, romanStyle)
                    drawText(
                        textMeasurer, degree.roman,
                        topLeft = Offset(pos.x - roman.size.width / 2f, top.y + measured.size.height),
                        style = romanStyle,
                    )
                }
            }

            for (position in 0 until 12) {
                drawLabel(Cell(position, outer = true))
                drawLabel(Cell(position, outer = false))
            }
        }

        // Objetivos táctiles accesibles por celda (≥48dp), superpuestos al canvas.
        val density = LocalDensity.current
        val touchTarget = 48.dp
        for (position in 0 until 12) {
            for (outer in listOf(true, false)) {
                val mid = if (outer) outerMid else innerMid
                val angle = Math.toRadians((-90 + position * 30).toDouble())
                val cx = radius + cos(angle).toFloat() * mid
                val cy = radius + sin(angle).toFloat() * mid
                val half = with(density) { touchTarget.toPx() / 2f }
                val degree = cells[Cell(position, outer)]
                val sector = CircleOfFifths.sectors[position]
                val description = if (degree != null) {
                    val functionLabel = if (degree.modalFunction != null) {
                        PedagogicalContent.modalFunctionLabel(degree.modalFunction!!)
                    } else {
                        PedagogicalContent.tonalFunctionLabel(degree.tonalFunction)
                    }
                    "Acorde ${DefaultNoteFormatter.format(degree.chord, state.notation)}, " +
                        "grado ${degree.roman}, $functionLabel" +
                        if (degree.isAmbiguous) ", función ambigua" else ""
                } else if (outer) {
                    "Elegir tonalidad ${DefaultNoteFormatter.format(sector.major, state.notation)} mayor"
                } else {
                    "Elegir tonalidad ${DefaultNoteFormatter.format(sector.minor, state.notation)} menor"
                }
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset((cx - half).toInt(), (cy - half).toInt())
                        }
                        .size(touchTarget)
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClickLabel = description,
                            onClick = { onCellTap(position, outer) },
                            onLongClick = { onCellLongPress(position, outer) },
                        ),
                )
            }
        }
    }
}
