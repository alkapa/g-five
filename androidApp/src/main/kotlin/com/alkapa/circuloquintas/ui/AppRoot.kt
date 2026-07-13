package com.alkapa.circuloquintas.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.alkapa.circuloquintas.AppContainer
import com.alkapa.circuloquintas.ui.theme.CirculoQuintasTheme
import com.alkapa.circuloquintas.ui.theme.Wheel
import com.alkapa.circuloquintas.ui.wheel.WheelScreen
import com.alkapa.circuloquintas.ui.wheel.WheelViewModel

@Composable
fun AppRoot(container: AppContainer) {
    val viewModel: WheelViewModel = viewModel(factory = viewModelFactory {
        initializer { WheelViewModel(createSavedStateHandle(), container) }
    })
    val state by viewModel.state.collectAsState()

    CirculoQuintasTheme(paletteIndex = state.palette) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Wheel.Bg),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                WheelScreen(viewModel)
            }
            BottomNav(tab = state.tab, onTab = viewModel::goTab)
        }
    }
}

/** Navegación inferior del diseño: dos destinos con indicador tipo pill. */
@Composable
private fun BottomNav(tab: Int, onTab: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Wheel.NavBorder),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Wheel.BgDeep)
                .padding(top = 7.dp, bottom = 9.dp),
        ) {
            NavItem(
                label = "Círculo",
                selected = tab == 0,
                onClick = { onTab(0) },
                modifier = Modifier.weight(1f),
            ) { color ->
                Canvas(Modifier.size(20.dp)) {
                    drawCircle(color, radius = size.width * 0.36f, style = Stroke(2.dp.toPx()))
                    drawCircle(color, radius = size.width * 0.125f)
                }
            }
            NavItem(
                label = "Progresiones",
                selected = tab == 1,
                onClick = { onTab(1) },
                modifier = Modifier.weight(1f),
            ) { color ->
                Canvas(Modifier.size(20.dp)) {
                    val widths = listOf(1f, 0.69f, 0.875f)
                    widths.forEachIndexed { i, w ->
                        drawRoundRect(
                            color,
                            topLeft = Offset(size.width * 0.17f, size.height * (0.21f + i * 0.23f)),
                            size = Size(size.width * 0.66f * w, size.height * 0.125f),
                            cornerRadius = CornerRadius(size.height * 0.0625f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (Color) -> Unit,
) {
    val color = if (selected) Wheel.TextPrimary else Wheel.TextMuted
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .semantics { contentDescription = label }
            .padding(top = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(30.dp)
                .background(if (selected) Wheel.Border else Color.Transparent, RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center,
        ) {
            icon(color)
        }
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color, modifier = Modifier.padding(top = 3.dp))
    }
}
