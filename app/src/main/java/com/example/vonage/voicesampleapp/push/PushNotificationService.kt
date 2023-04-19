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
        // When an incoming call comes in, notify the ClientManager
        App.coreContext.clientManager.processIncomingPush(remoteMessage)
    }
}