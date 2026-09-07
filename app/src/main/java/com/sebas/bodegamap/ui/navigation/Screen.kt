package com.sebas.bodegamap.ui.navigation

sealed class Screen(
    val route: String,
    val title: String
) {

    object Map : Screen(
        route = "map",
        title = "Mapa"
    )

    object Productos : Screen(
        route = "productos",
        title = "Productos"
    )

    object Bodegas : Screen(
        route = "bodegas/{idProducto}",
        title = "Bodegas"
    ) {

        fun createRoute(idProducto: Long): String {
            return "bodegas/$idProducto"
        }
    }
}