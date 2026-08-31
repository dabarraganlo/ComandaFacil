package com.dbarragan.comandafacil

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dbarragan.comandafacil.data.db.ComandaFacilDatabase
import com.dbarragan.comandafacil.data.model.GastoOperativo
import com.dbarragan.comandafacil.data.model.Producto
import com.dbarragan.comandafacil.data.repository.JornadaRepository
import com.dbarragan.comandafacil.data.repository.ProductosRepository
import com.dbarragan.comandafacil.data.repository.VentasRepository
import com.dbarragan.comandafacil.util.DateHelper
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de integración de los módulos a través de la base de datos.
 *
 * A diferencia de las pruebas unitarias, que evalúan cálculos aislados, estas
 * verifican que los datos persistan, se recuperen sin alteración y que una
 * operación de un módulo se refleje en los totales de otro. Se ejecutan sobre
 * la SQLite real del dispositivo con:
 *
 *     ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class IntegracionDatosTest {

    private lateinit var productosRepo: ProductosRepository
    private lateinit var ventasRepo: VentasRepository
    private lateinit var jornadaRepo: JornadaRepository

    @Before
    fun prepararBaseVacia() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Cada caso parte de una base limpia para no depender del orden de ejecución
        context.deleteDatabase(ComandaFacilDatabase.DATABASE_NAME)
        productosRepo = ProductosRepository(context)
        ventasRepo    = VentasRepository(context)
        jornadaRepo   = JornadaRepository(context)
    }

    private fun crearProducto(
        nombre: String = "Almuerzo corriente",
        costoIngredientes: Double = 5000.0,
        costoFijo: Double = 2000.0,
        margen: Double = 30.0,
        stockActual: Double = 10.0,
        stockMinimo: Double = 3.0
    ): Int = productosRepo.guardar(
        Producto(
            nombre               = nombre,
            costoIngredientes    = costoIngredientes,
            costoFijoProrrateado = costoFijo,
            margenGanancia       = margen,
            stockActual          = stockActual,
            stockMinimo          = stockMinimo
        )
    ).toInt()

    /** Flujo 1: registro y recuperación de producto */
    @Test
    fun productoRegistrado_seRecuperaSinAlteracion() {
        val id = crearProducto()

        val recuperado = productosRepo.obtenerPorId(id)

        assertNotNull("El producto debe existir tras guardarlo", recuperado)
        requireNotNull(recuperado)
        assertEquals("Almuerzo corriente", recuperado.nombre)
        assertEquals(5000.0, recuperado.costoIngredientes, 0.01)
        assertEquals(2000.0, recuperado.costoFijoProrrateado, 0.01)
        assertEquals(30.0, recuperado.margenGanancia, 0.01)
        assertEquals(7000.0, recuperado.costoTotal, 0.01)
        // 7000 / (1 - 0,30) = 10000
        assertEquals(10000.0, recuperado.precioSugerido, 0.01)
        assertTrue("El producto debe quedar activo", recuperado.activo)
        assertTrue("Debe registrarse la fecha de creación", recuperado.fechaCreacion.isNotEmpty())
    }

    /** Flujo 2: apertura de jornada, venta y consulta de totales */
    @Test
    fun ventaRegistrada_actualizaLosTotalesDeLaJornada() {
        val productoId = crearProducto()
        val jornada = jornadaRepo.obtenerOCrearJornadaHoy()

        assertEquals(DateHelper.hoy(), jornada.fecha)
        assertFalse("La jornada nace abierta", jornada.estaCerrada)

        ventasRepo.registrar(jornada.id, productoId, cantidad = 4.0, precioVenta = 10000.0)

        val detalles = ventasRepo.listarVentasDelDia(jornada.id)
        assertEquals(1, detalles.size)
        assertEquals("Almuerzo corriente", detalles[0].nombreProducto)
        assertEquals(4.0, detalles[0].cantidad, 0.01)

        val actualizada = jornadaRepo.obtenerJornadaHoy()
        requireNotNull(actualizada)
        assertEquals(40000.0, actualizada.totalIngresos, 0.01)   // 4 x 10.000
        assertEquals(28000.0, actualizada.totalCostos, 0.01)     // 4 x 7.000
        assertEquals(0.0, actualizada.totalGastos, 0.01)
        assertEquals(12000.0, actualizada.gananciaNeta, 0.01)
    }

    /** Flujo 3: la venta descuenta las existencias del producto */
    @Test
    fun ventaRegistrada_descuentaLasExistencias() {
        val productoId = crearProducto(stockActual = 10.0)
        val jornada = jornadaRepo.obtenerOCrearJornadaHoy()

        ventasRepo.registrar(jornada.id, productoId, cantidad = 3.0, precioVenta = 10000.0)

        assertEquals(7.0, productosRepo.obtenerPorId(productoId)!!.stockActual, 0.01)

        ventasRepo.registrar(jornada.id, productoId, cantidad = 2.0, precioVenta = 10000.0)

        assertEquals(5.0, productosRepo.obtenerPorId(productoId)!!.stockActual, 0.01)
    }

    /** Flujo 4: registro de gasto operativo y cierre de jornada */
    @Test
    fun gastoRegistrado_seReflejaEnElCierreDeLaJornada() {
        val productoId = crearProducto()
        val jornada = jornadaRepo.obtenerOCrearJornadaHoy()
        ventasRepo.registrar(jornada.id, productoId, cantidad = 4.0, precioVenta = 10000.0)

        jornadaRepo.registrarGasto(
            GastoOperativo(
                jornadaId   = jornada.id,
                descripcion = "Transporte al mercado",
                monto       = 5000.0,
                tipo        = "variable",
                fecha       = DateHelper.ahora()
            )
        )

        assertEquals(1, jornadaRepo.listarGastos(jornada.id).size)

        val cerrada = jornadaRepo.cerrarJornada(jornada.id)

        assertTrue("La jornada debe quedar cerrada", cerrada.estaCerrada)
        assertEquals(40000.0, cerrada.totalIngresos, 0.01)
        assertEquals(28000.0, cerrada.totalCostos, 0.01)
        assertEquals(5000.0, cerrada.totalGastos, 0.01)
        // El gasto reduce la ganancia: 40.000 - 28.000 - 5.000
        assertEquals(7000.0, cerrada.gananciaNeta, 0.01)
    }

    /** Flujo 5: alerta de existencias mínimas */
    @Test
    fun existenciasBajoElMinimo_activanLaAlerta() {
        val productoId = crearProducto(stockActual = 10.0, stockMinimo = 3.0)
        val jornada = jornadaRepo.obtenerOCrearJornadaHoy()

        assertTrue(
            "Con 10 unidades y mínimo 3 no debe haber alerta",
            productosRepo.listarConStockBajo().isEmpty()
        )

        ventasRepo.registrar(jornada.id, productoId, cantidad = 8.0, precioVenta = 10000.0)

        val enAlerta = productosRepo.listarConStockBajo()
        assertEquals(1, enAlerta.size)
        assertEquals(productoId, enAlerta[0].id)
        assertEquals(2.0, enAlerta[0].stockActual, 0.01)
        assertTrue(enAlerta[0].tieneStockBajo)
    }
}
