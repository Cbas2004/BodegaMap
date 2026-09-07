package com.sebas.bodegamap.data

import com.google.gson.annotations.SerializedName

/**
 * Nulabilidad y nombres de campo reflejan el contrato REAL observado en
 * GET /bodegas, no el "debería":
 * - el backend serializa el id como "idbodega" (minúscula), no "idBodega".
 * - "horario" a veces viene ausente del JSON. Gson, al no encontrar el
 *   campo, IGNORA la nulabilidad de Kotlin y deja el valor en null aunque
 *   el tipo diga String no-nulo, lo que revienta con NullPointerException
 *   en cualquier punto que trate ese valor como garantizado (ya nos pasó
 *   una vez con el ícono adaptativo). Declararlo String? fuerza a manejar
 *   el caso ausente en cada pantalla, en vez de confiar en un contrato que
 *   el backend no cumple.
 */
data class BodegaDTO(
    @SerializedName("idbodega") val idBodega: Long,
    val nombre: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val horario: String?
)