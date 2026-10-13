package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.util.DeviceStorage
import com.example.util.SpamChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

enum class ContactRule(val label: String) {
    NORMAL("Normal"),
    /** Always announced, even during quiet hours or when contacts are switched off. */
    VIP("Always announce"),
    NEVER("Never announce")
}

/** How one caller is announced, keyed by the last digits of their number. */
data class ContactPreference(
    val key: String,
    val displayName: String,
    val spokenName: String? = null,
    val rule: ContactRule = ContactRule.NORMAL
)

class ContactPreferences private constructor(context: Context) {

    private val lock = Any()
    private var prefs: SharedPreferences
    private val _all: MutableStateFlow<List<ContactPreference>>
    val all: StateFlow<List<ContactPreference>>

    init {
        synchronized(lock) {
            prefs = DeviceStorage.prefs(context, PREFS_NAME) { migrated ->
                synchronized(lock) {
                    prefs = migrated
                    _all.value = load()
                }
            }
            _all = MutableStateFlow(load())
            all = _all.asStateFlow()
        }
    }

    fun find(number: String?): ContactPreference? {
        val key = SpamChecker.keyOf(number) ?: return null
        return _all.value.firstOrNull { it.key == key }
    }

    fun save(number: String?, displayName: String, spokenName: String?, rule: ContactRule) {
        val key = SpamChecker.keyOf(number) ?: return
        val entry = ContactPreference(key, displayName, spokenName?.trim()?.takeIf { it.isNotEmpty() }, rule)
        update { list ->
            val rest = list.filterNot { it.key == key }
            if (entry.spokenName == null && entry.rule == ContactRule.NORMAL) rest else rest + entry
        }
    }

    fun remove(key: String) = update { list -> list.filterNot { it.key == key } }

    /** Re-reads the stored values, e.g. after a backup was restored. */
    fun reload() = synchronized(lock) {
        _all.value = load()
    }

    private fun update(transform: (List<ContactPreference>) -> List<ContactPreference>) = synchronized(lock) {
        val updated = transform(_all.value).sortedBy { it.displayName.lowercase() }
        prefs.edit().putString(KEY_ENTRIES, toJson(updated).toString()).apply()
        _all.value = updated
    }

    private fun load(): List<ContactPreference> = try {
        fromJson(JSONArray(prefs.getString(KEY_ENTRIES, "[]") ?: "[]"))
    } catch (_: Exception) {
        emptyList()
    }

    companion object {
        private const val PREFS_NAME = "contact_preferences"
        private const val KEY_ENTRIES = "entries"

        fun toJson(entries: List<ContactPreference>): JSONArray = JSONArray().apply {
            entries.forEach { e ->
                put(JSONObject().apply {
                    put("key", e.key)
                    put("name", e.displayName)
                    e.spokenName?.let { put("spoken", it) }
                    put("rule", e.rule.name)
                })
            }
        }

        fun fromJson(array: JSONArray): List<ContactPreference> = (0 until array.length()).mapNotNull { i ->
            val o = array.optJSONObject(i) ?: return@mapNotNull null
            val key = o.optString("key").takeIf { it.isNotBlank() } ?: return@mapNotNull null
            ContactPreference(
                key = key,
                displayName = o.optString("name", key),
                spokenName = o.optString("spoken").takeIf { it.isNotBlank() },
                rule = ContactRule.entries.firstOrNull { it.name == o.optString("rule") } ?: ContactRule.NORMAL
            )
        }

        @Volatile
        private var INSTANCE: ContactPreferences? = null

        fun getInstance(context: Context): ContactPreferences =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ContactPreferences(context.applicationContext).also { INSTANCE = it }
            }
    }
}
