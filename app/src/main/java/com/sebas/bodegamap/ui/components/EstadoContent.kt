package com.sebas.bodegamap.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sebas.bodegamap.ui.UiState

/**
 * Renderiza los 4 estados de un UiState de forma consistente en toda la app:
 * Idle -> mensaje guía, Loading -> spinner centrado, Error -> mensaje con
 * botón "Reintentar", Success -> contenido o mensaje de "sin resultados" si
 * la lista viene vacía. Evita repetir el mismo when() en cada pantalla.
 */
@Composable
fun <T> EstadoContent(
    estado: UiState<T>,
    mensajeIdle: String,
    mensajeVacio: String,
    onReintentar: () -> Unit,
    estaVacio: (T) -> Boolean,
    contenido: @Composable (T) -> Unit
) {
    when (estado) {
        is UiState.Idle -> MensajeCentrado(mensajeIdle)
        is UiState.Loading -> CargaCentrada()
        is UiState.Error -> ErrorConReintento(estado.message, onReintentar)
        is UiState.Success -> {
            if (estaVacio(estado.data)) MensajeCentrado(mensajeVacio)
            else contenido(estado.data)
        }
    }
}

@Composable
private fun MensajeCentrado(texto: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = texto, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun CargaCentrada() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorConReintento(mensaje: String, onReintentar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = mensaje, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onReintentar) {
            Text("Reintentar")
        }
    }
}
