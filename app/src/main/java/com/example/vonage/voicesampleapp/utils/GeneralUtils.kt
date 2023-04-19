package com.example.vonage.voicesampleapp.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

internal fun showToast(context: Context, text: String, duration: Int = Toast.LENGTH_LONG){
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, text, duration).show()
    }
}