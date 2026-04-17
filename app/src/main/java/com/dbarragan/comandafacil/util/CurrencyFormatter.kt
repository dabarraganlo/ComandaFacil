package com.dbarragan.comandafacil.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val fmt = NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    /** Formatea un Double como pesos colombianos: "$12.500" */
    fun format(value: Double): String = "$${fmt.format(value)}"

    /** Formatea con signo: "+$12.500" o "-$3.000" */
    fun formatConSigno(value: Double): String =
        if (value >= 0) "+${format(value)}" else "-${format(-value)}"
}
