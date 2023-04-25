package com.example.vonage.voicesampleapp.core

import android.content.Context
import android.telecom.DisconnectCause
import android.widget.Toast
import com.example.vonage.voicesampleapp.App
import com.example.vonage.voicesampleapp.telecom.CallConnection
import com.example.vonage.voicesampleapp.utils.*
import com.example.vonage.voicesampleapp.utils.notifyCallAnsweredToCallActivity
import com.example.vonage.voicesampleapp.utils.notifyCallDisconnectedToCallActivity
import com.example.vonage.voicesampleapp.utils.notifyIsMutedToCallActivity
import com.google.firebase.messaging.RemoteMessage
import com.vonage.android_core.PushType
import com.vonage.android_core.VGClientConfig
import com.vonage.clientcore.core.api.CallId
import com.vonage.clientcore.core.api.LoggingLevel
import com.vonage.clientcore.core.api.SessionErrorReason
import com.vonage.clientcore.core.api.models.Username
import com.vonage.clientcore.core.api.setDefaultLoggingLevel
import com.vonage.clientcore.core.reducers.call.CancelledReason
import com.vonage.voice.api.VoiceClient

/**
 * This Class will act as an interface
 * between the App and the Voice Client SDK
 */
class VoiceClientManager(private val context: Context) {
    private lateinit var client : VoiceClient
    private val coreContext = App.coreContext
    init {
        initClient()
        setClientListeners()
    }

    private fun initClient(){
        setDefaultLoggingLevel(LoggingLevel.Info)

        val config = VGClientConfig()

        config.apiUrl = "https://api.nexmo.com"
        config.websocketUrl = "https://ws.nexmo.com"

        client = VoiceClient(context)
        client.setConfig(config)
    }

    private fun setClientListeners(){

        client.setSessionErrorListener { err ->
            when(err){
                SessionErrorReason.EXPIRED_TOKEN -> TODO()
                SessionErrorReason.TRANSPORT_CLOSED -> TODO()
                SessionErrorReason.PING_TIMEOUT -> TODO()
            }
        }

        client.setCallInviteListener { callId, from, type ->
            // Temp Push notification bug:
            // reject incoming calls when there is an active one
            coreContext.activeCall?.let { return@setCallInviteListener }
            coreContext.telecomHelper.startIncomingCall(callId, from, type)
        }

        client.setOnLegStatusUpdate { callId, legId, status ->
            println("Call $callId has received status update $status for leg $legId")
            takeIfActive(callId)?.apply {
                if(status == Constants.CALL_STATE_ANSWERED){
                    setActive()
                    notifyCallAnsweredToCallActivity(context)
                }
            }
        }

        client.setOnCallHangupListener { callId, callQuality, reason ->
            println("Call $callId has been $reason up with quality: $callQuality")
            takeIfActive(callId)?.apply {
                val cause = if(isRemote) DisconnectCause(DisconnectCause.REMOTE) else DisconnectCause(DisconnectCause.LOCAL)
                setDisconnected(cause)
                clearActiveCall()
                notifyCallDisconnectedToCallActivity(context, isRemote)
            }
        }

        client.setCallInviteCancelListener { callId, reason ->
            println("Invite to Call $callId has been canceled with reason: ${reason.name}")
            takeIfActive(callId)?.apply {
                val cause = when(reason){
                    /*
                    //TODO: these are not being triggered as expected
                    CancelledReason.RemoteAnswer -> DisconnectCause(DisconnectCause.ANSWERED_ELSEWHERE)
                    CancelledReason.RemoteReject -> DisconnectCause(DisconnectCause.REMOTE)
                    CancelledReason.RemoteCancel -> DisconnectCause(DisconnectCause.CANCELED)
                    */
                    CancelledReason.RemoteTimeout -> DisconnectCause(DisconnectCause.MISSED)
                    else -> { return@apply }
                }
                setDisconnected(cause)
                clearActiveCall()
                notifyCallDisconnectedToCallActivity(context, true)
            }
        }

        client.setCallTransferListener { callId, conversationId ->
            println("Call $callId has been transferred to conversation $conversationId")
        }

        client.setOnMutedListener { callId, legId, isMuted ->
            println("LegId $legId for Call $callId has been ${if(isMuted) "muted" else "unmuted"}")
            takeIf { callId == legId } ?: return@setOnMutedListener
            // Update Active Call Mute State
            takeIfActive(callId)?.isMuted = isMuted
            // Notify Call Activity
            notifyIsMutedToCallActivity(context, isMuted)
        }

        client.setOnDTMFListener { callId, legId, digits ->
            println("LegId $legId has sent DTMF digits '$digits' to Call $callId")
        }
    }
    fun login(username: Username, token: String, onSuccessCallback: (() -> Unit)? = null){
        client.createSession(token){ error, sessionId ->
            sessionId?.let {
                showToast(context, "Logged in with session ID: $sessionId", Toast.LENGTH_SHORT)
                coreContext.sessionId = it
                coreContext.username = username
                coreContext.authToken = token
                onSuccessCallback?.invoke()
            } ?: error?.let {
                showToast(context, "Login Failed: ${error.message}")
            }
        }
    }

    fun logout(onSuccessCallback: (() -> Unit)? = null){
        client.deleteSession { error ->
            error?.let {
                showToast(context, "Error Logging Out: ${error.message}")
            } ?: run {
                coreContext.sessionId = null
                coreContext.username = null
                coreContext.authToken = null
                onSuccessCallback?.invoke()
            }
        }
    }

    fun startOutboundCall(callContext: Map<String, String>? = null){
        client.serverCall(callContext) { err, callId ->
            err?.let {
                println("Error starting outbound call: $it")
            } ?: callId?.let {
                println("Outbound Call successfully started with Call ID: $it")
                val to = callContext?.get(Constants.CONTEXT_KEY_RECIPIENT) ?: Constants.DEFAULT_DIALED_NUMBER
                coreContext.telecomHelper.startOutgoingCall(it, to)
            }
        }
    }

    fun registerDevicePushToken(){
        coreContext.pushToken?.let {
            client.registerDevicePushToken(it) { err, deviceId ->
                err?.let {
                    println("Error in registering Device Push Token: $err")
                } ?: deviceId?.let {
                    coreContext.deviceId = deviceId
                    println("Device Push Token successfully registered with Device ID: $deviceId")
                }
            }
        }
    }

    fun unregisterDevicePushToken(){
        coreContext.deviceId?.let {
            client.unregisterDevicePushToken(it) { err ->
                err?.let {
                    println("Error in unregistering Device Push Token: $err")
                }
            }
        }
    }

    fun processIncomingPush(remoteMessage: RemoteMessage) {
        val dataString = remoteMessage.data.toString()
        val type: PushType? = VoiceClient.getPushNotificationType(dataString)
        if (type == PushType.INCOMING_CALL) {
            // This method will trigger the Client's Call Invite Listener
            client.processPushCallInvite(dataString)
        }
    }

    fun answerCall(call: CallConnection){
        call.takeIfActive()?.apply {
            client.answer(callId) { err ->
                if (err != null) {
                    println("Error Answering Call: $err")
                    setDisconnected(DisconnectCause(DisconnectCause.ERROR))
                    clearActiveCall()
                } else {
                    println("Answered call with id: $callId")
                    setActive()
                }
            }
        } ?: call.selfDestroy()
    }

    fun rejectCall(call: CallConnection){
        call.takeIfActive()?.apply {
            client.reject(callId){ err ->
                if (err != null) {
                    println("Error Rejecting Call: $err")
                    setDisconnected(DisconnectCause(DisconnectCause.ERROR))
                } else {
                    println("Rejected call with id: $callId")
                    setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
                }
            }
        } ?: call.selfDestroy()
    }

    fun hangupCall(call: CallConnection){
        call.takeIfActive()?.apply {
            client.hangup(callId) { err ->
                if (err != null) {
                    println("Error Hanging Up Call: $err")
                    // If there has been an error
                    // the onCallHangupListener will not be invoked,
                    // hence the Call needs to be explicitly disconnected
                    setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
                    clearActiveCall()
                    notifyCallDisconnectedToCallActivity(context, false)
                } else {
                    println("Hung up call with id: $callId")
                }
            }
        } ?: call.selfDestroy()
    }

    fun muteCall(call: CallConnection){
        call.takeIfActive()?.apply {
            client.mute(callId) { err ->
                if (err != null) {
                    println("Error Muting Call: $err")
                } else {
                    println("Muted call with id: $callId")
                }
            }
        }
    }

    fun unmuteCall(call: CallConnection){
        call.takeIfActive()?.apply {
            client.unmute(callId) { err ->
                if (err != null) {
                    println("Error Un-muting Call: $err")
                } else {
                    println("Un-muted call with id: $callId")
                }
            }
        }
    }

    fun sendDtmf(call: CallConnection, digit: String){
        call.takeIfActive()?.apply {
            client.sendDTMF(callId, digit){ err ->
                if (err != null) {
                    println("Error in Sending DTMF '$digit': $err")
                } else {
                    println("Sent DTMF '$digit' on call with id: $callId")
                }
            }
        }
    }

    // Utilities to filter active calls
    private fun takeIfActive(callId: CallId) : CallConnection? {
        return coreContext.activeCall?.takeIf { it.callId == callId }
    }
    private fun CallConnection.takeIfActive() : CallConnection? {
        return takeIfActive(callId)
    }
}