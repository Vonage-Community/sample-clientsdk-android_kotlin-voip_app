package com.example.vonage.voicesampleapp.activities

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.vonage.voicesampleapp.App
import com.example.vonage.voicesampleapp.databinding.ActivityMainBinding
import com.example.vonage.voicesampleapp.utils.Constants
import com.example.vonage.voicesampleapp.utils.navigateToCallActivity
import com.example.vonage.voicesampleapp.utils.navigateToLoginActivity
import com.example.vonage.voicesampleapp.utils.showDialerFragment
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 123
    }

    // Only permission with a 'dangerous' Protection Level
    // need to be requested explicitly
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.MANAGE_OWN_CALLS,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.CALL_PHONE,
    )
    private val arePermissionsGranted : Boolean get() {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private val coreContext = App.coreContext
    private val clientManager = coreContext.clientManager
    private lateinit var binding:ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Set Bindings
        setBindings()
        // Firebase Tokens
        registerFirebaseTokens()
        // Check if all permissions are granted
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        coreContext.activeCall?.let {
            navigateToCallActivity()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clientManager.unregisterDevicePushToken()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(arePermissionsGranted){
            coreContext.telecomHelper
        }
    }

    private fun setBindings(){
        binding.apply {
            coreContext.username?.let {
                usernameLabel.text = it
            }
            logoutButton.setOnClickListener {
                logout()
            }
            callUserButton.setOnClickListener {
                callUser()
            }
            keypadButton.setOnClickListener {
                showDialerFragment()
            }
        }
    }

    private fun checkPermissions(){
        if (!arePermissionsGranted) {
            // Request permissions
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE)
        } else {
            coreContext.telecomHelper
        }
    }

    private fun registerFirebaseTokens() {
        // FCM Device Token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { token ->
                    println("FCM Device Token: $token")
                }
            }
        }
        // Push Token
        clientManager.registerDevicePushToken()
    }

    private fun logout(){
        clientManager.logout {
            navigateToLoginActivity()
        }
    }

    private fun callUser(){
        val username = binding.editUsername.text.toString().trim().takeIf { it.isNotEmpty() } ?: return
        val callContext = mapOf(
            Constants.CONTEXT_KEY_RECIPIENT to username,
            Constants.CONTEXT_KEY_TYPE to Constants.APP_TYPE
        )
        clientManager.startOutboundCall(callContext)
    }
}