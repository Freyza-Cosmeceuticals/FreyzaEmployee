package com.freyza.employee.core.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPreferencesHelper(context: Context) {

  companion object {
    private const val PREF_KEY = "PREF"
  }

  private val sharedPreferences: SharedPreferences =
    context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)

  fun saveStringData(key: String, data: String?) {
    if (data == null) {
      removeStringData(key)
    } else {
      sharedPreferences.edit { putString(key, data) }
    }
  }

  fun getStringData(key: String): String? {
    return sharedPreferences.getString(key, null)
  }

  fun removeStringData(key: String) {
    sharedPreferences.edit { remove(key) }
  }

  fun clearAll() {
    sharedPreferences.edit { clear() }
  }
}
