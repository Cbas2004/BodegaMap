package com.sebas.bodegamap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sebas.bodegamap.ui.navigation.AppNavigation
import com.sebas.bodegamap.ui.theme.BodegaMapTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Debe llamarse ANTES de super.onCreate() (contrato de la librería):
        // así el splash cubre el arranque en frío hasta el primer frame de
        // Compose, en vez de mostrar una pantalla en blanco intermedia.
        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            BodegaMapTheme {
                // Theme.BodegaMap (XML) hereda de Theme.Material.Light, con
                // fondo de ventana blanco fijo, sin relación con el esquema
                // de color de Compose (claro/oscuro). Sin este Surface, el
                // espacio entre composables (fuera de cards/texto) deja ver
                // ese blanco crudo del sistema en vez del fondo del tema.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
