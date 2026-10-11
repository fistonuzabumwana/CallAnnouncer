package com.example.util

enum class PairSpokenMode(val title: String, val description: String) {
    PAIRED_NUMBERS("Paired Numbers (zero seven, 85, 46)", "Reads standard two-digit number groups, pronouncing 0 as 'zero'"),
    PAIRED_DIGIT_WORDS("Spoken Digit Words (zero seven, eight five, four six)", "Spells out each digit in pairs for maximum clarity"),
    PAIRED_SPACED_DIGITS("Spaced Digits (zero seven, 8 5, 4 6)", "Separates digits with micro-pauses")
}

object PhoneNumberFormatter {

    val DIGIT_WORDS = mapOf(
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
     * Splits a phone number into pairs of 2 digits for visual display (e.g., "07 85 46").
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
     * Formats a 2-digit chunk (or single trailing digit) for speech.
     * Ensures leading zero (e.g. "07", "05", "00") is explicitly pronounced as "zero seven",
     * never dropping the zero or saying just "seven".
     */
    fun formatPairChunkForSpeech(chunk: String): String {
        return when {
            chunk.length == 2 && chunk[0] == '0' -> {
                // e.g. "07" -> "zero seven", "00" -> "zero zero", "05" -> "zero five"
                val secondWord = DIGIT_WORDS[chunk[1]] ?: chunk[1].toString()
                "zero $secondWord"
            }
            chunk == "0" -> "zero"
            else -> chunk
        }
    }

    /**
     * Formats the phone number for Text-To-Speech according to the selected PairSpokenMode.
     * Uses commas to introduce natural cadence and pauses between the two-digit pairs.
     * Pairs with leading zero like "07" or zeros in between like "05", "00" are pronounced as "zero seven", etc.
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
                // e.g. "zero seven, 85, 46"
                // Adding commas creates distinct pauses between two numbers at a time,
                // and 07 is pronounced as "zero seven", not just "seven"
                val pairs = digits.chunked(2).map { formatPairChunkForSpeech(it) }
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
                // e.g. "zero seven, 8 5, 4 6"
                val pairs = digits.chunked(2).map { chunk ->
                    if (chunk.length == 2 && chunk[0] == '0') {
                        val secondWord = DIGIT_WORDS[chunk[1]] ?: chunk[1].toString()
                        "zero $secondWord"
                    } else {
                        chunk.toCharArray().joinToString(" ")
                    }
                }
                prefix + pairs.joinToString(", ")
            }
        }
    }
}
