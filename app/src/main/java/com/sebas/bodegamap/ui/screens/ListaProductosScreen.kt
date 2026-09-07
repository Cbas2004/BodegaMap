package com.sebas.bodegamap.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sebas.bodegamap.R
import com.sebas.bodegamap.data.ProductoDTO
import com.sebas.bodegamap.ui.components.EstadoContent
import com.sebas.bodegamap.viewmodel.BodegaViewModel

@Composable
fun ListaProductosScreen(
    viewModel: BodegaViewModel,
    onProductoSeleccionado: (Long) -> Unit,
    onBack: () -> Unit
) {
    // Recibe la MISMA instancia del VM compartido. El término de búsqueda
    // lo dispara MapScreen; aquí solo consumimos el resultado.
    val estado by viewModel.estadoBusqueda.collectAsState()

    EstadoContent(
        estado = estado,
        mensajeIdle = "Escribe para buscar.",
        mensajeVacio = "No encontramos productos con ese nombre.",
        onReintentar = { viewModel.reintentarBusqueda() },
        estaVacio = { it.isEmpty() }
    ) { productos ->
        ListaProductosContenido(
            productos = productos,
            onClick = { producto ->
                // Guardamos el producto completo (con imagen) en el VM antes
                // de navegar: ProductoDisponibilidadDTO (lo que carga la
                // siguiente pantalla) no trae imagen, así que MapScreen
                // necesita este objeto aparte para mostrarla en el sheet.
                viewModel.seleccionarProducto(producto)
                onProductoSeleccionado(producto.idProducto)
            }
        )
    }
}

@Composable
private fun ListaProductosContenido(
    productos: List<ProductoDTO>,
    onClick: (ProductoDTO) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(productos) { producto ->
            Card(
                onClick = { onClick(producto) },
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    // clearAndSetSemantics fusiona la miniatura + nombre +
                    // categoría en un solo nodo accesible con una descripción
                    // coherente, en vez de que TalkBack los lea sueltos.
                    modifier = Modifier
                        .padding(14.dp)
                        .clearAndSetSemantics {
                            contentDescription =
                                "${producto.nombre}, categoría ${producto.nombreCategoria}"
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Thumbnail con Coil.
                    // - placeholder/fallback mientras carga o si la URL falla:
                    //   nunca vemos un hueco vacío, sino el ícono del launcher.
                    // - ContentScale.Crop: ocupa siempre el cuadrado sin deformar.
                    // contentDescription null: la imagen es decorativa; el nombre
                    // ya lo anuncia la descripción de la card (arriba).
                    AsyncImage(
                        model = producto.imagenUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        // R.mipmap.ic_launcher resuelve al ícono ADAPTATIVO
                        // (mipmap-anydpi/ic_launcher.xml) en cualquier
                        // dispositivo real. painterResource() no soporta
                        // adaptive icons (solo VectorDrawable/PNG/JPG/WEBP),
                        // así que crashea al pintar el placeholder. Usamos
                        // el layer "foreground", que es un <vector> plano.
                        placeholder = painterResource(R.drawable.ic_launcher_foreground),
                        error = painterResource(R.drawable.ic_launcher_foreground),
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = producto.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = producto.nombreCategoria,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
