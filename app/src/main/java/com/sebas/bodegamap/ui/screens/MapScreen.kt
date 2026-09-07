package com.sebas.bodegamap.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.locationcomponent.location
import com.sebas.bodegamap.R
import com.sebas.bodegamap.data.BodegaDTO
import com.sebas.bodegamap.data.ProductoDTO
import com.sebas.bodegamap.data.ProductoDisponibilidadDTO
import com.sebas.bodegamap.viewmodel.BodegaViewModel
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: BodegaViewModel,
    onBuscar: () -> Unit
) {

    val context = LocalContext.current
    // LifecycleOwner de la Activity. Nos sirve de puente para avisar al
    // MapView de los cambios de estado (resume/pause/stop/destroy).
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estilo del mapa según el tema del sistema, para que no quede un mapa
    // oscuro con UI clara (o viceversa). Se lee en composición y se usa en el
    // factory de abajo. El factory corre una sola vez, pero cambiar el modo
    // oscuro del sistema recrea la Activity (el manifest no fija
    // configChanges=uiMode), así que el mapa se reconstruye con el estilo
    // correcto sin tener que recargar el estilo en caliente (que borraría los
    // pines y obligaría a recrearlos).
    val mapStyle = if (isSystemInDarkTheme()) Style.DARK else Style.LIGHT

    // Usamos bodegasCercanas (filtradas por ubicación) en vez de todas,
    // para honrar la premisa "bodegas cercanas".
    val bodegas by viewModel.bodegasCercanas.collectAsState()
    val bodegaSeleccionada by viewModel.bodegaSeleccionada.collectAsState()
    val productoSeleccionado by viewModel.productoSeleccionado.collectAsState()
    val ubicacionUsuario by viewModel.ubicacionUsuario.collectAsState()

    // --- Estado de UI local.
    var query by remember { mutableStateOf("") }
    var activa by remember { mutableStateOf(false) }
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    // Controla el banner que explica por qué no hay "bodegas cercanas"
    // cuando el usuario rechaza el permiso de ubicación.
    var permisoDenegado by remember { mutableStateOf(false) }

    // El manager se crea UNA VEZ (en factory) y se reutiliza. Antes se
    // recreaba en cada recomposición, lo que borraba y redibujaba todos
    // los pins constantemente (parpadeo + CPU).
    val annotationManagerRef = remember { mutableStateOf<PointAnnotationManager?>(null) }

    // Mapbox identifica cada pin por un id de anotación, no por bodega. Este
    // mapa traduce ese id de vuelta a la BodegaDTO que representa, para
    // poder mostrar su detalle al tocarlo. Se repuebla junto con los pins.
    val annotationBodegaMap = remember { mutableMapOf<String, BodegaDTO>() }

    // Bodega tocada directamente en un pin del mapa (distinta de
    // bodegaSeleccionada, que viene de ListaBodegasScreen con datos de
    // precio/stock). No requiere HTTP: BodegaDTO ya trae todo lo necesario.
    var bodegaPinSeleccionada by remember { mutableStateOf<BodegaDTO?>(null) }

    // --- Sheet.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetScope = rememberCoroutineScope()
    val sheetPayload = bodegaSeleccionada
    val pinPayload = bodegaPinSeleccionada
    val mostrarSheet = sheetPayload != null || pinPayload != null

    // Cierra el sheet y centra el mapa embebido en la bodega. Reemplaza la
    // navegación a una app externa: BodegaMap ya tiene su propio mapa, así
    // que "ver la ubicación" no debería sacar al usuario de la app.
    fun verEnMapa(lat: Double?, lng: Double?) {
        if (lat != null && lng != null) {
            mapViewRef.value?.mapboxMap?.flyTo(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(lng, lat))
                    .zoom(ZOOM_BODEGA_SELECCIONADA)
                    .build(),
                MapAnimationOptions.Builder().duration(700L).build()
            )
        }
        sheetScope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            viewModel.limpiarSeleccion()
            bodegaPinSeleccionada = null
        }
    }

    // --- Permisos de ubicación en runtime.
    // El manifest solo DECLARA los permisos; en Android 6+ hay que pedirlos
    // al usuario. rememberLauncherForActivityResult abre el diálogo del SO.
    val permisoLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { resultados ->
        val concedido = resultados.values.any { it }
        permisoDenegado = !concedido
        if (concedido) {
            obtenerUltimaUbicacion(context) { lat, lng ->
                viewModel.setUbicacionUsuario(lat, lng)
            }
        }
    }

    // Al entrar a la pantalla, pedimos permiso si no está concedido.
    LaunchedEffect(Unit) {
        val tieneFina = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val tieneGruesa = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (tieneFina || tieneGruesa) {
            obtenerUltimaUbicacion(context) { lat, lng ->
                viewModel.setUbicacionUsuario(lat, lng)
            }
        } else {
            permisoLocationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // --- Centrar cámara cuando llega la ubicación del usuario.
    LaunchedEffect(ubicacionUsuario) {
        val mv = mapViewRef.value ?: return@LaunchedEffect
        val (lat, lng) = ubicacionUsuario ?: return@LaunchedEffect
        mv.mapboxMap.flyTo(
            CameraOptions.Builder()
                .center(Point.fromLngLat(lng, lat))
                .zoom(14.0)
                .build(),
            MapAnimationOptions.Builder().duration(900L).build()
        )
    }

    // --- Redibujo REACTIVO de pins: solo cuando cambia la lista.
    // Antes esto vivía en update {} y se ejecutaba en cada recomposición.
    // Ahora vive en un LaunchedEffect con key=bodegas: Compose solo lo
    // reejecuta cuando la lista realmente cambia.
    LaunchedEffect(bodegas) {
        val manager = annotationManagerRef.value ?: return@LaunchedEffect
        manager.deleteAll()
        annotationBodegaMap.clear()
        bodegas.forEach { bodega ->
            val pinOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(bodega.longitud, bodega.latitud))
                .withIconImage(crearIconoPin())
                // El icono ahora es una "gota" con la punta en la base del
                // bitmap (no en el centro): BOTTOM ancla esa punta exacta a
                // la coordenada, en vez de que el pin "flote" sobre el punto.
                .withIconAnchor(IconAnchor.BOTTOM)
                .withTextField(bodega.nombre)
                .withTextSize(14.0)
                // Sin esto, Mapbox pinta el texto en NEGRO por defecto -
                // invisible sobre Style.DARK. Blanco + halo oscuro garantiza
                // legibilidad sin importar qué haya debajo (calle, edificio).
                .withTextColor(android.graphics.Color.WHITE)
                .withTextHaloColor(android.graphics.Color.BLACK)
                .withTextHaloWidth(1.5)
                .withTextOffset(listOf(0.0, 2.2))
            val annotation = manager.create(pinOptions)
            annotationBodegaMap[annotation.id] = bodega
        }
    }

    // --- Forzar apertura del sheet.
    // Confiar solo en "if (mostrarSheet) { ModalBottomSheet(...) }" no basta:
    // al volver de ListaBodegasScreen (popBackStack), MapScreen se recompone
    // durante la transición de navegación y la animación interna de apertura
    // del sheet a veces no arranca en ese primer frame, dejándolo oculto
    // hasta la siguiente recomposición manual. show() es idempotente si ya
    // está visible, así que es seguro llamarlo cada vez que cambia el flag.
    LaunchedEffect(mostrarSheet) {
        if (mostrarSheet) {
            sheetState.show()
        }
    }

    // --- Centrar cámara al regresar con bodega seleccionada.
    LaunchedEffect(sheetPayload?.idBodega) {
        val mv = mapViewRef.value ?: return@LaunchedEffect
        val sel = sheetPayload ?: return@LaunchedEffect
        val lat = sel.latitud ?: return@LaunchedEffect
        val lng = sel.longitud ?: return@LaunchedEffect
        mv.mapboxMap.flyTo(
            CameraOptions.Builder()
                .center(Point.fromLngLat(lng, lat))
                .zoom(ZOOM_BODEGA_SELECCIONADA)
                .build(),
            MapAnimationOptions.Builder().duration(900L).build()
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ---------------- MAPA ----------------
        // El MapView es "con estado": por dentro mantiene un contexto OpenGL,
        // hilos de render, el LocationComponent (GPS) y caché de tiles. Todo eso
        // consume batería/CPU/memoria mientras vive, así que Mapbox REQUIERE que
        // le avisemos cuándo la pantalla está activa y cuándo no, replicando el
        // ciclo de vida de la Activity: onCreate -> onStart -> onResume ...
        // onPause -> onStop -> onDestroy.
        //
        // AndroidView NO reenvía automáticamente el ciclo de vida al View que
        // contiene; solo lo añade al árbol de vistas. Por eso somos nosotros el
        // puente entre la Activity y el MapView.
        //
        // Lo hacemos en dos partes:
        //  1) factory: crea el MapView y llama a onCreate() ANTES de cargar el
        //     estilo (contrato de Mapbox). Las referencias se guardan por estado.
        //  2) DisposableEffect de abajo: engancha el LifecycleOwner de la
        //     Activity y, en cada evento, llama al método equivalente del MapView.
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                MapView(context).apply {
                    mapViewRef.value = this

                    // En Mapbox v11 NO existe onCreate(): el MapView se
                    // inicializa en su propio constructor. El ciclo de vida que
                    // sí debemos replicar es onStart/onStop/onDestroy (ver
                    // DisposableEffect de abajo). onResume/onPause también
                    // desaparecieron en v11: el renderer se auto-gestiona con
                    // onAttachedToWindow, así que onStart/onStop bastan.

                    // Manager creado una sola vez y guardado por referencia.
                    annotationManagerRef.value =
                        this.annotations.createPointAnnotationManager()

                    // Tap en un pin: buscamos la bodega correspondiente en el
                    // mapa anotación->bodega y mostramos su detalle. Se
                    // registra una sola vez (factory corre una sola vez).
                    annotationManagerRef.value?.addClickListener(
                        OnPointAnnotationClickListener { annotation ->
                            bodegaPinSeleccionada = annotationBodegaMap[annotation.id]
                            true
                        }
                    )

                    mapboxMap.loadStyleUri(mapStyle) { style ->

                        // Cámara inicial: Lima por defecto. Si luego llega
                        // ubicación del usuario, el LaunchedEffect vuela ahí.
                        mapboxMap.setCamera(
                            CameraOptions.Builder()
                                .center(Point.fromLngLat(-77.0428, -12.0464))
                                .zoom(12.0)
                                .build()
                        )

                        // LocationComponent: pinta el punto azul de "mi
                        // ubicación" y el botón para centrar en ella.
                        // @SuppressLint("MissingPermission") arriba cubre el
                        // guard de permisos; solo se activa si se concedió.
                        try {
                            val locationPlugin = this.location
                            locationPlugin.updateSettings {
                                enabled = true
                                pulsingEnabled = true
                            }
                        } catch (e: Exception) {
                            // Sin permiso, el plugin falla silenciosamente.
                            e.printStackTrace()
                        }
                    }
                }
            }
        )

        // --- Puente ciclo de vida Activity <-> MapView.
        // Si el MapView ya existe, escuchamos los eventos de la Activity y los
        // reenviamos al método equivalente del MapView. Esto detiene el render
        // y el GPS cuando la app pasa a background (ahorra batería) y libera el
        // contexto GL al salir de la pantalla (evita leaks).
        //
        // Nota sobre la versión: Mapbox v11 simplificó la API y SOLO expone
        // onStart(), onStop() y onDestroy(). onResume()/onPause() dejaron de
        // existir (el renderer se gestiona solo vía onAttachedToWindow), así
        // que mapeamos ON_RESUME->onStart y ON_PAUSE->onStop para no perder
        // eventos de la Activity, y ON_DESTROY se atiende en onDispose.
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                val mapView = mapViewRef.value ?: return@LifecycleEventObserver
                when (event) {
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView.onStart()
                    Lifecycle.Event.ON_PAUSE -> mapView.onStop()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    else -> { /* ON_DESTROY se atiende en onDispose. */ }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            // --- Catch-up (late-attach).
            // Caso crítico: al volver a MapScreen con popBackStack, la Activity
            // YA está RESUMED. Eso significa que los eventos ON_START / ON_RESUME
            // ocurrieron ANTES de que este nuevo MapView existiera, así que el
            // observer recién registrado nunca los va a recibir -> el mapa
            // arrancaría detenido (pantalla negra).
            // Solución: al registrar el observer miramos el estado ACTUAL del
            // ciclo de vida y avanzamos el mapa a mano hasta donde corresponde.
            // Solo avanzamos hacia delante, y onStart es lo máximo que expone v11.
            mapViewRef.value?.let { map ->
                val state = lifecycleOwner.lifecycle.currentState
                if (state >= Lifecycle.State.STARTED) map.onStart()
            }

            // onDispose se ejecuta cuando MapScreen sale de la composición
            // (p. ej. al navegar a ListaProductos). Aquí liberamos TODO:
            // GL context, hilos y GPS. Sin esto se filtran recursos nativos.
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                mapViewRef.value?.let { map ->
                    map.onStop()
                    map.onDestroy()
                    mapViewRef.value = null
                }
            }
        }

        // ---------------- BARRA DE BÚSQUEDA ----------------
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = {
                        activa = false
                        if (query.isNotBlank()) {
                            viewModel.buscarProductos(query.trim())
                            onBuscar()
                        }
                    },
                    expanded = activa,
                    onExpandedChange = { activa = it },
                    placeholder = { Text("Busca un producto, ej. Inca Kola") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    // Solo aparece con texto escrito: limpia sin tener que
                    // borrar letra por letra, y sin ocupar espacio si no hace falta.
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    }
                )
            },
            expanded = activa,
            onExpandedChange = { activa = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 50.dp)
                .align(Alignment.TopCenter)
        ) {
            // Contenido expandido: antes era un texto de tagline decorativo
            // que no ayudaba a nadie ("Encuentra tu producto al mejor
            // precio"). Un hint + ejemplos concretos orienta mejor sobre qué
            // escribir, sobre todo la primera vez que se usa la app.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "Busca por nombre de producto",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Ej: Inca Kola, arroz, leche evaporada",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ---------------- BANNER: permiso de ubicación denegado ----------------
        // Sin esto, el usuario ve la lista de TODAS las bodegas (sin filtrar
        // por cercanía) sin entender por qué. Descartable para no ser intrusivo.
        if (permisoDenegado && ubicacionUsuario == null) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sin acceso a tu ubicación no podemos mostrarte las bodegas cercanas.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { permisoDenegado = false }) {
                        Text("Ok")
                    }
                }
            }
        }
    }

    // ---------------- BOTTOM SHEET DE DETALLE ----------------
    if (mostrarSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.limpiarSeleccion()
                bodegaPinSeleccionada = null
            },
            sheetState = sheetState
        ) {
            if (sheetPayload != null) {
                // Viene de ListaBodegasScreen: trae precio/stock del producto buscado.
                DetalleBodega(
                    bodega = sheetPayload,
                    producto = productoSeleccionado,
                    onVerEnMapa = { verEnMapa(sheetPayload.latitud, sheetPayload.longitud) }
                )
            } else if (pinPayload != null) {
                // Viene de tocar un pin directamente: solo datos generales de la bodega.
                DetalleBodegaGeneral(
                    bodega = pinPayload,
                    onVerEnMapa = { verEnMapa(pinPayload.latitud, pinPayload.longitud) }
                )
            }
        }
    }
}

/**
 * Obtiene la última ubicación conocida vía FusedLocationProviderClient.
 *
 * Por qué "última conocida" y no "en tiempo real":
 * BodegaMap necesita una ubicación aproximada para centrar el mapa y filtrar
 * "cercanas", no seguimiento en vivo. getLastLocation es instantáneo y sin
 * gasto de batería. Si el usuario se mueve, podría re-activarse con un botón.
 *
 * @SuppressLint("MissingPermission"): el llamador garantiza el permiso.
 */
@SuppressLint("MissingPermission")
private fun obtenerUltimaUbicacion(
    context: android.content.Context,
    onResult: (Double, Double) -> Unit
) {
    val client = LocationServices.getFusedLocationProviderClient(context)
    client.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                onResult(location.latitude, location.longitude)
            }
        }
        // Sin callback de error: si falla, no centramos y se mantiene el
        // default (Lima). Degradación elegante.
}

/**
 * Contenido del BottomSheet de detalle de bodega.
 *
 * No se llama al backend: todo lo que se muestra viene en el
 * ProductoDisponibilidadDTO (regla de arquitectura del proyecto) más el
 * ProductoDTO guardado al seleccionar el producto (solo para la miniatura;
 * ProductoDisponibilidadDTO no trae imagen).
 *
 * "Ver en el mapa": en vez de abrir una app externa, cierra el sheet y
 * centra el mapa embebido de BodegaMap en la bodega (ver onVerEnMapa/
 * verEnMapa en MapScreen). No tiene sentido redirigir a otra app de mapas
 * cuando la ubicación ya se puede ver dentro de esta misma pantalla.
 */
@Composable
private fun DetalleBodega(
    bodega: ProductoDisponibilidadDTO,
    producto: ProductoDTO?,
    onVerEnMapa: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Miniatura + nombre del producto: aclara PARA QUÉ producto se está
        // comparando este precio, algo que antes no se veía en este sheet
        // (solo aparecía el nombre de la bodega).
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (producto != null) {
                AsyncImage(
                    model = producto.imagenUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_launcher_foreground),
                    error = painterResource(R.drawable.ic_launcher_foreground),
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            Column {
                producto?.let {
                    Text(
                        text = it.nombre,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = bodega.nombreBodega,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
        Text(
            text = "Precio: S/ ${bodega.precio ?: "--"}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = "Stock: ${bodega.stock ?: "Sin dato"}")
        bodega.direccion?.let { Text(text = "Dirección: $it") }
        bodega.horario?.let { Text(text = "Horario: $it") }

        Spacer(Modifier.height(8.dp))

        // Botón "Ver en el mapa". Solo se habilita si hay coordenadas
        // válidas: si la bodega trae lat/lng null, deshabilitamos en vez de fallar.
        val hayCoordenadas = bodega.latitud != null && bodega.longitud != null
        Button(
            onClick = onVerEnMapa,
            enabled = hayCoordenadas,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver en el mapa")
        }
        if (!hayCoordenadas) {
            Text(
                text = "Ubicación no disponible para esta bodega.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Contenido del BottomSheet al tocar un pin DIRECTAMENTE en el mapa (fuera
 * del flujo de búsqueda de producto). BodegaDTO no tiene precio/stock -eso
 * solo existe en el contexto de un producto- así que este detalle es más
 * simple que DetalleBodega. Tampoco llama al backend: BodegaDTO ya trae
 * nombre/dirección/horario/lat/lng completos.
 */
@Composable
private fun DetalleBodegaGeneral(bodega: BodegaDTO, onVerEnMapa: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = bodega.nombre,
            style = MaterialTheme.typography.headlineSmall
        )
        bodega.horario?.let { Text(text = "Horario: $it") }
        Text(text = "Dirección: ${bodega.direccion}")

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onVerEnMapa,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver en el mapa")
        }
    }
}





/** Color de marca del pin: ámbar vívido, elegido para contrastar con
 *  Style.DARK del mapa (un tono más quemado se perdería contra el gris oscuro). */
private const val COLOR_PIN = 0xFFFFA726.toInt()

/**
 * Zoom al enfocar UNA bodega puntual: seleccionada en la lista (se vuelve al
 * mapa) o vía el botón "Ver en el mapa". Alto a propósito: en la demo hay
 * muchas bodegas a pocos metros entre sí (alrededor de una universidad), y
 * con un zoom más bajo los pines se encimaban. ~17.5 muestra la manzana y
 * permite distinguir bodegas vecinas. Ambos flujos usan el mismo valor para
 * que la experiencia sea consistente y fácil de ajustar desde un solo lugar.
 */
private const val ZOOM_BODEGA_SELECCIONADA = 17.5

/**
 * Dibuja un marcador tipo "gota" (el estándar de apps de mapas: Google Maps,
 * Uber, etc.) en vez del círculo plano anterior. La cabeza y la cola se
 * arman como un solo Path (Path.Op.UNION) para que no quede una costura
 * visible entre el círculo y el triángulo.
 *
 * El bitmap es más alto que ancho a propósito: la punta queda en la base,
 * no en el centro, y el pin se ancla con IconAnchor.BOTTOM (ver arriba) para
 * que esa punta caiga exacto sobre la coordenada de la bodega.
 */
fun crearIconoPin(): Bitmap {
    val ancho = 96
    val alto = 128
    val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val radioCabeza = ancho / 2.4f
    val centroX = ancho / 2f
    val centroYCabeza = radioCabeza + 8f
    val puntaY = alto - 8f

    val cabeza = Path().apply {
        addCircle(centroX, centroYCabeza, radioCabeza, Path.Direction.CW)
    }
    val cola = Path().apply {
        moveTo(centroX - radioCabeza * 0.85f, centroYCabeza + radioCabeza * 0.35f)
        lineTo(centroX + radioCabeza * 0.85f, centroYCabeza + radioCabeza * 0.35f)
        lineTo(centroX, puntaY)
        close()
    }
    val pin = Path()
    pin.op(cabeza, cola, Path.Op.UNION)

    // Sombra suave en la base: da sensación de que el pin "está parado"
    // sobre el mapa, en vez de flotar pegado a la superficie.
    val paintSombra = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(70, 0, 0, 0)
    }
    canvas.drawOval(
        centroX - radioCabeza * 0.7f, puntaY - 6f,
        centroX + radioCabeza * 0.7f, puntaY + 6f,
        paintSombra
    )

    val paintRelleno = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_PIN
        style = Paint.Style.FILL
    }
    canvas.drawPath(pin, paintRelleno)

    val paintBorde = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    canvas.drawPath(pin, paintBorde)

    val paintCentro = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    canvas.drawCircle(centroX, centroYCabeza, radioCabeza * 0.34f, paintCentro)

    return bitmap
}
