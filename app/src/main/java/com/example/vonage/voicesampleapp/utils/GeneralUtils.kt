package com.example.vonage.voicesampleapp.utils

import android.app.KeyguardManager
import android.app.KeyguardManager.KeyguardDismissCallback
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.os.postDelayed
import com.example.vonage.voicesampleapp.activities.CallActivity

internal fun showToast(context: Context, text: String, duration: Int = Toast.LENGTH_LONG){
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, text, duration).show()
    }
}

/**
 * If the device is locked the App will not be able to record audio
 */
internal fun isDeviceLocked(context: Context): Boolean {
    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return keyguardManager.isKeyguardLocked
}

/**
 * This method will allow the Call Activity to turn the screen on and unlock the device
 */
fun CallActivity.turnKeyguardOff(onSuccessCallback: (() -> Unit)? = null){
    val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    Handler(Looper.getMainLooper()).postDelayed(500){
        keyguardManager.requestDismissKeyguard(this@turnKeyguardOff, object : KeyguardDismissCallback() {
            override fun onDismissSucceeded() {
                super.onDismissSucceeded()
                Handler(Looper.getMainLooper()).post {
                    onSuccessCallback?.invoke()
                }
            }

            override fun onDismissError() {
                super.onDismissError()
            }

            override fun onDismissCancelled() {
                super.onDismissCancelled()
            }
        })
    }
}