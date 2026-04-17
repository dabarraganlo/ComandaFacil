package com.dbarragan.comandafacil

import com.dbarragan.comandafacil.data.model.Producto
import org.junit.Assert.*
import org.junit.Test

class ProductoTest {

    private fun productoBase(
        costoIngredientes: Double = 5000.0,
        costoFijo: Double = 2000.0,
        margenGanancia: Double = 30.0,
        stockActual: Double = 10.0,
        stockMinimo: Double = 3.0
    ) = Producto(
        id = 1,
        nombre = "Almuerzo",
        costoIngredientes = costoIngredientes,
        costoFijoProrrateado = costoFijo,
        margenGanancia = margenGanancia,
        stockActual = stockActual,
        stockMinimo = stockMinimo
    )

    @Test
    fun costoTotal_esSumaDeIngredientesYFijo() {
        val p = productoBase(costoIngredientes = 5000.0, costoFijo = 2000.0)
        assertEquals(7000.0, p.costoTotal, 0.01)
    }

    @Test
    fun precioSugerido_calculadoConFormula() {
        // Precio = costoTotal / (1 - margen/100)
        // = 7000 / (1 - 0.30) = 7000 / 0.70 = 10000
        val p = productoBase(costoIngredientes = 5000.0, costoFijo = 2000.0, margenGanancia = 30.0)
        assertEquals(10000.0, p.precioSugerido, 0.01)
    }

    @Test
    fun precioSugerido_conMargen100_retornaCero() {
        val p = productoBase(margenGanancia = 100.0)
        assertEquals(0.0, p.precioSugerido, 0.01)
    }

    @Test
    fun precioSugerido_conMargenSuperior100_retornaCero() {
        val p = productoBase(margenGanancia = 120.0)
        assertEquals(0.0, p.precioSugerido, 0.01)
    }

    @Test
    fun tieneStockBajo_cuandoStockActualMenorOIgualAlMinimo() {
        val p = productoBase(stockActual = 2.0, stockMinimo = 3.0)
        assertTrue(p.tieneStockBajo)
    }

    @Test
    fun tieneStockBajo_cuandoStockActualIgualAlMinimo() {
        val p = productoBase(stockActual = 3.0, stockMinimo = 3.0)
        assertTrue(p.tieneStockBajo)
    }

    @Test
    fun tieneStockBajo_falso_cuandoStockSuficiente() {
        val p = productoBase(stockActual = 10.0, stockMinimo = 3.0)
        assertFalse(p.tieneStockBajo)
    }

    @Test
    fun tieneStockBajo_falso_cuandoMinimoEsCero() {
        // stockMinimo = 0 significa sin alerta configurada
        val p = productoBase(stockActual = 0.0, stockMinimo = 0.0)
        assertFalse(p.tieneStockBajo)
    }

    @Test
    fun precioSugerido_conMargenCero_igualACostoTotal() {
        val p = productoBase(costoIngredientes = 5000.0, costoFijo = 2000.0, margenGanancia = 0.0)
        assertEquals(7000.0, p.precioSugerido, 0.01)
    }

    @Test
    fun precioSugerido_conMargen50_doblaElCosto() {
        val p = productoBase(costoIngredientes = 4000.0, costoFijo = 1000.0, margenGanancia = 50.0)
        // 5000 / 0.50 = 10000
        assertEquals(10000.0, p.precioSugerido, 0.01)
    }
}
