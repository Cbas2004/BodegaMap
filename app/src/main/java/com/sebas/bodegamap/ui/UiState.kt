package com.sebas.bodegamap.ui

/**
 * Estado de una operación de UI.
 *
 * Sellado (sealed interface): el compilador OBLIGA a tratar todos los casos
 * en un when(). No hay forma de "olvidar" manejar el Error o el Loading.
 *
 * Genérico para reutilizarlo en cualquier flujo (bodegas, búsqueda, etc.).
 */
sealed interface UiState<out T> {

    /** Inactivo: todavía no se disparó la operación. */
    data object Idle : UiState<Nothing>

    /** En curso. */
    data object Loading : UiState<Nothing>

    /** Éxito con datos. */
    data class Success<T>(val data: T) : UiState<T>

    /** Fallo con mensaje para el usuario. */
    data class Error(val message: String) : UiState<Nothing>
}
