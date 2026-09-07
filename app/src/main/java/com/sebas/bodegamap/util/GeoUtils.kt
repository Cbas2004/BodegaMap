package com.sebas.bodegamap.util

import kotlin.math.*

/**
 * Utilidades geográficas.
 *
 * Se separan de la UI porque son LÓGICA DE DOMINIO pura: sin dependencias de
 * Android, sin Compose. Eso las hace testeables con unit tests simples y
 * reutilizables desde cualquier capa.
 */

object GeoUtils {

    private const val RADIO_TIERRA_KM = 6371.0

    /**
     * Distancia entre dos puntos (lat/lng en grados) usando la fórmula de
     * Haversine. Devuelve kilómetros.
     *
     * Haversine es la aproximación estándar para distancias en la superficie
     * terrestre asumiendo una Tierra esférica. Para un producto de bodegas a
     * escala urbana es más que suficiente (error < 0.5%).
     *
     * Mejora futura: si se necesita precisión absoluta, usar la fórmula de
     * Vincenty (elipsoide). Para BodegaMap no vale la pena el coste.
     */
    fun distanciaKm(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return RADIO_TIERRA_KM * c
    }
}
