package com.dbarragan.comandafacil.data.model

/**
 * Jornada de trabajo diaria.
 *
 * Al cierre calcula:
 *   gananciaNeta = totalIngresos - totalCostos - totalGastos
 */
data class Jornada(
    val id: Int = 0,
    val fecha: String,                   // YYYY-MM-DD
    val estado: String = "abierta",      // "abierta" | "cerrada"
    val totalIngresos: Double = 0.0,
    val totalCostos: Double = 0.0,
    val totalGastos: Double = 0.0,
    val metaGanancia: Double = 0.0
) {
    /** Ganancia neta real del día */
    val gananciaNeta: Double
        get() = totalIngresos - totalCostos - totalGastos

    /** Margen real obtenido sobre los ingresos totales (%) */
    val margenReal: Double
        get() = if (totalIngresos <= 0.0) 0.0 else (gananciaNeta / totalIngresos) * 100.0

    /** Porcentaje de avance hacia la meta (0-100, puede superar 100) */
    val progresoMeta: Int
        get() = if (metaGanancia <= 0.0) 0
                else ((gananciaNeta / metaGanancia) * 100).toInt().coerceAtLeast(0)

    val metaAlcanzada: Boolean
        get() = metaGanancia > 0.0 && gananciaNeta >= metaGanancia

    val estaCerrada: Boolean
        get() = estado == "cerrada"
}
