package com.example.vonage.voicesampleapp.push

import com.example.vonage.voicesampleapp.App
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("PUSH TOKEN:  $token")
        // Set new Push Token
        App.coreContext.pushToken = token
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Whenever a Push Notification comes in
        // If there is no active session then
        // Create one using the latest valid Auth Token and notify the ClientManager
        // Else notify the ClientManager directly
        App.coreContext.run {
            if (sessionId == null) {
                val token = authToken ?: return@run
                val username = username ?: return@run
                clientManager.login(username, token) {
                    clientManager.processIncomingPush(remoteMessage)
                }
            } else {
                clientManager.processIncomingPush(remoteMessage)
            }
        }
    }
}