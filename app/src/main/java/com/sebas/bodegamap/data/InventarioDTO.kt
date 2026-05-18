package com.sebas.bodegamap.data

data class InventarioDTO(
    val idInventario: Long,
    val idBodega: Long,
    val nombreBodega: String,
    val idProducto: Long,
    val nombreProducto: String,
    val precio: Double,
    val stock: Int
)
