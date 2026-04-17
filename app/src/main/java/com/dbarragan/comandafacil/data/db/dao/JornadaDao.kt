package com.dbarragan.comandafacil.data.db.dao

import android.content.ContentValues
import android.database.Cursor
import com.dbarragan.comandafacil.data.db.ComandaFacilDatabase
import com.dbarragan.comandafacil.data.model.Jornada

class JornadaDao(private val db: ComandaFacilDatabase) {

    fun insertar(j: Jornada): Long {
        val cv = ContentValues().apply {
            put("fecha", j.fecha)
            put("estado", j.estado)
            put("total_ingresos", j.totalIngresos)
            put("total_costos", j.totalCostos)
            put("total_gastos", j.totalGastos)
            put("meta_ganancia", j.metaGanancia)
        }
        return db.writableDatabase.insert("jornadas", null, cv)
    }

    fun obtenerPorFecha(fecha: String): Jornada? {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT * FROM jornadas WHERE fecha = ?", arrayOf(fecha)
        )
        return cursor.use { if (it.moveToFirst()) it.toJornada() else null }
    }

    fun obtenerPorId(id: Int): Jornada? {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT * FROM jornadas WHERE id = ?", arrayOf(id.toString())
        )
        return cursor.use { if (it.moveToFirst()) it.toJornada() else null }
    }

    fun listarTodas(): List<Jornada> {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT * FROM jornadas ORDER BY fecha DESC", null
        )
        return cursor.use { c ->
            val list = mutableListOf<Jornada>()
            while (c.moveToNext()) list.add(c.toJornada())
            list
        }
    }

    /** Recalcula y guarda los totales de la jornada desde las ventas y gastos */
    fun recalcularTotales(jornadaId: Int) {
        val ingresos = db.readableDatabase.rawQuery(
            """SELECT COALESCE(SUM(dv.cantidad * dv.precio_unitario), 0)
               FROM detalle_ventas dv
               INNER JOIN ventas v ON dv.venta_id = v.id
               WHERE v.jornada_id = ?""",
            arrayOf(jornadaId.toString())
        ).use { it.moveToFirst(); it.getDouble(0) }

        val costos = db.readableDatabase.rawQuery(
            """SELECT COALESCE(SUM(dv.cantidad * dv.costo_unitario), 0)
               FROM detalle_ventas dv
               INNER JOIN ventas v ON dv.venta_id = v.id
               WHERE v.jornada_id = ?""",
            arrayOf(jornadaId.toString())
        ).use { it.moveToFirst(); it.getDouble(0) }

        val gastos = db.readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(monto), 0) FROM gastos_operativos WHERE jornada_id = ?",
            arrayOf(jornadaId.toString())
        ).use { it.moveToFirst(); it.getDouble(0) }

        val cv = ContentValues().apply {
            put("total_ingresos", ingresos)
            put("total_costos", costos)
            put("total_gastos", gastos)
        }
        db.writableDatabase.update("jornadas", cv, "id = ?", arrayOf(jornadaId.toString()))
    }

    fun cerrar(jornadaId: Int) {
        recalcularTotales(jornadaId)
        val cv = ContentValues().apply { put("estado", "cerrada") }
        db.writableDatabase.update("jornadas", cv, "id = ?", arrayOf(jornadaId.toString()))
    }

    private fun Cursor.toJornada() = Jornada(
        id             = getInt(getColumnIndexOrThrow("id")),
        fecha          = getString(getColumnIndexOrThrow("fecha")),
        estado         = getString(getColumnIndexOrThrow("estado")),
        totalIngresos  = getDouble(getColumnIndexOrThrow("total_ingresos")),
        totalCostos    = getDouble(getColumnIndexOrThrow("total_costos")),
        totalGastos    = getDouble(getColumnIndexOrThrow("total_gastos")),
        metaGanancia   = getDouble(getColumnIndexOrThrow("meta_ganancia"))
    )
}
