package com.alkapa.circuloquintas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.alkapa.circuloquintas.ui.AppRoot

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as CirculoApp).container
        setContent {
            AppRoot(container)
        }
    }
}
