package com.sebas.bodegamap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sebas.bodegamap.data.ProductoDisponibilidadDTO
import com.sebas.bodegamap.ui.components.EstadoContent
import com.sebas.bodegamap.util.EstadoHorario
import com.sebas.bodegamap.util.GeoUtils
import com.sebas.bodegamap.util.HorarioUtils
import com.sebas.bodegamap.viewmodel.BodegaViewModel

@Composable
fun ListaBodegasScreen(
    idProducto: Long,
    viewModel: BodegaViewModel,
    onBack: () -> Unit
) {
    // Al entrar a la pantalla, dispara la carga. El resultado queda en el VM
    // compartido, así MapScreen lo reutiliza al regresar.
    LaunchedEffect(idProducto) {
        viewModel.cargarDisponibilidad(idProducto)
    }

    val estado by viewModel.disponibilidad.collectAsState()
    // Reutilizamos la ubicación del usuario del VM compartido para calcular
    // distancia por bodega. Si es null (sin permiso), no mostramos distancia.
    val ubicacionUsuario by viewModel.ubicacionUsuario.collectAsState()

    EstadoContent(
        estado = estado,
        mensajeIdle = "Selecciona un producto.",
        mensajeVacio = "Ninguna bodega tiene este producto disponible.",
        onReintentar = { viewModel.cargarDisponibilidad(idProducto) },
        estaVacio = { it.isEmpty() }
    ) { bodegas ->
        ListaBodegasContenido(
            bodegas = bodegas,
            ubicacionUsuario = ubicacionUsuario,
            onSeleccionar = { bodega ->
                // KEY POINT: aquí vive la regla de arquitectura.
                // Guardamos la bodega en el VM y volvemos al mapa.
                // MapScreen centra y abre BottomSheet sin llamar al backend,
                // porque el objeto ya trae lat/long/precio/horario/direccion.
                viewModel.seleccionarBodega(bodega)
                onBack()
            }
        )
    }
}

@Composable
private fun ListaBodegasContenido(
    bodegas: List<ProductoDisponibilidadDTO>,
    ubicacionUsuario: Pair<Double, Double>?,
    onSeleccionar: (ProductoDisponibilidadDTO) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(bodegas) { index, bodega ->
            // La lista ya viene ordenada por precio ascendente (contrato del
            // backend), así que el índice 0 es la más barata. Se omite en
            // listas de un solo resultado: "mejor precio" sería redundante
            // si no hay nada con qué compararlo.
            val esMejorPrecio = index == 0 && bodegas.size > 1

            Card(
                onClick = { onSeleccionar(bodega) },
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    // clearAndSetSemantics fusiona las filas en UN solo nodo
                    // accesible. Sin esto, TalkBack lee cada Text por separado
                    // (nombre... precio... stock...), fragmentado y sin contexto.
                    // Con una descripción única, la card se anuncia como una
                    // frase coherente + "tocar dos veces para activar".
                    modifier = Modifier
                        .padding(18.dp)
                        .clearAndSetSemantics {
                            contentDescription =
                                descripcionAccesible(bodega, ubicacionUsuario, esMejorPrecio)
                        },
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    // Fila 1: nombre + badges (mejor precio / horario).
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bodega.nombreBodega,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Mismo color que el precio (colorScheme.primary):
                            // visualmente conecta el badge con el dato que respalda.
                            if (esMejorPrecio) {
                                BadgePill(texto = "★ Mejor precio", color = MaterialTheme.colorScheme.primary)
                            }
                            BadgeHorario(bodega.horario)
                        }
                    }

                    // Fila 2: precio (destacado) + distancia (si hay ubicación).
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "S/ ${bodega.precio ?: "--"}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        TextDistancia(bodega, ubicacionUsuario)
                    }

                    // Fila 3: datos secundarios.
                    Text(
                        text = "Stock: ${bodega.stock ?: "Sin dato"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    bodega.direccion?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

/**
 * Construye la descripción accesible de una card de bodega: una frase natural
 * en vez de fragmentos sueltos. Refleja lo mismo que muestra la UI visual
 * (nombre, estado de horario, precio, distancia, stock, dirección), pero
 * hilado para que un lector de pantalla lo lea de corrido.
 */
private fun descripcionAccesible(
    bodega: ProductoDisponibilidadDTO,
    ubicacionUsuario: Pair<Double, Double>?,
    esMejorPrecio: Boolean
): String {
    val partes = mutableListOf(bodega.nombreBodega)
    if (esMejorPrecio) partes += "mejor precio"

    when (HorarioUtils.evaluar(bodega.horario)) {
        EstadoHorario.Abierto -> partes += "abierto"
        EstadoHorario.Cerrado -> partes += "cerrado"
        EstadoHorario.Desconocido -> { /* se omite, igual que el badge */ }
    }

    partes += "precio ${bodega.precio ?: "no disponible"} soles"

    val lat = bodega.latitud
    val lng = bodega.longitud
    if (ubicacionUsuario != null && lat != null && lng != null) {
        val (uLat, uLng) = ubicacionUsuario
        val km = GeoUtils.distanciaKm(uLat, uLng, lat, lng)
        partes += if (km < 1.0) "a ${(km * 1000).toInt()} metros"
        else "a ${String.format("%.1f", km)} kilómetros"
    }

    partes += "stock ${bodega.stock ?: "sin dato"}"
    bodega.direccion?.let { partes += it }

    return partes.joinToString(", ")
}

/**
 * Badge de estado: Abierto (verde) / Cerrado (rojo) / nada si es desconocido.
 *
 * Devolver NADA en Desconocido es una decisión de UX: un badge gris "Horario
 * desconocido" ensuciaría la card sin aportar. Mejor omitir.
 */
@Composable
private fun BadgeHorario(horario: String?) {
    when (HorarioUtils.evaluar(horario)) {
        EstadoHorario.Abierto -> BadgePill(texto = "Abierto", color = Color(0xFF2E7D32))
        EstadoHorario.Cerrado -> BadgePill(texto = "Cerrado", color = Color(0xFFC62828))
        EstadoHorario.Desconocido -> { /* No se muestra nada */ }
    }
}

@Composable
private fun BadgePill(texto: String, color: Color) {
    Text(
        text = texto,
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/**
 * Texto de distancia. Si no hay ubicación del usuario, no se muestra:
 * mentir con "a 0 km" sería peor que no mostrar nada.
 */
@Composable
private fun TextDistancia(
    bodega: ProductoDisponibilidadDTO,
    ubicacionUsuario: Pair<Double, Double>?
) {
    val lat = bodega.latitud
    val lng = bodega.longitud
    if (ubicacionUsuario == null || lat == null || lng == null) return

    val (uLat, uLng) = ubicacionUsuario
    val km = GeoUtils.distanciaKm(uLat, uLng, lat, lng)
    val texto = if (km < 1.0) "${(km * 1000).toInt()} m" else String.format("%.1f km", km)

    Text(
        text = "a $texto",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
