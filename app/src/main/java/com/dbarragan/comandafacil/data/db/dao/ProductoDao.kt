package com.dbarragan.comandafacil.data.db.dao

import android.content.ContentValues
import android.database.Cursor
import com.dbarragan.comandafacil.data.db.ComandaFacilDatabase
import com.dbarragan.comandafacil.data.model.Producto

class ProductoDao(private val db: ComandaFacilDatabase) {

    fun insertar(p: Producto): Long {
        val cv = productoToValues(p)
        return db.writableDatabase.insert("productos", null, cv)
    }

    fun actualizar(p: Producto) {
        val cv = productoToValues(p)
        db.writableDatabase.update("productos", cv, "id = ?", arrayOf(p.id.toString()))
    }

    fun actualizarStock(productoId: Int, nuevoStock: Double) {
        val cv = ContentValues().apply { put("stock_actual", nuevoStock) }
        db.writableDatabase.update("productos", cv, "id = ?", arrayOf(productoId.toString()))
    }

    fun listarActivos(): List<Producto> {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT * FROM productos WHERE activo = 1 ORDER BY nombre ASC", null
        )
        return cursor.use { it.toList() }
    }

    fun obtenerPorId(id: Int): Producto? {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT * FROM productos WHERE id = ?", arrayOf(id.toString())
        )
        return cursor.use { if (it.moveToFirst()) it.toProducto() else null }
    }

    fun listarConStockBajo(): List<Producto> {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT * FROM productos WHERE activo = 1 AND stock_minimo > 0 AND stock_actual <= stock_minimo",
            null
        )
        return cursor.use { it.toList() }
    }

    private fun productoToValues(p: Producto) = ContentValues().apply {
        put("nombre", p.nombre)
        put("costo_ingredientes", p.costoIngredientes)
        put("costo_fijo_prorrateado", p.costoFijoProrrateado)
        put("margen_ganancia", p.margenGanancia)
        put("precio_sugerido", p.precioSugerido)
        put("stock_actual", p.stockActual)
        put("stock_minimo", p.stockMinimo)
        put("activo", if (p.activo) 1 else 0)
        put("fecha_creacion", p.fechaCreacion)
    }

    private fun Cursor.toProducto() = Producto(
        id                  = getInt(getColumnIndexOrThrow("id")),
        nombre              = getString(getColumnIndexOrThrow("nombre")),
        costoIngredientes   = getDouble(getColumnIndexOrThrow("costo_ingredientes")),
        costoFijoProrrateado= getDouble(getColumnIndexOrThrow("costo_fijo_prorrateado")),
        margenGanancia      = getDouble(getColumnIndexOrThrow("margen_ganancia")),
        stockActual         = getDouble(getColumnIndexOrThrow("stock_actual")),
        stockMinimo         = getDouble(getColumnIndexOrThrow("stock_minimo")),
        activo              = getInt(getColumnIndexOrThrow("activo")) == 1,
        fechaCreacion       = getString(getColumnIndexOrThrow("fecha_creacion"))
    )

    private fun Cursor.toList(): List<Producto> {
        val list = mutableListOf<Producto>()
        while (moveToNext()) list.add(toProducto())
        return list
    }
}
