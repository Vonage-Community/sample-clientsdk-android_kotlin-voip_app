package com.example.vonage.voicesampleapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.vonage.voicesampleapp.App
import com.example.vonage.voicesampleapp.BuildConfig
import com.example.vonage.voicesampleapp.databinding.ActivityLoginBinding
import com.example.vonage.voicesampleapp.utils.navigateToMainActivity


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
        clientManager.login(username, token){
            navigateToMainActivity()
        }
    }
}