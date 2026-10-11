package com.example.util

enum class PairSpokenMode(val title: String, val description: String) {
    PAIRED_NUMBERS("Paired Numbers (07, 85, 46)", "Reads standard two-digit number groups with pauses"),
    PAIRED_DIGIT_WORDS("Spoken Digit Words (zero seven, eight five, four six)", "Spells out each digit in pairs for maximum clarity"),
    PAIRED_SPACED_DIGITS("Spaced Digits (0 7, 8 5, 4 6)", "Separates each digit with micro-pauses")
}

object PhoneNumberFormatter {

    private val DIGIT_WORDS = mapOf(
        '0' to "zero",
        '1' to "one",
        '2' to "two",
        '3' to "three",
        '4' to "four",
        '5' to "five",
        '6' to "six",
        '7' to "seven",
        '8' to "eight",
        '9' to "nine"
    )

    /**
     * Splits a phone number into pairs of 2 digits (e.g., "07 85 46").
     */
    fun formatInPairs(rawNumber: String): String {
        val trimmed = rawNumber.trim()
        val hasPlus = trimmed.startsWith("+")
        val digits = trimmed.filter { it.isDigit() }

        if (digits.isEmpty()) {
            return rawNumber.ifBlank { "Unknown" }
        }

        val pairs = digits.chunked(2)
        val result = pairs.joinToString(" ")
        return if (hasPlus) "+$result" else result
    }

    /**
     * Formats the phone number for Text-To-Speech according to the selected PairSpokenMode.
     * Uses commas to introduce natural cadence and pauses between the two-digit pairs.
     */
    fun formatForSpeech(rawNumber: String, mode: PairSpokenMode = PairSpokenMode.PAIRED_NUMBERS): String {
        val trimmed = rawNumber.trim()
        val hasPlus = trimmed.startsWith("+")
        val digits = trimmed.filter { it.isDigit() }

        if (digits.isEmpty()) {
            return "unknown number"
        }

        val prefix = if (hasPlus) "plus, " else ""

        return when (mode) {
            PairSpokenMode.PAIRED_NUMBERS -> {
                // e.g. "07, 85, 46"
                // Adding commas creates distinct pauses between two numbers at a time
                val pairs = digits.chunked(2)
                prefix + pairs.joinToString(", ")
            }
            PairSpokenMode.PAIRED_DIGIT_WORDS -> {
                // e.g. "zero seven, eight five, four six"
                val pairs = digits.chunked(2).map { chunk ->
                    chunk.map { DIGIT_WORDS[it] ?: it.toString() }.joinToString(" ")
                }
                prefix + pairs.joinToString(", ")
            }
            PairSpokenMode.PAIRED_SPACED_DIGITS -> {
                // e.g. "0 7, 8 5, 4 6"
                val pairs = digits.chunked(2).map { chunk ->
                    chunk.toCharArray().joinToString(" ")
                }
                prefix + pairs.joinToString(", ")
            }
        }
    }
}
