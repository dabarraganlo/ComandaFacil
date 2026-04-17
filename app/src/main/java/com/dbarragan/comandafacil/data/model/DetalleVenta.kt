package com.dbarragan.comandafacil.data.model

/** Línea de detalle de una venta (un producto vendido) */
data class DetalleVenta(
    val id: Int = 0,
    val ventaId: Int,
    val productoId: Int,
    val nombreProducto: String = "",
    val cantidad: Double,
    val precioUnitario: Double,
    val costoUnitario: Double
) {
    val subtotalIngreso: Double get() = cantidad * precioUnitario
    val subtotalCosto: Double   get() = cantidad * costoUnitario
    val gananciaLinea: Double   get() = subtotalIngreso - subtotalCosto
}
