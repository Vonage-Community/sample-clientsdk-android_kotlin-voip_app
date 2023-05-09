package com.example.vonage.voicesampleapp.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telecom.Connection
import android.view.View
import com.example.vonage.voicesampleapp.App
import com.example.vonage.voicesampleapp.R
import com.example.vonage.voicesampleapp.databinding.ActivityCallBinding
import com.example.vonage.voicesampleapp.utils.Constants
import com.example.vonage.voicesampleapp.utils.showDialerFragment
import com.example.vonage.voicesampleapp.utils.turnKeyguardOff
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vonage.clientcore.core.api.models.Username
import com.vonage.clientcore.core.conversation.VoiceChannelType

class CallActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCallBinding
    private val coreContext = App.coreContext
    private val clientManager = coreContext.clientManager
    private val notificationManager = coreContext.notificationManager
    private val telecomHelper = coreContext.telecomHelper
    private var isMuteToggled = false

    /**
     * When an Active Call gets disconnected
     * (either remotely or locally) it will be null.
     * Hence, we use these variables to manually update the UI in that case
     */
    private var fallbackState: Int? = null

    private var fallbackUsername: Username? = null

    /**
     * This Local BroadcastReceiver will be used
     * to receive messages from other activities
     */
    private val messageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Handle the messages here

            // Call Is Muted Update
            intent?.getBooleanExtra(IS_MUTED, false)?.let {
                if(isMuteToggled != it){
                    toggleMute()
                }
            }
            // Call Remotely Disconnected
            intent?.getBooleanExtra(IS_REMOTE_DISCONNECT, false)?.let {
                fallbackState = if(it) Connection.STATE_DISCONNECTED else null
            }
            // Call State Updated
            intent?.getStringExtra(CALL_STATE)?.let {
                setStateUI()
                if(it == CALL_DISCONNECTED){
                    finish()
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleIntent(intent)
        setBindings()
        registerReceiver(messageReceiver, IntentFilter(MESSAGE_ACTION))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(messageReceiver)
    }

    /**
     * An Intent with extras will be received if
     * the App received an incoming call while device was locked.
     */
    private fun handleIntent(intent: Intent?){
        intent ?: return
        val callId = intent.getStringExtra(Constants.EXTRA_KEY_CALL_ID) ?: return
        val from = intent.getStringExtra(Constants.EXTRA_KEY_FROM) ?: return
        val typeString = intent.getStringExtra(Constants.EXTRA_KEY_CHANNEL_TYPE) ?: return
        val type = VoiceChannelType.valueOf(typeString)
        fallbackUsername = from
        fallbackState = Connection.STATE_RINGING
        turnKeyguardOff {
            if(notificationManager.isIncomingCallNotificationActive){
                notificationManager.dismissIncomingCallNotification(callId)
                telecomHelper.startIncomingCall(callId, from, type)
            } else {
                // If the Notification has been canceled in the meantime
                this.finish()
            }
        }
    }

    private fun setBindings(){
        setButtonListeners()
        setUserUI()
        setStateUI()
    }

    private fun setButtonListeners() = binding.run{
        // Button Listeners
        btnAnswer.setOnClickListener { onAnswer() }
        btnReject.setOnClickListener { onReject() }
        btnHangup.setOnClickListener { onHangup() }
        btnMute.setOnClickListener { onMute() }
        btnKeypad.setOnClickListener { onKeypad() }
    }

    private fun setUserUI() = binding.run{
        // Username Label
        userName.text = coreContext.activeCall?.callerDisplayName ?: fallbackUsername
    }

    private fun setStateUI() = binding.run {
        val state = coreContext.activeCall?.state ?: fallbackState
        //Buttons Visibility
        if(state == Connection.STATE_RINGING){
            btnAnswer.visibility = View.VISIBLE
            btnReject.visibility = View.VISIBLE
            btnHangup.visibility = View.GONE
            btnMute.visibility = View.GONE
            btnKeypad.visibility = View.GONE
        }
        else {
            btnAnswer.visibility = View.GONE
            btnReject.visibility = View.GONE
            btnHangup.visibility = View.VISIBLE
            btnMute.visibility = View.VISIBLE
            btnKeypad.visibility = View.VISIBLE
        }
        //Background Color and State label
        val (backgroundColor, stateLabel) = when(state) {
            Connection.STATE_RINGING -> R.color.gray to R.string.call_state_ringing_label
            Connection.STATE_DIALING -> R.color.gray to R.string.call_state_dialing_label
            Connection.STATE_ACTIVE -> R.color.green to R.string.call_state_active_label
            Connection.STATE_DISCONNECTED -> R.color.red to R.string.call_state_remotely_disconnected_label
            else -> R.color.red to R.string.call_state_locally_disconnected_label
        }
        cardView.setCardBackgroundColor(getColor(backgroundColor))
        callState.text = getString(stateLabel)
    }

    private fun onAnswer(){
        coreContext.activeCall?.let { call ->
            clientManager.answerCall(call)
        }
    }

    private fun onReject(){
        coreContext.activeCall?.let { call ->
            clientManager.rejectCall(call)
        }
    }

    private fun onHangup(){
        coreContext.activeCall?.let { call ->
            clientManager.hangupCall(call)
        }
    }

    private fun onMute(){
        coreContext.activeCall?.let { call ->
            if(toggleMute()){
                clientManager.muteCall(call)
            } else {
                clientManager.unmuteCall(call)
            }
        }
    }

    private fun onKeypad(){
        showDialerFragment()
    }

    private fun toggleMute() : Boolean{
        isMuteToggled = binding.btnMute.toggleButton(isMuteToggled)
        return isMuteToggled
    }

    private fun FloatingActionButton.toggleButton(toggle: Boolean): Boolean {
        backgroundTintList = ColorStateList.valueOf(getColor(if(!toggle) R.color.gray else R.color.white))
        imageTintList = ColorStateList.valueOf(getColor(if(!toggle) R.color.white else R.color.gray))
        return !toggle
    }

    companion object {
        const val MESSAGE_ACTION = "com.example.vonage.voicesampleapp.MESSAGE_TO_CALL_ACTIVITY"
        const val IS_MUTED = "isMuted"
        const val CALL_STATE = "callState"
        const val CALL_ANSWERED = "answered"
        const val CALL_DISCONNECTED = "disconnected"
        const val IS_REMOTE_DISCONNECT = "isRemoteDisconnect"
    }
}