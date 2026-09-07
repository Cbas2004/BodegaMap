package com.sebas.bodegamap.repository

import com.sebas.bodegamap.data.BodegaDTO
import com.sebas.bodegamap.data.ProductoDTO
import com.sebas.bodegamap.data.ProductoDisponibilidadDTO
import com.sebas.bodegamap.data.local.BodegaDao
import com.sebas.bodegamap.data.local.toDto
import com.sebas.bodegamap.data.local.toEntity
import com.sebas.bodegamap.network.ApiService
import com.sebas.bodegamap.network.RetrofitClient
import java.io.IOException

/**
 * Punto único de acceso a la API.
 *
 * La capa Repository abstrae el origen de datos: el ViewModel no sabe si los
 * datos vienen de Retrofit, de una caché local, o de Room. Esto permite
 * cambiar la fuente (p. ej. añadir caché) sin tocar el ViewModel ni la UI.
 *
 * `api` se recibe por constructor (default = Retrofit real) para poder
 * sustituirlo por un fake en tests, sin acoplar esta clase a RetrofitClient.
 *
 * `bodegaDao` es opcional (null en tests o si no hay caché disponible). Solo
 * se usa como RESPALDO offline de /bodegas, no como fuente primaria: cada
 * apertura de la app sigue pidiendo datos frescos al backend.
 */
class BodegaRepository(
    private val api: ApiService = RetrofitClient.apiService,
    private val bodegaDao: BodegaDao? = null
) {

    /**
     * Todas las bodegas (visor del mapa al abrir la app).
     *
     * Si el backend responde, se guarda una copia en Room y se devuelve esa
     * lista. Si falla por un problema de red (IOException), se cae a la
     * última copia cacheada en vez de dejar el mapa sin pines; si tampoco
     * hay caché, se propaga el error original (comportamiento previo).
     */
    suspend fun obtenerBodegas(): List<BodegaDTO> {
        return try {
            val remotas = api.obtenerBodegas()
            bodegaDao?.reemplazarTodas(remotas.map { it.toEntity() })
            remotas
        } catch (e: IOException) {
            val cacheadas = bodegaDao?.obtenerTodas()?.map { it.toDto() }
            if (cacheadas.isNullOrEmpty()) throw e else cacheadas
        }
    }

    /** Búsqueda de productos por nombre (desde MapScreen). */
    suspend fun buscarProductos(nombre: String): List<ProductoDTO> =
        api.buscarProductosPorNombre(nombre)

    /** Bodegas que venden un producto, ordenadas por precio asc. */
    suspend fun obtenerDisponibilidad(idProducto: Long): List<ProductoDisponibilidadDTO> =
        api.obtenerDisponibilidadProducto(idProducto)
}
