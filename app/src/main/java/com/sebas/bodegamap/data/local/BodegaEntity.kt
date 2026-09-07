package com.sebas.bodegamap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sebas.bodegamap.data.BodegaDTO

/**
 * Copia local de BodegaDTO para el respaldo offline (caché de Room).
 * Se mantiene separada del DTO de red: si el contrato de la API cambia,
 * el esquema de la base de datos no tiene por qué cambiar con él.
 */
@Entity(tableName = "bodegas")
data class BodegaEntity(
    @PrimaryKey val idBodega: Long,
    val nombre: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val horario: String?
)

fun BodegaEntity.toDto() = BodegaDTO(idBodega, nombre, direccion, latitud, longitud, horario)

fun BodegaDTO.toEntity() = BodegaEntity(idBodega, nombre, direccion, latitud, longitud, horario)
