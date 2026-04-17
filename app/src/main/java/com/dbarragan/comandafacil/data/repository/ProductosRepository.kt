package com.dbarragan.comandafacil.data.repository

import android.content.Context
import com.dbarragan.comandafacil.data.db.ComandaFacilDatabase
import com.dbarragan.comandafacil.data.db.dao.ProductoDao
import com.dbarragan.comandafacil.data.model.Producto
import com.dbarragan.comandafacil.util.DateHelper

class ProductosRepository(context: Context) {

    private val dao = ProductoDao(ComandaFacilDatabase(context))

    fun listarActivos(): List<Producto> = dao.listarActivos()

    fun listarConStockBajo(): List<Producto> = dao.listarConStockBajo()

    fun obtenerPorId(id: Int): Producto? = dao.obtenerPorId(id)

    fun guardar(p: Producto): Long {
        val conFecha = if (p.fechaCreacion.isEmpty()) p.copy(fechaCreacion = DateHelper.ahora()) else p
        return if (p.id == 0) dao.insertar(conFecha) else { dao.actualizar(conFecha); p.id.toLong() }
    }

    fun actualizarStock(productoId: Int, nuevoStock: Double) =
        dao.actualizarStock(productoId, nuevoStock)
}
