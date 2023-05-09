package com.example.vonage.voicesampleapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.vonage.voicesampleapp.App
import com.example.vonage.voicesampleapp.BuildConfig
import com.example.vonage.voicesampleapp.databinding.ActivityLoginBinding
import com.example.vonage.voicesampleapp.utils.navigateToMainActivity
import com.example.vonage.voicesampleapp.utils.showToast


class LoginActivity : AppCompatActivity() {
    private val coreContext = App.coreContext
    private val clientManager = coreContext.clientManager
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            edtToken.setText(BuildConfig.MY_VONAGE_API_TOKEN)
            btnLogin.setOnClickListener {
                login()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        coreContext.sessionId?.let {
            navigateToMainActivity()
        }
    }

    private fun login(){
        val username = "Logged User"
        val token = binding.edtToken.text.toString()
        val progressBar = binding.progressBar.apply {
            visibility = View.VISIBLE
        }
        clientManager.login(username, token,
            onErrorCallback = { error ->
                progressBar.visibility = View.INVISIBLE
                showToast(this, "Login Failed: ${error.message}")
            },
            onSuccessCallback = { sessionId ->
                progressBar.visibility = View.INVISIBLE
                showToast(this, "Logged in with session ID: $sessionId", Toast.LENGTH_SHORT)
                navigateToMainActivity()
            }
        )
    }
}