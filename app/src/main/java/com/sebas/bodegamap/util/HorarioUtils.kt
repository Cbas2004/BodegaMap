package com.sebas.bodegamap.util

import java.time.LocalTime
import java.time.format.DateTimeParseException

/**
 * Utilidades para interpretar el campo "horario" de las bodegas.
 *
 * El backend lo devuelve como string libre (p. ej. "08:00 - 22:00"), SIN
 * contrato estricto de formato. Por eso todo el parseo es DEFENSIVO: si el
 * formato no encaja, devolvemos null (estado Desconocido), nunca lanzamos.
 * Una bodega con horario no parseable no debe romper la UI.
 *
 * Estado sellado (3 casos, no 2): distinguir "cerrado" de "no pude saber"
 * es importante para no mentir al usuario con un badge rojo.
 */
sealed interface EstadoHorario {
    data object Abierto : EstadoHorario
    data object Cerrado : EstadoHorario
    data object Desconocido : EstadoHorario
}

object HorarioUtils {

    // Acepta "08:00 - 22:00", "08:00–22:00", con espacios variables.
    // El separador se captura con un grupo para no asumir "-" exacto.
    private val PATRON_RANGO = Regex(
        """(\d{1,2}:\d{2})\s*[-–]\s*(\d{1,2}:\d{2})"""
    )

    /**
     * Evalúa si la bodega está abierta AHORA según su string de horario.
     *
     * Devuelve Desconocido si:
     *  - el horario es null/vacío
     *  - el formato no casa con el patrón
     *  - los tiempos no parsean
     */
    fun evaluar(horario: String?, ahora: LocalTime = LocalTime.now()): EstadoHorario {
        if (horario.isNullOrBlank()) return EstadoHorario.Desconocido

        val match = PATRON_RANGO.find(horario) ?: return EstadoHorario.Desconocido
        val (inicioStr, finStr) = match.destructured

        val inicio = parsearHora(inicioStr) ?: return EstadoHorario.Desconocido
        val fin = parsearHora(finStr) ?: return EstadoHorario.Desconocido

        return if (esAbierto(ahora, inicio, fin)) EstadoHorario.Abierto
        else EstadoHorario.Cerrado
    }

    /**
     * Cobertura del caso de rango que cruza medianoche (p. ej. 18:00 - 02:00).
     * Si fin < inicio, el rango cruza 00:00: abierto si ahora >= inicio O ahora < fin.
     */
    private fun esAbierto(ahora: LocalTime, inicio: LocalTime, fin: LocalTime): Boolean {
        return if (inicio <= fin) {
            ahora in inicio..fin
        } else {
            // Cruza medianoche: dos tramos [inicio,23:59] y [00:00,fin].
            ahora >= inicio || ahora < fin
        }
    }

    private fun parsearHora(texto: String): LocalTime? = try {
        LocalTime.parse(texto)
    } catch (e: DateTimeParseException) {
        null
    }
}
