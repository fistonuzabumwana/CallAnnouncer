package com.example.i18n

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/** Language of the app's screens; SYSTEM follows the phone when it is Kinyarwanda or French. */
enum class AppLanguage(val label: String, val tag: String) {
    SYSTEM("Phone language", ""),
    ENGLISH("English", "en"),
    KINYARWANDA("Kinyarwanda", "rw"),
    FRENCH("Français", "fr");

    companion object {
        fun fromName(name: String?): AppLanguage = entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

/**
 * Translations keyed by the English text, so a missing translation simply shows English.
 * Reading [language] inside a composable makes the screen redraw when the language changes.
 */
object I18n {

    var language by mutableStateOf("en")
        private set

    fun apply(setting: AppLanguage) {
        language = when (setting) {
            AppLanguage.SYSTEM -> Locale.getDefault().language.takeIf { it == "rw" || it == "fr" } ?: "en"
            else -> setting.tag
        }
    }

    internal fun lookup(text: String): String = when (language) {
        "fr" -> FrenchStrings.table[text]
        "rw" -> KinyarwandaStrings.table[text]
        else -> null
    } ?: text
}

/** The English [text] in the app's language; [args] fill %s / %d placeholders. */
fun tr(text: String, vararg args: Any?): String {
    val translated = I18n.lookup(text)
    return if (args.isEmpty()) translated else translated.format(*args)
}
