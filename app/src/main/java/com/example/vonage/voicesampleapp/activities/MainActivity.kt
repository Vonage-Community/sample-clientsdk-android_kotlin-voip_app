package com.example.vonage.voicesampleapp.activities

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.vonage.voicesampleapp.App
import com.example.vonage.voicesampleapp.databinding.ActivityMainBinding
import com.example.vonage.voicesampleapp.utils.*
import com.example.vonage.voicesampleapp.utils.navigateToCallActivity
import com.example.vonage.voicesampleapp.utils.navigateToLoginActivity
import com.example.vonage.voicesampleapp.utils.showDialerFragment

class MainActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 123
        var isInForeground = false
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
        // Check if all permissions are granted
        checkPermissions()
    }

    override fun onStop() {
        super.onStop()
        isInForeground = false
    }

    override fun onResume() {
        super.onResume()
        isInForeground = true
        coreContext.activeCall?.let {
            navigateToCallActivity()
        }
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

    private fun logout(){
        clientManager.logout {
            navigateToLoginActivity()
        }
    }

    private fun callUser(){
        val callContext = binding.editUsername.text.toString().trim().takeIf { it.isNotEmpty() }?.let {
            mapOf(
                Constants.CONTEXT_KEY_RECIPIENT to it,
                Constants.CONTEXT_KEY_TYPE to Constants.APP_TYPE
            )
        }
        clientManager.startOutboundCall(callContext)
    }
}