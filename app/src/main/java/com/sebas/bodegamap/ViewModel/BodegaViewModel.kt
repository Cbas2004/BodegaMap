package com.sebas.bodegamap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sebas.bodegamap.data.BodegaDTO
import com.sebas.bodegamap.repository.BodegaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BodegaViewModel : ViewModel() {

    private val repository = BodegaRepository()

    private val _bodegas = MutableStateFlow<List<BodegaDTO>>(emptyList())
    val bodegas: StateFlow<List<BodegaDTO>> = _bodegas

    init {
        cargarBodegas()
    }

    private fun cargarBodegas() {

        viewModelScope.launch {

            try {

                val resultado = repository.obtenerBodegas()

                _bodegas.value = resultado

            } catch (e: Exception) {

                e.printStackTrace()

            }
        }
    }
}