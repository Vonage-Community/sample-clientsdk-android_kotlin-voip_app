package com.example.vonage.voicesampleapp.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telecom.Connection
import com.example.vonage.voicesampleapp.App
import com.example.vonage.voicesampleapp.R
import com.example.vonage.voicesampleapp.databinding.ActivityCallBinding
import com.example.vonage.voicesampleapp.utils.showDialerFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CallActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCallBinding
    private val coreContext = App.coreContext
    private val clientManager = coreContext.clientManager
    private var isMuteToggled = false

    /**
     * When an Active Call gets disconnected
     * (either remotely or locally) it will be null.
     * Hence, we use this variable to check
     * if the hang-up was local (STATE_DISCONNECTED) or remote (null).
     */
    private var fallbackState: Int? = null

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
        setButtonListeners()
        setBindings()
        registerReceiver(messageReceiver, IntentFilter(MESSAGE_ACTION))
    }

    override fun onResume() {
        super.onResume()
        coreContext.activeCall ?: run {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(messageReceiver)
    }

    private fun setBindings(){
        setButtonListeners()
        setUserUI()
        setStateUI()
    }

    private fun setButtonListeners() = binding.run{
        // Button Listeners
        btnHangup.setOnClickListener { onHangup() }
        btnMute.setOnClickListener { onMute() }
        btnKeypad.setOnClickListener { onKeypad() }
    }

    private fun setUserUI() = binding.run{
        // Username Label
        userName.text = coreContext.activeCall?.callerDisplayName
    }

    private fun setStateUI() = binding.run {
        //Background Color and State label
        val (backgroundColor, stateLabel) = when(coreContext.activeCall?.state ?: fallbackState) {
            Connection.STATE_RINGING, Connection.STATE_DIALING -> R.color.gray to R.string.call_state_ringing_label
            Connection.STATE_ACTIVE -> R.color.green to R.string.call_state_active_label
            Connection.STATE_DISCONNECTED -> R.color.red to R.string.call_state_remotely_disconnected_label
            null -> R.color.red to R.string.call_state_locally_disconnected_label
            else -> null to null
        }
        backgroundColor?.let {
            cardView.setCardBackgroundColor(getColor(it))
        }
        stateLabel?.let {
            callState.text = getString(it)
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