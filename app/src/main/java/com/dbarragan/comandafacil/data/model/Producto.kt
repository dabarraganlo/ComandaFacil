package com.dbarragan.comandafacil.data.model

/**
 * Producto o plato del negocio.
 *
 * Fórmula de precio sugerido (margen sobre costo):
 *   precioSugerido = costoTotal / (1 - margenGanancia / 100)
 *
 * Ejemplo: costo $8.000, margen 30% → precio = 8000 / 0.70 = $11.429
 */
data class Producto(
    val id: Int = 0,
    val nombre: String,
    // ¿Cuánto gastaste en ingredientes para preparar este plato?
    val costoIngredientes: Double,
    // ¿Cuánto del arriendo y servicios le toca a este plato?
    val costoFijoProrrateado: Double,
    // ¿Qué porcentaje de ganancia quieres obtener? (0-99)
    val margenGanancia: Double,
    val stockActual: Double = 0.0,
    val stockMinimo: Double = 0.0,
    val activo: Boolean = true,
    val fechaCreacion: String = ""
) {
    /** Costo total = ingredientes + proporción de gastos fijos */
    val costoTotal: Double
        get() = costoIngredientes + costoFijoProrrateado

    /** Precio de venta sugerido para alcanzar el margen deseado */
    val precioSugerido: Double
        get() {
            val factor = 1.0 - (margenGanancia / 100.0)
            return if (factor <= 0.0) 0.0 else costoTotal / factor
        }

    /** Verdadero si el stock está en o por debajo del mínimo */
    val tieneStockBajo: Boolean
        get() = stockMinimo > 0 && stockActual <= stockMinimo
}
