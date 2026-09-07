package com.sebas.bodegamap.data

/**
 * Contrato del endpoint GET /productos/{id}/disponibilidad.
 *
 * Es un DTO de CASO DE USO, no de entidad: agrupa datos que en el backend
 * viven en distintas tablas (DetalleInventario + Inventario + Bodega), pero
 * que la UI necesita juntos para mostrar y comparar bodegas, y para centrar
 * el mapa al seleccionar una.
 *
 * Nulabilidad:
 * - idBodega y nombreBodega NO son anulables: son la identidad del objeto.
 *   Sin ellos, la fila no tiene sentido.
 * - El resto SÍ es anulable, porque el backend (ProductoDisponibilidadDTO.java)
 *   no aplica @NotNull. Reflejamos aquí el contrato real, no el "suele venir".
 *   Esto nos obliga a decidir en cada pantalla qué hacer si falta un dato,
 *   en vez de crashear o mostrar "0.0" como si fuera real.
 *
 * Decisión de arquitectura: este DTO trae la ubicación (latitud/longitud)
 * a propósito, para que al seleccionar una bodega NO se haga otra llamada
 * HTTP. Quien consuma este objeto tiene TODO lo necesario.
 */
data class ProductoDisponibilidadDTO(
    val idBodega: Long,
    val nombreBodega: String,
    val precio: Double?,
    val stock: Int?,
    val horario: String?,
    val direccion: String?,
    val latitud: Double?,
    val longitud: Double?
)
