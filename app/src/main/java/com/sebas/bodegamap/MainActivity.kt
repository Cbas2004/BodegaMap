package com.sebas.bodegamap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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

                    Text(
                        text = bodega.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = bodega.direccion ?: "Sin dirección"
                    )

                    Text(
                        text = "Horario: ${bodega.horario ?: "No disponible"}"
                    )

                }

            }

        }

    }

}