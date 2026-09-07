package com.sebas.bodegamap.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.sebas.bodegamap.R

// 1. Crear el proveedor de Google Fonts
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// 2. Definir la fuente que quieres buscar en el catálogo de Google
val MontserratFont = GoogleFont("Montserrat") // <-- Aquí puedes cambiar "Inter" por "Poppins", etc.

// 3. Crear la familia de fuentes asociando los grosores (Weights)
val MontserratFontFamily = FontFamily(
    Font(googleFont = MontserratFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = MontserratFont, fontProvider = provider, weight = FontWeight.Bold)
)

/**
 * Antes, Typography() solo definía bodyLarge: cualquier otro rol (titleMedium,
 * titleSmall, headlineSmall, labelSmall, etc. -que es lo que usan en la
 * práctica las pantallas de la app-) caía al Roboto por defecto de Material3,
 * así que Montserrat casi no se veía. Se parte de la escala por defecto de
 * Material3 (tamaños/line-height/letter-spacing ya probados) y solo se
 * reemplaza la fuente y el peso en cada rol, para que toda la app use la
 * misma tipografía de forma consistente.
 *
 * Solo existen variantes Normal y Bold de Montserrat (arriba). Se fuerza
 * cada rol a uno de los dos pesos disponibles para evitar que Compose
 * sintetice un peso intermedio que no se descargó.
 */
private val baseline = Typography()

val Typography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Bold),
    displayMedium = baseline.displayMedium.copy(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Bold),
    displaySmall = baseline.displaySmall.copy(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Bold),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Bold),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Bold),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Bold),
    titleLarge = baseline.titleLarge.copy(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Bold),
    titleMedium = baseline.titleMedium.copy(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Bold),
    titleSmall = baseline.titleSmall.copy(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Bold),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Normal),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Normal),
    bodySmall = baseline.bodySmall.copy(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Normal),
    labelLarge = baseline.labelLarge.copy(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Bold),
    labelMedium = baseline.labelMedium.copy(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Normal),
    labelSmall = baseline.labelSmall.copy(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Normal)
)
