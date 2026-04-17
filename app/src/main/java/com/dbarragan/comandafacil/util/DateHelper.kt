package com.dbarragan.comandafacil.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateHelper {
    private val fmtFecha   = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val fmtDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val fmtDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun hoy(): String = fmtFecha.format(Date())

    fun ahora(): String = fmtDateTime.format(Date())

    /** Convierte "yyyy-MM-dd" a "dd/MM/yyyy" para mostrar al usuario */
    fun formatoDisplay(fecha: String): String {
        return try {
            val d = fmtFecha.parse(fecha) ?: return fecha
            fmtDisplay.format(d)
        } catch (e: Exception) { fecha }
    }
}
