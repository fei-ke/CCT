package com.example.cct

import android.content.Context
import android.content.SharedPreferences

object Pref {
    private var preferences: SharedPreferences? = null

    private fun getPreference(context: Context) = preferences
            ?: context.getSharedPreferences(Constants.IGNORE_LIST_PREF_NAME, Context.MODE_PRIVATE)

    fun getAll(context: Context): Map<String, *> =
            getPreference(context).all

    fun hasKey(context: Context, key: String): Boolean =
            getPreference(context).contains(key)

    fun add(context: Context, key: String, pattern: String) =
            getPreference(context).edit().putString(key, pattern).apply()

    fun remove(context: Context, key: String) =
            getPreference(context).edit().remove(key).apply()


    fun isInIgnoreList(context: Context, url: String): Boolean =
            getPreference(context).all.values.find { Regex(it.toString()).matches(url) } != null

}