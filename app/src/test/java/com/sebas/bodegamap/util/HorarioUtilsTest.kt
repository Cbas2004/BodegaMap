package com.sebas.bodegamap.util

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class HorarioUtilsTest {

    @Test
    fun `horario nulo o vacio es desconocido`() {
        assertEquals(EstadoHorario.Desconocido, HorarioUtils.evaluar(null))
        assertEquals(EstadoHorario.Desconocido, HorarioUtils.evaluar("  "))
    }

    @Test
    fun `formato no reconocido es desconocido`() {
        assertEquals(EstadoHorario.Desconocido, HorarioUtils.evaluar("todo el día"))
    }

    @Test
    fun `dentro del rango normal esta abierto`() {
        val estado = HorarioUtils.evaluar("08:00 - 22:00", ahora = LocalTime.of(10, 0))
        assertEquals(EstadoHorario.Abierto, estado)
    }

    @Test
    fun `fuera del rango normal esta cerrado`() {
        val estado = HorarioUtils.evaluar("08:00 - 22:00", ahora = LocalTime.of(23, 0))
        assertEquals(EstadoHorario.Cerrado, estado)
    }

    @Test
    fun `acepta guion largo como separador`() {
        val estado = HorarioUtils.evaluar("08:00–22:00", ahora = LocalTime.of(10, 0))
        assertEquals(EstadoHorario.Abierto, estado)
    }

    @Test
    fun `rango que cruza medianoche esta abierto antes y despues de las 00 00`() {
        // 18:00 - 02:00: abierto a las 23:00 y a la 01:00, cerrado a las 10:00.
        assertEquals(EstadoHorario.Abierto, HorarioUtils.evaluar("18:00 - 02:00", ahora = LocalTime.of(23, 0)))
        assertEquals(EstadoHorario.Abierto, HorarioUtils.evaluar("18:00 - 02:00", ahora = LocalTime.of(1, 0)))
        assertEquals(EstadoHorario.Cerrado, HorarioUtils.evaluar("18:00 - 02:00", ahora = LocalTime.of(10, 0)))
    }

    @Test
    fun `los limites del rango se consideran abiertos`() {
        assertEquals(EstadoHorario.Abierto, HorarioUtils.evaluar("08:00 - 22:00", ahora = LocalTime.of(8, 0)))
        assertEquals(EstadoHorario.Abierto, HorarioUtils.evaluar("08:00 - 22:00", ahora = LocalTime.of(22, 0)))
    }
}
