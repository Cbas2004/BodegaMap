package com.sebas.bodegamap.ui.screens

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.maps.MapView
import com.mapbox.maps.Style

@SuppressLint("MissingPermission")
@Composable
fun MapScreen() {

    AndroidView(
        factory = { context ->

            MapView(context).apply {

                mapboxMap.loadStyleUri(Style.MAPBOX_STREETS)
            }
        }
    )
}