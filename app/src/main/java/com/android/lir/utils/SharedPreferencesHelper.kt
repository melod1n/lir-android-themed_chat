package com.android.lir.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class SharedPreferencesHelper private constructor(context: Context) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun saveToPrefs(key: String?, value: Long) {
        val editor = prefs.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun saveToPrefs(key: String?, value: String?) {
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getStringFromPrefs(key: String?): String? {
        return try {
            prefs.getString(key, null)
        } catch (e: Exception) {
            null
        }
    }

    fun getLongFromPrefs(key: String?): Long {
        return try {
            prefs.getLong(key, 0)
        } catch (e: Exception) {
            0
        }
    }

    fun getBooleanFromPrefs(key: String?): Boolean {
        return try {
            prefs.getBoolean(key, false)
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private var instance: SharedPreferencesHelper? = null
        fun init(context: Context): SharedPreferencesHelper? {
            if (instance == null) {
                instance = SharedPreferencesHelper(context)
            }
            return instance
        }

        @JvmStatic
        fun get(): SharedPreferencesHelper? {
            checkNotNull(instance) { "SharedPreferencesHelper is not initialized" }
            return instance
        }
    }
}
