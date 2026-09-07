package com.sebas.bodegamap.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta propia de BodegaMap: ámbar/dorado como color de marca (guiño a
 * Inca Kola, el producto de referencia en todo el catálogo de prueba) en
 * vez del morado por defecto de la plantilla de Compose. El verde/rojo se
 * reutilizan intencionalmente con los mismos tonos que ya usan los badges
 * de "Abierto"/"Cerrado" (ListaBodegasScreen), para que la marca y el
 * estado de las bodegas se sientan parte del mismo sistema visual.
 */

// --- Esquema claro ---
val AmberPrimaryLight = Color(0xFFB35A00)
val OnAmberPrimaryLight = Color(0xFFFFFFFF)
val AmberPrimaryContainerLight = Color(0xFFFFDCB8)
val OnAmberPrimaryContainerLight = Color(0xFF3A1800)

val GreenSecondaryLight = Color(0xFF2E7D32)
val OnGreenSecondaryLight = Color(0xFFFFFFFF)
val GreenSecondaryContainerLight = Color(0xFFC8F5C8)
val OnGreenSecondaryContainerLight = Color(0xFF002200)

val BlueTertiaryLight = Color(0xFF3C6E9F)
val OnBlueTertiaryLight = Color(0xFFFFFFFF)

val BackgroundLight = Color(0xFFFFFBF5)
val OnBackgroundLight = Color(0xFF201A14)
val SurfaceVariantLight = Color(0xFFF0E4D4)
val OnSurfaceVariantLight = Color(0xFF4E4539)

// --- Esquema oscuro ---
// Fondo cálido (no gris neutro) para que combine con Style.DARK del mapa
// Mapbox, en vez del negro azulado por defecto de Material.
val AmberPrimaryDark = Color(0xFFFFB77C)
val OnAmberPrimaryDark = Color(0xFF4A2800)
val AmberPrimaryContainerDark = Color(0xFF6B3900)
val OnAmberPrimaryContainerDark = Color(0xFFFFDCB8)

val GreenSecondaryDark = Color(0xFF8DD68F)
val OnGreenSecondaryDark = Color(0xFF00390A)
val GreenSecondaryContainerDark = Color(0xFF1E4620)
val OnGreenSecondaryContainerDark = Color(0xFFC8F5C8)

val BlueTertiaryDark = Color(0xFFA7CDEF)
val OnBlueTertiaryDark = Color(0xFF0A3A57)

val BackgroundDark = Color(0xFF1A1410)
val OnBackgroundDark = Color(0xFFEDE0D4)
val SurfaceVariantDark = Color(0xFF4E4539)
val OnSurfaceVariantDark = Color(0xFFD3C4B4)
