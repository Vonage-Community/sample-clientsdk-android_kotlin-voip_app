package com.example.vonage.voicesampleapp.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE

object PrivatePreferences {
    private const val NAME = "MY_PREF"
    const val PUSH_TOKEN = "PUSH_TOKEN"
    const val DEVICE_ID = "DEVICE_ID"
    fun set(key: String, value: String?, context: Context){
        context.getSharedPreferences(NAME, MODE_PRIVATE)?.edit()?.apply {
            putString(key, value)
            apply()
        }
    }
    fun get(key: String, context: Context) : String? {
        return context.getSharedPreferences(NAME, MODE_PRIVATE)?.getString(key, null)
    }
}