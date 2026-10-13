package com.example

import com.example.data.HeadphoneMode
import com.example.i18n.AppLanguage
import com.example.i18n.FrenchStrings
import com.example.i18n.KinyarwandaStrings
import com.example.ui.ScreenTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TranslationsTest {

    private val literal = "\"(?:[^\"\\\\$]|\\\\.|\\$(?!\\{))*\""
    private val trCall = Regex("""\btr\(\s*($literal(?:\s*\+\s*$literal)*)""")

    private fun unescape(s: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                when (val n = s[i + 1]) {
                    'u' -> { out.append(s.substring(i + 2, i + 6).toInt(16).toChar()); i += 6; continue }
                    'n' -> out.append('\n')
                    't' -> out.append('\t')
                    else -> out.append(n)
                }
                i += 2
            } else {
                out.append(c)
                i++
            }
        }
        return out.toString()
    }

    private fun sourceKeys(): Set<String> {
        val root = listOf(File("src/main/java"), File("app/src/main/java")).first { it.isDirectory }
        return root.walk()
            .filter { it.extension == "kt" && "i18n" !in it.path }
            .flatMap { file ->
                trCall.findAll(file.readText()).map { match ->
                    Regex(literal).findAll(match.groupValues[1])
                        .joinToString("") { unescape(it.value.substring(1, it.value.length - 1)) }
                }
            }
            .toSet()
    }

    private fun labelKeys(): Set<String> =
        ScreenTab.entries.flatMap { listOf(it.label, it.title) }.toSet() +
            HeadphoneMode.entries.map { it.label } +
            AppLanguage.entries.map { it.label } +
            listOf("Paused", "Setup needed", "Active", "Unknown number", "Saved contact", "Rwandan name", "MTN contact")

    @Test
    fun everyScreenTextHasFrenchAndKinyarwanda() {
        val keys = sourceKeys() + labelKeys()
        assertTrue("found too few keys: ${keys.size}", keys.size > 200)
        val missingFrench = keys - FrenchStrings.table.keys
        val missingKinyarwanda = keys - KinyarwandaStrings.table.keys
        assertEquals("Missing French: $missingFrench", emptySet<String>(), missingFrench)
        assertEquals("Missing Kinyarwanda: $missingKinyarwanda", emptySet<String>(), missingKinyarwanda)
    }

    @Test
    fun placeholdersMatchTheEnglish() {
        val placeholder = Regex("%[sd]")
        for (table in listOf(FrenchStrings.table, KinyarwandaStrings.table)) {
            for ((english, translated) in table) {
                assertEquals(
                    "Placeholders differ for \"$english\"",
                    placeholder.findAll(english).map { it.value }.toList(),
                    placeholder.findAll(translated).map { it.value }.toList()
                )
            }
        }
    }
}
