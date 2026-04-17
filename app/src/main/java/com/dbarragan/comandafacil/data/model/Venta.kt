package com.dbarragan.comandafacil.data.model

/** Encabezado de una venta registrada en la jornada */
data class Venta(
    val id: Int = 0,
    val jornadaId: Int,
    val fecha: String   // ISO 8601: yyyy-MM-dd HH:mm:ss
)
