package com.alkapa.circuloquintas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tokens de color de función armónica (§4/§5):
 * functionTonic (verde), functionSubdominant (ámbar), functionDominant (rojo),
 * functionModalColor (violeta, lente modal) + neutro.
 */
@Immutable
data class FunctionColors(
    val tonic: Color,
    val subdominant: Color,
    val dominant: Color,
    val modalColor: Color,
    val neutral: Color,
)

val LocalFunctionColors = staticCompositionLocalOf {
    FunctionColors(
        tonic = Color(0xFF2E7D32),
        subdominant = Color(0xFFF9A825),
        dominant = Color(0xFFC62828),
        modalColor = Color(0xFF6A1B9A),
        neutral = Color(0xFF757575),
    )
}

private val LightFunctionColors = FunctionColors(
    tonic = Color(0xFF2E7D32),
    subdominant = Color(0xFFF9A825),
    dominant = Color(0xFFC62828),
    modalColor = Color(0xFF6A1B9A),
    neutral = Color(0xFF757575),
)

private val DarkFunctionColors = FunctionColors(
    tonic = Color(0xFF81C784),
    subdominant = Color(0xFFFFD54F),
    dominant = Color(0xFFE57373),
    modalColor = Color(0xFFCE93D8),
    neutral = Color(0xFFBDBDBD),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1B5E20),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA5D6A7),
    onPrimaryContainer = Color(0xFF0A2E0D),
    secondary = Color(0xFF5D4037),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7CCC8),
    onSecondaryContainer = Color(0xFF2D1B16),
    surface = Color(0xFFFBFDF7),
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFE0E4DB),
    onSurfaceVariant = Color(0xFF43483F),
    background = Color(0xFFFBFDF7),
    onBackground = Color(0xFF1A1C19),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF0A2E0D),
    primaryContainer = Color(0xFF2E5231),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFFBCAAA4),
    onSecondary = Color(0xFF2D1B16),
    secondaryContainer = Color(0xFF4E342E),
    onSecondaryContainer = Color(0xFFD7CCC8),
    surface = Color(0xFF121412),
    onSurface = Color(0xFFE2E3DD),
    surfaceVariant = Color(0xFF43483F),
    onSurfaceVariant = Color(0xFFC3C8BC),
    background = Color(0xFF121412),
    onBackground = Color(0xFFE2E3DD),
)

@Composable
fun CirculoQuintasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val functionColors = if (darkTheme) DarkFunctionColors else LightFunctionColors
    CompositionLocalProvider(LocalFunctionColors provides functionColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            content = content,
        )
    }
}
