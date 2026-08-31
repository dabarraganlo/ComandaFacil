package com.dbarragan.comandafacil.data.repository

import android.content.Context
import com.dbarragan.comandafacil.data.db.ComandaFacilDatabase
import com.dbarragan.comandafacil.data.db.dao.JornadaDao
import com.dbarragan.comandafacil.data.db.dao.ProductoDao
import com.dbarragan.comandafacil.data.db.dao.VentaDao
import com.dbarragan.comandafacil.data.model.DetalleVenta
import com.dbarragan.comandafacil.data.model.Venta
import com.dbarragan.comandafacil.util.DateHelper

class VentasRepository(context: Context) {

    private val db         = ComandaFacilDatabase(context)
    private val ventaDao   = VentaDao(db)
    private val productoDao= ProductoDao(db)
    private val jornadaDao = JornadaDao(db)

    /**
     * Registra una venta completa:
     * 1. Inserta la venta y su detalle
     * 2. Descuenta el stock del producto
     * 3. Recalcula los totales de la jornada
     *
     * Los tres pasos van dentro de una misma transaccion: si alguno falla se
     * revierte todo. De lo contrario el vendedor podria quedar con una venta
     * guardada pero con el stock o los totales del dia sin actualizar.
     */
    fun registrar(jornadaId: Int, productoId: Int, cantidad: Double, precioVenta: Double) {
        val producto = productoDao.obtenerPorId(productoId)
            ?: throw IllegalArgumentException("Producto no encontrado: $productoId")

        val venta = Venta(jornadaId = jornadaId, fecha = DateHelper.ahora())
        val detalle = DetalleVenta(
            ventaId        = 0,
            productoId     = productoId,
            cantidad       = cantidad,
            precioUnitario = precioVenta,
            costoUnitario  = producto.costoTotal
        )

        val dbW = db.writableDatabase
        dbW.beginTransaction()
        try {
            ventaDao.insertarConDetalle(venta, listOf(detalle))

            // Descontar stock
            val nuevoStock = (producto.stockActual - cantidad).coerceAtLeast(0.0)
            productoDao.actualizarStock(productoId, nuevoStock)

            // Actualizar totales en la jornada
            jornadaDao.recalcularTotales(jornadaId)

            dbW.setTransactionSuccessful()
        } finally {
            dbW.endTransaction()
        }
    }

    fun listarVentasDelDia(jornadaId: Int): List<DetalleVenta> =
        ventaDao.listarDetallesPorJornada(jornadaId)
}
