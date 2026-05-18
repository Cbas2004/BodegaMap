package com.sebas.bodegamap.data

data class BodegaDTO(
    val idBodega: Long,
    val nombre: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val horario: String
)