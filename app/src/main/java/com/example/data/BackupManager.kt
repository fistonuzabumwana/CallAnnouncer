package com.example.data

import android.content.Context
import android.net.Uri
import com.example.util.DeviceStorage
import org.json.JSONArray
import org.json.JSONObject

/** Saves settings, contact rules and spam numbers to a file the user picks, and restores them. */
object BackupManager {

    private const val FORMAT = "call-announcer-backup"
    private const val VERSION = 1

    /** Stores copied as-is; the privacy consent is deliberately left out so it is given on each phone. */
    private val STORES = listOf("call_announcer_prefs", "contact_preferences", "spam_numbers")
    private val EXCLUDED_KEYS = setOf("accepted_privacy_version")

    fun export(context: Context, uri: Uri) {
        val stores = JSONObject()
        STORES.forEach { name ->
            val values = JSONObject()
            DeviceStorage.prefs(context, name).all.forEach { (key, value) ->
                if (key in EXCLUDED_KEYS || value == null) return@forEach
                values.put(key, encode(value))
            }
            stores.put(name, values)
        }
        val root = JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("createdAt", System.currentTimeMillis())
            .put("stores", stores)
        context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(root.toString(2).toByteArray()) }
            ?: error("Cannot write the backup file")
    }

    /** Replaces the current settings with the backup; throws if the file is not a backup. */
    fun import(context: Context, uri: Uri) {
        val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
            ?: error("Cannot read the file")
        val root = JSONObject(text)
        require(root.optString("format") == FORMAT) { "This file is not a Call Announcer backup" }
        val stores = root.getJSONObject("stores")
        STORES.forEach { name ->
            val values = stores.optJSONObject(name) ?: return@forEach
            val editor = DeviceStorage.prefs(context, name).edit()
            val kept = DeviceStorage.prefs(context, name).all.filterKeys { it in EXCLUDED_KEYS }
            editor.clear()
            kept.forEach { (key, value) -> putValue(editor, key, value) }
            values.keys().forEach { key ->
                if (key !in EXCLUDED_KEYS) putValue(editor, key, decode(values.getJSONObject(key)))
            }
            editor.commit()
        }
        AppSettings.getInstance(context).reload()
        ContactPreferences.getInstance(context).reload()
    }

    private fun encode(value: Any): JSONObject = when (value) {
        is Boolean -> JSONObject().put("t", "b").put("v", value)
        is Int -> JSONObject().put("t", "i").put("v", value)
        is Long -> JSONObject().put("t", "l").put("v", value)
        is Float -> JSONObject().put("t", "f").put("v", value.toDouble())
        is Set<*> -> JSONObject().put("t", "set").put("v", JSONArray(value.map { it.toString() }))
        else -> JSONObject().put("t", "s").put("v", value.toString())
    }

    private fun decode(o: JSONObject): Any? = when (o.optString("t")) {
        "b" -> o.getBoolean("v")
        "i" -> o.getInt("v")
        "l" -> o.getLong("v")
        "f" -> o.getDouble("v").toFloat()
        "set" -> o.getJSONArray("v").let { array -> (0 until array.length()).map { array.getString(it) }.toSet() }
        "s" -> o.getString("v")
        else -> null
    }

    private fun putValue(editor: android.content.SharedPreferences.Editor, key: String, value: Any?) {
        @Suppress("UNCHECKED_CAST")
        when (value) {
            is Boolean -> editor.putBoolean(key, value)
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is Float -> editor.putFloat(key, value)
            is Set<*> -> editor.putStringSet(key, value as Set<String>)
            is String -> editor.putString(key, value)
        }
    }
}
