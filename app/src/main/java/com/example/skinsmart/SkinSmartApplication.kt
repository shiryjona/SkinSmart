package com.example.skinsmart

import android.app.Application
import com.google.firebase.FirebaseApp

class SkinSmartApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Centralized Firebase initialization
        FirebaseApp.initializeApp(this)
    }
}
