package com.example.util

import java.text.Normalizer

/** Part of an announcement spoken with one voice; a null language means the announcement's own voice. */
data class SpeechSegment(val text: String, val languageTag: String? = null)

/**
 * Makes caller names sound right. English voices read Kinyarwanda names with English spelling rules
 * ("Uwimana" becomes "You-wim-ana"), so names are spoken with the closest installed voice instead:
 * Swahili for Rwandan/Bantu names (same pure vowels and sounds like ny, mb, nd), French for French
 * names. Without a Swahili voice, Rwandan names are respelled so an English voice reads them correctly.
 */
object NamePronunciation {

    enum class Origin { RWANDAN, FRENCH, OTHER }

    const val SWAHILI = "sw"
    const val FRENCH = "fr"
    private const val ENGLISH = "en"
    private val NATIVE_BANTU_VOICES = setOf("sw", "rw", "rn", "lg")

    private const val VOWELS = "aeiou"

    // Kinyarwanda syllables are open: an onset of (nasal +) consonant (+ y/w), then one vowel
    private val validOnset = Regex("^[mn]?(sh|ts|pf|[bcdfghjkprstvz])?[yw]?$|^ny[w]?$")
    private val notKinyarwanda = Regex("[lqx]|th|ph|ck|ee|oo|ou|ea|ss|tt|pp|ff")
    private val strongStart = Regex("^(uw|ny|nsh|nk|nt|nd|mb|mp|ng|nz|ns|rw|mw|bw|cy|shy|by|jy|tw|kw|gw)")
    private val strongInside = Regex("nsh|cy|shy|jy|rw|mw|bw|gw|nyw|yi|iy|uy|imana|yimana|kunda|mahoro|gisha|ishimwe|izer|nzi|nganz|hagi|habi|hakiz|niyo|nyiri|uwa|uwi|uwe|mut|muk|mug|ruk|rut|kam|tuy|dus")

    private val commonRwandanNames = setOf(
        "uwimana", "niyonzima", "habimana", "mugisha", "nshimiyimana", "ingabire", "uwase", "kwizera",
        "iradukunda", "niyonsaba", "mukamana", "uwamahoro", "mukeshimana", "hakizimana", "nsengiyumva",
        "ndayisaba", "niyitegeka", "tuyishime", "ishimwe", "mutoni", "keza", "teta", "gasana", "kamanzi",
        "rukundo", "nkurunziza", "bizimana", "twagirayezu", "munyaneza", "hategekimana", "ndikumana", "uwera",
        "umutoni", "umuhoza", "isimbi", "ganza", "shema", "cyiza", "manzi", "gatete", "mugabo", "sibomana",
        "nsabimana", "nzeyimana", "dusabe", "dukuze", "kagabo", "karangwa", "kayitesi", "mukeshimana",
        "nyiraneza", "uwineza", "umuhire", "hirwa", "gisa", "sangwa", "kalisa", "murenzi", "rwigema",
        "byiringiro", "niyigena", "ndahiro", "nsanzimana", "twizeyimana", "uwamariya", "mukandayisenga",
        "cyusa", "cyubahiro", "cyizere", "shyaka", "shyirambere"
    )

    private val commonFrenchNames = setOf(
        "jean", "claude", "pierre", "jacques", "marie", "françois", "francois", "françoise", "francoise",
        "théogène", "theogene", "théoneste", "theoneste", "innocent", "céline", "celine", "aimable", "fidèle",
        "fidele", "bosco", "didier", "olivier", "thierry", "yves", "gilbert", "gérard", "gerard", "vianney",
        "valens", "félicien", "felicien", "anastase", "alphonse", "chantal", "clarisse", "josiane", "solange",
        "béatrice", "beatrice", "fabrice", "gaspard", "grâce", "évariste", "evariste", "célestin", "celestin",
        "augustin", "faustin", "sylvestre", "callixte", "narcisse", "désiré", "desire", "modeste", "révérien",
        "reverien", "ildephonse", "laurent", "dieudonné", "dieudonne", "aloys", "cyprien", "damascène",
        "damascene", "baptiste", "jeanne", "claudine", "aline", "delphine", "ernestine", "angélique",
        "angelique", "espérance", "esperance", "consolée", "consolee", "vestine", "jeannette", "liliane",
        "pascal", "pascaline", "emmanuel", "emmanuella", "jean-paul", "jean-pierre", "jean-claude",
        "jean-baptiste", "marie-claire", "jean-bosco", "jean-damascène", "jean-damascene", "léonard", "leonard"
    )
    private val frenchAccents = Regex("[éèêëàâçîïôûùœ]")
    private val frenchEndings = Regex("(ien|ienne|ette|eau|aux|oise|ière|iere|ard|ain|aine|ais|aise|ence|ance|ine)$")

    fun originOf(word: String): Origin {
        val lower = word.lowercase().trim().trim('.', ',', '\'', '"')
        if (lower.isEmpty()) return Origin.OTHER
        if (lower in commonFrenchNames || frenchAccents.containsMatchIn(lower)) return Origin.FRENCH
        if (lower.contains('-') && lower.split('-').any { it in commonFrenchNames }) return Origin.FRENCH

        val plain = stripAccents(lower)
        if (plain in commonRwandanNames) return Origin.RWANDAN
        if (looksKinyarwanda(plain)) return Origin.RWANDAN
        if (frenchEndings.containsMatchIn(plain) && plain.length > 4 && notKinyarwanda.containsMatchIn(plain).not() &&
            plain.last() == 'e'
        ) return Origin.FRENCH
        return Origin.OTHER
    }

    private fun looksKinyarwanda(w: String): Boolean {
        if (w.length < 3 || !w.all { it in 'a'..'z' }) return false
        if (w.last() !in VOWELS) return false
        if (notKinyarwanda.containsMatchIn(w)) return false
        val onsets = Regex("([^aeiou]*)[aeiou]").findAll(w).map { it.groupValues[1] }.toList()
        if (onsets.any { !validOnset.matches(it) }) return false
        return strongStart.containsMatchIn(w) || strongInside.containsMatchIn(w)
    }

    /**
     * Rewrites Kinyarwanda spellings that Swahili writes differently, e.g. Cyusa → Chusa
     * (Kinyarwanda "c"/"cy" is the "ch" sound; Swahili has no "cy" and would say "Kyusa").
     */
    fun toSwahiliSpelling(word: String): String {
        var w = word
        w = w.replace(Regex("(?i)cy")) { if (it.value[0].isUpperCase()) "Ch" else "ch" }
        w = w.replace(Regex("(?i)c(?!h)")) { if (it.value[0].isUpperCase()) "Ch" else "ch" }
        w = w.replace(Regex("(?i)(sh|j)y(?=[aeiou])")) { it.groupValues[1] }
        return w
    }

    /** Spells a Kinyarwanda name so an English voice reads it correctly, e.g. Uwimana → Oo-wee-mah-nah. */
    fun respellForEnglish(word: String): String {
        val w = stripAccents(word.lowercase())
        if (w.isEmpty() || !w.all { it in 'a'..'z' } || w.last() !in VOWELS) return word

        data class Syllable(var onset: String, val vowel: Char, var coda: String = "")
        val syllables = Regex("([^aeiou]*)([aeiou])").findAll(w)
            .map { Syllable(it.groupValues[1], it.groupValues[2][0]) }
            .toMutableList()

        // A nasal before a consonant sounds like the end of the previous syllable to English ears: ku-nda → koon-dah
        for (i in 1 until syllables.size) {
            val onset = syllables[i].onset
            if (onset.length >= 2 && onset[0] in "mn" && onset[1] !in "yw" && !onset.startsWith("ny")) {
                syllables[i - 1].coda += onset[0]
                syllables[i].onset = onset.substring(1)
            }
        }

        val spelled = syllables.joinToString("-") { s ->
            val onset = when {
                s.onset == "c" || s.onset == "cy" -> "ch"
                s.onset == "shy" -> "sh"
                s.onset == "jy" -> "j"
                s.onset.endsWith("g") && s.vowel in "ei" -> s.onset + "h"
                else -> s.onset
            }
            val vowel = when (s.vowel) {
                'a' -> "ah"
                'e' -> "eh"
                'i' -> "ee"
                'o' -> "oh"
                else -> "oo"
            }
            onset + vowel + s.coda
        }
        return spelled.replaceFirstChar { it.uppercase() }
    }

    /**
     * Splits [text] so that [name] is spoken with the voice that suits each of its words.
     * [mainLanguage] is the announcement voice's language; [installedLanguages] are offline voice languages.
     */
    fun plan(text: String, name: String?, mainLanguage: String, installedLanguages: Set<String>): List<SpeechSegment> {
        val plain = listOf(SpeechSegment(text))
        if (name.isNullOrBlank()) return plain
        val spokenName = AnnouncementBuilder.sanitizeMtnForSpeech(name.trim())
        val start = text.indexOf(spokenName)
        if (start < 0) return plain

        val main = mainLanguage.lowercase()
        val hasSwahili = SWAHILI in installedLanguages
        val hasFrench = FRENCH in installedLanguages

        val segments = mutableListOf<SpeechSegment>()
        fun add(part: String, language: String?) {
            if (part.isBlank()) return
            val last = segments.lastOrNull()
            if (last != null && last.languageTag == language) {
                segments[segments.lastIndex] = last.copy(text = "${last.text.trimEnd()} ${part.trim()}")
            } else {
                segments += SpeechSegment(part.trim(), language)
            }
        }

        add(text.substring(0, start), null)
        spokenName.split(Regex("\\s+")).forEach { word ->
            when (originOf(word)) {
                Origin.RWANDAN -> when {
                    main in NATIVE_BANTU_VOICES -> add(word, null)
                    hasSwahili -> add(toSwahiliSpelling(word), SWAHILI)
                    main == ENGLISH -> add(respellForEnglish(word), null)
                    else -> add(word, null)
                }
                Origin.FRENCH -> add(word, if (main != FRENCH && hasFrench) FRENCH else null)
                Origin.OTHER -> add(word, null)
            }
        }
        add(text.substring(start + spokenName.length), null)
        return segments.ifEmpty { plain }
    }

    private fun stripAccents(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")
}
