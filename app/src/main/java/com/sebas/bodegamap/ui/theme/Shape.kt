package com.sebas.bodegamap.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Escala de esquinas más redondeada que el default de Material3 (que va de
 * 4dp a 28dp). Se aplica una sola vez aquí y de ahí en más TODO lo que use
 * MaterialTheme.shapes (Card, Button, ModalBottomSheet, SearchBar) se ve
 * consistente, sin tocar cada composable por separado.
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
