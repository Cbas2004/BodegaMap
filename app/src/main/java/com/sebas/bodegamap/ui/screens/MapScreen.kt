package com.sebas.bodegamap.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.sebas.bodegamap.viewmodel.BodegaViewModel

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    viewModel: BodegaViewModel = viewModel()
) {

    // Observa automáticamente cambios del backend
    val bodegas by viewModel.bodegas.collectAsState()

    AndroidView(

        modifier = Modifier.fillMaxSize(),

        // mapa
        factory = { context ->

            MapView(context).apply {

                mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {

                    // Cámara inicial en Lima
                    mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(
                                Point.fromLngLat(
                                    -77.0428,
                                    -12.0464
                                )
                            )
                            .zoom(12.0)
                            .build()
                    )
                }
            }
        },

        // update de bodegas
        update = { mapView ->

            println("BODEGAS RECIBIDAS: ${bodegas.size}")

            val pointAnnotationManager =
                mapView.annotations.createPointAnnotationManager()

            // Limpia pins antiguos para evitar duplicados
            pointAnnotationManager.deleteAll()

            // Recorre bodegas del backend
            bodegas.forEach { bodega ->

                println("Bodega: ${bodega.nombre}")
                println("Latitud: ${bodega.latitud}")
                println("Longitud: ${bodega.longitud}")

                val pinOptions = PointAnnotationOptions()
                    .withPoint(
                        Point.fromLngLat(
                            bodega.longitud,
                            bodega.latitud
                        )
                    )
                    .withIconImage(crearIconoPin())
                    .withTextField(bodega.nombre)
                    .withTextSize(14.0)
                    .withTextOffset(listOf(0.0, 1.5))

                pointAnnotationManager.create(pinOptions)
            }
        }
    )
}


fun crearIconoPin(): Bitmap {

    val size = 40

    val bitmap = Bitmap.createBitmap(
        size,
        size,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)

    // Círculo rojo
    val paintCirculo = Paint().apply {
        color = Color.RED
        isAntiAlias = true
    }

    canvas.drawCircle(
        size / 2f,
        size / 2f,
        size / 2f,
        paintCirculo
    )

    // Punto blanco central
    val paintCentro = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
    }

    canvas.drawCircle(
        size / 2f,
        size / 2f,
        size / 6f,
        paintCentro
    )

    return bitmap
}