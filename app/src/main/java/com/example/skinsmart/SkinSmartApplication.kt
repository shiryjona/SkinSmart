package com.example.skinsmart

import android.app.Application
import com.google.firebase.FirebaseApp

class SkinSmartApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Explicitly initialize Firebase to prevent "not initialized" errors during startup
        FirebaseApp.initializeApp(this)
    }
}
