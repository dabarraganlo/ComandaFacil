package com.dbarragan.comandafacil.data.db.dao

import android.content.ContentValues
import android.database.Cursor
import com.dbarragan.comandafacil.data.db.ComandaFacilDatabase
import com.dbarragan.comandafacil.data.model.GastoOperativo

class GastoDao(private val db: ComandaFacilDatabase) {

    fun insertar(g: GastoOperativo): Long {
        val cv = ContentValues().apply {
            put("jornada_id", g.jornadaId)
            put("descripcion", g.descripcion)
            put("monto", g.monto)
            put("tipo", g.tipo)
            put("fecha", g.fecha)
        }
        return db.writableDatabase.insert("gastos_operativos", null, cv)
    }

    fun listarPorJornada(jornadaId: Int): List<GastoOperativo> {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT * FROM gastos_operativos WHERE jornada_id = ? ORDER BY fecha DESC",
            arrayOf(jornadaId.toString())
        )
        return cursor.use { c ->
            val list = mutableListOf<GastoOperativo>()
            while (c.moveToNext()) {
                list.add(GastoOperativo(
                    id          = c.getInt(c.getColumnIndexOrThrow("id")),
                    jornadaId   = c.getInt(c.getColumnIndexOrThrow("jornada_id")),
                    descripcion = c.getString(c.getColumnIndexOrThrow("descripcion")),
                    monto       = c.getDouble(c.getColumnIndexOrThrow("monto")),
                    tipo        = c.getString(c.getColumnIndexOrThrow("tipo")),
                    fecha       = c.getString(c.getColumnIndexOrThrow("fecha"))
                ))
            }
            list
        }
    }
}
