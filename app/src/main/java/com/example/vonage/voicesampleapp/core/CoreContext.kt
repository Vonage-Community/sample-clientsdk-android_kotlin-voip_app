package com.example.vonage.voicesampleapp.core

import android.content.Context
import com.example.vonage.voicesampleapp.telecom.CallConnection
import com.example.vonage.voicesampleapp.telecom.TelecomHelper
import com.example.vonage.voicesampleapp.utils.PrivatePreferences

/**
 * A singleton class for storing and accessing Core Application Data
 */
class CoreContext private constructor(context: Context) {
    private val applicationContext: Context = context.applicationContext
    val telecomHelper: TelecomHelper by lazy { TelecomHelper(applicationContext) }
    val clientManager: VoiceClientManager by lazy { VoiceClientManager(applicationContext) }
    var authToken: String? = null
    var username: String? = null
    var sessionId: String? = null
    var activeCall:CallConnection? = null

    /**
     * The Firebase Push Token obtained via PushNotificationService.
     */
    var pushToken: String? get() {
        return PrivatePreferences.get(PrivatePreferences.PUSH_TOKEN, applicationContext)
    } set(value) {
        PrivatePreferences.set(PrivatePreferences.PUSH_TOKEN, value, applicationContext)
    }
    /**
     * The Device ID bound to the Push Token once it will be registered.
     * It will be used to unregister the Push Token later on.
     */
    var deviceId: String? get() {
        return PrivatePreferences.get(PrivatePreferences.DEVICE_ID, applicationContext)
    } set(value) {
        PrivatePreferences.set(PrivatePreferences.DEVICE_ID, value, applicationContext)
    }

    companion object {
        // Volatile will guarantee a thread-safe & up-to-date version of the instance
        @Volatile
        private var instance: CoreContext? = null

        fun getInstance(context: Context): CoreContext {
            return instance ?: synchronized(this) {
                instance ?: CoreContext(context).also { instance = it }
            }
        }
    }
}
