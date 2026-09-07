package com.sebas.bodegamap.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.sebas.bodegamap.R
import com.sebas.bodegamap.viewmodel.BodegaViewModel

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    viewModel: BodegaViewModel = viewModel()
) {

    // Observa automáticamente cambios del backend
    val bodegas by viewModel.bodegas.collectAsState()
    val context = LocalContext.current

    AndroidView(

        modifier = Modifier.fillMaxSize(),

        // mapa
        factory = { ctx ->

            MapView(ctx).apply {

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

            // Icono Boxicons: bxs-store dentro de pin circular
            val boxiconPin = crearIconoPinBoxicons(context)

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
                    .withIconImage(boxiconPin)
                    .withTextField(bodega.nombre)
                    .withTextSize(14.0)
                    .withTextOffset(listOf(0.0, 1.5))

                pointAnnotationManager.create(pinOptions)
            }
        }
    )
}


/**
 * Pin con Boxicons: círculo + icono bxs-store (Boxicons Solid).
 * Usa R.drawable.ic_bxs_store como icono Boxicons.
 */
fun crearIconoPinBoxicons(context: Context): Bitmap {

    val size = 80
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Fondo circular (color Boxicons primario)
    val paintCirculo = Paint().apply {
        color = Color.parseColor("#FF3D00") // Naranja Boxicons/bodega
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paintCirculo)

    // Borde blanco
    val paintBorde = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, paintBorde)

    // Dibuja icono Boxicons bxs-store centrado (24dp -> escalado a 44px)
    val drawable = AppCompatResources.getDrawable(context, R.drawable.ic_bxs_store)
    drawable?.let {
        val iconSize = 44
        val left = (size - iconSize) / 2
        val top = (size - iconSize) / 2
        it.setBounds(left, top, left + iconSize, top + iconSize)
        it.draw(canvas)
    }

    return bitmap
}

// Mantener compatibilidad: crearIconoPin() ahora delega a Boxicons
fun crearIconoPin(): Bitmap {
    // Fallback sin Context: pin simple rojo (no Boxicons)
    val size = 40
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paintCirculo = Paint().apply {
        color = Color.parseColor("#FF3D00")
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paintCirculo)
    val paintCentro = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 6f, paintCentro)
    return bitmap
}