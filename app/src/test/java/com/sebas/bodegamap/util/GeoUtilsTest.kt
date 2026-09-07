package com.sebas.bodegamap.util

import org.junit.Assert.assertEquals
import org.junit.Test

class GeoUtilsTest {

    @Test
    fun `distancia entre el mismo punto es cero`() {
        val distancia = GeoUtils.distanciaKm(-12.0464, -77.0428, -12.0464, -77.0428)
        assertEquals(0.0, distancia, 0.0001)
    }

    @Test
    fun `un grado de latitud equivale a aprox 111 km`() {
        // Con radio terrestre 6371km: distancia = R * dLat(rad) = 6371 * (PI/180) ~= 111.19 km.
        val distancia = GeoUtils.distanciaKm(0.0, 0.0, 1.0, 0.0)
        assertEquals(111.19, distancia, 0.1)
    }

    @Test
    fun `la distancia es simetrica sin importar el orden de los puntos`() {
        val ida = GeoUtils.distanciaKm(-12.0464, -77.0428, -12.05, -77.08)
        val vuelta = GeoUtils.distanciaKm(-12.05, -77.08, -12.0464, -77.0428)
        assertEquals(ida, vuelta, 0.0001)
    }

    @Test
    fun `un grado de longitud en el ecuador equivale a aprox 111 km`() {
        // En el ecuador (lat=0), un grado de longitud recorre el mismo arco
        // que un grado de latitud: R * (PI/180) ~= 111.19 km.
        val distancia = GeoUtils.distanciaKm(0.0, 0.0, 0.0, 1.0)
        assertEquals(111.19, distancia, 0.1)
    }
}
