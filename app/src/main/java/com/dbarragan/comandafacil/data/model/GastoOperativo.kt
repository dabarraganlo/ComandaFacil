package com.dbarragan.comandafacil.data.model

/** Gasto operativo (fijo o variable) registrado en una jornada */
data class GastoOperativo(
    val id: Int = 0,
    val jornadaId: Int,
    // ¿En qué gastaste? (lenguaje cotidiano, sin términos técnicos)
    val descripcion: String,
    val monto: Double,
    val tipo: String = "variable",   // "fijo" | "variable"
    val fecha: String
)
