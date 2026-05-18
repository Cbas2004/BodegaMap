package com.sebas.bodegamap.data

data class ProductoDTO(
    val idProducto: Long,
    val nombre: String,
    val descripcion: String,
    val imagenUrl: String,
    val idCategoria: Long,
    val nombreCategoria: String
)
