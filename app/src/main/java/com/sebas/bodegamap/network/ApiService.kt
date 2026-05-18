package com.sebas.bodegamap.network

import com.sebas.bodegamap.data.BodegaDTO
import com.sebas.bodegamap.data.InventarioDTO
import com.sebas.bodegamap.data.ProductoDTO
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // Visor de bodegas
    @GET("bodegas")
    suspend fun obtenerBodegas(): List<BodegaDTO>

    // Buscador
    @GET("productos/buscar")
    suspend fun buscarProductosPorNombre(
        @Query("nombre") nombre: String
    ): List<ProductoDTO>

    // Comparador
    @GET("inventarios/producto/{id}")
    suspend fun obtenerPreciosProducto(
        @Path("id") idProducto: Long
    ): List<InventarioDTO>
}