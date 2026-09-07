package com.sebas.bodegamap.network

import com.sebas.bodegamap.data.BodegaDTO
import com.sebas.bodegamap.data.ProductoDTO
import com.sebas.bodegamap.data.ProductoDisponibilidadDTO
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // Visor de bodegas (pantalla del mapa al abrir la app).
    @GET("bodegas")
    suspend fun obtenerBodegas(): List<BodegaDTO>

    // Buscador de productos (desde MapScreen).
    @GET("productos/buscar")
    suspend fun buscarProductosPorNombre(
        @Query("nombre") nombre: String
    ): List<ProductoDTO>

    // Comparador: bodegas que venden un producto, ordenadas por precio.
    // Endpoint CANÓNICO de la app. Trae ubicación a propósito para no
    // requerir otra llamada HTTP al seleccionar una bodega.
    @GET("productos/{id}/disponibilidad")
    suspend fun obtenerDisponibilidadProducto(
        @Path("id") idProducto: Long
    ): List<ProductoDisponibilidadDTO>
}
