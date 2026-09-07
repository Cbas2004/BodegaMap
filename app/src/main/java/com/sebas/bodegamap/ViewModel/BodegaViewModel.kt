package com.sebas.bodegamap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sebas.bodegamap.data.BodegaDTO
import com.sebas.bodegamap.data.ProductoDTO
import com.sebas.bodegamap.data.ProductoDisponibilidadDTO
import com.sebas.bodegamap.repository.BodegaRepository
import com.sebas.bodegamap.ui.UiState
import com.sebas.bodegamap.util.GeoUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel COMPARTIDO entre todas las pantallas del flujo.
 *
 * Decisión 4.1 (S3 - Activity scoped):
 * Una sola instancia asociada al ViewModelStoreOwner de la Activity, no a
 * una pantalla. Esto permite que el estado sobreviva entre navegaciones y,
 * sobre todo, materializa la regla de arquitectura "no llamar HTTP al
 * seleccionar una bodega": la lista de bodegas del producto y la bodega
 * seleccionada viven aquí, así MapScreen siempre las tiene disponibles al
 * regresar de ListaBodegasScreen.
 *
 * El precio de esta simplicidad es que el VM vive toda la sesión. En una app
 * con múltiples features eso ensuciaría la memoria; aquí, con un solo flujo,
 * es la herramienta correcta. Si la app crece, se migra a scoping por subgrafo.
 *
 * `repository` se recibe por constructor (default = instancia real) en vez
 * de instanciarse aquí adentro: permite pasar un fake en tests sin necesitar
 * un framework de DI, adecuado para el alcance actual (un solo VM).
 */
class BodegaViewModel(
    private val repository: BodegaRepository = BodegaRepository()
) : ViewModel() {

    // ------------------------------------------------------------------
    // Bodegas para el visor del mapa (GET /bodegas)
    // ------------------------------------------------------------------
    private val _bodegas = MutableStateFlow<List<BodegaDTO>>(emptyList())
    val bodegas: StateFlow<List<BodegaDTO>> = _bodegas.asStateFlow()

    // ------------------------------------------------------------------
    // Búsqueda de productos (GET /productos/buscar)
    // ------------------------------------------------------------------
    private val _estadoBusqueda = MutableStateFlow<UiState<List<ProductoDTO>>>(UiState.Idle)
    val estadoBusqueda: StateFlow<UiState<List<ProductoDTO>>> = _estadoBusqueda.asStateFlow()

    // ------------------------------------------------------------------
    // Disponibilidad de un producto (GET /productos/{id}/disponibilidad)
    // Esta lista es la que ListaBodegasScreen muestra y MapScreen reutiliza
    // al regresar, sin volver a llamar al backend.
    // ------------------------------------------------------------------
    private val _disponibilidad = MutableStateFlow<UiState<List<ProductoDisponibilidadDTO>>>(UiState.Idle)
    val disponibilidad: StateFlow<UiState<List<ProductoDisponibilidadDTO>>> = _disponibilidad.asStateFlow()

    /**
     * Bodega seleccionada en ListaBodegasScreen.
     * MapScreen la lee al regresar para centrar la cámara y abrir el
     * BottomSheet. No requiere llamada HTTP: el objeto ya viene completo
     * desde /disponibilidad (con lat/long/precio/horario/direccion).
     */
    private val _bodegaSeleccionada = MutableStateFlow<ProductoDisponibilidadDTO?>(null)
    val bodegaSeleccionada: StateFlow<ProductoDisponibilidadDTO?> = _bodegaSeleccionada.asStateFlow()

    /**
     * Producto elegido en ListaProductosScreen (con imagen, nombre, etc.).
     * ProductoDisponibilidadDTO (arriba) NO trae imagen -eso vive solo en
     * ProductoDTO-, así que MapScreen necesita este objeto aparte para poder
     * mostrar la miniatura del producto en el BottomSheet.
     */
    private val _productoSeleccionado = MutableStateFlow<ProductoDTO?>(null)
    val productoSeleccionado: StateFlow<ProductoDTO?> = _productoSeleccionado.asStateFlow()

    // ------------------------------------------------------------------
    // Ubicación del usuario (para centrar el mapa y filtrar "cercanas")
    // ------------------------------------------------------------------
    /**
     * Ubicación actual del usuario en grados (lat, lng), o null si todavía
     * no se conoce / no se concedió permiso. La cámara inicial del mapa se
     * centra aquí cuando está disponible; si no, cae al default (Lima).
     */
    private val _ubicacionUsuario = MutableStateFlow<Pair<Double, Double>?>(null)
    val ubicacionUsuario: StateFlow<Pair<Double, Double>?> = _ubicacionUsuario.asStateFlow()

    /**
     * Radio en kilómetros para considerar una bodega "cercana".
     * Concentra la regla de negocio en un solo lugar (fácil de ajustar o
     * de mover a Settings en el futuro).
     */
    private val radioCercaniaKm = 3.0

    /**
     * Bodegas dentro del radio de cercanía respecto a la ubicación del
     * usuario. Si no hay ubicación, devuelve todas (mejor mostrar algo que
     * una pantalla vacía).
     *
     * combine(): reagrupa dos StateFlow (_bodegas y _ubicacionUsuario) y
     * recalcula solo cuando alguno cambia. Es reactivo y eficiente.
     */
    val bodegasCercanas: StateFlow<List<BodegaDTO>> =
        combine(_bodegas, _ubicacionUsuario) { todas, ubicacion ->
            val (lat, lng) = ubicacion ?: return@combine todas
            todas.filter { bodega ->
                GeoUtils.distanciaKm(lat, lng, bodega.latitud, bodega.longitud) <= radioCercaniaKm
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    init {
        cargarBodegas()
    }

    /** Carga inicial de todas las bodegas para el mapa de entrada. */
    fun cargarBodegas() {
        viewModelScope.launch {
            try {
                _bodegas.value = repository.obtenerBodegas()
            } catch (e: Exception) {
                // El visor del mapa degrada con elegancia: sin pins, pero la
                // app no muere. El error visible se gestiona por pantalla.
                e.printStackTrace()
            }
        }
    }

    /** Último término buscado, para poder reintentar tras un error. */
    private var ultimoQueryBusqueda: String = ""

    /** Disparado desde la barra de búsqueda de MapScreen. */
    fun buscarProductos(nombre: String) {
        ultimoQueryBusqueda = nombre
        viewModelScope.launch {
            _estadoBusqueda.value = UiState.Loading
            try {
                val resultado = repository.buscarProductos(nombre)
                _estadoBusqueda.value = UiState.Success(resultado)
            } catch (e: Exception) {
                _estadoBusqueda.value = UiState.Error(
                    "No se pudo buscar. Verifica tu conexión."
                )
            }
        }
    }

    /** Llamado por el botón "Reintentar" de ListaProductosScreen. */
    fun reintentarBusqueda() {
        if (ultimoQueryBusqueda.isNotBlank()) buscarProductos(ultimoQueryBusqueda)
    }

    /**
     * Disparado al seleccionar un producto. La lista resultante queda en
     * memoria (_disponibilidad) y la reutilizamos al regresar al mapa.
     */
    fun cargarDisponibilidad(idProducto: Long) {
        viewModelScope.launch {
            _disponibilidad.value = UiState.Loading
            try {
                val resultado = repository.obtenerDisponibilidad(idProducto)
                _disponibilidad.value = UiState.Success(resultado)
            } catch (e: Exception) {
                _disponibilidad.value = UiState.Error(
                    "No se pudo cargar la disponibilidad."
                )
            }
        }
    }

    /** Llamado por ListaBodegasScreen al tocar una bodega. */
    fun seleccionarBodega(bodega: ProductoDisponibilidadDTO) {
        _bodegaSeleccionada.value = bodega
    }

    /** Llamado por ListaProductosScreen al tocar un producto. */
    fun seleccionarProducto(producto: ProductoDTO) {
        _productoSeleccionado.value = producto
    }

    /** Llamado por MapScreen tras mostrar el BottomSheet, para limpiar. */
    fun limpiarSeleccion() {
        _bodegaSeleccionada.value = null
        _productoSeleccionado.value = null
    }

    /**
     * Llamado por MapScreen cuando obtiene la ubicación del usuario (vía
     * FusedLocationProviderClient). Dispara el recálculo de bodegasCercanas.
     */
    fun setUbicacionUsuario(lat: Double, lng: Double) {
        _ubicacionUsuario.value = Pair(lat, lng)
    }

    /**
     * Un constructor con parámetro deja de ser instanciable por reflexión
     * por el factory por defecto de Compose (busca un constructor vacío o
     * con SavedStateHandle). Este factory explícito reemplaza esa reflexión
     * y es el punto donde, en tests o en un futuro con más pantallas, se
     * inyectaría un repository distinto (fake o con caché).
     */
    companion object {
        fun factory(repository: BodegaRepository = BodegaRepository()): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BodegaViewModel(repository) as T
                }
            }
    }
}
