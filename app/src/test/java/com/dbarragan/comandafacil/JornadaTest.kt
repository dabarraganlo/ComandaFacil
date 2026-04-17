package com.dbarragan.comandafacil

import com.dbarragan.comandafacil.data.model.Jornada
import org.junit.Assert.*
import org.junit.Test

class JornadaTest {

    private fun jornadaBase(
        totalIngresos: Double = 0.0,
        totalCostos: Double = 0.0,
        totalGastos: Double = 0.0,
        metaGanancia: Double = 0.0,
        estado: String = "abierta"
    ) = Jornada(
        id = 1,
        fecha = "2024-01-15",
        totalIngresos = totalIngresos,
        totalCostos = totalCostos,
        totalGastos = totalGastos,
        metaGanancia = metaGanancia,
        estado = estado
    )

    // --- gananciaNeta ---

    @Test
    fun gananciaNeta_esIngresosMinusCostosMinusGastos() {
        val j = jornadaBase(totalIngresos = 100_000.0, totalCostos = 40_000.0, totalGastos = 15_000.0)
        assertEquals(45_000.0, j.gananciaNeta, 0.01)
    }

    @Test
    fun gananciaNeta_puedeSerNegativa() {
        val j = jornadaBase(totalIngresos = 10_000.0, totalCostos = 8_000.0, totalGastos = 5_000.0)
        assertEquals(-3_000.0, j.gananciaNeta, 0.01)
    }

    @Test
    fun gananciaNeta_ceroSiTodoEsCero() {
        val j = jornadaBase()
        assertEquals(0.0, j.gananciaNeta, 0.01)
    }

    // --- margenReal ---

    @Test
    fun margenReal_calculadoCorrectamente() {
        // ganancia = 45000, ingresos = 100000 → margen = 45%
        val j = jornadaBase(totalIngresos = 100_000.0, totalCostos = 40_000.0, totalGastos = 15_000.0)
        assertEquals(45.0, j.margenReal, 0.01)
    }

    @Test
    fun margenReal_ceroSiIngresosEsCero() {
        val j = jornadaBase(totalIngresos = 0.0, totalCostos = 5_000.0)
        assertEquals(0.0, j.margenReal, 0.01)
    }

    @Test
    fun margenReal_negativoSiHayPerdida() {
        val j = jornadaBase(totalIngresos = 10_000.0, totalCostos = 8_000.0, totalGastos = 5_000.0)
        // ganancia = -3000, margen = -30%
        assertEquals(-30.0, j.margenReal, 0.01)
    }

    // --- progresoMeta ---

    @Test
    fun progresoMeta_calculadoCorrectamente() {
        // ganancia = 45000, meta = 60000 → 75%
        val j = jornadaBase(
            totalIngresos = 100_000.0, totalCostos = 40_000.0, totalGastos = 15_000.0,
            metaGanancia = 60_000.0
        )
        assertEquals(75, j.progresoMeta)
    }

    @Test
    fun progresoMeta_ceroSiMetaEsCero() {
        val j = jornadaBase(metaGanancia = 0.0)
        assertEquals(0, j.progresoMeta)
    }

    @Test
    fun progresoMeta_noBajaDeZero_cuandoGananciaNegativa() {
        val j = jornadaBase(
            totalIngresos = 5_000.0, totalCostos = 8_000.0,
            metaGanancia = 20_000.0
        )
        assertEquals(0, j.progresoMeta)
    }

    @Test
    fun progresoMeta_100_cuandoMetaExacta() {
        val j = jornadaBase(
            totalIngresos = 80_000.0, totalCostos = 30_000.0, totalGastos = 10_000.0,
            metaGanancia = 40_000.0
        )
        assertEquals(100, j.progresoMeta)
    }

    // --- metaAlcanzada ---

    @Test
    fun metaAlcanzada_verdadero_cuandoGananciaIgualOMayorAMeta() {
        val j = jornadaBase(
            totalIngresos = 80_000.0, totalCostos = 30_000.0, totalGastos = 10_000.0,
            metaGanancia = 40_000.0
        )
        assertTrue(j.metaAlcanzada)
    }

    @Test
    fun metaAlcanzada_falso_cuandoGananciaMenorAMeta() {
        val j = jornadaBase(
            totalIngresos = 50_000.0, totalCostos = 30_000.0, totalGastos = 10_000.0,
            metaGanancia = 40_000.0
        )
        assertFalse(j.metaAlcanzada) // ganancia = 10000 < 40000
    }

    @Test
    fun metaAlcanzada_falso_cuandoMetaEsCero() {
        val j = jornadaBase(totalIngresos = 100_000.0, metaGanancia = 0.0)
        assertFalse(j.metaAlcanzada)
    }

    // --- estaCerrada ---

    @Test
    fun estaCerrada_verdadero_cuandoEstadoEsCerrada() {
        val j = jornadaBase(estado = "cerrada")
        assertTrue(j.estaCerrada)
    }

    @Test
    fun estaCerrada_falso_cuandoEstadoEsAbierta() {
        val j = jornadaBase(estado = "abierta")
        assertFalse(j.estaCerrada)
    }
}
