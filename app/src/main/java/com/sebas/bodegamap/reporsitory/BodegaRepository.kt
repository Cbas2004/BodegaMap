package com.sebas.bodegamap.repository

import com.sebas.bodegamap.data.BodegaDTO
import com.sebas.bodegamap.network.RetrofitClient

class BodegaRepository {

    suspend fun obtenerBodegas(): List<BodegaDTO> {

        return RetrofitClient.apiService.obtenerBodegas()

    }
}