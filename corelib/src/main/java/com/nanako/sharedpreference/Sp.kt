package com.nanako.sharedpreference

import android.content.Context
import android.content.SharedPreferences
import com.nanako.log.Log
import com.google.gson.Gson
import java.lang.Exception
import java.lang.reflect.Type

object Sp {
    private lateinit var sSharedPreferences: SharedPreferences
    private lateinit var sGson: Gson
    private val log = Log()

    /**
     * call this method in Application's onCreate method
     */
    fun init(context: Context, debug: Boolean) {
        sSharedPreferences = context.getSharedPreferences("P_nanako", Context.MODE_PRIVATE)
        sGson = Gson()
        log.filterTag="[Sp]"
        log.isEnabled = debug
    }

    fun putInt(key: String, value: Int) {
        log.d("key[$key], value[$value]")
        sSharedPreferences.edit().putInt(key, value).commit()
    }

    fun getInt(key: String): Int {
        return getInt(key, 0)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        val value = sSharedPreferences.getInt(key, defaultValue)
        log.d("key[$key], value[$value]")
        return value
    }

    fun putLong(key: String, value: Long) {
        sSharedPreferences.edit().putLong(key, value).commit()
        log.d("key[$key], value[$value]")
    }

    fun getLong(key: String): Long {
        return getLong(key, 0L)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        val value = sSharedPreferences.getLong(key, defaultValue)
        log.d("key[$key], value[$value]")
        return value
    }

    fun putString(key: String, value: String) {
        sSharedPreferences.edit().putString(key, value).commit()
        log.d("key[$key], value[$value]")
    }

    fun getString(key: String): String {
        return sSharedPreferences.getString(key, "")!!
    }

    fun getString(key: String, defaultValue: String): String {
        val value = sSharedPreferences.getString(key, defaultValue)
        log.d("key[$key], value[$value]")
        return value!!
    }

    fun putFloat(key: String, value: Float) {
        sSharedPreferences.edit().putFloat(key, value).commit()
        log.d("key[$key], value[$value]")
    }

    fun getFloat(key: String): Float {
        return getFloat(key, .0f)
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        val value = sSharedPreferences.getFloat(key, defaultValue)
        log.d("key[$key], value[$value]")
        return value
    }

    fun putBoolean(key: String, value: Boolean) {
        sSharedPreferences.edit().putBoolean(key, value).commit()
        log.d("key[$key], value[$value]")
    }

    fun getBoolean(key: String): Boolean {
        return getBoolean(key, false)
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val value = sSharedPreferences.getBoolean(key, defaultValue)
        log.d("key[$key], value[$value]")
        return value
    }

    fun <T> getObject(key: String, type: Type): T? {
        try {
            val json = getString(key)
            log.d("key[$key], value[$json], class[${type.toString()}]")
            return sGson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun putObject(key: String, value: Any?) {
        try {
            var json: String? = ""
            var className: String? = "null"
            if (value != null) {
                json = sGson.toJson(value)
                className = value.javaClass.name
            }
            sSharedPreferences.edit().putString(key, json).commit()
            log.d("key[$key], value[$json], class[$className]")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun remove(key: String) {
        sSharedPreferences.edit().remove(key).commit()
    }
}