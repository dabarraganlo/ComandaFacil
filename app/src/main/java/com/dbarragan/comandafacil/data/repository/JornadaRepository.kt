package com.dbarragan.comandafacil.data.repository

import android.content.Context
import com.dbarragan.comandafacil.data.db.ComandaFacilDatabase
import com.dbarragan.comandafacil.data.db.dao.GastoDao
import com.dbarragan.comandafacil.data.db.dao.JornadaDao
import com.dbarragan.comandafacil.data.model.GastoOperativo
import com.dbarragan.comandafacil.data.model.Jornada
import com.dbarragan.comandafacil.util.DateHelper

class JornadaRepository(context: Context) {

    private val db         = ComandaFacilDatabase(context)
    private val jornadaDao = JornadaDao(db)
    private val gastoDao   = GastoDao(db)

    /** Devuelve la jornada de hoy. Si no existe, la crea con estado 'abierta'. */
    fun obtenerOCrearJornadaHoy(): Jornada {
        val hoy = DateHelper.hoy()
        return jornadaDao.obtenerPorFecha(hoy)
            ?: run {
                val nueva = Jornada(fecha = hoy)
                val id = jornadaDao.insertar(nueva)
                nueva.copy(id = id.toInt())
            }
    }

    fun obtenerJornadaHoy(): Jornada? = jornadaDao.obtenerPorFecha(DateHelper.hoy())

    fun listarHistorial(): List<Jornada> = jornadaDao.listarTodas()

    fun registrarGasto(gasto: GastoOperativo) {
        gastoDao.insertar(gasto)
        jornadaDao.recalcularTotales(gasto.jornadaId)
    }

    fun listarGastos(jornadaId: Int): List<GastoOperativo> = gastoDao.listarPorJornada(jornadaId)

    /** Cierra la jornada: calcula totales finales y cambia el estado a 'cerrada'. */
    fun cerrarJornada(jornadaId: Int): Jornada {
        jornadaDao.cerrar(jornadaId)
        return jornadaDao.obtenerPorId(jornadaId)!!
    }
}
