package com.sebas.bodegamap.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sebas.bodegamap.BodegaMapApplication
import com.sebas.bodegamap.repository.BodegaRepository
import com.sebas.bodegamap.ui.screens.ListaBodegasScreen
import com.sebas.bodegamap.ui.screens.ListaProductosScreen
import com.sebas.bodegamap.ui.screens.MapScreen
import com.sebas.bodegamap.viewmodel.BodegaViewModel

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    // La base de datos vive en la Application (singleton para toda la app).
    // Se inyecta aquí el DAO real; BodegaRepository la usa solo como
    // respaldo offline de /bodegas (ver BodegaRepository.obtenerBodegas).
    val application = LocalContext.current.applicationContext as BodegaMapApplication

    // Decisión 4.1 (S3): el ViewModel se crea AQUÍ, antes del NavHost.
    // Como AppNavigation está en la composición de la Activity, su
    // LocalViewModelStoreOwner es la Activity -> una sola instancia para
    // toda la app. Todas las pantallas reciben LA MISMA instancia y
    // comparten estado (bodega seleccionada, disponibilidad, etc.).
    val viewModel: BodegaViewModel = viewModel(
        factory = BodegaViewModel.factory(
            BodegaRepository(bodegaDao = application.database.bodegaDao())
        )
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Map.route
    ) {

        composable(route = Screen.Map.route) {
            MapScreen(
                viewModel = viewModel,
                onBuscar = { navController.navigate(Screen.Productos.route) }
            )
        }

        composable(route = Screen.Productos.route) {
            ListaProductosScreen(
                viewModel = viewModel,
                onProductoSeleccionado = { idProducto ->
                    navController.navigate(Screen.Bodegas.createRoute(idProducto))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Bodegas.route,
            arguments = listOf(
                navArgument("idProducto") { type = NavType.LongType }
            )
        ) { backStackEntry ->

            val idProducto =
                backStackEntry.arguments?.getLong("idProducto") ?: 0L

            ListaBodegasScreen(
                idProducto = idProducto,
                viewModel = viewModel,
                // Al seleccionar bodega: NO navegamos hacia adelante.
                // Volvemos al mapa y el VM ya tiene la bodega seleccionada.
                // El mapa centra y abre BottomSheet.
                //
                // popBackStack() simple solo saca UN nivel de la pila
                // (Bodegas -> Productos), no hasta Map (la pila real es
                // Map -> Productos -> Bodegas). Por eso el sheet nunca
                // aparecía: el usuario caía en ListaProductosScreen y solo
                // veía el mapa (con el sheet ya listo) al presionar atrás
                // una segunda vez. popBackStack(ruta, inclusive=false) saca
                // todo lo que esté por encima de Map, incluyendo Map mismo
                // NO (inclusive=false lo preserva), aterrizando ahí directo.
                onBack = { navController.popBackStack(Screen.Map.route, inclusive = false) }
            )
        }
    }
}
