package com.alkapa.circuloquintas.ui.wheel

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alkapa.circuloquintas.domain.ChordQuality
import com.alkapa.circuloquintas.domain.Notation
import com.alkapa.circuloquintas.domain.wheel.WheelModel
import com.alkapa.circuloquintas.ui.theme.FnPalette
import com.alkapa.circuloquintas.ui.theme.LocalFnPalette

/** Color de una función del diseño en la paleta activa. */
fun FnPalette.of(fn: WheelModel.Fn): Color = when (fn) {
    WheelModel.Fn.T -> rest
    WheelModel.Fn.S -> motion
    WheelModel.Fn.D -> tension
}

/** Etiquetas de la leyenda (coloquial + formal + descripción, texto del diseño). */
fun fnLabel(fn: WheelModel.Fn): String = when (fn) {
    WheelModel.Fn.T -> "Reposo"
    WheelModel.Fn.S -> "Movimiento"
    WheelModel.Fn.D -> "Tensión"
}

fun fnLong(fn: WheelModel.Fn): String = when (fn) {
    WheelModel.Fn.T -> "Tónica"
    WheelModel.Fn.S -> "Subdominante"
    WheelModel.Fn.D -> "Dominante"
}

fun fnDesc(fn: WheelModel.Fn): String = when (fn) {
    WheelModel.Fn.T -> "Suena a llegada y descanso. Empieza y termina aquí tus frases."
    WheelModel.Fn.S -> "Se aleja de casa y prepara lo que viene después."
    WheelModel.Fn.D -> "Pide resolver. Suéltala volviendo a un acorde de reposo."
}

@Composable
fun fnColor(fn: WheelModel.Fn): Color = LocalFnPalette.current.of(fn)

/** Nombre de acorde del diseño: raíz por regla de bemoles + sufijo del diseño. */
fun wheelChordName(rootPc: Int, quality: ChordQuality, flats: Boolean, notation: Notation): String =
    WheelModel.pcName(rootPc, flats, notation) + WheelModel.designSuffix(quality)
