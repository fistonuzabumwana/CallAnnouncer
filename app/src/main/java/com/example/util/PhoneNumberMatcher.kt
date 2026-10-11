package com.example.util

object PhoneNumberMatcher {

    private const val SIGNIFICANT_DIGITS = 9

    /**
     * True when two numbers refer to the same line, e.g. "+250722936482" (call screening)
     * and "0722936482" (phone-state broadcast). Compares the last 9 digits.
     */
    fun isSameNumber(a: String?, b: String?): Boolean {
        if (a == null || b == null) return false
        val digitsA = a.filter { it.isDigit() }
        val digitsB = b.filter { it.isDigit() }
        val compareLength = minOf(SIGNIFICANT_DIGITS, digitsA.length, digitsB.length)
        if (compareLength == 0) return digitsA == digitsB
        return digitsA.takeLast(compareLength) == digitsB.takeLast(compareLength)
    }
}
