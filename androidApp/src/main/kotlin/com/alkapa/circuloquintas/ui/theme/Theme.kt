package com.alkapa.circuloquintas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tokens del refactor «2a — Claridad+» (Refactor Círculo.dc.html).
 * El diseño es oscuro-primero: la app usa siempre esta paleta.
 */
object Wheel {
    val Bg = Color(0xFF0F1319)
    val BgDeep = Color(0xFF0D1117)
    val Hub = Color(0xFF151A23)
    val Card = Color(0xFF171C26)
    val CardAlt = Color(0xFF1A202B)
    val Surface = Color(0xFF10141B)
    val SurfaceAlt = Color(0xFF151B24)
    val Border = Color(0xFF2A3140)
    val BorderSoft = Color(0xFF262D3A)
    val BorderStrong = Color(0xFF3A4356)
    val Divider = Color(0xFF232A38)
    val NavBorder = Color(0xFF1E2430)
    val TextPrimary = Color(0xFFE8ECF4)
    val TextSecondary = Color(0xFF9AA5B8)
    val TextMuted = Color(0xFF5E6980)
    val OnAccent = Color(0xFF0E1116)
    val KeyWhite = Color(0xFFE9EDF4)
    val KeyBlack = Color(0xFF0B0E13)
}

/** Paleta de las 3 funciones (Reposo / Movimiento / Tensión). */
@Immutable
data class FnPalette(val rest: Color, val motion: Color, val tension: Color)

/** Las tres paletas del ajuste «Paleta» del diseño. */
val FN_PALETTES = listOf(
    "Verde · Ámbar · Coral" to FnPalette(Color(0xFF4EC9A4), Color(0xFFE0B04F), Color(0xFFE2705C)),
    "Azul · Violeta · Rosa" to FnPalette(Color(0xFF5FA8EE), Color(0xFF9D8CF0), Color(0xFFEE6D9A)),
    "Cian · Arena · Fuego" to FnPalette(Color(0xFF53C2CE), Color(0xFFD8B26A), Color(0xFFE56545)),
)

val LocalFnPalette = staticCompositionLocalOf { FN_PALETTES[0].second }

private fun schemeFor(p: FnPalette) = darkColorScheme(
    primary = p.rest,
    onPrimary = Wheel.OnAccent,
    primaryContainer = Wheel.CardAlt,
    onPrimaryContainer = Wheel.TextPrimary,
    secondary = p.motion,
    onSecondary = Wheel.OnAccent,
    secondaryContainer = Wheel.CardAlt,
    onSecondaryContainer = Wheel.TextPrimary,
    tertiary = p.tension,
    onTertiary = Wheel.OnAccent,
    error = p.tension,
    background = Wheel.Bg,
    onBackground = Wheel.TextPrimary,
    surface = Wheel.Bg,
    onSurface = Wheel.TextPrimary,
    surfaceVariant = Wheel.Card,
    onSurfaceVariant = Wheel.TextSecondary,
    outline = Wheel.Border,
    outlineVariant = Wheel.BorderSoft,
)

@Composable
fun CirculoQuintasTheme(
    paletteIndex: Int = 0,
    content: @Composable () -> Unit,
) {
    val palette = FN_PALETTES.getOrElse(paletteIndex) { FN_PALETTES[0] }.second
    CompositionLocalProvider(LocalFnPalette provides palette) {
        MaterialTheme(
            colorScheme = schemeFor(palette),
            content = content,
        )
    }
}
