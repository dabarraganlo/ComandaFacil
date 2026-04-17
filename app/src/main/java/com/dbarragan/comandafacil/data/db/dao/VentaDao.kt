package com.dbarragan.comandafacil.data.db.dao

import android.content.ContentValues
import android.database.Cursor
import com.dbarragan.comandafacil.data.db.ComandaFacilDatabase
import com.dbarragan.comandafacil.data.model.DetalleVenta
import com.dbarragan.comandafacil.data.model.Venta

class VentaDao(private val db: ComandaFacilDatabase) {

    /** Inserta una venta y sus líneas de detalle. Devuelve el id de la venta. */
    fun insertarConDetalle(venta: Venta, detalles: List<DetalleVenta>): Long {
        val dbW = db.writableDatabase
        dbW.beginTransaction()
        return try {
            val cvVenta = ContentValues().apply {
                put("jornada_id", venta.jornadaId)
                put("fecha", venta.fecha)
            }
            val ventaId = dbW.insert("ventas", null, cvVenta)

            detalles.forEach { d ->
                val cvDetalle = ContentValues().apply {
                    put("venta_id", ventaId)
                    put("producto_id", d.productoId)
                    put("cantidad", d.cantidad)
                    put("precio_unitario", d.precioUnitario)
                    put("costo_unitario", d.costoUnitario)
                }
                dbW.insert("detalle_ventas", null, cvDetalle)
            }

            dbW.setTransactionSuccessful()
            ventaId
        } finally {
            dbW.endTransaction()
        }
    }

    /** Lista los detalles de ventas de una jornada con el nombre del producto */
    fun listarDetallesPorJornada(jornadaId: Int): List<DetalleVenta> {
        val cursor = db.readableDatabase.rawQuery(
            """SELECT dv.*, p.nombre as nombre_producto
               FROM detalle_ventas dv
               INNER JOIN ventas v ON dv.venta_id = v.id
               INNER JOIN productos p ON dv.producto_id = p.id
               WHERE v.jornada_id = ?
               ORDER BY v.fecha DESC""",
            arrayOf(jornadaId.toString())
        )
        return cursor.use { c ->
            val list = mutableListOf<DetalleVenta>()
            while (c.moveToNext()) {
                list.add(DetalleVenta(
                    id             = c.getInt(c.getColumnIndexOrThrow("id")),
                    ventaId        = c.getInt(c.getColumnIndexOrThrow("venta_id")),
                    productoId     = c.getInt(c.getColumnIndexOrThrow("producto_id")),
                    nombreProducto = c.getString(c.getColumnIndexOrThrow("nombre_producto")),
                    cantidad       = c.getDouble(c.getColumnIndexOrThrow("cantidad")),
                    precioUnitario = c.getDouble(c.getColumnIndexOrThrow("precio_unitario")),
                    costoUnitario  = c.getDouble(c.getColumnIndexOrThrow("costo_unitario"))
                ))
            }
            list
        }
    }
}
