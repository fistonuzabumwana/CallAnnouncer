package com.example.util

/** Mobile network of a Rwandan number, read from its prefix. */
object CallerNetwork {

    private val PREFIXES = mapOf(
        "078" to "MTN",
        "079" to "MTN",
        "072" to "Airtel",
        "073" to "Airtel"
    )

    /** "MTN", "Airtel" or null when the number is not a Rwandan mobile number. */
    fun of(number: String?): String? {
        if (number.isNullOrBlank()) return null
        val digits = number.filter { it.isDigit() }
        val local = when {
            digits.length == 12 && digits.startsWith("250") -> "0" + digits.drop(3)
            digits.length == 14 && digits.startsWith("00250") -> "0" + digits.drop(5)
            digits.length == 10 && digits.startsWith("0") -> digits
            digits.length == 9 && digits.startsWith("7") && !number.trim().startsWith("+") -> "0$digits"
            else -> return null
        }
        return PREFIXES[local.take(3)]
    }
}
