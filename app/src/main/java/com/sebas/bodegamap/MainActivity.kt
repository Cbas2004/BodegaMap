package com.sebas.bodegamap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.devsrsouza.compose.icons.boxicons.Boxicons
import br.com.devsrsouza.compose.icons.boxicons.Regular
import br.com.devsrsouza.compose.icons.boxicons.Solid
import br.com.devsrsouza.compose.icons.boxicons.regular.Store
import br.com.devsrsouza.compose.icons.boxicons.regular.Search
import br.com.devsrsouza.compose.icons.boxicons.regular.Map
import br.com.devsrsouza.compose.icons.boxicons.solid.Navigation
import com.sebas.bodegamap.R
import com.sebas.bodegamap.data.BodegaDTO
import com.sebas.bodegamap.ui.screens.MapScreen
import com.sebas.bodegamap.ui.theme.BodegaMapTheme
import com.sebas.bodegamap.viewmodel.BodegaViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            BodegaMapTheme {

                MapScreen()

                }

            }

        }
    }

@Composable
fun PantallaBodegas(
    modifier: Modifier = Modifier,
    viewModel: BodegaViewModel = viewModel()
) {

    val bodegas by viewModel.bodegas.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        items(bodegas) { bodega ->

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    // Header con Boxicon bxs-store
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Boxicons.Regular.Store,
                            contentDescription = "Bodega",
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = bodega.nombre,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Direccion con Boxicon bx-map
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Boxicons.Regular.Map,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = bodega.direccion ?: "Sin dirección",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Horario con Boxicon drawable
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bx_navigation),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Horario: ${bodega.horario ?: "No disponible"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                }

            }

        }

    }

}

/**
 * Demo de todos los Boxicons disponibles en la app.
 * Usa Boxicons.Regular y Boxicons.Solid de compose-icons.
 */
@Composable
fun BoxiconsDemoRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Boxicons.Regular.Store, contentDescription = "bx-store", modifier = Modifier.size(24.dp))
        Icon(Boxicons.Regular.Search, contentDescription = "bx-search", modifier = Modifier.size(24.dp))
        Icon(Boxicons.Regular.Map, contentDescription = "bx-map", modifier = Modifier.size(24.dp))
        Icon(Boxicons.Solid.Navigation, contentDescription = "bxs-navigation", modifier = Modifier.size(24.dp))
        // Fallback via Vector Drawable (también Boxicons)
        Icon(painterResource(id = R.drawable.ic_bxs_store), contentDescription = "bxs-store drawable", modifier = Modifier.size(24.dp))
        Icon(painterResource(id = R.drawable.ic_bxs_map_pin), contentDescription = "bxs-map-pin drawable", modifier = Modifier.size(24.dp))
    }
}