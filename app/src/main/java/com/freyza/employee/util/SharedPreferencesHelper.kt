package com.freyza.employee.util

import android.content.Context
import androidx.core.content.edit

class SharedPreferencesHelper(private val context: Context) {

    companion object {
        private const val PREF_KEY = "PREF"
    }

    fun saveStringData(key: String, data: String?) {
        val sharedPreferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
        sharedPreferences.edit { putString(key, data) }
    }

    fun getStringData(key: String): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, null)
    }

    fun removeStringData(key: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
        sharedPreferences.edit { remove(key) }
    }

}
