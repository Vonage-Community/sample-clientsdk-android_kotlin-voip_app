package com.example.vonage.voicesampleapp

import android.app.Application
import com.example.vonage.voicesampleapp.core.CoreContext

class App: Application() {

    companion object {
        lateinit var coreContext: CoreContext
    }

    override fun onCreate() {
        super.onCreate()
        coreContext = CoreContext.getInstance(applicationContext)
    }

}
